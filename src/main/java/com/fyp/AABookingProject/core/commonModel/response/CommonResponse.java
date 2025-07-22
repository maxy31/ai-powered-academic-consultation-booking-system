package com.fyp.AABookingProject.core.commonModel.response;

import com.fyp.AABookingProject.core.commonModel.status.CommonResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommonResponse {
    private CommonResponseStatus response;

    public void setResponse(String responseCode, String status, String statusMsg, String errorCode, String errorMsg, String uuid) {
        this.response = new CommonResponseStatus(responseCode, status, statusMsg, errorCode, errorMsg, uuid);
    }

    public void setResponse(String responseCode, String status, String statusMsg, String errorCode, String errorMsg) {
        this.response = new CommonResponseStatus(responseCode, status, statusMsg, errorCode, errorMsg);
    }
}
