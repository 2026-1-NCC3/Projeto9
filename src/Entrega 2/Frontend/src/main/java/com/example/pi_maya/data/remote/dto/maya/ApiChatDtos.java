package com.example.pi_maya.data.remote.dto.maya;

import java.util.List;

public class ApiChatDtos {

    public static class MensagensResponse {
        public List<Mensagem> mensagens;
    }

    public static class EnviarRequest {
        public String salaId;
        public String conteudo;

        public EnviarRequest(String salaId, String conteudo) {
            this.salaId = salaId;
            this.conteudo = conteudo;
        }
    }

    public static class EnviarResponse {
        public Mensagem mensagem;
    }

    public static class Mensagem {
        public String id;
        public String salaId;
        public String remetenteId;
        public String conteudo;
        public String anexoUrl;
        public String anexoTipo;
        public String lidaEm;
        public String criadaEm;
    }
}
