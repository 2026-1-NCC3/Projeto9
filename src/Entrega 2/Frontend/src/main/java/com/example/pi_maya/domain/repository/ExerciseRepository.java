package com.example.pi_maya.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.ExerciseAssignment;

import java.util.List;

public interface ExerciseRepository {
    LiveData<Resource<List<ExerciseAssignment>>> getMyAssignments();
}
