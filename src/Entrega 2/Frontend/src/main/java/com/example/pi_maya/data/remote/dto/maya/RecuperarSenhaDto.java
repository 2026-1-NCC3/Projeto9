package com.example.pi_maya.data.remote.dto.maya;

public class RecuperarSenhaDto {
    public String email;
    public String urlRedirecionamento;

    public RecuperarSenhaDto(String email, String urlRedirecionamento) {
        this.email = email;
        this.urlRedirecionamento = urlRedirecionamento;
    }
}
