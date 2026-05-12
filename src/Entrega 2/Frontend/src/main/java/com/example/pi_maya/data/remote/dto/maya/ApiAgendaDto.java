package com.example.pi_maya.data.remote.dto.maya;

import java.util.List;

public class ApiAgendaDto {
    public List<Agendamento> agendamentos;

    public static class Agendamento {
        public String id;
        public String inicioEm;
        public String fimEm;
        public String status;
        public String observacoes;
        public String fisioId;
    }
}
