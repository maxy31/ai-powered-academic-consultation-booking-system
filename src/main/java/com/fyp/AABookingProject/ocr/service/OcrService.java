package com.fyp.AABookingProject.ocr.service;

import com.fyp.AABookingProject.core.entity.Timetable;
import com.fyp.AABookingProject.core.entity.TimetableEntry;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.ocr.repository.TimetableEntryRepository;
import com.fyp.AABookingProject.ocr.NamedByteArrayResource;
import com.fyp.AABookingProject.ocr.repository.TimetableRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.Optional;

@Service
public class OcrService {
    @Value("${python.ocr.url}")
    private String pythonUrl;
    @Value("${python.ocr.read-timeout-ms:30000}")
    private int readTimeoutMs;
    private final WebClient webClient;
    private final TimetableEntryRepository repository;
    private final TimetableRepository timetableRepository;
    private final UserRepository userRepository;

    // 这里 pythonServiceUrl 指向运行中的 Flask 服务
    private final String pythonServiceUrl = "http://python-host:5001/parse-timetable";

    public OcrService(WebClient webClient, TimetableEntryRepository repository, TimetableRepository timetableRepository, UserRepository userRepository) {
        this.webClient = webClient;
        this.repository = repository;
        this.timetableRepository = timetableRepository;
        this.userRepository = userRepository;
    }

    public List<TimetableEntry> forwardToPythonAndSave(byte[] fileBytes, String filename) {
        // 构造 multipart
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        multipart.add("file", new NamedByteArrayResource(fileBytes, filename));

        // 假设 webClient 已注入，pythonUrl 由 @Value 注入
        Map<String,Object> resp = webClient.post()
                .uri(pythonUrl) // 从配置注入，例如 @Value("${python.service.url}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipart))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500)).maxBackoff(Duration.ofSeconds(2)))
                .block(Duration.ofMillis(readTimeoutMs + 2000)); // 阻塞等待（如果你使用 blocking）

        if (resp == null || !resp.containsKey("slots")) {
            throw new RuntimeException("Invalid response from OCR service");
        }

        Timetable timetable = new Timetable();
        UserDetails userDetails = getUserDetails();
        Optional<User> userTarget =  userRepository.findByUsername(userDetails.getUsername());

        if(userTarget.isPresent()){
            User user = userTarget.get();
            timetable.setUserId(user.getId());
            timetable.setCreatedAt(LocalDateTime.now());
            timetableRepository.save(timetable);
        }

        List<Map<String, Object>> slots = (List<Map<String, Object>>) resp.get("slots");
        List<TimetableEntry> entities = new ArrayList<>();
        for (Map<String,Object> m : slots) {
            TimetableEntry e = new TimetableEntry();
            e.setTimetable(timetable);
            e.setDay((String) m.get("day"));
            e.setStartTime((String) m.get("start_time"));
            e.setEndTime((String) m.get("end_time"));
            Number gi = (Number) m.get("grid_index");
            Number ns = (Number) m.get("num_slots");
            e.setGridIndex(gi != null ? gi.intValue() : null);
            e.setNumSlots(ns != null ? ns.intValue() : null);
            e.setCreatedAt(LocalDateTime.now());
            entities.add(e);
        }
        return repository.saveAll(entities);
    }

    private UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (UserDetails) authentication.getPrincipal();
    }
}
