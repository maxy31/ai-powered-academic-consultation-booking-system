package com.fyp.AABookingProject.ai.controller;

import com.fyp.AABookingProject.ai.model.FreeSlot;
import com.fyp.AABookingProject.ai.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GET /api/availability/free?studentUserId=&lecturerUserId=&topN=
 * 省略 userId 参数则使用当前登录学生及其 advisor
 */
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/free")
    public ResponseEntity<List<FreeSlot>> getFree() {
        List<FreeSlot> slots = availabilityService.recommend();
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/free")
    public ResponseEntity<List<FreeSlot>> postFree(@RequestBody Map<String,Object> body) {
        Long studentUserId = body.get("studentUserId") == null ? null : ((Number)body.get("studentUserId")).longValue();
        Long lecturerUserId = body.get("lecturerUserId") == null ? null : ((Number)body.get("lecturerUserId")).longValue();
        int topN = body.get("topN") == null ? 0 : ((Number)body.get("topN")).intValue();
        List<FreeSlot> slots = availabilityService.recommend();
        return ResponseEntity.ok(slots);
    }
}