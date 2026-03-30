package com.example.trabalhopi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class TecnicasFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla o layout da tela de técnicas RPG
        View view = inflater.inflate(R.layout.activity_tecnicas, container, false);

        // Botão que navega para a tela de postura
        View btnPostura = view.findViewById(R.id.btnPostura);

        // Botão voltar — retorna ao menu anterior
        ImageView imgVoltarTecnicas = view.findViewById(R.id.imgVoltarTecnicas);

        btnPostura.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new PosturaFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        imgVoltarTecnicas.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }
}