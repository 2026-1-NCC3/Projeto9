package com.example.pi_maya.presentation.exercises;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.pi_maya.R;
import com.example.pi_maya.presentation.exercises.camera.PoseEvaluator;
import com.example.pi_maya.presentation.exercises.camera.PoseLandmarkerHelper;
import com.example.pi_maya.presentation.exercises.camera.PoseOverlayView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tela de execução do exercício com câmera + MediaPipe Pose Landmarker.
 *
 * Pipeline:
 *   CameraX (PreviewView + ImageAnalysis) -> PoseLandmarkerHelper.detectAsync ->
 *   PoseOverlayView desenha 33 landmarks por cima do preview.
 *
 * Próximas iterações:
 *   - Comparar pose atual com reference_pose do exercise (ângulos articulares)
 *   - Feedback de áudio quando detectar erro postural
 *   - Persistir exercise_executions ao final
 *   - Contagem regressiva e duração do exercício
 */
@OptIn(markerClass = ExperimentalGetImage.class)
public class ExerciseCameraActivity extends AppCompatActivity
        implements PoseLandmarkerHelper.ResultListener {

    private static final String TAG = "ExerciseCamera";

    public static final String EXTRA_EXERCISE_TITLE = "extra_exercise_title";

    private PreviewView previewView;
    private PoseOverlayView overlayView;
    private TextView statusText;
    private TextView inferenceText;

    private PoseLandmarkerHelper poseHelper;
    private ExecutorService cameraExecutor;
    private boolean isFrontCamera = true;

    /** Sequência de etapas do exercício. Pode ter 1 (só uma pose) ou várias (ex: cervical = 2). */
    private List<PoseEvaluator> etapas = java.util.Collections.emptyList();
    private int etapaIndex = 0;

    /** Configurações do timer de "pose mantida". */
    private static final long DURACAO_POSE_MS = 5_000L; // 5s na posição correta
    private Long inicioPoseCorreta = null;
    private final List<Integer> scoresEtapaAtual = new ArrayList<>();
    private final List<Integer> scoresTodasEtapas = new ArrayList<>();
    private boolean exercicioConcluido = false;

    private PoseEvaluator avaliadorAtual() {
        if (etapaIndex < 0 || etapaIndex >= etapas.size()) return null;
        return etapas.get(etapaIndex);
    }

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            startCamera();
                        } else {
                            showPermissionDeniedDialog();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_camera);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        statusText = findViewById(R.id.statusText);
        inferenceText = findViewById(R.id.inferenceText);

        TextView title = findViewById(R.id.exerciseTitle);
        String exerciseTitle = getIntent().getStringExtra(EXTRA_EXERCISE_TITLE);
        title.setText(exerciseTitle != null ? exerciseTitle : "Exercício");

        // Carrega as etapas do exercício. Cervical tem 2 (direita, depois esquerda);
        // demais exercícios avaliados têm 1 só (ex: "braços em T").
        etapas = PoseEvaluator.etapasParaExercicio(exerciseTitle);
        overlayView.setModoAvaliacao(!etapas.isEmpty());
        if (!etapas.isEmpty()) {
            statusText.setText("Etapa 1 de " + etapas.size() + ": " + etapas.get(0).getTituloPose());
        }

        findViewById(R.id.closeButton).setOnClickListener(v -> finish());

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (hasCameraPermission()) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissão de câmera necessária")
                .setMessage("Para corrigir sua postura em tempo real, precisamos da câmera. " +
                        "O vídeo NÃO sai do seu celular — apenas você o vê.")
                .setPositiveButton("Abrir configurações", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void startCamera() {
        cameraExecutor.execute(() -> {
            poseHelper = new PoseLandmarkerHelper(getApplicationContext(), this);
            poseHelper.setup();
            runOnUiThread(this::bindCamera);
        });
    }

    private void bindCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                bindUseCases(provider);
            } catch (Exception e) {
                Log.e(TAG, "Falha ao iniciar câmera", e);
                statusText.setText("Não foi possível iniciar a câmera.");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            try {
                if (poseHelper != null) {
                    poseHelper.detectAsync(image, isFrontCamera);
                }
            } finally {
                image.close();
            }
        });

        CameraSelector selector = isFrontCamera
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.bindToLifecycle(this, selector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Bind use cases falhou", e);
            statusText.setText("Erro ao conectar com a câmera.");
        }
    }

    // ===== PoseLandmarkerHelper.ResultListener =====

    @Override
    public void onResult(@NonNull PoseLandmarkerResult result, int imageWidth, int imageHeight,
                         long inferenceTimeMs) {
        runOnUiThread(() -> {
            if (exercicioConcluido) return;

            overlayView.setResult(result, imageWidth, imageHeight);

            PoseEvaluator avaliador = avaliadorAtual();

            if (result.landmarks().isEmpty()) {
                statusText.setText("Posicione-se na frente da câmera");
                if (avaliador != null) overlayView.setLandmarksErrados(null);
                resetTimer();
            } else if (avaliador != null) {
                List<NormalizedLandmark> pose = result.landmarks().get(0);
                PoseEvaluator.Resultado res = avaliador.avaliar(pose);
                overlayView.setLandmarksErrados(res.landmarksErrados);

                if (res.poseCorreta) {
                    if (inicioPoseCorreta == null) {
                        inicioPoseCorreta = System.currentTimeMillis();
                        scoresEtapaAtual.clear();
                    }
                    scoresEtapaAtual.add(res.pontuacao);
                    long faltam = DURACAO_POSE_MS
                            - (System.currentTimeMillis() - inicioPoseCorreta);
                    if (faltam <= 0) {
                        completarEtapa();
                    } else {
                        long seg = (faltam + 999L) / 1000L;
                        statusText.setText(prefixoEtapa() + "✓ Mantém! Faltam " + seg + "s · " + res.pontuacao + "%");
                    }
                } else {
                    resetTimer();
                    String dica = !res.dicas.isEmpty() ? res.dicas.get(0) : "Ajuste a posição";
                    statusText.setText(prefixoEtapa() + dica + " · " + res.pontuacao + "%");
                }
            } else {
                statusText.setText("Detectando sua postura…");
            }

            inferenceText.setText(String.format(Locale.getDefault(),
                    "Inferência: %d ms", inferenceTimeMs));
        });
    }

    private String prefixoEtapa() {
        if (etapas.size() <= 1) return "";
        return "Etapa " + (etapaIndex + 1) + "/" + etapas.size() + " · ";
    }

    private void resetTimer() {
        inicioPoseCorreta = null;
        scoresEtapaAtual.clear();
    }

    /** Etapa atual completa. Avança pra próxima ou encerra o exercício. */
    private void completarEtapa() {
        scoresTodasEtapas.addAll(scoresEtapaAtual);
        scoresEtapaAtual.clear();
        inicioPoseCorreta = null;
        etapaIndex++;

        if (etapaIndex >= etapas.size()) {
            encerrarComConclusao();
            return;
        }

        // Mostra qual é a próxima etapa no status — o timer só volta a contar
        // quando a nova pose for atingida.
        PoseEvaluator proxima = etapas.get(etapaIndex);
        statusText.setText(prefixoEtapa() + "Agora: " + proxima.getTituloPose());
        overlayView.setLandmarksErrados(null);
    }

    private void encerrarComConclusao() {
        if (exercicioConcluido) return;
        exercicioConcluido = true;

        int media = 0;
        if (!scoresTodasEtapas.isEmpty()) {
            int soma = 0;
            for (int s : scoresTodasEtapas) soma += s;
            media = soma / scoresTodasEtapas.size();
        }

        String avaliacao;
        if (media >= 90) avaliacao = "Excelente!";
        else if (media >= 75) avaliacao = "Muito bom!";
        else if (media >= 60) avaliacao = "Bom trabalho.";
        else avaliacao = "Pode melhorar — tente de novo.";

        new AlertDialog.Builder(this)
                .setTitle("Exercício concluído")
                .setMessage(avaliacao + "\n\nPontuação: " + media + "%")
                .setCancelable(false)
                .setPositiveButton("Voltar", (d, w) -> finish())
                .show();
    }

    @Override
    public void onError(@NonNull String message) {
        runOnUiThread(() -> {
            statusText.setText(message);
            overlayView.clear();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseHelper != null) {
            poseHelper.close();
            poseHelper = null;
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
