package com.example.trabalhopi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout da tela de login
        setContentView(R.layout.activity_main);

        // Ao clicar, navega para a tela do menu principal
        View btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Encerra o Login para não voltar com o botão de voltar
        });
    }
}