package com.example.pi_maya.domain.model;

import java.time.OffsetDateTime;

public class ChatMessage {
    public final String id;
    public final String roomId;
    public final String senderId;
    public final String content;
    public final String attachmentUrl;
    public final String attachmentType;
    public final OffsetDateTime createdAt;
    public final boolean read;

    public ChatMessage(String id, String roomId, String senderId, String content,
                       String attachmentUrl, String attachmentType,
                       OffsetDateTime createdAt, boolean read) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.attachmentUrl = attachmentUrl;
        this.attachmentType = attachmentType;
        this.createdAt = createdAt;
        this.read = read;
    }

    public boolean isMine(String myProfileId) {
        return senderId != null && senderId.equals(myProfileId);
    }
}
