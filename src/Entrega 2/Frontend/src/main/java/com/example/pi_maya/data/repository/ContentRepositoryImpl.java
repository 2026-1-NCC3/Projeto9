package com.example.pi_maya.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.data.remote.dto.maya.ApiConteudosDto;
import com.example.pi_maya.domain.model.EducationalContent;
import com.example.pi_maya.domain.repository.ContentRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentRepositoryImpl implements ContentRepository {

    private static final String TAG = "ContentRepo";

    private final MayaApiClient client;

    public ContentRepositoryImpl(MayaApiClient client) {
        this.client = client;
    }

    @Override
    public LiveData<Resource<List<EducationalContent>>> getPublishedContent() {
        MutableLiveData<Resource<List<EducationalContent>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().conteudosPublicados().enqueue(new Callback<ApiConteudosDto>() {
            @Override
            public void onResponse(@NonNull Call<ApiConteudosDto> call,
                                   @NonNull Response<ApiConteudosDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EducationalContent> mapped = new ArrayList<>();
                    for (ApiConteudosDto.Conteudo c :
                            response.body().conteudos != null
                                    ? response.body().conteudos
                                    : new ArrayList<ApiConteudosDto.Conteudo>()) {
                        mapped.add(new EducationalContent(
                                c.id, c.titulo, c.corpo, c.capaUrl, c.videoUrl,
                                EducationalContent.typeFromString(c.tipo), c.categoria
                        ));
                    }
                    result.setValue(Resource.success(mapped));
                } else {
                    result.setValue(Resource.error("Não foi possível carregar o conteúdo."));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiConteudosDto> call, @NonNull Throwable t) {
                Log.e(TAG, "conteudos failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });
        return result;
    }
}
