package com.backend.modulo.service;

import com.backend.modulo.document.ExerciseDocument;
import com.backend.modulo.document.RoutineDocument;
import com.backend.modulo.repository.ExerciseSearchRepository;
import com.backend.modulo.repository.RoutineSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializerService implements CommandLineRunner {

    private final ExerciseSearchRepository exerciseRepository;
    private final RoutineSearchRepository routineRepository;

    @Override
    public void run(String... args) throws Exception {
        seedExercises();
        seedRoutines();
    }

    private void seedExercises() {
        if (exerciseRepository.count() > 0) {
            log.info("Índice 'exercises' ya contiene datos. Se omite el seed.");
            return;
        }
        log.info("Índice 'exercises' vacío. Insertando datos semilla...");

        List<ExerciseDocument> seeds = List.of(
            ExerciseDocument.builder().id("ex_1").name("Push-ups Clásicos").description("Flexiones de brazo estándar en suelo.").category("upper_body").level("principiante").icon("💪").muscles(List.of("Pecho", "Tríceps", "Hombros anterior")).build(),
            ExerciseDocument.builder().id("ex_2").name("Pull-ups (Dominadas)").description("Dominadas con agarre prono abierto.").category("upper_body").level("intermedio").icon("🏋️").muscles(List.of("Dorsales", "Bíceps", "Antebrazos")).build(),
            ExerciseDocument.builder().id("ex_3").name("Dips (Fondos en Paralelas)").description("Fondos en barras paralelas inclinando el torso.").category("upper_body").level("intermedio").icon("🪑").muscles(List.of("Tríceps", "Pecho inferior", "Hombros")).build(),
            ExerciseDocument.builder().id("ex_4").name("Pistol Squats").description("Sentadilla a una pierna sin apoyo.").category("lower_body").level("avanzado").icon("🦵").muscles(List.of("Cuádriceps", "Glúteos", "Estabilizadores")).build(),
            ExerciseDocument.builder().id("ex_5").name("L-Sit").description("Sostener el cuerpo con piernas estiradas a 90 grados.").category("core").level("intermedio").icon("🧘").muscles(List.of("Abdomen", "Cadera", "Tríceps")).build(),
            ExerciseDocument.builder().id("ex_6").name("Muscle-up").description("Transición completa de dominada a fondo en barra.").category("upper_body").level("avanzado").icon("👑").muscles(List.of("Espalda", "Pectorales", "Tríceps", "Core")).build(),
            ExerciseDocument.builder().id("ex_7").name("Burpees").description("Ejercicio cardiovascular de cuerpo completo.").category("full_body").level("intermedio").icon("🚀").muscles(List.of("Cardio", "Piernas", "Pecho")).build(),
            ExerciseDocument.builder().id("ex_8").name("Plank Isométrico").description("Plancha abdominal estática en antebrazos.").category("core").level("principiante").icon("🔥").muscles(List.of("Core", "Hombros")).build(),
            ExerciseDocument.builder().id("ex_9").name("Hollow Body Hold").description("Mantener tensión abdominal formando una U en el suelo.").category("core").level("principiante").icon("🍌").muscles(List.of("Abdomen transverso", "Core bajo")).build(),
            ExerciseDocument.builder().id("ex_10").name("Handstand Push-ups").description("Flexiones en posición de pino contra la pared.").category("upper_body").level("avanzado").icon("🤸").muscles(List.of("Hombros", "Tríceps", "Core")).build()
        );

        exerciseRepository.saveAll(seeds);
        log.info("Seed de ejercicios completo. {} documentos insertados.", seeds.size());
    }

    private void seedRoutines() {
        if (routineRepository.count() > 0) {
            log.info("Índice 'routines' ya contiene datos. Se omite el seed.");
            return;
        }
        log.info("Índice 'routines' vacío. Insertando datos semilla...");

        List<RoutineDocument> seeds = List.of(
            RoutineDocument.builder().id("rt_1").name("Upper Body Fuerza").description("Pull-ups, dips y push-ups para tren superior.").category("upper_body").level("intermedio").exerciseIds(List.of("ex_1", "ex_2", "ex_3")).durationMinutes(45).caloriesEstimate(320).hrRange("130-160").build(),
            RoutineDocument.builder().id("rt_2").name("Core & Planche").description("L-sit, planche lean y hollow body para core avanzado.").category("core").level("avanzado").exerciseIds(List.of("ex_5", "ex_8", "ex_9")).durationMinutes(35).caloriesEstimate(200).hrRange("110-140").build(),
            RoutineDocument.builder().id("rt_3").name("Full Body Inicio").description("Sentadillas, flexiones y dominadas asistidas para principiantes.").category("full_body").level("principiante").exerciseIds(List.of("ex_1", "ex_7")).durationMinutes(30).caloriesEstimate(220).hrRange("120-150").build(),
            RoutineDocument.builder().id("rt_4").name("Muscle-up Progression").description("Progresión explosiva y negativos hacia el muscle-up completo.").category("upper_body").level("avanzado").exerciseIds(List.of("ex_2", "ex_6")).durationMinutes(40).caloriesEstimate(290).hrRange("140-175").build(),
            RoutineDocument.builder().id("rt_5").name("Lower Body Potencia").description("Pistol squats, saltos y zancadas para tren inferior.").category("lower_body").level("intermedio").exerciseIds(List.of("ex_4")).durationMinutes(40).caloriesEstimate(350).hrRange("135-165").build()
        );

        routineRepository.saveAll(seeds);
        log.info("Seed de rutinas completo. {} documentos insertados.", seeds.size());
    }
}
