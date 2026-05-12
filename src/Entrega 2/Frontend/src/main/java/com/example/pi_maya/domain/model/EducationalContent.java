package com.example.pi_maya.domain.model;

public class EducationalContent {
    public enum Type { POST, VIDEO, TIP, UNKNOWN }

    public final String id;
    public final String title;
    public final String body;
    public final String coverUrl;
    public final String videoUrl;
    public final Type type;
    public final String category;

    public EducationalContent(String id, String title, String body, String coverUrl,
                              String videoUrl, Type type, String category) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.coverUrl = coverUrl;
        this.videoUrl = videoUrl;
        this.type = type;
        this.category = category;
    }

    public static Type typeFromString(String raw) {
        if (raw == null) return Type.UNKNOWN;
        switch (raw) {
            case "post":  return Type.POST;
            case "video": return Type.VIDEO;
            case "tip":   return Type.TIP;
            default: return Type.UNKNOWN;
        }
    }
}
