package com.fyp.AABookingProject.ocr.repository;

import com.fyp.AABookingProject.core.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Optional<Timetable> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
