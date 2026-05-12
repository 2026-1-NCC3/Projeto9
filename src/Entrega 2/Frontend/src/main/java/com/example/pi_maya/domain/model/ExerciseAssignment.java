package com.example.pi_maya.domain.model;

public class ExerciseAssignment {
    public final String id;
    public final String patientId;
    public final Exercise exercise;
    public final Integer targetRepetitions;
    public final Integer targetSets;
    public final Integer frequencyPerWeek;
    public final String notes;
    public final boolean active;

    public ExerciseAssignment(String id, String patientId, Exercise exercise,
                              Integer targetRepetitions, Integer targetSets,
                              Integer frequencyPerWeek, String notes, boolean active) {
        this.id = id;
        this.patientId = patientId;
        this.exercise = exercise;
        this.targetRepetitions = targetRepetitions;
        this.targetSets = targetSets;
        this.frequencyPerWeek = frequencyPerWeek;
        this.notes = notes;
        this.active = active;
    }
}
