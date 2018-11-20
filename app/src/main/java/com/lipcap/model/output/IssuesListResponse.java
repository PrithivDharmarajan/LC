package com.lipcap.model.output;

public class IssuesListResponse extends CommonResponse {

    private ResultListEntity IssueType = new ResultListEntity();

    public ResultListEntity getIssueType() {
        return IssueType;
    }

    public void setIssueType(ResultListEntity issueType) {
        IssueType = issueType;
    }


}
