package com.fyp.AABookingProject.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "advisors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Advisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Optional numeric department id (can be null if not used)
    @Column(name = "depart_id")
    private Long departmentId;

    // Actual department name column present in DB and required (resolves 'Field \"department\" doesn't have a default value')
    @Column(name = "department", nullable = false)
    private String department = "null";

    @Column(name = "max_daily_appointments")
    private int maxDailyAppointments = 4;
}