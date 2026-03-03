-- Migration: Add performance indexes for better query optimization
-- Created: 2024

-- Index for updated_at to optimize sorting queries
CREATE INDEX IF NOT EXISTS idx_tasks_updated_at ON tasks(updated_at);

-- Composite index for common query pattern: user_id + deleted + status
CREATE INDEX IF NOT EXISTS idx_tasks_user_deleted_status ON tasks(user_id, deleted, status);

-- Index for priority sorting (often used with status)
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);

-- Composite index for priority + created_at (common sorting pattern)
CREATE INDEX IF NOT EXISTS idx_tasks_priority_created_at ON tasks(priority DESC, created_at DESC);
