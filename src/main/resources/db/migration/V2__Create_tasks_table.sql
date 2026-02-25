CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON tasks(created_at);
CREATE INDEX IF NOT EXISTS idx_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_user_status ON tasks(user_id, status);
