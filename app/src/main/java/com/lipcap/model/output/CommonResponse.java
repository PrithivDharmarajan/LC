package com.lipcap.model.output;

import java.io.Serializable;

class CommonResponse implements Serializable {

    private String MsgCode="";
    private String Message="";

    public String getMsgCode() {
        return MsgCode;
    }

    public void setMsgCode(String msgCode) {
        MsgCode = msgCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
