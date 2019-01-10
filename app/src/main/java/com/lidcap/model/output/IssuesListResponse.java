package com.lidcap.model.output;

import java.util.ArrayList;

public class IssuesListResponse  {

    private String status="";
    private String statusCode="";
    private String message="";
    private ArrayList<AppointmentDetailsEntity> result = new ArrayList<>();

    public ArrayList<AppointmentDetailsEntity> getResult() {
        return result;
    }

    public void setResult(ArrayList<AppointmentDetailsEntity> result) {
        this.result = result;
    }


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
