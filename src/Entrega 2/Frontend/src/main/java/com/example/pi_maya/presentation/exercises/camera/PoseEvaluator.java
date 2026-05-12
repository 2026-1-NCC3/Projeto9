package com.example.pi_maya.presentation.exercises.camera;

import androidx.annotation.NonNull;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Avalia uma pose detectada pelo MediaPipe contra uma "pose de referência"
 * configurada (lista de ângulos articulares com tolerância).
 *
 * Por que ângulos e não posições absolutas:
 *   pessoas baixas/altas, distâncias diferentes da câmera, etc. geram posições
 *   diferentes pro mesmo movimento. Ângulos articulares são invariantes a tudo
 *   isso — fechar 90° é 90° pra qualquer um.
 *
 * Índices dos landmarks (MediaPipe Pose, 33 pontos):
 *   nariz=0, ombro_e=11, ombro_d=12, cotovelo_e=13, cotovelo_d=14,
 *   pulso_e=15, pulso_d=16, quadril_e=23, quadril_d=24,
 *   joelho_e=25, joelho_d=26, tornozelo_e=27, tornozelo_d=28
 */
public class PoseEvaluator {

    /**
     * Regra: ângulo formado por 3 landmarks (a, b, c — vértice em b).
     *
     * Suporta um ângulo-alvo principal e um alternativo opcional, útil pra
     * exercícios que aceitam mais de uma posição (ex: inclinar a cabeça
     * pra direita OU pra esquerda).
     */
    public static class RegraAngulo {
        public final String nome;
        public final int a, b, c;
        public final float anguloAlvo;
        public final Float anguloAlvoAlternativo; // pode ser null
        public final float tolerancia;
        public final String dica;

        public RegraAngulo(String nome, int a, int b, int c, float anguloAlvo,
                           float tolerancia, String dica) {
            this(nome, a, b, c, anguloAlvo, null, tolerancia, dica);
        }

        public RegraAngulo(String nome, int a, int b, int c, float anguloAlvo,
                           Float anguloAlvoAlternativo, float tolerancia, String dica) {
            this.nome = nome;
            this.a = a;
            this.b = b;
            this.c = c;
            this.anguloAlvo = anguloAlvo;
            this.anguloAlvoAlternativo = anguloAlvoAlternativo;
            this.tolerancia = tolerancia;
            this.dica = dica;
        }

        /** Menor diferença em relação a qualquer um dos alvos. */
        public float menorDiferenca(float anguloAtual) {
            float d1 = Math.abs(anguloAtual - anguloAlvo);
            if (anguloAlvoAlternativo == null) return d1;
            float d2 = Math.abs(anguloAtual - anguloAlvoAlternativo);
            return Math.min(d1, d2);
        }
    }

    public static class Resultado {
        public final boolean poseCorreta;
        public final List<String> dicas;
        public final Set<Integer> landmarksErrados;
        /** Pontuação 0-100 (média das articulações). */
        public final int pontuacao;

        public Resultado(boolean ok, List<String> dicas, Set<Integer> erros, int pontuacao) {
            this.poseCorreta = ok;
            this.dicas = dicas;
            this.landmarksErrados = erros;
            this.pontuacao = pontuacao;
        }

        public static Resultado semDeteccao() {
            return new Resultado(false, Collections.singletonList("Posicione-se na frente da câmera."),
                    Collections.emptySet(), 0);
        }
    }

    private final List<RegraAngulo> regras;
    private final String tituloPose;

    public PoseEvaluator(@NonNull String tituloPose, @NonNull List<RegraAngulo> regras) {
        this.tituloPose = tituloPose;
        this.regras = regras;
    }

    public String getTituloPose() {
        return tituloPose;
    }

    public Resultado avaliar(@NonNull List<NormalizedLandmark> pose) {
        if (pose.size() < 33) return Resultado.semDeteccao();

        List<String> dicas = new ArrayList<>();
        Set<Integer> erros = new HashSet<>();
        int somaScore = 0;

        for (RegraAngulo r : regras) {
            float angulo = calcularAngulo(pose.get(r.a), pose.get(r.b), pose.get(r.c));
            float diff = r.menorDiferenca(angulo);
            if (diff > r.tolerancia) {
                dicas.add(r.dica);
                erros.add(r.a);
                erros.add(r.b);
                erros.add(r.c);
                // Pontuação por articulação: 100 dentro, 0 se passa do triplo da tolerância
                float fora = (diff - r.tolerancia) / (r.tolerancia * 2);
                int score = Math.max(0, Math.round(100 * (1 - fora)));
                somaScore += score;
            } else {
                somaScore += 100;
            }
        }

        int pontuacao = regras.isEmpty() ? 0 : somaScore / regras.size();
        boolean ok = erros.isEmpty();
        return new Resultado(ok, dicas, erros, pontuacao);
    }

    /**
     * Calcula o ângulo no vértice b formado pelos vetores ba e bc.
     * Resultado em graus, 0–180.
     */
    private static float calcularAngulo(NormalizedLandmark a, NormalizedLandmark b, NormalizedLandmark c) {
        float bax = a.x() - b.x();
        float bay = a.y() - b.y();
        float bcx = c.x() - b.x();
        float bcy = c.y() - b.y();
        float dot = bax * bcx + bay * bcy;
        float magBa = (float) Math.sqrt(bax * bax + bay * bay);
        float magBc = (float) Math.sqrt(bcx * bcx + bcy * bcy);
        if (magBa == 0 || magBc == 0) return 0f;
        float cos = dot / (magBa * magBc);
        if (cos > 1f) cos = 1f;
        if (cos < -1f) cos = -1f;
        return (float) Math.toDegrees(Math.acos(cos));
    }

    // ---------- POSES PRÉ-DEFINIDAS ----------

    /**
     * "Postura sentada com braços abertos" — braços em T.
     * Verifica: cotovelos estendidos (~180°) e ombros abertos lateralmente (~90°).
     */
    public static PoseEvaluator bracosAbertosEmT() {
        return new PoseEvaluator("Braços abertos em T", Arrays.asList(
                new RegraAngulo(
                        "Cotovelo direito estendido",
                        12 /*ombro_d*/, 14 /*cotovelo_d*/, 16 /*pulso_d*/,
                        180f, 20f,
                        "Estique o braço direito"
                ),
                new RegraAngulo(
                        "Cotovelo esquerdo estendido",
                        11 /*ombro_e*/, 13 /*cotovelo_e*/, 15 /*pulso_e*/,
                        180f, 20f,
                        "Estique o braço esquerdo"
                ),
                new RegraAngulo(
                        "Ombro direito aberto",
                        24 /*quadril_d*/, 12 /*ombro_d*/, 14 /*cotovelo_d*/,
                        90f, 25f,
                        "Eleve o braço direito até a linha do ombro"
                ),
                new RegraAngulo(
                        "Ombro esquerdo aberto",
                        23 /*quadril_e*/, 11 /*ombro_e*/, 13 /*cotovelo_e*/,
                        90f, 25f,
                        "Eleve o braço esquerdo até a linha do ombro"
                )
        ));
    }

    /**
     * "Inclinar para a direita" do ponto de vista do usuário.
     *
     * Como a câmera frontal é espelho, quando o usuário inclina a cabeça pra
     * DIREITA dele, na imagem o lado ESQUERDO da tela desce. Por isso a
     * detecção usa Lado.ESQUERDA mesmo o rótulo dizendo "direita".
     */
    public static PoseEvaluator inclinacaoCabecaDireita() {
        return new AvaliadorInclinacaoLateralCabeca(
                "Inclinar para a direita",
                AvaliadorInclinacaoLateralCabeca.Lado.ESQUERDA
        );
    }

    /** "Inclinar para a esquerda" — invertido pelo mesmo motivo (espelho). */
    public static PoseEvaluator inclinacaoCabecaEsquerda() {
        return new AvaliadorInclinacaoLateralCabeca(
                "Inclinar para a esquerda",
                AvaliadorInclinacaoLateralCabeca.Lado.DIREITA
        );
    }

    /**
     * Avaliador específico para inclinação lateral da cabeça.
     *
     * Em vez de medir ângulos (que dependem muito da pose neutra de cada pessoa),
     * compara a altura vertical das orelhas — quando a cabeça inclina pro lado X,
     * a orelha do lado X desce em relação à orelha do outro lado.
     *
     * A medida é normalizada pela distância entre os ombros, então fica
     * invariante à escala (perto/longe da câmera).
     */
    public static class AvaliadorInclinacaoLateralCabeca extends PoseEvaluator {
        public enum Lado { DIREITA, ESQUERDA }

        /**
         * Inclinação mínima (relativa à distância dos ombros) pra considerar correta.
         * Calibrado com base no movimento real: a pessoa raramente consegue
         * inclinar mais de ~0.15-0.17 sem flexão exagerada da coluna.
         */
        private static final float LIMIAR = 0.08f;
        /** Inclinação que dá nota máxima (100%). */
        private static final float ALVO = 0.15f;

        private final Lado lado;

        public AvaliadorInclinacaoLateralCabeca(String titulo, Lado lado) {
            super(titulo, Collections.<RegraAngulo>emptyList());
            this.lado = lado;
        }

        @Override
        public Resultado avaliar(@NonNull List<NormalizedLandmark> pose) {
            if (pose.size() < 33) return Resultado.semDeteccao();

            NormalizedLandmark orelhaEsq = pose.get(7);
            NormalizedLandmark orelhaDir = pose.get(8);
            NormalizedLandmark ombroEsq = pose.get(11);
            NormalizedLandmark ombroDir = pose.get(12);

            float distOmbros = Math.abs(ombroDir.x() - ombroEsq.x());
            if (distOmbros < 0.05f) {
                // Ombros muito juntos: pessoa muito longe ou pose estranha
                return Resultado.semDeteccao();
            }

            // diffY > 0 → orelha direita está mais baixa (inclinou pra direita)
            // diffY < 0 → orelha esquerda mais baixa (inclinou pra esquerda)
            float diffY = (orelhaDir.y() - orelhaEsq.y()) / distOmbros;
            float inclinacao = (lado == Lado.DIREITA) ? diffY : -diffY;

            Set<Integer> erros = new HashSet<>();
            List<String> dicas = new ArrayList<>();
            boolean ok;
            int pontuacao;

            if (inclinacao >= LIMIAR) {
                ok = true;
                pontuacao = Math.min(100, Math.round(inclinacao / ALVO * 100));
            } else {
                ok = false;
                // Pontuação parcial até o limiar (motiva o usuário a inclinar mais)
                pontuacao = inclinacao < 0
                        ? 0
                        : Math.round(inclinacao / LIMIAR * 50);
                String direcao = (lado == Lado.DIREITA) ? "DIREITA" : "ESQUERDA";
                dicas.add("Incline a cabeça para a " + direcao);
                // Pinta orelhas e ombros pra dar contexto visual
                erros.add(7);
                erros.add(8);
                erros.add(11);
                erros.add(12);
            }

            return new Resultado(ok, dicas, erros, pontuacao);
        }
    }

    /**
     * Etapas do exercício (lista ordenada). Cervical = 2 etapas (direita, esquerda).
     * Para exercícios sem etapas avaliadas, devolve lista vazia.
     */
    public static List<PoseEvaluator> etapasParaExercicio(String titulo) {
        if (titulo == null) return Collections.emptyList();
        String lower = titulo.toLowerCase();
        if (lower.contains("cervical")) {
            return Arrays.asList(
                    inclinacaoCabecaDireita(),
                    inclinacaoCabecaEsquerda()
            );
        }
        if (lower.contains("braço") || lower.contains("bracos") || lower.contains("braços")) {
            return Collections.singletonList(bracosAbertosEmT());
        }
        return Collections.emptyList();
    }
}
