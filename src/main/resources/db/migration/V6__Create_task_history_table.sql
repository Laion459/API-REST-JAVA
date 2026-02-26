-- Table for task change history (data auditing)
CREATE TABLE IF NOT EXISTS task_history (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_task_history_task_id ON task_history(task_id);
CREATE INDEX IF NOT EXISTS idx_task_history_changed_at ON task_history(changed_at);
CREATE INDEX IF NOT EXISTS idx_task_history_field ON task_history(field_name);

-- Comments for documentation
COMMENT ON TABLE task_history IS 'Task field change history for auditing';
COMMENT ON COLUMN task_history.field_name IS 'Name of the field that was changed';
COMMENT ON COLUMN task_history.old_value IS 'Previous field value';
COMMENT ON COLUMN task_history.new_value IS 'New field value';
COMMENT ON COLUMN task_history.changed_by IS 'Username who made the change';
