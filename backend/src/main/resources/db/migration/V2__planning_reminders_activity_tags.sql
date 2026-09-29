-- Rich task-management extension inspired by the supplied task_manager schema.
-- Adds activity history, tags/meta, planning and reminders while preserving the existing API tables.

ALTER TABLE users
    ADD COLUMN first_name VARCHAR(50) NULL,
    ADD COLUMN middle_name VARCHAR(50) NULL,
    ADD COLUMN last_name VARCHAR(50) NULL,
    ADD COLUMN username VARCHAR(50) NULL,
    ADD COLUMN mobile VARCHAR(20) NULL,
    ADD COLUMN registered_at DATETIME(6) NULL,
    ADD COLUMN last_login DATETIME(6) NULL,
    ADD COLUMN intro VARCHAR(255) NULL,
    ADD COLUMN profile TEXT NULL,
    ADD UNIQUE KEY uq_users_username (username),
    ADD UNIQUE KEY uq_users_mobile (mobile),
    ADD INDEX idx_users_role (role);

ALTER TABLE tasks
    ADD COLUMN estimated_hours DECIMAL(8,2) NOT NULL DEFAULT 0,
    ADD COLUMN actual_hours DECIMAL(8,2) NOT NULL DEFAULT 0,
    ADD COLUMN planned_start_at DATETIME(6) NULL,
    ADD COLUMN planned_end_at DATETIME(6) NULL,
    ADD COLUMN actual_start_at DATETIME(6) NULL,
    ADD COLUMN actual_end_at DATETIME(6) NULL,
    ADD COLUMN content TEXT NULL;

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    project_id BIGINT NULL,
    created_by_id BIGINT NOT NULL,
    updated_by_id BIGINT NULL,
    title VARCHAR(512) NOT NULL,
    description VARCHAR(2048) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    hours DECIMAL(8,2) NOT NULL DEFAULT 0,
    planned_start_at DATETIME(6) NULL,
    planned_end_at DATETIME(6) NULL,
    actual_start_at DATETIME(6) NULL,
    actual_end_at DATETIME(6) NULL,
    content TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_activity_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_creator FOREIGN KEY (created_by_id) REFERENCES users(id),
    CONSTRAINT fk_activity_modifier FOREIGN KEY (updated_by_id) REFERENCES users(id),
    INDEX idx_activity_user (user_id),
    INDEX idx_activity_task (task_id),
    INDEX idx_activity_project (project_id),
    INDEX idx_activity_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE comments
    ADD COLUMN activity_id BIGINT NULL,
    ADD COLUMN title VARCHAR(100) NULL,
    ADD COLUMN updated_at DATETIME(6) NULL,
    ADD CONSTRAINT fk_comment_activity FOREIGN KEY (activity_id) REFERENCES activity_logs(id) ON DELETE SET NULL,
    ADD INDEX idx_comments_activity (activity_id);

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(75) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    color VARCHAR(20) NOT NULL DEFAULT '#2563eb',
    UNIQUE KEY uq_tags_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS task_meta (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    meta_key VARCHAR(50) NOT NULL,
    content TEXT NULL,
    UNIQUE KEY uq_task_meta (task_id, meta_key),
    CONSTRAINT fk_task_meta_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    INDEX idx_task_meta_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS task_tags (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_task_tags_task (task_id),
    INDEX idx_task_tags_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plans (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NULL,
    created_by_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    start_date DATE NULL,
    end_date DATE NULL,
    progress_percent INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_plan_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_plan_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_plan_creator FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_plans_user (user_id),
    INDEX idx_plans_project (project_id),
    INDEX idx_plans_status (status),
    INDEX idx_plans_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plan_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'TODO',
    planned_start_at DATETIME(6) NULL,
    planned_end_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_plan_item_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_item_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    INDEX idx_plan_items_plan (plan_id),
    INDEX idx_plan_items_task (task_id),
    INDEX idx_plan_items_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reminders (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    plan_id BIGINT NULL,
    created_by_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NULL,
    remind_at DATETIME(6) NOT NULL,
    repeat_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reminder_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    CONSTRAINT fk_reminder_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL,
    CONSTRAINT fk_reminder_creator FOREIGN KEY (created_by_id) REFERENCES users(id),
    INDEX idx_reminders_user (user_id),
    INDEX idx_reminders_task (task_id),
    INDEX idx_reminders_plan (plan_id),
    INDEX idx_reminders_due (remind_at, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reminder_delivery_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reminder_id BIGINT NOT NULL,
    delivery_channel VARCHAR(30) NOT NULL DEFAULT 'IN_APP',
    delivery_status VARCHAR(30) NOT NULL,
    delivered_at DATETIME(6) NULL,
    error_message VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_reminder_log_reminder FOREIGN KEY (reminder_id) REFERENCES reminders(id) ON DELETE CASCADE,
    INDEX idx_reminder_log_reminder (reminder_id),
    INDEX idx_reminder_log_status (delivery_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO tags (title, slug, color) VALUES
('Frontend', 'frontend', '#2f8cff'),
('Backend', 'backend', '#24d3c8'),
('Database', 'database', '#f1b84b'),
('JWT / Security', 'jwt-security', '#ff6b78'),
('Testing', 'testing', '#38d996'),
('Urgent', 'urgent', '#ff6b78');
