package com.example.pi_maya.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.core.util.DateUtils;
import com.example.pi_maya.data.remote.dto.maya.ApiChatDtos;
import com.example.pi_maya.data.remote.dto.maya.ApiPacienteDto;
import com.example.pi_maya.domain.model.ChatMessage;
import com.example.pi_maya.domain.model.ChatRoom;
import com.example.pi_maya.domain.repository.ChatRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Chat via API. O realtime continua DIRETO Supabase via SupabaseRealtimeClient
 * — funções serverless não conseguem manter WebSocket aberto.
 */
public class ChatRepositoryImpl implements ChatRepository {

    private static final String TAG = "ChatRepo";

    private final MayaApiClient client;
    private final SessionManager session;

    public ChatRepositoryImpl(MayaApiClient client, SessionManager session) {
        this.client = client;
        this.session = session;
    }

    @Override
    public LiveData<Resource<ChatRoom>> getMyChatRoom() {
        MutableLiveData<Resource<ChatRoom>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().meuPaciente().enqueue(new Callback<ApiPacienteDto>() {
            @Override
            public void onResponse(@NonNull Call<ApiPacienteDto> call,
                                   @NonNull Response<ApiPacienteDto> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().paciente == null
                        || response.body().paciente.salaChat == null) {
                    result.setValue(Resource.success(null));
                    return;
                }
                ApiPacienteDto.Paciente p = response.body().paciente;
                result.setValue(Resource.success(new ChatRoom(
                        p.salaChat,
                        p.id,
                        p.fisio != null ? p.fisio.id : null,
                        p.fisio != null ? p.fisio.nome : null,
                        null
                )));
            }

            @Override
            public void onFailure(@NonNull Call<ApiPacienteDto> call, @NonNull Throwable t) {
                Log.e(TAG, "getMyChatRoom failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<List<ChatMessage>>> getMessages(String roomId) {
        MutableLiveData<Resource<List<ChatMessage>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi().mensagensDaSala(roomId).enqueue(new Callback<ApiChatDtos.MensagensResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiChatDtos.MensagensResponse> call,
                                   @NonNull Response<ApiChatDtos.MensagensResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    result.setValue(Resource.error("Não foi possível carregar as mensagens."));
                    return;
                }
                List<ChatMessage> mapped = new ArrayList<>();
                for (ApiChatDtos.Mensagem m : response.body().mensagens != null
                        ? response.body().mensagens
                        : new ArrayList<ApiChatDtos.Mensagem>()) {
                    mapped.add(toDomain(m));
                }
                result.setValue(Resource.success(mapped));
            }

            @Override
            public void onFailure(@NonNull Call<ApiChatDtos.MensagensResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "getMessages failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<ChatMessage>> sendMessage(String roomId, String content) {
        MutableLiveData<Resource<ChatMessage>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        ApiChatDtos.EnviarRequest body = new ApiChatDtos.EnviarRequest(roomId, content);
        client.getApi().enviarMensagem(body).enqueue(new Callback<ApiChatDtos.EnviarResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiChatDtos.EnviarResponse> call,
                                   @NonNull Response<ApiChatDtos.EnviarResponse> response) {
                if (!response.isSuccessful()) {
                    result.setValue(Resource.error("Não foi possível enviar."));
                    return;
                }
                if (response.body() == null || response.body().mensagem == null) {
                    // 2xx sem body — sucesso silencioso. Realtime trará a mensagem.
                    result.setValue(Resource.success(null));
                    return;
                }
                result.setValue(Resource.success(toDomain(response.body().mensagem)));
            }

            @Override
            public void onFailure(@NonNull Call<ApiChatDtos.EnviarResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "sendMessage failure", t);
                result.setValue(Resource.error("Sem conexão.", t));
            }
        });

        return result;
    }

    private ChatMessage toDomain(ApiChatDtos.Mensagem m) {
        return new ChatMessage(
                m.id,
                m.salaId,
                m.remetenteId,
                m.conteudo,
                m.anexoUrl,
                m.anexoTipo,
                DateUtils.parseIsoOffset(m.criadaEm),
                m.lidaEm != null
        );
    }
}
