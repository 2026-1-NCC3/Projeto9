package com.example.pi_maya.data.remote.api.maya;

import com.example.pi_maya.data.remote.dto.maya.ApiAgendaDto;
import com.example.pi_maya.data.remote.dto.maya.ApiChatDtos;
import com.example.pi_maya.data.remote.dto.maya.ApiConteudosDto;
import com.example.pi_maya.data.remote.dto.maya.ApiExerciciosDto;
import com.example.pi_maya.data.remote.dto.maya.ApiPacienteDto;
import com.example.pi_maya.data.remote.dto.maya.ApiUsuarioDto;
import com.example.pi_maya.data.remote.dto.maya.CadastroDtos;
import com.example.pi_maya.data.remote.dto.maya.EntrarDtos;
import com.example.pi_maya.data.remote.dto.maya.RecuperarSenhaDto;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Interface única para todos os endpoints do servidor próprio.
 * Pontos finais ficam em /api/* no projeto Next.js da Vercel.
 */
public interface MayaApi {

    // ===== AUTH =====
    @POST("api/auth/cadastro")
    Call<CadastroDtos.Response> cadastrar(@Body CadastroDtos.Request body);

    @POST("api/auth/entrar")
    Call<EntrarDtos.Response> entrar(@Body EntrarDtos.Request body);

    @POST("api/auth/recuperar-senha")
    Call<ResponseBody> recuperarSenha(@Body RecuperarSenhaDto body);

    @GET("api/auth/usuario")
    Call<UsuarioWrapper> usuarioAtual();

    class UsuarioWrapper {
        public ApiUsuarioDto usuario;
    }

    // ===== PACIENTE (app) =====
    @GET("api/meu-paciente")
    Call<ApiPacienteDto> meuPaciente();

    // ===== AGENDA (app) =====
    @GET("api/minha-agenda")
    Call<ApiAgendaDto> minhaAgenda(@Query("futuro") Boolean somenteFuturo);

    // ===== EXERCÍCIOS (app) =====
    @GET("api/meus-exercicios")
    Call<ApiExerciciosDto> meusExercicios();

    // ===== CONTEÚDO (app) =====
    @GET("api/conteudos-publicados")
    Call<ApiConteudosDto> conteudosPublicados();

    // ===== CHAT =====
    @GET("api/chat/mensagens")
    Call<ApiChatDtos.MensagensResponse> mensagensDaSala(@Query("salaId") String salaId);

    @POST("api/chat/mensagens")
    Call<ApiChatDtos.EnviarResponse> enviarMensagem(@Body ApiChatDtos.EnviarRequest body);
}
