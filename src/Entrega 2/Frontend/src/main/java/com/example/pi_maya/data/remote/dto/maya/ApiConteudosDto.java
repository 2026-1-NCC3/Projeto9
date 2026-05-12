package com.example.pi_maya.data.remote.dto.maya;

import java.util.List;

public class ApiConteudosDto {
    public List<Conteudo> conteudos;

    public static class Conteudo {
        public String id;
        public String titulo;
        public String corpo;
        public String capaUrl;
        public String videoUrl;
        public String tipo;
        public String categoria;
        public String publicadoEm;
    }
}
