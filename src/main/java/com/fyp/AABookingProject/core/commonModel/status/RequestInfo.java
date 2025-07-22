package com.fyp.AABookingProject.core.commonModel.status;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestInfo {
    @Max(512)
    private String deviceId;
    @Max(50)
    private String ipAddress;
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss.SSS")
    private Date reqDatetime;
}
