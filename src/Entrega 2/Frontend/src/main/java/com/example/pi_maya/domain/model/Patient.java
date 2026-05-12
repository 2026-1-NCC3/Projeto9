package com.example.pi_maya.domain.model;

public class Patient {
    public final String id;
    public final String profileId;
    public final String therapistId;
    public final String primaryComplaint;
    public final String status;

    public Patient(String id, String profileId, String therapistId,
                   String primaryComplaint, String status) {
        this.id = id;
        this.profileId = profileId;
        this.therapistId = therapistId;
        this.primaryComplaint = primaryComplaint;
        this.status = status;
    }
}
