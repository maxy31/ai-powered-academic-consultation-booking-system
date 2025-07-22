//package com.fyp.AABookingProject.ocr.service;
//
//import com.fyp.AABookingProject.core.entity.Timetable;
//import com.fyp.AABookingProject.core.entity.User;
//import com.fyp.AABookingProject.core.repository.ScheduleBlockRepository;
//import com.fyp.AABookingProject.core.repository.TimetableRepository;
//import com.fyp.AABookingProject.ocr.integration.GoogleVisionService;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//
//@Service
//public class OCRProcessingService {
//
//    private final GoogleVisionService visionService;
//    private final TimetableParser timetableParser;
//    private final TimetableRepository timetableRepository;
//    private final ScheduleBlockRepository blockRepository;
//
//    public OCRProcessingService(GoogleVisionService visionService,
//                                TimetableParser timetableParser,
//                                TimetableRepository timetableRepository,
//                                ScheduleBlockRepository blockRepository) {
//        this.visionService = visionService;
//        this.timetableParser = timetableParser;
//        this.timetableRepository = timetableRepository;
//        this.blockRepository = blockRepository;
//    }
//
//    @Async
//    public void processTimetable(MultipartFile file, User user) throws IOException {
//        // Step 1: OCR Processing
//        String ocrText = visionService.detectDocumentText(file.getBytes());
//
//        // Step 2: Parse OCR text
//        List<ScheduleBlock> blocks = timetableParser.parseTimetable(ocrText);
//
//        // Step 3: Save to database
//        Timetable timetable = new Timetable();
//        timetable.setUser(user);
//        timetable.setOcrRawText(ocrText);
//        timetable.setCreatedAt(LocalDateTime.now());
//        timetable.setSemester("Spring 2025"); // Should be dynamic
//
//        Timetable savedTimetable = timetableRepository.save(timetable);
//
//        // Save schedule blocks with reference to timetable
//        blocks.forEach(block -> {
//            block.setTimetable(savedTimetable);
//            blockRepository.save(block);
//        });
//    }
//}