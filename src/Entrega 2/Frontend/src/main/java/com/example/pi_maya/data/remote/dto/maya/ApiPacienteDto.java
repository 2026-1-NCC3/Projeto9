package com.example.pi_maya.data.remote.dto.maya;

public class ApiPacienteDto {
    public Paciente paciente;

    public static class Paciente {
        public String id;
        public String queixaPrincipal;
        public String status;
        public String criadoEm;
        public Fisio fisio;
        public String salaChat;
    }

    public static class Fisio {
        public String id;
        public String nome;
        public String avatarUrl;
        public String telefone;
    }
}
