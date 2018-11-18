package com.lipcap.model.output;

public class UserDetailsEntity {

    private int Id = 0;
    private String Name;
    private String MobileNo = "";
    private String CreatedDT = "";
    private int UserType = 0;
    private String DeviceId = "";

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public String getCreatedDT() {
        return CreatedDT;
    }

    public void setCreatedDT(String createdDT) {
        CreatedDT = createdDT;
    }

    public int getUserType() {
        return UserType;
    }

    public void setUserType(int userType) {
        UserType = userType;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

}
