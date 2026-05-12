package com.example.pi_maya.presentation.exercises.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

/**
 * Wrapper sobre o MediaPipe Pose Landmarker.
 *
 * Responsabilidades:
 *   - Inicialização preguiçosa do modelo (do assets/)
 *   - Conversão ImageProxy (CameraX) -> Bitmap -> MPImage
 *   - Execução assíncrona em modo LIVE_STREAM
 *   - Callback com landmarks normalizados + tempo de inferência
 *
 * Performance: GPU delegate, modelo Lite, 1 pose máx.
 */
public class PoseLandmarkerHelper {

    private static final String TAG = "PoseLandmarkerHelper";
    private static final String MODEL_NAME = "pose_landmarker_lite.task";

    public interface ResultListener {
        void onResult(@NonNull PoseLandmarkerResult result, int imageWidth, int imageHeight, long inferenceTimeMs);
        void onError(@NonNull String message);
    }

    private final Context context;
    private final ResultListener listener;
    private PoseLandmarker poseLandmarker;

    public PoseLandmarkerHelper(@NonNull Context context, @NonNull ResultListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    /**
     * Inicializa o detector. Tenta GPU primeiro, cai para CPU se falhar.
     */
    public void setup() {
        try {
            init(Delegate.GPU);
        } catch (Exception e) {
            Log.w(TAG, "GPU delegate falhou, tentando CPU", e);
            try {
                init(Delegate.CPU);
            } catch (Exception e2) {
                Log.e(TAG, "Falha ao inicializar PoseLandmarker", e2);
                listener.onError("Não foi possível iniciar a detecção de pose: " + e2.getMessage());
            }
        }
    }

    private void init(Delegate delegate) {
        BaseOptions baseOptions = BaseOptions.builder()
                .setDelegate(delegate)
                .setModelAssetPath(MODEL_NAME)
                .build();

        PoseLandmarker.PoseLandmarkerOptions options =
                PoseLandmarker.PoseLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setRunningMode(RunningMode.LIVE_STREAM)
                        .setNumPoses(1)
                        .setMinPoseDetectionConfidence(0.5f)
                        .setMinTrackingConfidence(0.5f)
                        .setMinPosePresenceConfidence(0.5f)
                        .setResultListener(this::onResult)
                        .setErrorListener(error -> {
                            Log.e(TAG, "PoseLandmarker error", error);
                            listener.onError(error.getMessage() != null ? error.getMessage() : "Erro de detecção");
                        })
                        .build();

        poseLandmarker = PoseLandmarker.createFromOptions(context, options);
    }

    /**
     * Detecta pose em um frame da câmera. Não bloqueia (LIVE_STREAM).
     *
     * @param imageProxy frame do CameraX. Será fechado por quem chamou.
     * @param isFrontCamera se a câmera é frontal (precisa flip horizontal)
     */
    public void detectAsync(@NonNull ImageProxy imageProxy, boolean isFrontCamera) {
        if (poseLandmarker == null) {
            return;
        }

        long frameTime = SystemClock.uptimeMillis();

        Bitmap bitmap = imageProxyToBitmap(imageProxy);
        if (bitmap == null) return;

        // Rotaciona o bitmap conforme a orientação da câmera
        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
        if (isFrontCamera) {
            // Flip horizontal para câmera frontal (espelho)
            matrix.postScale(-1f, 1f, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        MPImage mpImage = new BitmapImageBuilder(rotated).build();
        try {
            poseLandmarker.detectAsync(mpImage, frameTime);
        } catch (Exception e) {
            Log.e(TAG, "detectAsync falhou", e);
        }
    }

    private void onResult(PoseLandmarkerResult result, MPImage input) {
        long inferenceTime = SystemClock.uptimeMillis() - result.timestampMs();
        listener.onResult(result, input.getWidth(), input.getHeight(), inferenceTime);
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(
                    imageProxy.getWidth(),
                    imageProxy.getHeight(),
                    Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "imageProxyToBitmap falhou", e);
            return null;
        }
    }

    public void close() {
        if (poseLandmarker != null) {
            try {
                poseLandmarker.close();
            } catch (Exception e) {
                Log.w(TAG, "Erro ao fechar landmarker", e);
            }
            poseLandmarker = null;
        }
    }
}
