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
    private Long id;  // Matches users.id

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String department;

    private String phone;

    @Column(name = "max_daily_appointments")
    private int maxDailyAppointments = 4;
}