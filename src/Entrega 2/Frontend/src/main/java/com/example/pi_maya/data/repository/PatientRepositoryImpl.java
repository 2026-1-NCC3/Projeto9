package com.example.pi_maya.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.data.remote.dto.maya.ApiPacienteDto;
import com.example.pi_maya.domain.model.Patient;
import com.example.pi_maya.domain.repository.PatientRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientRepositoryImpl implements PatientRepository {

    private static final String TAG = "PatientRepository";

    private final MayaApiClient client;

    public PatientRepositoryImpl(MayaApiClient client) {
        this.client = client;
    }

    @Override
    public LiveData<Resource<Patient>> getMyPatientRecord() {
        MutableLiveData<Resource<Patient>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().meuPaciente().enqueue(new Callback<ApiPacienteDto>() {
            @Override
            public void onResponse(@NonNull Call<ApiPacienteDto> call,
                                   @NonNull Response<ApiPacienteDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiPacienteDto.Paciente p = response.body().paciente;
                    if (p == null) {
                        result.setValue(Resource.success(null));
                        return;
                    }
                    result.setValue(Resource.success(new Patient(
                            p.id,
                            /* profileId */ null,
                            p.fisio != null ? p.fisio.id : null,
                            p.queixaPrincipal,
                            p.status
                    )));
                } else {
                    result.setValue(Resource.error("Não foi possível carregar seus dados."));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiPacienteDto> call, @NonNull Throwable t) {
                Log.e(TAG, "meuPaciente failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }
}
