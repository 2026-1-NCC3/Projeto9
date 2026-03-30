package com.example.trabalhopi;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout da tela de tutorial
        setContentView(R.layout.activity_postura);

        // Botão voltar — encerra a Activity e retorna à tela anterior
        ImageView imgVoltarTutorial = findViewById(R.id.imgVoltarTutorial);
        imgVoltarTutorial.setOnClickListener(v -> finish());
    }
}