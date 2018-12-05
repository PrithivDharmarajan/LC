package com.lipcap.model.output;

import java.util.ArrayList;

public class SelectIssuesTypeResponse  {

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

    public ArrayList<IssueListEntity> getResult() {
        return result;
    }

    public void setResult(ArrayList<IssueListEntity> result) {
        this.result = result;
    }

    private ArrayList<IssueListEntity> result = new ArrayList<>();



}
