package com.fyp.AABookingProject.ai.service;

import com.fyp.AABookingProject.ai.model.CandidateDto;
import com.fyp.AABookingProject.ai.model.FreeSlot;
import com.fyp.AABookingProject.core.entity.TimetableEntry;
import com.fyp.AABookingProject.ocr.repository.TimetableEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {
    private final TimetableEntryRepository entryRepo;
    private final WebClient webClient;

    @Value("${python.scoring.url:http://localhost:8001/score-slots}")
    private String scoringUrl;

    private final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");
    private final List<String> DAYS = Arrays.asList("Mon","Tue","Wed","Thu","Fri","Sat","Sun");

    public AvailabilityService(TimetableEntryRepository entryRepo, WebClient webClient) {
        this.entryRepo = entryRepo;
        this.webClient = webClient;
    }

    // 生成 08:00..17:30 的起始时间点
    private List<LocalTime> buildBusinessSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 30); // last slot start at 17:30 -> ends 18:00
        while (!t.isAfter(end)) { slots.add(t); t = t.plusMinutes(30); }
        return slots;
    }

    // 把已有 entries 映射成 day -> boolean[] occupied （长度 slots.size()）
    private Map<String, boolean[]> buildOccupiedMap(List<TimetableEntry> entries) {
        List<LocalTime> slots = buildBusinessSlots();
        Map<String, boolean[]> map = new HashMap<>();
        for (String d : DAYS) map.put(d, new boolean[slots.size()]);
        for (TimetableEntry e : entries) {
            String day = e.getDay();
            if (!map.containsKey(day)) continue;
            boolean[] occ = map.get(day);
            try {
                int start = timeToIndex(e.getStartTime(), slots);
                int end = timeToIndex(e.getEndTime(), slots);
                for (int i = Math.max(0,start); i < Math.min(end, occ.length); i++) occ[i] = true;
            } catch (Exception ex) {
                // 忽略格式异常或记录日志
            }
        }
        return map;
    }

    // 将 "HH:mm" 映到 slots 索引（若不匹配返回最近floor）
    private int timeToIndex(String hhmm, List<LocalTime> slots) {
        LocalTime t = LocalTime.parse(hhmm, TF);
        for (int i = 0; i < slots.size(); i++) if (slots.get(i).equals(t)) return i;
        // fallback: take nearest previous
        for (int i = 0; i < slots.size()-1; i++) {
            if (!t.isBefore(slots.get(i)) && t.isBefore(slots.get(i+1))) return i;
        }
        return slots.size()-1;
    }

    /**
     * 主方法：返回按分数排序的可选 30-min 槽（可选 topN）
     */
    public List<FreeSlot> recommendForStudent(Long studentUserId, Long lecturerUserId, int topN) {
        List<TimetableEntry> studentEntries = entryRepo.findByTimetableUserId(studentUserId);
        List<TimetableEntry> lecturerEntries = entryRepo.findByTimetableUserId(lecturerUserId);

        List<LocalTime> businessSlots = buildBusinessSlots();
        Map<String, boolean[]> studentOcc = buildOccupiedMap(studentEntries);
        Map<String, boolean[]> lecturerOcc = buildOccupiedMap(lecturerEntries);

        // 1) 构造确定性候选（双方都 free）
        List<CandidateDto> candidates = new ArrayList<>();
        for (String day : DAYS) {
            boolean[] sOcc = studentOcc.getOrDefault(day, new boolean[businessSlots.size()]);
            boolean[] lOcc = lecturerOcc.getOrDefault(day, new boolean[businessSlots.size()]);
            for (int i = 0; i < businessSlots.size(); i++) {
                if (!sOcc[i] && !lOcc[i]) {
                    // 计算简单的 free rate 特征（可替换为更精细统计）
                    double sFreeRate = computeFreeRate(studentOcc, day);
                    double lFreeRate = computeFreeRate(lecturerOcc, day);
                    candidates.add(new CandidateDto(day, i, sFreeRate, lFreeRate));
                }
            }
        }

        if (candidates.isEmpty()) return Collections.emptyList();

        // 2) 调评分服务
        try {
            Map<String,Object> body = new HashMap<>();
            body.put("studentId", studentUserId);
            body.put("lecturerId", lecturerUserId);
            // fastapi 接口期待 day & slot_index & free rates
            List<Map<String,Object>> candForApi = candidates.stream().map(c -> {
                Map<String,Object> m = new HashMap<>();
                m.put("day", c.getDay());
                m.put("slot_index", c.getSlotIndex());
                m.put("student_free_rate", c.getStudentFreeRate());
                m.put("lecturer_free_rate", c.getLecturerFreeRate());
                return m;
            }).collect(Collectors.toList());
            body.put("candidates", candForApi);

            // 使用 WebClient 调用（带简单重试）
            Map<String,Object> resp = webClient.post()
                    .uri(scoringUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(300)).maxBackoff(Duration.ofSeconds(2)))
                    .block(Duration.ofSeconds(10));

            // 解析 scores（与候选一一对应）
            @SuppressWarnings("unchecked")
            List<Double> scores = (List<Double>) resp.get("scores");

            // 组装结果并按分排序
            List<Map.Entry<CandidateDto, Double>> zipped = new ArrayList<>();
            for (int i = 0; i < candidates.size() && i < scores.size(); i++) {
                zipped.add(new AbstractMap.SimpleEntry<>(candidates.get(i), scores.get(i)));
            }
            zipped.sort((a,b) -> Double.compare(b.getValue(), a.getValue())); // 降序

            // 只返回 topN（若 topN<=0 表示全部）
            int limit = topN > 0 ? Math.min(topN, zipped.size()) : zipped.size();
            List<FreeSlot> result = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                CandidateDto c = zipped.get(i).getKey();
                LocalTime st = businessSlots.get(c.getSlotIndex());
                LocalTime et = st.plusMinutes(30);
                result.add(new FreeSlot(c.getDay(), st.format(TF), et.format(TF)));
            }
            return result;

        } catch (Exception ex) {
            // scoring 服务失败：回退到确定性候选（按 day/time 排序）
            List<FreeSlot> fallback = candidates.stream()
                    .sorted(Comparator.comparing(CandidateDto::getDay)
                            .thenComparing(CandidateDto::getSlotIndex))
                    .map(c -> {
                        LocalTime st = businessSlots.get(c.getSlotIndex());
                        return new FreeSlot(c.getDay(), st.format(TF), st.plusMinutes(30).format(TF));
                    }).collect(Collectors.toList());
            return fallback;
        }
    }

    // 计算某人当天或整周的简单 free rate（这里按整周可用槽比例）
    private double computeFreeRate(Map<String, boolean[]> occMap, String day) {
        long free = 0;
        long total = 0;
        for (Map.Entry<String, boolean[]> e : occMap.entrySet()) {
            boolean[] arr = e.getValue();
            for (boolean b : arr) { total++; if (!b) free++; }
        }
        return total == 0 ? 0.5 : ((double) free) / total;
    }
}
