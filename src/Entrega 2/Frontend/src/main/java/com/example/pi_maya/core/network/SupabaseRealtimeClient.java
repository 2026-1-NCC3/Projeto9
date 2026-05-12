package com.example.pi_maya.core.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pi_maya.BuildConfig;
import com.example.pi_maya.core.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Cliente WebSocket para o Supabase Realtime (Phoenix Channels v2).
 *
 * Protocolo simplificado:
 *   - Conecta em wss://[ref].supabase.co/realtime/v1/websocket?apikey=ANON&vsn=1.0.0
 *   - Envia phx_join no tópico "realtime:public:<table>" com filtro opcional
 *   - Recebe eventos postgres_changes (INSERT/UPDATE/DELETE)
 *   - Envia heartbeat a cada 30s para manter a conexão
 *
 * Uso típico:
 *   var client = new SupabaseRealtimeClient(session);
 *   client.subscribeToTable("chat_messages",
 *                          "room_id=eq." + roomId,
 *                          new Listener() { ... });
 *   ...
 *   client.disconnect();
 */
public class SupabaseRealtimeClient {

    private static final String TAG = "SupabaseRealtime";
    private static final long HEARTBEAT_INTERVAL_MS = 30_000L;

    public interface Listener {
        /** Chamado na thread principal para INSERT/UPDATE/DELETE no Postgres. */
        void onPostgresChange(@NonNull String eventType, @NonNull JsonObject record);
        /** Chamado na thread principal quando a conexão cai. */
        void onDisconnected();
    }

    private final SessionManager session;
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private OkHttpClient client;
    private WebSocket webSocket;
    private Listener listener;
    private String topic;
    private long ref = 1;
    private boolean joined = false;

    private final Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            sendHeartbeat();
            mainHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
        }
    };

    public SupabaseRealtimeClient(SessionManager session) {
        this.session = session;
    }

    public void subscribeToTable(@NonNull String table, @NonNull String filter,
                                 @NonNull Listener listener) {
        this.listener = listener;
        this.topic = "realtime:public:" + table + ":" + filter;
        connect();
    }

    private void connect() {
        if (BuildConfig.SUPABASE_URL == null || BuildConfig.SUPABASE_URL.isEmpty()) {
            Log.w(TAG, "SUPABASE_URL não configurado");
            return;
        }

        // Converte https:// → wss://
        // vsn=2.0.0 → Phoenix Channels v2 (formato de mensagem em array,
        // [join_ref, ref, topic, event, payload]). É o que enviamos abaixo.
        String wsUrl = BuildConfig.SUPABASE_URL
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                + "/realtime/v1/websocket?apikey=" + BuildConfig.SUPABASE_ANON_KEY + "&vsn=2.0.0";

        client = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                Log.d(TAG, "WebSocket aberto");
                joinChannel();
                mainHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                handleMessage(text);
            }

            @Override
            public void onClosing(@NonNull WebSocket ws, int code, @NonNull String reason) {
                Log.d(TAG, "WS closing: " + code + " " + reason);
                ws.close(1000, null);
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t,
                                  Response response) {
                Log.e(TAG, "WS falhou", t);
                joined = false;
                mainHandler.removeCallbacks(heartbeatRunnable);
                if (listener != null) {
                    mainHandler.post(() -> listener.onDisconnected());
                }
            }
        });
    }

    private void joinChannel() {
        // Phoenix Channels v2: [join_ref, ref, topic, event, payload]
        // Supabase Realtime espera: payload = { config: {...}, access_token: "<jwt>" }
        // (access_token é TOP-LEVEL no payload, não dentro de config!)
        JsonObject payload = new JsonObject();
        JsonObject config = new JsonObject();

        // Postgres CDC config
        com.google.gson.JsonArray pgChanges = new com.google.gson.JsonArray();
        JsonObject changesConfig = new JsonObject();
        changesConfig.addProperty("event", "*");
        changesConfig.addProperty("schema", "public");
        // Extrai table e filter do topic "realtime:public:chat_messages:room_id=eq.xxx"
        String[] parts = topic.split(":", 4);
        if (parts.length >= 3) {
            changesConfig.addProperty("table", parts[2]);
        }
        if (parts.length >= 4 && !parts[3].isEmpty()) {
            changesConfig.addProperty("filter", parts[3]);
        }
        pgChanges.add(changesConfig);
        config.add("postgres_changes", pgChanges);

        // Broadcast/presence defaults (estabilidade entre versões do servidor)
        JsonObject broadcast = new JsonObject();
        broadcast.addProperty("ack", false);
        broadcast.addProperty("self", false);
        config.add("broadcast", broadcast);

        JsonObject presence = new JsonObject();
        presence.addProperty("key", "");
        config.add("presence", presence);

        config.addProperty("private", false);

        payload.add("config", config);

        // access_token TOP-LEVEL (necessário pra RLS funcionar via realtime)
        String accessToken = session.getAccessToken();
        if (accessToken != null && !accessToken.isEmpty()) {
            payload.addProperty("access_token", accessToken);
        }

        sendPhoenixMessage("phx_join", payload);
    }

    private void sendPhoenixMessage(String event, JsonObject payload) {
        if (webSocket == null) return;
        com.google.gson.JsonArray msg = new com.google.gson.JsonArray();
        msg.add((String) null);                         // join_ref
        msg.add(String.valueOf(ref++));                  // ref
        // Phoenix v2 espera o topic com prefixo "realtime:" mas SEM o filter
        // que já foi enviado via config. Por isso reduzimos:
        String[] parts = topic.split(":", 4);
        String channelTopic = (parts.length >= 3)
                ? "realtime:" + parts[1] + ":" + parts[2]
                : topic;
        msg.add(channelTopic);
        msg.add(event);
        msg.add(payload);
        String jsonText = msg.toString();
        Log.d(TAG, "→ " + jsonText);
        webSocket.send(jsonText);
    }

    private void sendHeartbeat() {
        if (webSocket == null) return;
        com.google.gson.JsonArray msg = new com.google.gson.JsonArray();
        msg.add((String) null);
        msg.add(String.valueOf(ref++));
        msg.add("phoenix");
        msg.add("heartbeat");
        msg.add(new JsonObject());
        webSocket.send(msg.toString());
    }

    private void handleMessage(String text) {
        try {
            Log.d(TAG, "← " + text);
            com.google.gson.JsonArray arr = gson.fromJson(text, com.google.gson.JsonArray.class);
            if (arr == null || arr.size() < 5) return;
            String event = arr.get(3).getAsString();
            JsonObject payload = arr.get(4).getAsJsonObject();

            if ("phx_reply".equals(event)) {
                if (payload.has("status")
                        && "ok".equals(payload.get("status").getAsString())
                        && !joined) {
                    joined = true;
                    Log.d(TAG, "Canal joinado: " + topic);
                } else if (payload.has("status")
                        && "error".equals(payload.get("status").getAsString())) {
                    Log.e(TAG, "Erro no join: " + payload);
                }
                return;
            }

            // postgres_changes vem como event "postgres_changes"
            if ("postgres_changes".equals(event) && listener != null) {
                JsonObject data = payload.getAsJsonObject("data");
                if (data == null) return;
                String type = data.has("type") ? data.get("type").getAsString() : "";
                JsonObject record = data.has("record") && data.get("record").isJsonObject()
                        ? data.getAsJsonObject("record")
                        : null;
                if (record != null) {
                    mainHandler.post(() -> listener.onPostgresChange(type, record));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Falha ao parsear msg realtime: " + text, e);
        }
    }

    public void disconnect() {
        joined = false;
        mainHandler.removeCallbacks(heartbeatRunnable);
        if (webSocket != null) {
            try { webSocket.close(1000, "client_disconnect"); } catch (Exception ignored) {}
            webSocket = null;
        }
        if (client != null) {
            try { client.dispatcher().executorService().shutdown(); } catch (Exception ignored) {}
        }
        listener = null;
    }
}
