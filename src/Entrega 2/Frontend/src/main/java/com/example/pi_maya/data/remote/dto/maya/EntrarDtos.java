package com.example.pi_maya.data.remote.dto.maya;

public class EntrarDtos {

    public static class Request {
        public String email;
        public String senha;

        public Request(String email, String senha) {
            this.email = email;
            this.senha = senha;
        }
    }

    public static class Response {
        public String token;
        public String refreshToken;
        public Long expiraEm;
        public ApiUsuarioDto usuario;
    }
}
