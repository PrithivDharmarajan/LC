package com.lidcap.model.output;

import java.io.Serializable;

public class AppointmentAcceptResponse implements Serializable {

    private String status="";
    private String statusCode="";
    private String message="";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
