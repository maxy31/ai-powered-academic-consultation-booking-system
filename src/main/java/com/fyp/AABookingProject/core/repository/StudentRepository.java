package com.fyp.AABookingProject.core.repository;

import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findById(Long Id);
}
