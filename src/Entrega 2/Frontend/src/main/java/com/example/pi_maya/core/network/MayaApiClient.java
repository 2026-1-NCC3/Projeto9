package com.example.pi_maya.core.network;

import com.example.pi_maya.BuildConfig;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.data.remote.api.maya.MayaApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente HTTP do servidor próprio (Next.js na Vercel).
 *
 * Todas as chamadas (exceto realtime do chat) passam por aqui.
 * O realtime do chat continua direto Supabase via SupabaseRealtimeClient.
 */
public class MayaApiClient {

    private final MayaApi api;
    private final Gson gson;

    public MayaApiClient(SessionManager sessionManager) {
        this.gson = new GsonBuilder().create();

        String baseUrl = BuildConfig.API_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://placeholder.vercel.app";
        }
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new MayaApiAuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(http)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.api = retrofit.create(MayaApi.class);
    }

    public MayaApi getApi() {
        return api;
    }
}
