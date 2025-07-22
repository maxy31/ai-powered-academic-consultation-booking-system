package com.fyp.AABookingProject.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "timetables")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "ocr_raw_text")
    private String ocrRawText;
    @Column(name = "semester")
    private String semester;
    @Column(name = "created_at")
    private String createdAt;
}
