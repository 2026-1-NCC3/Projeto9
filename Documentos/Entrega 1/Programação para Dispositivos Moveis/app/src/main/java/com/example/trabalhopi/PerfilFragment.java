package com.example.trabalhopi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class PerfilFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla o layout do perfil do usuário
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Botão voltar — retorna ao menu principal
        ImageView imgVoltarPerfil = view.findViewById(R.id.imgVoltarPerfil);
        imgVoltarPerfil.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new MenuFragment());
            transaction.commit();
        });

        return view;
    }
}