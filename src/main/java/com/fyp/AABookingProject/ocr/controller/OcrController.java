package com.fyp.AABookingProject.ocr.controller;

import com.fyp.AABookingProject.core.entity.TimetableEntry;
import com.fyp.AABookingProject.ocr.service.OcrService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @GetMapping("/test1")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Welcome to ocr.");
    }

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/parse-and-save")
    public ResponseEntity<List<TimetableEntry>> parseAndSave(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        byte[] bytes = file.getBytes();
        List<TimetableEntry> saved = ocrService.forwardToPythonAndSave(bytes, file.getOriginalFilename());
        return ResponseEntity.ok(saved);
    }
}
