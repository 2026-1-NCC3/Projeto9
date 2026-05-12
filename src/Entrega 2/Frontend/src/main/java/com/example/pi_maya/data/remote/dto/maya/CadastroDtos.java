package com.example.pi_maya.data.remote.dto.maya;

public class CadastroDtos {

    public static class Request {
        public String email;
        public String senha;
        public String nome;
        public String telefone;
        public String urlConfirmacao;

        public Request(String email, String senha, String nome, String telefone, String urlConfirmacao) {
            this.email = email;
            this.senha = senha;
            this.nome = nome;
            this.telefone = telefone;
            this.urlConfirmacao = urlConfirmacao;
        }
    }

    public static class Response {
        public UsuarioCadastrado usuario;

        public static class UsuarioCadastrado {
            public String id;
            public String email;
            public Boolean confirmacaoEnviada;
        }
    }
}
