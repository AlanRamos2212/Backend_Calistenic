ALTER TABLE exercises
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS gif_url VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS technique_description TEXT;

CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    workout_session_id BIGINT REFERENCES wearable_sessions(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);

CREATE TABLE IF NOT EXISTS post_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_post_likes_post_user UNIQUE (post_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_post_likes_post_id ON post_likes(post_id);

CREATE TABLE IF NOT EXISTS post_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_post_comments_post_id ON post_comments(post_id, created_at);

CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(500) NOT NULL,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_achievements_user_code UNIQUE (user_id, code)
);

CREATE INDEX IF NOT EXISTS idx_achievements_user_id ON achievements(user_id);

-- Desbloqueos automáticos mínimos y deterministas para la primera entrega.
-- La lógica de negocio completa puede crecer en el servicio de logros sin
-- cambiar el contrato de las tablas.
CREATE OR REPLACE FUNCTION unlock_training_achievements()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'COMPLETED' AND (TG_OP = 'INSERT' OR OLD.status IS DISTINCT FROM 'COMPLETED') THEN
        INSERT INTO achievements (user_id, code, title, description)
        VALUES (NEW.user_id, 'FIRST_WORKOUT', 'Primer entrenamiento', 'Completaste tu primera sesión de entrenamiento.')
        ON CONFLICT (user_id, code) DO NOTHING;

        IF (
            SELECT COUNT(*) FROM wearable_sessions
            WHERE user_id = NEW.user_id AND status = 'COMPLETED'
        ) >= 100 THEN
            INSERT INTO achievements (user_id, code, title, description)
            VALUES (NEW.user_id, '100_WORKOUTS', '100 entrenamientos', 'Completaste 100 sesiones de entrenamiento.')
            ON CONFLICT (user_id, code) DO NOTHING;
        END IF;

        IF (
            SELECT COUNT(*)
            FROM generate_series(NEW.started_at::date - 6, NEW.started_at::date, INTERVAL '1 day') expected(day)
            WHERE EXISTS (
                SELECT 1 FROM wearable_sessions ws
                WHERE ws.user_id = NEW.user_id
                  AND ws.status = 'COMPLETED'
                  AND ws.started_at::date = expected.day::date
            )
        ) = 7 THEN
            INSERT INTO achievements (user_id, code, title, description)
            VALUES (NEW.user_id, 'FIRE_WEEK', 'Semana de fuego', 'Entrenaste durante siete días consecutivos.')
            ON CONFLICT (user_id, code) DO NOTHING;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_unlock_training_achievements ON wearable_sessions;
CREATE TRIGGER trg_unlock_training_achievements
AFTER INSERT OR UPDATE OF status ON wearable_sessions
FOR EACH ROW EXECUTE FUNCTION unlock_training_achievements();
