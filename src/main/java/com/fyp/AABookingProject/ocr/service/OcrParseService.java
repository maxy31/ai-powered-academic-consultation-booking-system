package com.fyp.AABookingProject.ocr.service;

import com.fyp.AABookingProject.ocr.model.ScheduleBlockDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class OcrParseService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ScheduleBlockDTO> parseRawOcrText(String rawText) throws Exception {
        String url = "http://localhost:5001/parse-timetable";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("ocr_raw_text", rawText);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        String json = response.getBody();

        return objectMapper.readValue(json, new TypeReference<List<ScheduleBlockDTO>>() {});
    }
}
