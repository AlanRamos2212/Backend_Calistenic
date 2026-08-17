CREATE TABLE IF NOT EXISTS wearable_pairing_codes (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(6) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL,
  consumed_at TIMESTAMPTZ,
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_wearable_pairing_codes_user_id ON wearable_pairing_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_wearable_pairing_codes_code ON wearable_pairing_codes(code);
