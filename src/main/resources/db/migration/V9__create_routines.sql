-- Routines: curated exercise sequences consumed by the mobile app and web.
-- image assets referenced by cover_image_url are served by MediaStorage
-- (/api/media/... locally or Cloudinary CDN in production).

CREATE TABLE IF NOT EXISTS routines (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(120) NOT NULL,
    subtitle    VARCHAR(200),
    category    VARCHAR(50)  NOT NULL,
    level       VARCHAR(50)  NOT NULL,
    duration_min INT,
    estimated_calories INT,
    cover_image_url VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS routine_exercises (
    id          BIGSERIAL PRIMARY KEY,
    routine_id  BIGINT NOT NULL REFERENCES routines(id) ON DELETE CASCADE,
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    position    INT NOT NULL,
    sets        INT NOT NULL DEFAULT 3,
    reps        INT,
    rest_seconds INT DEFAULT 60
);

CREATE INDEX IF NOT EXISTS idx_routine_exercises_routine ON routine_exercises(routine_id);
CREATE INDEX IF NOT EXISTS idx_routines_category ON routines(category);
CREATE INDEX IF NOT EXISTS idx_routines_level ON routines(level);

-- Seed routines built from the V1 exercise seed (ids 1..6)
INSERT INTO routines (title, subtitle, category, level, duration_min, estimated_calories) VALUES
('Full Body Inicial', 'Circuito de introducción a la calistenia', 'full_body', 'principiante', 30, 400),
('Fuerza Superior', 'Espalda y empuje para ganar fuerza', 'upper_body', 'intermedio', 40, 450),
('Core de Acero', 'Estabilidad abdominal sin equipo', 'core', 'principiante', 25, 280);

INSERT INTO routine_exercises (routine_id, exercise_id, position, sets, reps, rest_seconds) VALUES
(1, 1, 1, 3, 10, 60),
(1, 4, 2, 3, 40, 45),
(1, 5, 3, 3, 8, 90),
(2, 2, 1, 4, 8, 90),
(2, 3, 2, 4, 10, 90),
(3, 4, 1, 4, 60, 45);
