package com.lidcap.model.input;

public class PendingAppointmentInputEntity {
    private String userId = "";
    private String dateTime = "";

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
