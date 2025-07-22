package com.fyp.AABookingProject.ocr.controller;

import com.fyp.AABookingProject.core.entity.ScheduleBlock;
import com.fyp.AABookingProject.core.entity.Timetable;

import com.fyp.AABookingProject.ocr.model.ScheduleBlockDTO;
import com.fyp.AABookingProject.ocr.repository.ScheduleBlockRepository;
import com.fyp.AABookingProject.ocr.repository.TimetableRepository;
import com.fyp.AABookingProject.ocr.service.GoogleOcrService;
import com.fyp.AABookingProject.ocr.service.OcrParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Time;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetables")
public class TimetableController {

    @Autowired
    private GoogleOcrService googleOcrService;

    @Autowired
    private TimetableRepository timetableRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTimetable(@RequestParam("file") MultipartFile file,
                                             @RequestParam("userId") Long userId) {
        try {
            String imageUrl = "/fake-storage/" + file.getOriginalFilename();

            // 使用 Google OCR 提取文字
            String rawText = googleOcrService.extractTextFromImage(file);

            // 保存 Timetable
            Timetable timetable = new Timetable();
            timetable.setUserId(userId);
            timetable.setImageUrl(imageUrl);
            timetable.setOcrRawText(rawText);
            timetable.setSemester("2025S1");
            timetableRepository.save(timetable);

            // 返回 JSON 数据，前端方便提取 ID
            return ResponseEntity.ok(Map.of(
                    "message", "Timetable uploaded and OCR done.",
                    "timetableId", timetable.getId()
            ));

        } catch (Exception e) {
            e.printStackTrace(); // 打印错误堆栈，方便调试
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

}
// 解析 OCR 文本并写入 sch
