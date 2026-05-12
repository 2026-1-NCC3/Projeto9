package com.example.pi_maya.presentation.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.core.network.SupabaseRealtimeClient;
import com.example.pi_maya.core.util.DateUtils;
import com.example.pi_maya.domain.model.ChatMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Tela de mensagens com a fisioterapeuta.
 *
 * Features:
 *  - Carrega histórico via REST
 *  - Subscribe em postgres_changes (Supabase Realtime) para receber mensagens novas
 *  - IME insets ajustam o input quando o teclado abre
 *  - Envia via REST (PostgREST INSERT) — o realtime devolve o INSERT pra todos os clientes
 */
public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private TextInputEditText input;
    private FloatingActionButton sendButton;
    private View inputContainer;
    private MessageAdapter adapter;
    private String roomId;
    private String myId;

    private SupabaseRealtimeClient realtime;
    private final Set<String> seenMessageIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Habilita edge-to-edge para que o IME inset funcione corretamente
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        roomId = getIntent().getStringExtra(ChatFragment.EXTRA_ROOM_ID);
        String therapistName = getIntent().getStringExtra(ChatFragment.EXTRA_THERAPIST_NAME);

        TextView title = findViewById(R.id.therapistTitle);
        title.setText(therapistName != null ? therapistName : "Conversa");

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        recycler = findViewById(R.id.messagesRecycler);
        input = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        inputContainer = findViewById(R.id.inputContainer);

        setupInsets();

        myId = MayaApp.get().getSessionManager().getUserId();
        adapter = new MessageAdapter(myId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());

        if (roomId != null) {
            loadMessages();
            startRealtime();
        }
    }

    /**
     * Aplica padding no inputContainer e statusBar inset no toolbar para que
     * a UI não fique atrás das system bars / IME.
     */
    private void setupInsets() {
        View root = findViewById(android.R.id.content);
        View toolbar = findViewById(R.id.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Toolbar: padding-top pelo statusBar
            toolbar.setPadding(toolbar.getPaddingLeft(), sys.top,
                    toolbar.getPaddingRight(), toolbar.getPaddingBottom());

            // Input container: padding-bottom = max(IME, navBar)
            int bottom = Math.max(ime.bottom, sys.bottom);
            inputContainer.setPadding(inputContainer.getPaddingLeft(),
                    inputContainer.getPaddingTop(),
                    inputContainer.getPaddingRight(),
                    bottom + dp(12));

            // Quando o IME abre, faz scroll pra última mensagem
            if (ime.bottom > 0 && adapter != null && adapter.getItemCount() > 0) {
                recycler.post(() -> recycler.scrollToPosition(adapter.getItemCount() - 1));
            }
            return insets;
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void loadMessages() {
        MayaApp.get().getChatRepository().getMessages(roomId)
                .observe(this, resource -> {
                    if (resource.isSuccess() && resource.getData() != null) {
                        adapter.submit(resource.getData());
                        for (ChatMessage m : resource.getData()) {
                            seenMessageIds.add(m.id);
                        }
                        if (resource.getData().size() > 0) {
                            recycler.scrollToPosition(resource.getData().size() - 1);
                        }
                    }
                });
    }

    private void startRealtime() {
        realtime = new SupabaseRealtimeClient(MayaApp.get().getSessionManager());
        realtime.subscribeToTable(
                "chat_messages",
                "room_id=eq." + roomId,
                new SupabaseRealtimeClient.Listener() {
                    @Override
                    public void onPostgresChange(@androidx.annotation.NonNull String eventType,
                                                 @androidx.annotation.NonNull JsonObject record) {
                        if (!"INSERT".equals(eventType)) return;
                        ChatMessage msg = parseMessage(record);
                        if (msg == null) return;
                        if (seenMessageIds.contains(msg.id)) return;
                        seenMessageIds.add(msg.id);
                        adapter.append(msg);
                        recycler.scrollToPosition(adapter.getItemCount() - 1);
                    }

                    @Override
                    public void onDisconnected() {
                        // silencioso. Caso queira mostrar status, dá pra atualizar a UI.
                    }
                }
        );
    }

    private ChatMessage parseMessage(JsonObject r) {
        try {
            String id = r.has("id") ? r.get("id").getAsString() : null;
            String roomIdField = r.has("room_id") ? r.get("room_id").getAsString() : null;
            String senderId = r.has("sender_id") ? r.get("sender_id").getAsString() : null;
            String content = r.has("content") && !r.get("content").isJsonNull()
                    ? r.get("content").getAsString() : null;
            String createdAt = r.has("created_at") && !r.get("created_at").isJsonNull()
                    ? r.get("created_at").getAsString() : null;
            return new ChatMessage(
                    id, roomIdField, senderId, content,
                    null, null,
                    DateUtils.parseIsoOffset(createdAt),
                    false
            );
        } catch (Exception e) {
            return null;
        }
    }

    private void sendMessage() {
        String text = input.getText() != null ? input.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text) || roomId == null) return;
        input.setText("");
        sendButton.setEnabled(false);

        MayaApp.get().getChatRepository().sendMessage(roomId, text)
                .observe(this, resource -> {
                    sendButton.setEnabled(true);
                    if (resource.isSuccess() && resource.getData() != null) {
                        ChatMessage m = resource.getData();
                        // Optimistic append: o realtime pode mandar de novo, então marca como visto
                        if (!seenMessageIds.contains(m.id)) {
                            seenMessageIds.add(m.id);
                            adapter.append(m);
                            recycler.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    } else if (resource.isError()) {
                        Snackbar.make(sendButton,
                                resource.getMessage() != null ? resource.getMessage()
                                        : "Erro ao enviar.",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtime != null) {
            realtime.disconnect();
            realtime = null;
        }
    }
}
