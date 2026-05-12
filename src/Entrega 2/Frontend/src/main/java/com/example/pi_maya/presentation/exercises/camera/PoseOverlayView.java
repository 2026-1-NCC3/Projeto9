package com.example.pi_maya.presentation.exercises.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * View custom que desenha os 33 landmarks do MediaPipe Pose por cima do preview.
 *
 * Quando há uma avaliação ativa, articulações fora da tolerância são pintadas
 * em vermelho coral; as demais em verde sucesso.
 */
public class PoseOverlayView extends View {

    private static final List<int[]> CONNECTIONS = Arrays.asList(
            // Tronco
            new int[]{11, 12},
            new int[]{11, 23},
            new int[]{12, 24},
            new int[]{23, 24},
            // Braço direito
            new int[]{11, 13},
            new int[]{13, 15},
            // Braço esquerdo
            new int[]{12, 14},
            new int[]{14, 16},
            // Perna direita
            new int[]{23, 25},
            new int[]{25, 27},
            new int[]{27, 29},
            new int[]{27, 31},
            // Perna esquerda
            new int[]{24, 26},
            new int[]{26, 28},
            new int[]{28, 30},
            new int[]{28, 32},
            // Cabeça
            new int[]{0, 2},
            new int[]{0, 5},
            new int[]{2, 7},
            new int[]{5, 8}
    );

    private final Paint pointOkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointErroPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineOkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineErroPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointNeutroPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lineNeutroPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PoseLandmarkerResult result;
    private int imageWidth = 1;
    private int imageHeight = 1;

    /** Quando vazio, modo neutro (sem avaliação). */
    private Set<Integer> landmarksErrados = Collections.emptySet();
    private boolean modoAvaliacao = false;

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        // Neutro (sem avaliação): paleta da identidade visual (ciano)
        pointNeutroPaint.setColor(Color.parseColor("#46B6CC"));
        pointNeutroPaint.setStyle(Paint.Style.FILL);
        pointNeutroPaint.setStrokeWidth(8f);

        lineNeutroPaint.setColor(Color.parseColor("#D8EFF6"));
        lineNeutroPaint.setStyle(Paint.Style.STROKE);
        lineNeutroPaint.setStrokeWidth(6f);

        // Correto (verde sucesso)
        pointOkPaint.setColor(Color.parseColor("#2D6A4F"));
        pointOkPaint.setStyle(Paint.Style.FILL);

        lineOkPaint.setColor(Color.parseColor("#B7E4C7"));
        lineOkPaint.setStyle(Paint.Style.STROKE);
        lineOkPaint.setStrokeWidth(6f);

        // Errado (coral)
        pointErroPaint.setColor(Color.parseColor("#C45A5A"));
        pointErroPaint.setStyle(Paint.Style.FILL);

        lineErroPaint.setColor(Color.parseColor("#E87461"));
        lineErroPaint.setStyle(Paint.Style.STROKE);
        lineErroPaint.setStrokeWidth(7f);
    }

    public void setResult(PoseLandmarkerResult result, int imageWidth, int imageHeight) {
        this.result = result;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

    /** Habilita/desabilita modo de avaliação (cores diferentes pra OK/erro). */
    public void setModoAvaliacao(boolean ativo) {
        this.modoAvaliacao = ativo;
        invalidate();
    }

    /** Lista de índices de landmarks com problema. Usado em modoAvaliacao. */
    public void setLandmarksErrados(Set<Integer> errados) {
        this.landmarksErrados = errados != null ? errados : Collections.emptySet();
        invalidate();
    }

    public void clear() {
        this.result = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (result == null || result.landmarks().isEmpty()) return;

        float scale = Math.max(
                getWidth() / (float) imageWidth,
                getHeight() / (float) imageHeight
        );
        float scaledWidth = imageWidth * scale;
        float scaledHeight = imageHeight * scale;
        float offsetX = (getWidth() - scaledWidth) / 2f;
        float offsetY = (getHeight() - scaledHeight) / 2f;

        for (List<NormalizedLandmark> pose : result.landmarks()) {
            // Conexões
            for (int[] conn : CONNECTIONS) {
                if (conn[0] >= pose.size() || conn[1] >= pose.size()) continue;
                NormalizedLandmark a = pose.get(conn[0]);
                NormalizedLandmark b = pose.get(conn[1]);
                Paint p;
                if (!modoAvaliacao) {
                    p = lineNeutroPaint;
                } else if (landmarksErrados.contains(conn[0])
                        || landmarksErrados.contains(conn[1])) {
                    p = lineErroPaint;
                } else {
                    p = lineOkPaint;
                }
                canvas.drawLine(
                        a.x() * scaledWidth + offsetX,
                        a.y() * scaledHeight + offsetY,
                        b.x() * scaledWidth + offsetX,
                        b.y() * scaledHeight + offsetY,
                        p
                );
            }
            // Pontos
            for (int i = 0; i < pose.size(); i++) {
                NormalizedLandmark lm = pose.get(i);
                Paint p;
                if (!modoAvaliacao) {
                    p = pointNeutroPaint;
                } else if (landmarksErrados.contains(i)) {
                    p = pointErroPaint;
                } else {
                    p = pointOkPaint;
                }
                canvas.drawCircle(
                        lm.x() * scaledWidth + offsetX,
                        lm.y() * scaledHeight + offsetY,
                        9f,
                        p
                );
            }
        }
    }
}
