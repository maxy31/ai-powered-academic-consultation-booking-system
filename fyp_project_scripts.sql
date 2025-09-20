DROP DATABASE IF EXISTS fyp_project;
CREATE DATABASE fyp_project;
USE fyp_project;

-- Users table (Base table for both students and advisors)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL, 
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('STUDENT', 'ADVISOR', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Admin table
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Advisors table
CREATE TABLE advisors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    depart_id BIGINT NOT NULL,
    max_daily_appointments INT DEFAULT 4,
    CONSTRAINT fk_advisor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_department FOREIGN KEY (depart_id) REFERENCES department(id) ON DELETE CASCADE
);

-- Students table
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT NOT NULL,
    advisor_id BIGINT,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_advisor FOREIGN KEY (advisor_id) REFERENCES advisors(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS `department` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    departmentName VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Timetables -- 
CREATE TABLE IF NOT EXISTS `timetables` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_timetable_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ;

-- Timetable entries -- 
CREATE TABLE IF NOT EXISTS `timetable_entry` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `day` VARCHAR(16) NOT NULL,
  `start_time` VARCHAR(16) NULL,
  `end_time` VARCHAR(16) NULL,
  `grid_index` INT DEFAULT NULL,
  `num_slots` INT DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `timetable_id` BIGINT NOT NULL,
  CONSTRAINT `fk_timetable_entry_to_timetables`
    FOREIGN KEY (`timetable_id`) REFERENCES `timetables` (`id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

-- Appointments table
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- Changed to BIGINT
    student_id BIGINT NOT NULL,
    advisor_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_advisor FOREIGN KEY (advisor_id) REFERENCES advisors(id) ON DELETE CASCADE
);

-- Announcement
CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    publisherName VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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

