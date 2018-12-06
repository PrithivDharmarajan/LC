package com.lipcap.model.output;

import java.io.Serializable;

public class UploadedResponse implements Serializable {

    private String status="";
    private String statusCode="";
    private String message="";

    public UploadedEntity getResult() {
        return result;
    }

    public void setResult(UploadedEntity result) {
        this.result = result;
    }

    private  UploadedEntity result=new UploadedEntity();



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
