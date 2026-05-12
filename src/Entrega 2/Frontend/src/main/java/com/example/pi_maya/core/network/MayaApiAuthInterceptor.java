package com.example.pi_maya.core.network;

import androidx.annotation.NonNull;

import com.example.pi_maya.core.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Anexa Authorization: Bearer <token> em toda chamada para a API.
 * Endpoints públicos (login, cadastro, recuperar-senha) ignoram esse header.
 */
public class MayaApiAuthInterceptor implements Interceptor {

    private final SessionManager session;

    public MayaApiAuthInterceptor(SessionManager session) {
        this.session = session;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        if (original.body() != null && original.header("Content-Type") == null) {
            builder.header("Content-Type", "application/json");
        }

        String token = session.getAccessToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }
}
