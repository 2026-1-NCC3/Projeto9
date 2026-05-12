package com.example.pi_maya.presentation.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.presentation.main.MainActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity que recebe o deep link disparado pelo email de confirmação do Supabase.
 *
 * URL esperada (formato GoTrue):
 *   pi-maya://auth/callback#access_token=...&refresh_token=...&expires_in=3600&type=signup
 *
 * Tokens vêm no FRAGMENT (depois do #), não na query string.
 */
public class AuthCallbackActivity extends AppCompatActivity {

    private static final String TAG = "AuthCallback";

    private TextView statusText;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_callback);

        statusText = findViewById(R.id.statusText);
        progress = findViewById(R.id.progress);

        statusText.setText(R.string.auth_processing_callback);

        Uri data = getIntent() != null ? getIntent().getData() : null;
        if (data == null) {
            failAndGoToLogin();
            return;
        }

        Log.d(TAG, "Deep link recebido: " + data);

        // GoTrue manda tokens no fragment: #access_token=...&refresh_token=...
        Map<String, String> params = parseFragment(data.getFragment());
        if (params.isEmpty()) {
            // Fallback: às vezes vem na query
            params = parseQuery(data);
        }

        String accessToken = params.get("access_token");
        String refreshToken = params.get("refresh_token");
        String type = params.get("type");
        String error = params.get("error_description");

        if (!TextUtils.isEmpty(error)) {
            Log.w(TAG, "Erro vindo do callback: " + error);
            failAndGoToLogin();
            return;
        }

        if (TextUtils.isEmpty(accessToken)) {
            failAndGoToLogin();
            return;
        }

        // Calcular expires_at
        long expiresAt = System.currentTimeMillis() / 1000L + 3600L;
        try {
            String expiresIn = params.get("expires_in");
            if (!TextUtils.isEmpty(expiresIn)) {
                expiresAt = System.currentTimeMillis() / 1000L + Long.parseLong(expiresIn);
            }
        } catch (NumberFormatException ignored) {}

        SessionManager session = MayaApp.get().getSessionManager();
        session.saveSession(
                accessToken,
                refreshToken != null ? refreshToken : "",
                expiresAt,
                /* userId */ null,        // será preenchido após primeiro getUser
                session.getUserEmail(),    // mantém se já tinha
                session.getUserName() != null ? session.getUserName() : ""
        );

        // Tipo "signup" = confirmação de email. "recovery" = redefinir senha.
        // Por simplicidade, mandamos pra MainActivity em ambos os casos
        // (em "recovery" a UX correta seria abrir uma tela de redefinir senha,
        // mas isso fica para uma próxima iteração).
        Log.d(TAG, "Tipo do callback: " + type);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void failAndGoToLogin() {
        statusText.setText(R.string.auth_callback_invalid);
        progress.setVisibility(View.GONE);
        statusText.postDelayed(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000L);
    }

    private Map<String, String> parseFragment(String fragment) {
        Map<String, String> map = new HashMap<>();
        if (TextUtils.isEmpty(fragment)) return map;
        for (String pair : fragment.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) continue;
            try {
                String key = java.net.URLDecoder.decode(pair.substring(0, eq), "UTF-8");
                String value = java.net.URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                map.put(key, value);
            } catch (Exception e) {
                Log.w(TAG, "Falha ao parsear fragmento", e);
            }
        }
        return map;
    }

    private Map<String, String> parseQuery(Uri uri) {
        Map<String, String> map = new HashMap<>();
        for (String name : uri.getQueryParameterNames()) {
            map.put(name, uri.getQueryParameter(name));
        }
        return map;
    }
}
