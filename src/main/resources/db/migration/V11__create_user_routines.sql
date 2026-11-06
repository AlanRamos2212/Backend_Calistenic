-- Custom routines owned by the authenticated user ('mis rutinas').
-- Distinct from admin-curated routines (V9): these belong to one user.

CREATE TABLE IF NOT EXISTS user_routines (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_routine_exercises (
    id               BIGSERIAL PRIMARY KEY,
    user_routine_id  BIGINT NOT NULL REFERENCES user_routines(id) ON DELETE CASCADE,
    exercise_id      BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    position         INT NOT NULL,
    sets             INT NOT NULL DEFAULT 3,
    reps             INT,
    rest_seconds     INT NOT NULL DEFAULT 60
);

CREATE INDEX IF NOT EXISTS idx_user_routine_exercises_routine ON user_routine_exercises(user_routine_id);
CREATE INDEX IF NOT EXISTS idx_user_routines_user ON user_routines(user_id);
