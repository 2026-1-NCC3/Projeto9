package com.example.pi_maya.data.remote.dto.maya;

import java.util.List;

public class ApiExerciciosDto {
    public List<ExercicioAtribuido> exercicios;

    public static class ExercicioAtribuido {
        public String atribuicaoId;
        public String exercicioId;
        public String titulo;
        public String descricao;
        public String instrucoes;
        public String categoria;
        public Integer dificuldade;
        public Integer duracaoSegundos;
        public String videoUrl;
        public String thumbnailUrl;
        public Integer series;
        public Integer repeticoes;
        public Integer frequenciaSemanal;
        public String observacoes;
    }
}
