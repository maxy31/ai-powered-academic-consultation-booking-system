package com.fyp.AABookingProject.ocr.repository;

import com.fyp.AABookingProject.core.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableEntryRepository  extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByTimetableUserId(Long id);
    List<TimetableEntry> findByTimetableId(Long timetableId);
}
