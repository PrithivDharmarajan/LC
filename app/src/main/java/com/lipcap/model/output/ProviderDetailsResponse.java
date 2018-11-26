package com.lipcap.model.output;

import java.io.Serializable;
import java.util.ArrayList;

public class ProviderDetailsResponse implements Serializable {

    private String MsgCode="";
    private ArrayList<ProviderDetailsEntity> UserDetail=new ArrayList<>();

    public String getMsgCode() {
        return MsgCode;
    }

    public void setMsgCode(String msgCode) {
        MsgCode = msgCode;
    }

    public ArrayList<ProviderDetailsEntity> getUserDetail() {
        return UserDetail;
    }

    public void setUserDetail(ArrayList<ProviderDetailsEntity> userDetail) {
        UserDetail = userDetail;
    }
}
