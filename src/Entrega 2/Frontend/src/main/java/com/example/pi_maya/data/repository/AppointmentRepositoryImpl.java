package com.example.pi_maya.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.core.util.DateUtils;
import com.example.pi_maya.data.remote.dto.maya.ApiAgendaDto;
import com.example.pi_maya.domain.model.Appointment;
import com.example.pi_maya.domain.repository.AppointmentRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentRepositoryImpl implements AppointmentRepository {

    private static final String TAG = "AppointmentRepo";

    private final MayaApiClient client;

    public AppointmentRepositoryImpl(MayaApiClient client) {
        this.client = client;
    }

    @Override
    public LiveData<Resource<Appointment>> getNextAppointment() {
        MutableLiveData<Resource<Appointment>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().minhaAgenda(true).enqueue(new Callback<ApiAgendaDto>() {
            @Override
            public void onResponse(@NonNull Call<ApiAgendaDto> call,
                                   @NonNull Response<ApiAgendaDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    result.setValue(Resource.error("Não foi possível carregar a agenda."));
                    return;
                }
                List<ApiAgendaDto.Agendamento> list = response.body().agendamentos;
                if (list == null || list.isEmpty()) {
                    result.setValue(Resource.success(null));
                    return;
                }
                result.setValue(Resource.success(toDomain(list.get(0))));
            }

            @Override
            public void onFailure(@NonNull Call<ApiAgendaDto> call, @NonNull Throwable t) {
                Log.e(TAG, "getNextAppointment failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<List<Appointment>>> getUpcomingAppointments() {
        return fetchList(true);
    }

    @Override
    public LiveData<Resource<List<Appointment>>> getAllMyAppointments() {
        return fetchList(false);
    }

    private LiveData<Resource<List<Appointment>>> fetchList(boolean apenasFuturo) {
        MutableLiveData<Resource<List<Appointment>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().minhaAgenda(apenasFuturo).enqueue(new Callback<ApiAgendaDto>() {
            @Override
            public void onResponse(@NonNull Call<ApiAgendaDto> call,
                                   @NonNull Response<ApiAgendaDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    result.setValue(Resource.error("Não foi possível carregar a agenda."));
                    return;
                }
                List<Appointment> mapped = new ArrayList<>();
                for (ApiAgendaDto.Agendamento a : response.body().agendamentos != null
                        ? response.body().agendamentos
                        : new ArrayList<ApiAgendaDto.Agendamento>()) {
                    mapped.add(toDomain(a));
                }
                result.setValue(Resource.success(mapped));
            }

            @Override
            public void onFailure(@NonNull Call<ApiAgendaDto> call, @NonNull Throwable t) {
                Log.e(TAG, "agenda failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }

    private Appointment toDomain(ApiAgendaDto.Agendamento a) {
        return new Appointment(
                a.id,
                null,
                a.fisioId,
                null,
                DateUtils.parseIsoOffset(a.inicioEm),
                DateUtils.parseIsoOffset(a.fimEm),
                Appointment.statusFromString(a.status),
                a.observacoes
        );
    }
}
