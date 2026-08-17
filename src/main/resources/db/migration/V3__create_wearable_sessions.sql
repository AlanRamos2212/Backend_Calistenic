-- Flyway migration: create wearable session tables
CREATE TABLE IF NOT EXISTS wearable_sessions (
  id SERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  exercise_id BIGINT NOT NULL,
  wearable_id VARCHAR(128),
  status VARCHAR(50) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  ended_at TIMESTAMPTZ,
  duration_sec INTEGER,
  calories_burned DOUBLE PRECISION,
  avg_hr INTEGER,
  max_hr INTEGER,
  min_hr INTEGER,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_wearable_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_wearable_sessions_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id)
);

CREATE TABLE IF NOT EXISTS wearable_session_points (
  id SERIAL PRIMARY KEY,
  session_id BIGINT NOT NULL,
  timestamp TIMESTAMPTZ NOT NULL,
  heart_rate INTEGER,
  cadence INTEGER,
  distance DOUBLE PRECISION,
  extra_data TEXT,
  CONSTRAINT fk_wearable_session_points_session FOREIGN KEY (session_id) REFERENCES wearable_sessions(id)
);
