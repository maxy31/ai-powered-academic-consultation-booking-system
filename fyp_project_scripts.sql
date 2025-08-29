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
    department VARCHAR(100) NOT NULL,
    max_daily_appointments INT DEFAULT 4,
    CONSTRAINT fk_advisor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Students table
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT NOT NULL,
    advisor_id BIGINT,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_advisor FOREIGN KEY (advisor_id) REFERENCES advisors(id) ON DELETE SET NULL
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
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED'ai_training_dataai_training_data) DEFAULT 'PENDING',
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

