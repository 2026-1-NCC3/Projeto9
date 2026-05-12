package com.example.pi_maya.domain.model;

import java.time.OffsetDateTime;

public class Appointment {
    public enum Status { SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW, UNKNOWN }

    public final String id;
    public final String patientId;
    public final String therapistId;
    public final String therapistName;
    public final OffsetDateTime startsAt;
    public final OffsetDateTime endsAt;
    public final Status status;
    public final String notes;

    public Appointment(String id, String patientId, String therapistId, String therapistName,
                       OffsetDateTime startsAt, OffsetDateTime endsAt, Status status, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.therapistId = therapistId;
        this.therapistName = therapistName;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = status;
        this.notes = notes;
    }

    public static Status statusFromString(String raw) {
        if (raw == null) return Status.UNKNOWN;
        switch (raw) {
            case "scheduled": return Status.SCHEDULED;
            case "confirmed": return Status.CONFIRMED;
            case "completed": return Status.COMPLETED;
            case "cancelled": return Status.CANCELLED;
            case "no_show":   return Status.NO_SHOW;
            default: return Status.UNKNOWN;
        }
    }
}
