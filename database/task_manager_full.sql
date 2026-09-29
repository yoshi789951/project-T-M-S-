CREATE DATABASE IF NOT EXISTS `task_manager` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `task_manager`;

-- TaskFlow Task Manager full schema
-- Compatible with MySQL 8.x and designed in the same dump-style structure as the supplied task_manager SQL.

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `reminder_delivery_log`;
DROP TABLE IF EXISTS `reminders`;
DROP TABLE IF EXISTS `plan_items`;
DROP TABLE IF EXISTS `plans`;
DROP TABLE IF EXISTS `task_tags`;
DROP TABLE IF EXISTS `task_meta`;
DROP TABLE IF EXISTS `tags`;
DROP TABLE IF EXISTS `activity_logs`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `tasks`;
DROP TABLE IF EXISTS `projects`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `middle_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt hash',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `registered_at` datetime(6) DEFAULT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `intro` varchar(255) COLLATE utf8mb4_unicode_ci,
  `profile` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_email` (`email`),
  UNIQUE KEY `uq_users_username` (`username`),
  UNIQUE KEY `uq_users_mobile` (`mobile`),
  KEY `idx_users_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `color` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#2563eb',
  `created_by_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_projects_status` (`status`),
  KEY `idx_projects_creator` (`created_by_id`),
  CONSTRAINT `fk_project_creator` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `assignee_id` bigint DEFAULT NULL,
  `created_by_id` bigint NOT NULL,
  `title` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDIUM',
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TODO',
  `due_date` date DEFAULT NULL,
  `estimated_hours` decimal(8,2) NOT NULL DEFAULT '0.00',
  `actual_hours` decimal(8,2) NOT NULL DEFAULT '0.00',
  `planned_start_at` datetime(6) DEFAULT NULL,
  `planned_end_at` datetime(6) DEFAULT NULL,
  `actual_start_at` datetime(6) DEFAULT NULL,
  `actual_end_at` datetime(6) DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tasks_project` (`project_id`),
  KEY `idx_tasks_assignee` (`assignee_id`),
  KEY `idx_tasks_status` (`status`),
  KEY `idx_tasks_due_date` (`due_date`),
  KEY `idx_tasks_priority` (`priority`),
  KEY `idx_tasks_creator` (`created_by_id`),
  CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_task_creator` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `author_id` bigint NOT NULL,
  `activity_id` bigint DEFAULT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `body` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_comments_task` (`task_id`),
  KEY `idx_comments_author` (`author_id`),
  KEY `idx_comments_activity` (`activity_id`),
  CONSTRAINT `fk_comment_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `activity_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `project_id` bigint DEFAULT NULL,
  `created_by_id` bigint NOT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `title` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(2048) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OPEN',
  `hours` decimal(8,2) NOT NULL DEFAULT '0.00',
  `planned_start_at` datetime(6) DEFAULT NULL,
  `planned_end_at` datetime(6) DEFAULT NULL,
  `actual_start_at` datetime(6) DEFAULT NULL,
  `actual_end_at` datetime(6) DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_activity_user` (`user_id`),
  KEY `idx_activity_task` (`task_id`),
  KEY `idx_activity_project` (`project_id`),
  KEY `idx_activity_created_at` (`created_at`),
  CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_activity_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_activity_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_activity_creator` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_activity_modifier` FOREIGN KEY (`updated_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `comments` ADD CONSTRAINT `fk_comment_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity_logs` (`id`) ON DELETE SET NULL;

CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(75) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `color` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#2563eb',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tags_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `task_meta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `meta_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_task_meta` (`task_id`,`meta_key`),
  KEY `idx_task_meta_task` (`task_id`),
  CONSTRAINT `fk_task_meta_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `task_tags` (
  `task_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`task_id`,`tag_id`),
  KEY `idx_task_tags_task` (`task_id`),
  KEY `idx_task_tags_tag` (`tag_id`),
  CONSTRAINT `fk_task_tags_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `plans` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `created_by_id` bigint NOT NULL,
  `title` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MEDIUM',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `progress_percent` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plans_user` (`user_id`),
  KEY `idx_plans_project` (`project_id`),
  KEY `idx_plans_status` (`status`),
  KEY `idx_plans_dates` (`start_date`,`end_date`),
  CONSTRAINT `fk_plan_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_plan_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_plan_creator` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `plan_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `title` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'TODO',
  `planned_start_at` datetime(6) DEFAULT NULL,
  `planned_end_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_plan_items_plan` (`plan_id`),
  KEY `idx_plan_items_task` (`task_id`),
  KEY `idx_plan_items_status` (`status`),
  CONSTRAINT `fk_plan_item_plan` FOREIGN KEY (`plan_id`) REFERENCES `plans` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_plan_item_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reminders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_id` bigint DEFAULT NULL,
  `plan_id` bigint DEFAULT NULL,
  `created_by_id` bigint NOT NULL,
  `title` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci,
  `remind_at` datetime(6) NOT NULL,
  `repeat_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NONE',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reminders_user` (`user_id`),
  KEY `idx_reminders_task` (`task_id`),
  KEY `idx_reminders_plan` (`plan_id`),
  KEY `idx_reminders_due` (`remind_at`,`status`),
  CONSTRAINT `fk_reminder_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_reminder_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_reminder_plan` FOREIGN KEY (`plan_id`) REFERENCES `plans` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_reminder_creator` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reminder_delivery_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reminder_id` bigint NOT NULL,
  `delivery_channel` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN_APP',
  `delivery_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_reminder_log_reminder` (`reminder_id`),
  KEY `idx_reminder_log_status` (`delivery_status`),
  CONSTRAINT `fk_reminder_log_reminder` FOREIGN KEY (`reminder_id`) REFERENCES `reminders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Required/reference data used by the UI and task-management workflow.
LOCK TABLES `tags` WRITE;
INSERT INTO `tags` (`title`,`slug`,`color`) VALUES
('Frontend','frontend','#2f8cff'),
('Backend','backend','#24d3c8'),
('Database','database','#f1b84b'),
('JWT / Security','jwt-security','#ff6b78'),
('Testing','testing','#38d996'),
('Urgent','urgent','#ff6b78');
UNLOCK TABLES;

-- Application seed note:
-- The ADMIN user is inserted by Spring Boot DataSeeder with a BCrypt password hash.
-- Default development account: admin@taskflow.local / Admin123!

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
