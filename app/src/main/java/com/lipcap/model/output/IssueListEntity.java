package com.lipcap.model.output;

public class IssueListEntity {

    private int Id=0;
    private int UserId=0;
    private int IssueId=0;
    private double UserLatitude=0;
    private double UserLongitude=0;
    private String AppointmentStatus="";
    private String CreatedDate="";
    private int Id1=0;
    private String IssueName="";
    private String IssueDescription="";


    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getIssueId() {
        return IssueId;
    }

    public void setIssueId(int issueId) {
        IssueId = issueId;
    }

    public double getUserLatitude() {
        return UserLatitude;
    }

    public void setUserLatitude(double userLatitude) {
        UserLatitude = userLatitude;
    }

    public double getUserLongitude() {
        return UserLongitude;
    }

    public void setUserLongitude(double userLongitude) {
        UserLongitude = userLongitude;
    }

    public String getAppointmentStatus() {
        return AppointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        AppointmentStatus = appointmentStatus;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public int getId1() {
        return Id1;
    }

    public void setId1(int id1) {
        Id1 = id1;
    }


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
