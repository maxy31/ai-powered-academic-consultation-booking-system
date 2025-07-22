package com.fyp.AABookingProject.core.entity;

import com.fyp.AABookingProject.core.enumClass.Platform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_devices")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "device_token")
    private String deviceToken;
    @Column(name = "platform")
    @Enumerated(EnumType.STRING)
    private Platform platform;
    @Column(name = "last_active")
    private String lastActive;
}
