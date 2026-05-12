package com.example.pi_maya.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.Patient;

public interface PatientRepository {
    LiveData<Resource<Patient>> getMyPatientRecord();
}
