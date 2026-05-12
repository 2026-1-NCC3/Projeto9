package com.example.pi_maya.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pi_maya.core.network.AuthDeepLink;
import com.example.pi_maya.core.network.MayaApiClient;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.data.remote.dto.maya.CadastroDtos;
import com.example.pi_maya.data.remote.dto.maya.EntrarDtos;
import com.example.pi_maya.data.remote.dto.maya.RecuperarSenhaDto;
import com.example.pi_maya.domain.model.User;
import com.example.pi_maya.domain.repository.AuthRepository;

import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementação do AuthRepository que conversa com o servidor próprio (api/).
 */
public class AuthRepositoryImpl implements AuthRepository {

    private static final String TAG = "AuthRepository";

    private final MayaApiClient client;
    private final SessionManager session;

    public AuthRepositoryImpl(MayaApiClient client, SessionManager session) {
        this.client = client;
        this.session = session;
    }

    @Override
    public LiveData<Resource<User>> signIn(String email, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        client.getApi()
                .entrar(new EntrarDtos.Request(email.trim(), password))
                .enqueue(new Callback<EntrarDtos.Response>() {
                    @Override
                    public void onResponse(@NonNull Call<EntrarDtos.Response> call,
                                           @NonNull Response<EntrarDtos.Response> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EntrarDtos.Response body = response.body();
                            long expiresAt = body.expiraEm != null
                                    ? body.expiraEm
                                    : System.currentTimeMillis() / 1000L + 3600L;
                            session.saveSession(
                                    body.token,
                                    body.refreshToken != null ? body.refreshToken : "",
                                    expiresAt,
                                    body.usuario != null ? body.usuario.id : null,
                                    body.usuario != null ? body.usuario.email : null,
                                    body.usuario != null && body.usuario.nome != null ? body.usuario.nome : ""
                            );
                            result.setValue(Resource.success(new User(
                                    body.usuario != null ? body.usuario.id : null,
                                    body.usuario != null ? body.usuario.email : null,
                                    body.usuario != null ? body.usuario.nome : null,
                                    null,
                                    "patient"
                            )));
                        } else {
                            result.setValue(Resource.error(extrairErro(response, "E-mail ou senha incorretos.")));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EntrarDtos.Response> call, @NonNull Throwable t) {
                        Log.e(TAG, "signIn failure", t);
                        result.setValue(Resource.error("Sem conexão. Tente novamente.", t));
                    }
                });

        return result;
    }

    @Override
    public LiveData<Resource<User>> signUp(String email, String password,
                                           String fullName, String phone,
                                           boolean lgpdConsent) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        CadastroDtos.Request body = new CadastroDtos.Request(
                email.trim(), password, fullName.trim(), phone, AuthDeepLink.REDIRECT_URL
        );

        client.getApi().cadastrar(body)
                .enqueue(new Callback<CadastroDtos.Response>() {
                    @Override
                    public void onResponse(@NonNull Call<CadastroDtos.Response> call,
                                           @NonNull Response<CadastroDtos.Response> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            session.setLgpdConsentGranted(lgpdConsent);
                            session.clearSession();
                            CadastroDtos.Response.UsuarioCadastrado u = response.body().usuario;
                            result.setValue(Resource.success(new User(
                                    u != null ? u.id : null,
                                    u != null ? u.email : email,
                                    fullName,
                                    phone,
                                    "patient"
                            )));
                        } else {
                            result.setValue(Resource.error(extrairErro(response, "Não foi possível criar sua conta.")));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CadastroDtos.Response> call, @NonNull Throwable t) {
                        Log.e(TAG, "signUp failure", t);
                        result.setValue(Resource.error("Sem conexão. Tente novamente.", t));
                    }
                });

        return result;
    }

    @Override
    public LiveData<Resource<Void>> requestPasswordRecovery(String email) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        RecuperarSenhaDto body = new RecuperarSenhaDto(email.trim(), AuthDeepLink.REDIRECT_URL);
        client.getApi().recuperarSenha(body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            result.setValue(Resource.success(null));
                        } else {
                            result.setValue(Resource.error("Não foi possível enviar o link."));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(TAG, "recover failure", t);
                        result.setValue(Resource.error("Sem conexão.", t));
                    }
                });

        return result;
    }

    @Override
    public LiveData<Resource<Void>> signOut() {
        // No servidor próprio o "sair" é só limpar localmente — não há endpoint dedicado.
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        session.clearSession();
        result.setValue(Resource.success(null));
        return result;
    }

    @Override
    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    private String extrairErro(Response<?> response, String fallback) {
        if (response.errorBody() == null) return fallback;
        try {
            String body = response.errorBody().string();
            JSONObject json = new JSONObject(body);
            if (json.has("erro")) return json.getString("erro");
            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
