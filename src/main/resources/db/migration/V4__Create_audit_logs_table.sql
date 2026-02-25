CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    username VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    description VARCHAR(1000),
    old_value TEXT,
    new_value TEXT,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_entity_id ON audit_logs(entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_action_created ON audit_logs(action, created_at);
