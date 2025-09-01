-- SQL DDL for notifications table
-- Adjust engine/charset as needed; add foreign key constraints if your DB has existing tables `users` and `appointments`

CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `recipient_user_id` BIGINT NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `message` VARCHAR(1000) NOT NULL,
  `related_appointment_id` BIGINT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `read_at` DATETIME(6) NULL,
  `deleted_at` DATETIME(6) NULL,
  `message_key` VARCHAR(150) NULL,
  `message_args` VARCHAR(500) NULL,
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`recipient_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notifications_appt` FOREIGN KEY (`related_appointment_id`) REFERENCES `appointments`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_notifications_user_created` ON `notifications`(`recipient_user_id`, `created_at` DESC);
CREATE INDEX `idx_notifications_user_unread` ON `notifications`(`recipient_user_id`, `read_at`);
CREATE INDEX `idx_notifications_deleted` ON `notifications`(`deleted_at`);
