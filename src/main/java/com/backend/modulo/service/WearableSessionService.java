package com.backend.modulo.service;

import com.backend.modulo.dto.*;
import com.backend.modulo.entity.Exercise;
import com.backend.modulo.entity.User;
import com.backend.modulo.entity.WearableSession;
import com.backend.modulo.entity.WearableSessionPoint;
import com.backend.modulo.repository.ExerciseRepository;
import com.backend.modulo.repository.UserRepository;
import com.backend.modulo.repository.WearableSessionPointRepository;
import com.backend.modulo.repository.WearableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WearableSessionService {

    private final WearableSessionRepository wearableSessionRepository;
    private final WearableSessionPointRepository wearableSessionPointRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public WearableSessionResponse startSession(WearableSessionStartRequest request) {
        User user = resolveUser(request.getUserId());
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + request.getExerciseId()));

        WearableSession session = WearableSession.builder()
                .user(user)
                .exercise(exercise)
                .wearableId(request.getWearableId())
                .status(WearableSession.Status.STARTED)
                .startedAt(OffsetDateTime.now())
                .build();

        WearableSession saved = wearableSessionRepository.save(session);
        return toResponse(saved);
    }

    @Transactional
    public WearableSessionResponse addMetrics(Long sessionId, WearableSessionMetricsRequest request) {
        WearableSession session = wearableSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        WearableSessionPoint point = WearableSessionPoint.builder()
                .session(session)
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : OffsetDateTime.now())
                .heartRate(request.getHeartRate())
                .cadence(request.getCadence())
                .distance(request.getDistance())
                .extraData(request.getExtraData())
                .build();

        wearableSessionPointRepository.save(point);

        if (session.getStatus() == WearableSession.Status.STARTED) {
            session.setStatus(WearableSession.Status.RUNNING);
        }

        recalculateAggregates(session);
        return toResponse(wearableSessionRepository.save(session));
    }

    @Transactional
    public WearableSessionResponse stopSession(Long sessionId, WearableSessionStopRequest request) {
        WearableSession session = wearableSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        session.setStatus(WearableSession.Status.COMPLETED);
        session.setEndedAt(request.getEndedAt() != null ? request.getEndedAt() : OffsetDateTime.now());
        session.setDurationSec(request.getDurationSec());
        session.setCaloriesBurned(request.getCaloriesBurned());
        session.setAvgHr(request.getAvgHr());
        session.setMaxHr(request.getMaxHr());
        session.setMinHr(request.getMinHr());

        return toResponse(wearableSessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public WearableSessionResponse getSession(Long sessionId) {
        return wearableSessionRepository.findById(sessionId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    @Transactional(readOnly = true)
    public List<WearableSessionResponse> getSessionsForUser(Long userId) {
        return wearableSessionRepository.findByUser_IdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WearableSessionResponse> getLatestSessions(Long userId, int limit) {
        return wearableSessionRepository.findByUser_IdOrderByStartedAtDesc(userId)
                .stream()
                .limit(Math.max(limit, 1))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getStatsForUser(Long userId) {
        List<WearableSession> sessions = wearableSessionRepository.findByUser_IdOrderByStartedAtDesc(userId)
                .stream()
                .filter(s -> s.getStatus() == WearableSession.Status.COMPLETED)
                .toList();

        int totalWorkouts = sessions.size();
        int totalMinutes = sessions.stream()
                .mapToInt(s -> s.getDurationSec() != null ? s.getDurationSec() / 60 : 0)
                .sum();
        int avgBpm = (int) Math.round(sessions.stream()
                .filter(s -> s.getAvgHr() != null)
                .mapToInt(s -> s.getAvgHr())
                .average()
                .orElse(0));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate()
                .atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime weekEnd = weekStart.plusDays(7);

        List<WearableSession> weekSessions = wearableSessionRepository
                .findByUser_IdAndStartedAtBetweenOrderByStartedAtAsc(
                        userId, weekStart, weekEnd);

        List<UserStatsResponse.ChartPointDto> weeklySeries = buildWeeklySeries(weekSessions);
        List<UserStatsResponse.ChartPointDto> bpmSeries = buildBpmSeries(sessions);

        return UserStatsResponse.builder()
                .totalWorkouts(totalWorkouts)
                .totalMinutes(totalMinutes)
                .avgBpm(avgBpm)
                .weeklySeries(weeklySeries)
                .bpmSeries(bpmSeries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<WearableSessionResponse> getExerciseSessions(Long exerciseId) {
        return wearableSessionRepository.findByExercise_IdOrderByStartedAtDesc(exerciseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private List<UserStatsResponse.ChartPointDto> buildWeeklySeries(List<WearableSession> sessions) {
        Map<DayOfWeek, Double> byDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            byDay.put(day, 0d);
        }
        for (WearableSession session : sessions) {
            DayOfWeek day = session.getStartedAt().getDayOfWeek();
            double minutes = session.getDurationSec() != null ? session.getDurationSec() / 60.0 : 0d;
            byDay.put(day, byDay.get(day) + minutes);
        }

        List<DayOfWeek> order = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY);

        return order.stream()
                .map(day -> UserStatsResponse.ChartPointDto.builder()
                        .label(dayToLabel(day))
                        .value(byDay.get(day))
                        .build())
                .toList();
    }

    private List<UserStatsResponse.ChartPointDto> buildBpmSeries(List<WearableSession> sessions) {
        return sessions.stream()
                .filter(s -> s.getAvgHr() != null)
                .filter(s -> s.getStartedAt() != null)
                .sorted(Comparator.comparing((WearableSession s) -> s.getStartedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .map(session -> UserStatsResponse.ChartPointDto.builder()
                        .label(session.getExercise() != null ? session.getExercise().getName() : "Sesión")
                        .value(session.getAvgHr())
                        .build())
                .toList();
    }

    private void recalculateAggregates(WearableSession session) {
        List<WearableSessionPoint> points = wearableSessionPointRepository
                .findBySession_IdOrderByTimestampAsc(session.getId());
        if (points.isEmpty()) {
            return;
        }

        int sumHr = 0;
        int countHr = 0;
        int maxHr = Integer.MIN_VALUE;
        int minHr = Integer.MAX_VALUE;
        Double distance = 0d;

        for (WearableSessionPoint point : points) {
            if (point.getHeartRate() != null) {
                sumHr += point.getHeartRate();
                countHr++;
                maxHr = Math.max(maxHr, point.getHeartRate());
                minHr = Math.min(minHr, point.getHeartRate());
            }
            if (point.getDistance() != null) {
                distance += point.getDistance();
            }
        }

        if (countHr > 0) {
            session.setAvgHr(Math.round((float) sumHr / countHr));
            session.setMaxHr(maxHr);
            session.setMinHr(minHr);
        }
        if (distance != null) {
            session.setCaloriesBurned(session.getCaloriesBurned() != null ? session.getCaloriesBurned() : 0d);
        }
    }

    private WearableSessionResponse toResponse(WearableSession session) {
        return WearableSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser() != null ? session.getUser().getId() : null)
                .exerciseId(session.getExercise() != null ? session.getExercise().getId() : null)
                .exerciseName(session.getExercise() != null ? session.getExercise().getName() : null)
                .wearableId(session.getWearableId())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationSec(session.getDurationSec())
                .caloriesBurned(session.getCaloriesBurned())
                .avgHr(session.getAvgHr())
                .maxHr(session.getMaxHr())
                .minHr(session.getMinHr())
                .build();
    }

    private User resolveUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private String dayToLabel(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lun";
            case TUESDAY -> "Mar";
            case WEDNESDAY -> "Mié";
            case THURSDAY -> "Jue";
            case FRIDAY -> "Vie";
            case SATURDAY -> "Sáb";
            case SUNDAY -> "Dom";
        };
    }
}
