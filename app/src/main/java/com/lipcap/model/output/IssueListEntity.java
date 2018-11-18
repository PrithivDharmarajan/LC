package com.lipcap.model.output;

public class IssueListEntity {

    private int Id=0;
    private String IssueName="";
    private String IssueDescription="";

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getIssueName() {
        return IssueName;
    }

    public void setIssueName(String issueName) {
        IssueName = issueName;
    }

    public String getIssueDescription() {
        return IssueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        IssueDescription = issueDescription;
    }



}
