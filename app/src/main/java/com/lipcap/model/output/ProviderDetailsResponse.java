package com.lipcap.model.output;

import java.io.Serializable;
import java.util.ArrayList;

public class ProviderDetailsResponse implements Serializable {

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

    private ArrayList<UserDetailsEntity> result=new ArrayList<>();

    public ArrayList<UserDetailsEntity> getResult() {
        return result;
    }

    public void setResult(ArrayList<UserDetailsEntity> result) {
        this.result = result;
    }
}
