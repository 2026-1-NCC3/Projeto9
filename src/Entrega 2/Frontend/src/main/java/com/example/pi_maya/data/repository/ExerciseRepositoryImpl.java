package com.example.pi_maya.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.data.remote.dto.maya.ApiExerciciosDto;
import com.example.pi_maya.domain.model.Exercise;
import com.example.pi_maya.domain.model.ExerciseAssignment;
import com.example.pi_maya.domain.repository.ExerciseRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseRepositoryImpl implements ExerciseRepository {

    private static final String TAG = "ExerciseRepo";

    private final MayaApiClient client;

    public ExerciseRepositoryImpl(MayaApiClient client) {
        this.client = client;
    }

    @Override
    public LiveData<Resource<List<ExerciseAssignment>>> getMyAssignments() {
        MutableLiveData<Resource<List<ExerciseAssignment>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().meusExercicios().enqueue(new Callback<ApiExerciciosDto>() {
            @Override
            public void onResponse(@NonNull Call<ApiExerciciosDto> call,
                                   @NonNull Response<ApiExerciciosDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    result.setValue(Resource.error("Não foi possível carregar exercícios."));
                    return;
                }
                List<ExerciseAssignment> mapped = new ArrayList<>();
                for (ApiExerciciosDto.ExercicioAtribuido a :
                        response.body().exercicios != null
                                ? response.body().exercicios
                                : new ArrayList<ApiExerciciosDto.ExercicioAtribuido>()) {

                    String thumbnailFixa;

                    // 1. Removidas as aspas simples do ID numérico
                    if ("36bccdc0-34b1-4f58-a641-f89754bce393".equals(a.exercicioId)) {
                        thumbnailFixa = "img2"; // Apenas o nome do arquivo em res/drawable
                    } else if ("8adc9214-c82e-4ce8-a878-a2c2f38aa8ff".equals(a.exercicioId)) {
                        thumbnailFixa = "img1";
                    } else {
                        thumbnailFixa = "placeholder";
                    }
                    Exercise ex = new Exercise(
                            a.exercicioId,
                            a.titulo,
                            a.descricao,
                            a.instrucoes,
                            a.videoUrl,
                            thumbnailFixa,
                            a.dificuldade,
                            a.duracaoSegundos,
                            a.categoria
                    );
                    mapped.add(new ExerciseAssignment(
                            a.atribuicaoId,
                            null,
                            ex,
                            a.repeticoes,
                            a.series,
                            a.frequenciaSemanal,
                            a.observacoes,
                            true
                    ));
                }
                result.setValue(Resource.success(mapped));
            }

            @Override
            public void onFailure(@NonNull Call<ApiExerciciosDto> call, @NonNull Throwable t) {
                Log.e(TAG, "meusExercicios failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }
}
