-- Tabela para histórico de mudanças de tasks (auditoria de dados)
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

-- Comentários para documentação
COMMENT ON TABLE task_history IS 'Histórico de mudanças de campos de tasks para auditoria';
COMMENT ON COLUMN task_history.field_name IS 'Nome do campo que foi alterado';
COMMENT ON COLUMN task_history.old_value IS 'Valor anterior do campo';
COMMENT ON COLUMN task_history.new_value IS 'Novo valor do campo';
COMMENT ON COLUMN task_history.changed_by IS 'Username que fez a alteração';
