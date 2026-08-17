-- Flyway migration: create exercises table and seed with initial data
CREATE TABLE IF NOT EXISTS exercises (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(100) NOT NULL,
  level VARCHAR(50) NOT NULL,
  duration_min INTEGER,
  estimated_calories INTEGER,
  target_hr_min INTEGER,
  target_hr_max INTEGER,
  icon VARCHAR(64),
  exercise_count INTEGER,
  muscles VARCHAR(500)
);

-- Seed initial data (mirrors frontend data)
INSERT INTO exercises (name, description, category, level, duration_min, estimated_calories, target_hr_min, target_hr_max, icon, exercise_count, muscles) VALUES
('Push-ups Clásicos', 'El ejercicio fundamental de calistenia. Trabaja pecho, tríceps y hombros con tu propio peso corporal.', 'upper_body', 'principiante', 15, 120, 100, 140, '💪', 4, 'Pecho,Tríceps,Hombros'),
('Pull-ups', 'El rey de la calistenia para espalda. Requiere barra fija. Desarrolla latissimus dorsi y bíceps.', 'upper_body', 'intermedio', 20, 150, 120, 160, '🏋️', 5, 'Espalda,Bíceps,Core'),
('Dips en Paralelas', 'Fortalece tríceps, pecho inferior y hombros. Excelente complemento al press de banca sin pesas.', 'upper_body', 'intermedio', 18, 130, 110, 150, '⚡', 4, 'Tríceps,Pecho,Hombros'),
('Plank Isométrico', 'Ejercicio isométrico de core. Activa transverso abdominal, oblicuos y estabilizadores espinales.', 'core', 'principiante', 12, 80, 80, 120, '🔥', 3, 'Core,Abdomen,Lumbares'),
('Burpees', 'Ejercicio full body de alta intensidad. Combina fuerza explosiva y cardio para máxima quema calórica.', 'full_body', 'intermedio', 20, 220, 150, 185, '🚀', 4, 'Full Body,Cardio,Explosividad'),
('Pistol Squats', 'Sentadilla a una pierna. Requiere equilibrio, fuerza y movilidad excepcionales. Máximo reto de pierna.', 'lower_body', 'avanzado', 25, 180, 130, 170, '🦵', 5, 'Cuádriceps,Glúteos,Equilibrio'),
('Handstand Push-ups', 'Press de hombros en pino. Desarrolla hombros, tríceps y equilibrio al límite.', 'upper_body', 'avanzado', 30, 200, 130, 175, '🤸', 5, 'Hombros,Tríceps,Core,Equilibrio'),
('L-Sit', 'Posición isométrica con las piernas extendidas horizontalmente. Máxima activación de core y caderas.', 'core', 'avanzado', 20, 140, 100, 145, '🧘', 4, 'Abdomen,Cadera,Tríceps,Core'),
('Muscle-up', 'La habilidad cumbre de la calistenia. Combina pull-up y dip en un movimiento fluido sobre la barra.', 'upper_body', 'avanzado', 35, 250, 150, 185, '👑', 6, 'Espalda,Pecho,Tríceps,Core'),
('Dragon Flag', 'Ejercicio de core extremo popularizado por Bruce Lee. Cuerpo rígido con pivote en los hombros.', 'core', 'avanzado', 25, 160, 120, 160, '🐉', 4, 'Core,Lumbares,Glúteos');
