package com.backend.modulo.service;

import com.backend.modulo.document.ExerciseDocument;
import com.backend.modulo.repository.ExerciseSearchRepository;
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

    @Override
    public void run(String... args) throws Exception {
        if (exerciseRepository.count() == 0) {
            log.info("Elasticsearch index is empty. Injecting seed data...");

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
            log.info("Seed data injected successfully. {} exercises added.", seeds.size());
        } else {
            log.info("Elasticsearch index already contains data. Skipping seed.");
        }
    }
}
