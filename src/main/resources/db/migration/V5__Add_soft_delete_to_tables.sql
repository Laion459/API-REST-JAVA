-- Add soft delete columns to tasks table
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Add soft delete columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Create indexes for soft delete queries
CREATE INDEX IF NOT EXISTS idx_tasks_deleted ON tasks(deleted);
CREATE INDEX IF NOT EXISTS idx_tasks_deleted_at ON tasks(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);

-- Update existing indexes to include deleted flag for better query performance
CREATE INDEX IF NOT EXISTS idx_tasks_user_deleted ON tasks(user_id, deleted);
CREATE INDEX IF NOT EXISTS idx_tasks_status_deleted ON tasks(status, deleted);
