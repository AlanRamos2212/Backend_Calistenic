-- Users must exist before wearable foreign keys are created.
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP
);
