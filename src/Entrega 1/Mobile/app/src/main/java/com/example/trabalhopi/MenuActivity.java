package com.example.trabalhopi;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Define o layout da tela de menu
        setContentView(R.layout.activity_menu);

        // Carrega o MenuFragment como tela inicial
        if (savedInstanceState == null) {
            carregarFragment(new MenuFragment());
        }

        // Ícones da barra de navegação inferior
        ImageView iconeHome = findViewById(R.id.iconeHome);
        ImageView iconePerfil = findViewById(R.id.iconePerfil);

        // Ao clicar em Home, volta para o menu principal
        iconeHome.setOnClickListener(v -> carregarFragment(new MenuFragment()));

        // Ao clicar em Perfil, abre o fragment de perfil
        iconePerfil.setOnClickListener(v -> carregarFragment(new PerfilFragment()));
    }

    // Substitui o fragment atual pelo novo, limpando o histórico
    private void carregarFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}