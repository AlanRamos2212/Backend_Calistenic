package com.backend.modulo.service;

import com.backend.modulo.dto.CreateSupportReportRequest;
import com.backend.modulo.dto.SupportReportResponse;
import com.backend.modulo.entity.SupportReport;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.SupportReportRepository;
import com.backend.modulo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportReportService {
    private final SupportReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MediaStorage mediaStorage;

    /** Read-only so the lazy User proxy can resolve name inside the session. */
    @Transactional(readOnly = true)
    public List<SupportReportResponse> listMine(Long userId) {
        return reportRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SupportReportResponse> listAll(String status) {
        List<SupportReport> rows;
        if (status == null || status.isBlank()) {
            rows = reportRepository.findAllByOrderByCreatedAtDesc();
        } else {
            SupportReport.Status s;
            try {
                s = SupportReport.Status.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido. Usa OPEN, IN_PROGRESS o RESOLVED.");
            }
            rows = reportRepository.findByStatusOrderByCreatedAtDesc(s);
        }
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional
    public SupportReportResponse create(Long userId, CreateSupportReportRequest request, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        SupportReport.Category cat = SupportReport.Category.OTRO;
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            try {
                cat = SupportReport.Category.valueOf(request.getCategory().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida. Usa CRASH, UI, DATOS, RED u OTRO.");
            }
        }
        SupportReport report = SupportReport.builder()
                .user(user)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(cat)
                .status(SupportReport.Status.OPEN)
                .imageUrl(storeImage(image))
                .build();
        return toResponse(reportRepository.save(report));
    }

    @Transactional
    public SupportReportResponse updateStatus(Long id, String status) {
        SupportReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado."));
        try {
            report.setStatus(SupportReport.Status.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido. Usa OPEN, IN_PROGRESS o RESOLVED.");
        }
        return toResponse(reportRepository.save(report));
    }

    /** Admin-only delete: only RESOLVED reports can be removed. */
    @Transactional
    public void delete(Long id) {
        SupportReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado."));
        if (report.getStatus() != SupportReport.Status.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden eliminar reportes RESOLVED.");
        }
        reportRepository.delete(report);
    }

    private String storeImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        try {
            return mediaStorage.store(image);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo guardar la captura del reporte.");
        }
    }

    private SupportReportResponse toResponse(SupportReport r) {
        return SupportReportResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getName())
                .title(r.getTitle())
                .description(r.getDescription())
                .category(r.getCategory() == null ? null : r.getCategory().name())
                .imageUrl(r.getImageUrl())
                .status(r.getStatus() == null ? null : r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
