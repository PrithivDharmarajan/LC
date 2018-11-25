package com.lipcap.model.output;

import java.util.ArrayList;

public class LoginResponse extends CommonModelResponse {

    public ArrayList<UserDetailsEntity> getUserDetail() {
        return UserDetail;
    }

    public void setUserDetail(ArrayList<UserDetailsEntity> userDetail) {
        UserDetail = userDetail;
    }

    private ArrayList<UserDetailsEntity> UserDetail=new ArrayList<>();
}
