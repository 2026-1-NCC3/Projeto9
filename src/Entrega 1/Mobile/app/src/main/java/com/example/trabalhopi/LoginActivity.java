package com.example.trabalhopi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoginActivity extends AppCompatActivity {

    // URL base do backend
    private static final String BASE_URL = "https://fecap-2026-2-git-main-susters-projects.vercel.app";

    private EditText edtEmail, edtSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout da tela de login
        setContentView(R.layout.activity_main);

        // Desativa verificação SSL globalmente para desenvolvimento
        desativarVerificacaoSSL();

        // Inicializa os campos de email e senha
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        // Botão entrar — chama o endpoint de login
        View btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> fazerLogin());

        // Link de cadastro — navegação futura
        View txtCadastrar = findViewById(R.id.txtCadastrar);
        txtCadastrar.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Cadastro em breve!", Toast.LENGTH_SHORT).show()
        );
    }

    // Desativa verificação de certificado SSL para o ambiente de desenvolvimento
    private void desativarVerificacaoSSL() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Realiza a chamada ao endpoint POST /auth/login
    private void fazerLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        // Valida se os campos estão preenchidos
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(LoginActivity.this, "Conectando...", Toast.LENGTH_SHORT).show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Configura a conexão HTTP
                URL url = new URL(BASE_URL + "/auth/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                // Monta o corpo da requisição em JSON
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("senha", senha);

                // Envia o JSON para o backend
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                // Lê a resposta do backend
                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                String finalResponse = response.toString();
                int finalCode = responseCode;

                // Processa a resposta na thread principal
                handler.post(() -> {
                    try {
                        JSONObject json = new JSONObject(finalResponse);
                        if (finalCode == 200) {
                            // Salva o token na variável global
                            AppConfig.token = json.getString("token");
                            Toast.makeText(LoginActivity.this, "Login realizado!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String mensagem = json.optString("mensagem", "Erro ao fazer login");
                            Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Erro ao processar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                handler.post(() ->
                        Toast.makeText(LoginActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}