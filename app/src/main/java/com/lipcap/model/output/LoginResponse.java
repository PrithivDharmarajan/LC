package com.lipcap.model.output;

import java.util.ArrayList;

public class LoginResponse extends CommonModelResponse {

    private ArrayList<UserDetailsEntity> result=new ArrayList<>();

    public ArrayList<UserDetailsEntity> getResult() {
        return result;
    }

    public void setResult(ArrayList<UserDetailsEntity> result) {
        this.result = result;
    }
}
