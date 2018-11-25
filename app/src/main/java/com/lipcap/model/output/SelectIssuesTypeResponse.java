package com.lipcap.model.output;

import java.util.ArrayList;

public class SelectIssuesTypeResponse extends CommonModelResponse {

    private ArrayList<IssueListEntity> IssueType = new ArrayList<>();

    public ArrayList<IssueListEntity> getIssueType() {
        return IssueType;
    }

    public void setIssueType(ArrayList<IssueListEntity> issueType) {
        IssueType = issueType;
    }


}