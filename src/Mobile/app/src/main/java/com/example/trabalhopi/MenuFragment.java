package com.example.trabalhopi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla o layout do menu principal
        View view = inflater.inflate(R.layout.fragment_conteudo_menu, container, false);

        // Botão que navega para a tela de técnicas RPG
        Button btnTecnicas = view.findViewById(R.id.btnTecnicas);
        btnTecnicas.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new TecnicasFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}