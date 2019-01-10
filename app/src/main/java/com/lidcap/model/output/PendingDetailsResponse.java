package com.lidcap.model.output;

public class PendingDetailsResponse   {

    private String status="";
    private String statusCode="";
    private String message="";

    public PendingDetailsEntity getResult() {
        return result;
    }

    public void setResult(PendingDetailsEntity result) {
        this.result = result;
    }

    private  PendingDetailsEntity result=new PendingDetailsEntity();




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
