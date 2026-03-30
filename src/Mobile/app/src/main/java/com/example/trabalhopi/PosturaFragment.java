package com.example.trabalhopi;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

public class PosturaFragment extends Fragment {

    private ImageView imgCarrossel;
    private ProgressBar progressBarVideo;
    private TextView txtTempoAtual;
    private TextView txtCalorias;
    private Button btnIniciarTutorial;
    private CountDownTimer timer;

    // Array com os 3 GIFs de postura
    private int[] gifs = {
            R.drawable.gif_postura_1,
            R.drawable.gif_postura_2,
            R.drawable.gif_postura_3
    };

    private int indiceAtual = 0;
    private boolean isPlaying = false;
    private long tempoRestante = 120000;

    // Cada sessão completa = 10 calorias estimadas
    private static final int CALORIAS_POR_SESSAO = 10;
    private int totalCalorias = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_postura, container, false);

        // Inicializa os componentes da tela
        ImageView imgVoltarTutorial = view.findViewById(R.id.imgVoltarTutorial);
        btnIniciarTutorial = view.findViewById(R.id.btnIniciarTutorial);
        txtTempoAtual = view.findViewById(R.id.txtTempoAtual);
        progressBarVideo = view.findViewById(R.id.progressBarVideo);
        imgCarrossel = view.findViewById(R.id.imgCarrossel);
        txtCalorias = view.findViewById(R.id.txtCalorias);

        // Mostra o primeiro frame parado até clicar em iniciar
        imgCarrossel.setImageResource(gifs[indiceAtual]);

        // Botão voltar
        imgVoltarTutorial.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Botão iniciar/pausar
        btnIniciarTutorial.setOnClickListener(v -> {
            if (!isPlaying) {
                iniciarCarrossel(tempoRestante);
            } else {
                pausarCarrossel();
            }
        });

        return view;
    }

    // Carrega o GIF animado ou congela dependendo do estado
    private void carregarGif(int indice) {
        if (isPlaying) {
            Glide.with(this)
                    .asGif()
                    .load(gifs[indice])
                    .into(imgCarrossel);
        } else {
            imgCarrossel.setImageResource(gifs[indice]);
        }
    }

    // Atualiza o texto de calorias na tela
    private void atualizarCalorias() {
        txtCalorias.setText("🔥 Calorias estimadas: " + totalCalorias + " kcal");
    }

    private void iniciarCarrossel(long millis) {
        isPlaying = true;
        btnIniciarTutorial.setText("PAUSAR TUTORIAL");

        // Inicia o GIF animado
        carregarGif(indiceAtual);

        timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestante = millisUntilFinished;
                long segundosDecorridos = (120000 - millisUntilFinished) / 1000;

                // Atualiza o tempo na tela
                long minutos = segundosDecorridos / 60;
                long segundos = segundosDecorridos % 60;
                txtTempoAtual.setText(String.format("%d:%02d", minutos, segundos));

                // Atualiza a barra de progresso
                progressBarVideo.setProgress((int) segundosDecorridos);

                // Troca o GIF a cada 40 segundos
                int novoIndice = (int) (segundosDecorridos / 40);
                if (novoIndice >= gifs.length) novoIndice = gifs.length - 1;

                if (novoIndice != indiceAtual) {
                    indiceAtual = novoIndice;
                    carregarGif(indiceAtual);
                }
            }

            @Override
            public void onFinish() {
                finalizarCarrossel();
            }
        }.start();
    }

    // Pausa o tutorial e congela o GIF
    private void pausarCarrossel() {
        isPlaying = false;
        btnIniciarTutorial.setText("RETOMAR TUTORIAL");
        if (timer != null) timer.cancel();
        imgCarrossel.setImageResource(gifs[indiceAtual]);
    }

    // Finaliza o tutorial e soma as calorias
    private void finalizarCarrossel() {
        isPlaying = false;
        indiceAtual = 0;
        tempoRestante = 120000;
        btnIniciarTutorial.setText("REINICIAR TUTORIAL");
        txtTempoAtual.setText("0:00");
        progressBarVideo.setProgress(0);
        imgCarrossel.setImageResource(gifs[0]);

        // Soma 10 calorias ao completar a sessão
        totalCalorias += CALORIAS_POR_SESSAO;
        atualizarCalorias();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
}