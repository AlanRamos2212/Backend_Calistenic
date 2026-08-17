-- Flyway migration: create wearable_bindings table
CREATE TABLE IF NOT EXISTS wearable_bindings (
  id BIGSERIAL PRIMARY KEY,
  wearable_id VARCHAR(255) NOT NULL,
  user_id BIGINT NOT NULL REFERENCES users(id),
  device_token VARCHAR(2048) NOT NULL,
  paired_at TIMESTAMP WITH TIME ZONE NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_wearable_bindings_user_id ON wearable_bindings(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_wearable_bindings_wearable_id ON wearable_bindings(wearable_id) WHERE active = true;
