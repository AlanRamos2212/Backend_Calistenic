-- Support reports (Practica 7): bug reports opened by authenticated users.
CREATE TABLE IF NOT EXISTS support_reports (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    category    VARCHAR(32) NOT NULL DEFAULT 'OTRO',
    image_url   VARCHAR(1000),
    status      VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_support_reports_user ON support_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_support_reports_status_created ON support_reports(status, created_at DESC);
