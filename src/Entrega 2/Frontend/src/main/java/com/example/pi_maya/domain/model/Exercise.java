package com.example.pi_maya.domain.model;

public class Exercise {
    public final String id;
    public final String title;
    public final String description;
    public final String instructions;
    public final String videoUrl;
    public final String thumbnailUrl;
    public final Integer difficulty;
    public final Integer durationSeconds;
    public final String category;

    public Exercise(String id, String title, String description, String instructions,
                    String videoUrl, String thumbnailUrl, Integer difficulty,
                    Integer durationSeconds, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructions = instructions;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.difficulty = difficulty;
        this.durationSeconds = durationSeconds;
        this.category = category;
    }
}
