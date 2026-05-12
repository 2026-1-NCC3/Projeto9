package com.example.pi_maya.domain.model;

import java.time.OffsetDateTime;

public class ChatRoom {
    public final String id;
    public final String patientId;
    public final String therapistId;
    public final String therapistName;
    public final OffsetDateTime lastMessageAt;

    public ChatRoom(String id, String patientId, String therapistId,
                    String therapistName, OffsetDateTime lastMessageAt) {
        this.id = id;
        this.patientId = patientId;
        this.therapistId = therapistId;
        this.therapistName = therapistName;
        this.lastMessageAt = lastMessageAt;
    }
}
