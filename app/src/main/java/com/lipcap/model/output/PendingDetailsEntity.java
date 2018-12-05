package com.lipcap.model.output;

import java.util.ArrayList;

public class PendingDetailsEntity {

    private ArrayList<UserDetailsEntity>profile=new ArrayList<>();

    public ArrayList<UserDetailsEntity> getAnotheruser() {
        return anotheruser;
    }

    public void setAnotheruser(ArrayList<UserDetailsEntity> anotheruser) {
        this.anotheruser = anotheruser;
    }

    private ArrayList<UserDetailsEntity>anotheruser=new ArrayList<>();
    private ArrayList<AppointmentDetailsEntity>appointments=new ArrayList<>();

    public ArrayList<AppointmentDetailsEntity> getAppointments() {
        return appointments;
    }

    public void setAppointments(ArrayList<AppointmentDetailsEntity> appointments) {
        this.appointments = appointments;
    }

    public ArrayList<UserDetailsEntity> getProfile() {
        return profile;
    }

    public void setProfile(ArrayList<UserDetailsEntity> profile) {
        this.profile = profile;
    }
}
