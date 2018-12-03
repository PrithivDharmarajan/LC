package com.lipcap.model.output;

import java.util.ArrayList;

public class PendingDetailsResponse   {

    private String MsgCode="";

    private ArrayList<PendingDetailsEntity> UserDetail=new ArrayList<>();

    public String getMsgCode() {
        return MsgCode;
    }

    public void setMsgCode(String msgCode) {
        MsgCode = msgCode;
    }

    public ArrayList<PendingDetailsEntity> getUserDetail() {
        return UserDetail;
    }

    public void setUserDetail(ArrayList<PendingDetailsEntity> userDetail) {
        UserDetail = userDetail;
    }
}
