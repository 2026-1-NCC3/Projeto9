package com.example.pi_maya.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.Appointment;

import java.util.List;

public interface AppointmentRepository {
    LiveData<Resource<Appointment>> getNextAppointment();
    LiveData<Resource<List<Appointment>>> getUpcomingAppointments();
    LiveData<Resource<List<Appointment>>> getAllMyAppointments();
}
