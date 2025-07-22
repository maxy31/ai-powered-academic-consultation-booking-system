package com.fyp.AABookingProject.core.repository;

import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findById(Long id);
}
