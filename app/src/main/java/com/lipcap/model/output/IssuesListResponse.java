package com.lipcap.model.output;

import java.util.ArrayList;

public class IssuesListResponse extends CommonModelResponse {

    private ArrayList<IssueListEntity> UserDetail = new ArrayList<>();

    public ArrayList<IssueListEntity> getUserDetail() {
        return UserDetail;
    }

    public void setUserDetail(ArrayList<IssueListEntity> userDetail) {
        UserDetail = userDetail;
    }




}
