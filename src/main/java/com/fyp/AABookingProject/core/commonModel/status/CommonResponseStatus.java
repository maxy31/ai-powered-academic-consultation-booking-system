package com.fyp.AABookingProject.core.commonModel.status;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommonResponseStatus {
    private String responseCode;
    private String status;
    private String statusMsg;
    private String errorCode;
    private String errorMsg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date respDatetime;
    private String uuid;

    public CommonResponseStatus(String responseCode, String status, String statusMsg, String errorCode, String errorMsg, String uuid) {
        this.responseCode = responseCode;
        this.status = status;
        this.statusMsg = statusMsg;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.respDatetime = new Date();
        this.uuid = uuid;
    }

    public CommonResponseStatus(String responseCode, String status, String statusMsg, String errorCode, String errorMsg) {
        this.responseCode = responseCode;
        this.status = status;
        this.statusMsg = statusMsg;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.respDatetime = new Date();
    }
}
