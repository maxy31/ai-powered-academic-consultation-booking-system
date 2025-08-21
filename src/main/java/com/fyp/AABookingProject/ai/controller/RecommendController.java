package com.fyp.AABookingProject.ai.controller;
import com.fyp.AABookingProject.ai.model.FreeSlot;
import com.fyp.AABookingProject.ai.service.AvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
public class RecommendController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    private final AvailabilityService availabilityService;

    public RecommendController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * GET 版本（简单，适合前端用 query params 调用）
     * Example:
     *  GET /api/availability/recommend?studentId=1&lecturerId=2&topN=10
     */
    @GetMapping("/recommend")
    public ResponseEntity<List<FreeSlot>> recommendGet(
            @RequestParam Long studentId,
            @RequestParam Long lecturerId,
            @RequestParam(required = false, defaultValue = "0") int topN) {
        try {
            List<FreeSlot> slots = availabilityService.recommendForStudent(studentId, lecturerId, topN);
            return ResponseEntity.ok(slots);
        } catch (Exception ex) {
            logger.error("recommendGet error for studentId={}, lecturerId={}", studentId, lecturerId, ex);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * POST 版本（接收 JSON）—— 如果前端想以 body 传入更多选项（例如 preferredDays），可用此接口。
     * Request body shape (JSON):
     * { "studentId":1, "lecturerId":2, "topN":10 }
     */
    @PostMapping("/recommend")
    public ResponseEntity<List<FreeSlot>> recommendPost(@RequestBody Map<String, Object> body) {
        try {
            Long studentId = ((Number) body.get("studentId")).longValue();
            Long lecturerId = ((Number) body.get("lecturerId")).longValue();
            int topN = body.get("topN") == null ? 0 : ((Number) body.get("topN")).intValue();

            List<FreeSlot> slots = availabilityService.recommendForStudent(studentId, lecturerId, topN);
            return ResponseEntity.ok(slots);
        } catch (Exception ex) {
            logger.error("recommendPost error, body={}", body, ex);
            return ResponseEntity.status(400).body(Collections.emptyList());
        }
    }
}
