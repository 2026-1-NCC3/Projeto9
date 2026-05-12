# FECAP - Fundação de Comércio Álvares Penteado

<p align="center">
<a href= "https://www.fecap.br/"><img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRhZPrRa89Kma0ZZogxm0pi-tCn_TLKeHGVxywp-LXAFGR3B1DPouAJYHgKZGV0XTEf4AE&usqp=CAU" alt="FECAP - Fundação de Comércio Álvares Penteado" border="0"></a>
</p>

# Projeto Maya Yamamoto - RPG
<br>

## CTRL+ALT+DEL

## Integrantesdo Grupo

[Yuri Oliveira Santana](https://www.linkedin.com/in/yuri-oliveira-santana/)<br>
[Mauricio Suster](https://www.linkedin.com/in/mauricio-suster?utm_source=share_via&utm_content=profile&utm_medium=member_android)<br>
[Kaio Inglez](https://www.linkedin.com/in/kaio-inglez-874812391?utm_source=share_via&utm_content=profile&utm_medium=member_ios)<br>
[Igor Almeida](https://www.linkedin.com/in/igor-almeida-635656342?utm_source=share_via&utm_content=profile&utm_medium=member_android)

## Professores Orientadores

[Kátia M. Lara Bossi](https://www.linkedin.com/in/katia-bossi/)<br>
[Marco Aurélio Lima Barbosa](https://buscatextual.cnpq.br/buscatextual/visualizacv.do)<br>
[Victor Bruno Alexander Rosetti de Quiroz](https://www.linkedin.com/in/victorbarq/)<br>
[Rodrigo da Rosa](https://www.linkedin.com/in/rodrigo-da-rosa-phd/)

<br>

## Entregas das Disciplinas

| Disciplina | Entrega 1 | Entrega 2 |
| :--- | :--- | :--- |
| Análise Descritiva de Dados | [/Documentos/Entrega 1/Analise Descritiva de Dados](./Documentos/Entrega%201/Analise%20Descritiva%20de%20Dados) | [/Documentos/Entrega 2/Analise Descritiva de Dados](./Documentos/Entrega%202/Analise%20Descritiva%20de%20Dados) |
| Programação Orientada a Objetos e Estrutura de Dados | [/Documentos/Entrega 1/Programação Orientada a Objetos e Estrutura de Dados](./Documentos/Entrega%201/Programação%20Orientada%20a%20Objetos%20e%20Estrutura%20de%20Dados) | [/Documentos/Entrega 2/Programação Orientada a Objetos e Estrutura de Dados](./Documentos/Entrega%202/Programação%20Orientada%20a%20Objetos%20e%20Estrutura%20de%20Dados) | 
| Programação para Dispositivos Moveis | [/Documentos/src/Entrega 1](./src/Entrega%201) | [/Documentos/src/Entrega 2](./src/Entrega%202) | 
| Projeto Interdisciplinar Aplicativo Movel | [/Documentos/src/Backend](./src/Entrega%201) | [/Documentos/src/Backend](./src/Entrega%202) | 
<br>


## Descrição

A profissional fisioterapeuta Maya Yoshiko Yamamoto presta atendimento com foco em Reeducação Postural 
Global (RPG) e necessita de uma solução digital para melhorar a comunicação com seus pacientes e 
acompanhar a evolução ao longo do tratamento. Atualmente, parte do acompanhamento é realizado de forma 
dispersa (mensagens e registros não padronizados), o que dificulta a rastreabilidade do prontuário, o 
planejamento de exercícios domiciliares e o controle de agenda. 

O projeto consiste no desenvolvimento de uma solução tecnologica (web e mobile) desenhada para transformar a gestão da clinica. O objetivo central é eliminar processos manuais, centralizar o controle de pacientes e oferecer uma ferramenta de suporte.
<br><br>

## 🛠 Estrutura de pastas

```text|
├── Documentos
|   ├── Entrega 1
|   |   ├── Análise Descritiva de Dados
|   |   ├── Programação Orientada a Objetos e Estrutura de Dados
|   |   ├── Programação para Dispositivos Móveis
|   |   └── Projeto Interdisciplinar: Aplicativo Móvel
|   └── Entrega 2
|       ├── Análise Descritiva de Dados
|       ├── Programação Orientada a Objetos e Estrutura de Dados
|       ├── Programação para Dispositivos Móveis
|       └── Projeto Interdisciplinar: Aplicativo Móvel
|
├── imagens
|
├── src
|   ├── Entrega 1
|       ├── Backend
|       ├── Mobile
|       ├── Web
|   ├── Entrega 2
|       ├── Backend
|       ├── Mobile
|       ├── Web
| 
└── README.md
```

## 🛠 Instalação

# Maya RPG · App Android

Aplicativo do paciente da clínica de RPG da Maya Yamamoto. Construído em Java,
consome a API REST (`api/`) e usa Supabase Realtime direto pra chat ao vivo.

## Stack

- **Linguagem:** Java 11
- **AGP:** 9.0.1 · **compileSdk:** 36 · **minSdk:** 24
- **UI:** Material 3 + ViewBinding + Navigation Component + Tabs/Pager
- **Arquitetura:** Clean Architecture + MVVM com DI manual (ServiceLocator em
  `MayaApp.java`)
- **HTTP:** Retrofit + OkHttp + Gson — todas as chamadas vão pra `api/`
- **Realtime:** WebSocket manual (`SupabaseRealtimeClient.java`) direto no
  Supabase, porque funções serverless não mantêm WebSocket aberto
- **Imagens:** Glide
- **Visão computacional:** MediaPipe Tasks Vision 0.10.14 + CameraX 1.3.4
- **Segurança local:** EncryptedSharedPreferences pra guardar o token
- **Deep link:** `pi-maya://auth/callback` pra confirmação de email do Supabase

## Setup local

### 1. local.properties

Na raiz do repo (`C:\pi\`):


# Supabase — usado APENAS pelo realtime do chat (WebSocket)
SUPABASE_URL=https://seu-projeto.supabase.co
SUPABASE_ANON_KEY=eyJh...

# URL do servidor próprio (api/). Para dev no emulador: http://10.0.2.2:3001
API_URL=https://pi-maia-api.vercel.app
```

> **Importante:** valores são injetados em build time via `BuildConfig`.
> Sempre que mudar `local.properties`, rode **Clean Project → Rebuild** antes
> de Run, senão a build antiga continua usando os valores velhos.

### 2. Abrir e rodar

1. Abra a pasta `C:\pi` no Android Studio
2. Aguarde o Gradle sincronizar
3. Conecte o celular via USB (modo desenvolvedor + USB debugging ativados)
4. **Build → Clean Project → Rebuild Project**
5. **Run** ▶ — escolhe o seu celular

Na primeira execução o Gradle baixa o modelo do MediaPipe (~5 MB) e empacota
junto no APK — veja `app/build.gradle.kts` (task `downloadPoseModel`).

## Estrutura de pastas

```
app/src/main/java/com/example/pi_maya/
├── MayaApp.java                       # Application + ServiceLocator
├── core/
│   ├── network/
│   │   ├── MayaApiClient.java         # Retrofit pra api/
│   │   ├── MayaApiAuthInterceptor.java # injeta "Authorization: Bearer ..."
│   │   ├── SupabaseRealtimeClient.java # WebSocket Phoenix v2 para o chat
│   │   └── AuthDeepLink.java          # constante pi-maya://auth/callback
│   ├── session/SessionManager.java    # tokens em EncryptedSharedPreferences
│   ├── result/Resource.java           # wrapper { loading | success | error }
│   └── util/{DateUtils,ValidationUtils}.java
├── data/
│   ├── remote/
│   │   ├── api/maya/MayaApi.java      # interface Retrofit
│   │   └── dto/maya/...               # DTOs em pt-BR (EntrarDtos, ApiPacienteDto, ...)
│   └── repository/
│       ├── AuthRepositoryImpl.java
│       ├── PatientRepositoryImpl.java
│       ├── AppointmentRepositoryImpl.java
│       ├── ExerciseRepositoryImpl.java
│       ├── ChatRepositoryImpl.java
│       └── ContentRepositoryImpl.java
├── domain/
│   ├── model/                         # entidades puras: User, Patient, Exercise, ...
│   └── repository/                    # interfaces
└── presentation/
    ├── splash/SplashActivity.java
    ├── onboarding/OnboardingActivity.java
    ├── auth/{Login,Register,ForgotPassword,AuthCallback}Activity.java
    ├── main/MainActivity.java          # host do BottomNavigation
    ├── home/HomeFragment.java
    ├── schedule/                       # ScheduleFragment + MonthCalendarView custom
    ├── exercises/
    │   ├── ExercisesFragment.java
    │   ├── ExerciseDetailActivity.java
    │   ├── ExerciseCameraActivity.java  # ★ CameraX + MediaPipe
    │   └── camera/                       # PoseLandmarkerHelper, PoseOverlayView, PoseEvaluator
    ├── chat/                            # lista + ChatRoomActivity com realtime
    ├── content/                         # feed + detalhe com cover
    └── profile/                         # perfil + logout + LGPD
```

## Telas

| Tela | Função |
|---|---|
| Splash | Decide entre Onboarding / Login / Home |
| Onboarding | 3 telas explicando o app (vista 1 vez) |
| Login / Cadastro / Recuperar | Auth via API. Cadastro pede aceite LGPD |
| AuthCallback | Recebe deep link `pi-maya://auth/callback` do email do Supabase |
| Home | Próxima sessão, exercícios de hoje, posts recentes |
| Agenda | Toggle Lista / Calendário mensal (custom view) |
| Exercícios | Lista de exercícios atribuídos pela fisio |
| Detalhe do exercício | Vídeo demo, instruções e botão "Iniciar" |
| **Câmera + MediaPipe** | Detecção de pose em tempo real (★ feature principal) |
| Chat | Conversa com a fisio + realtime WebSocket |
| Conteúdo | Feed de posts educativos com capa |
| Perfil | Dados pessoais, logout, opções LGPD |


## Chat realtime

O envio e o histórico passam pela API (`POST /api/chat/mensagens` e
`GET /api/chat/mensagens?salaId=...`). Mas pra receber **mensagens novas ao
vivo**, o app abre um WebSocket direto com o Supabase Realtime:

- Implementação: [`SupabaseRealtimeClient.java`](src/main/java/com/example/pi_maya/core/network/SupabaseRealtimeClient.java)
- Protocolo: Phoenix Channels v2 (mensagens em array, `vsn=2.0.0`)
- O JWT do usuário é enviado top-level no payload do `phx_join` pra RLS funcionar
- Heartbeat a cada 30 s mantém a conexão viva

Pra ativar do lado do banco, rode uma vez no SQL Editor do Supabase:

```sql
ALTER PUBLICATION supabase_realtime ADD TABLE public.chat_messages;
```

## Auth + deep link

1. Usuário cadastra → API → Supabase Auth manda email de confirmação
2. Usuário clica no link → Supabase redireciona pra `pi-maya://auth/callback#access_token=...`
3. Android abre `AuthCallbackActivity` (intent-filter no manifest)
4. Activity lê o fragment, persiste tokens em EncryptedSharedPreferences e
   vai pra `MainActivity`

Pra configurar no Supabase, adicione `pi-maya://auth/callback` em
**Authentication → URL Configuration → Redirect URLs**.


## O que ainda não tem (futuro)

- Calibração de pose pela fisio (gravar referência via vídeo) — hoje os
  alvos são hardcoded
- Persistir resultados das execuções no banco (`exercise_executions`)
- Push notifications de lembretes
- Anexos no chat
- Modo offline (cache local)


## 📋 Licença/License
<a href="https://example.com">Projeto Maya Yamamoto - RPG</a> © 2026 by <a href="https://example.com">Yuri Santana, Mauricio Suster, Kaio Inglez e Igor Almeida</a> is licensed under <a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a><img src="https://mirrors.creativecommons.org/presskit/icons/cc.svg" alt="" style="max-width: 1em;max-height:1em;margin-left: .2em;"><img src="https://mirrors.creativecommons.org/presskit/icons/by.svg" alt="" style="max-width: 1em;max-height:1em;margin-left: .2em;">

## 🎓 Referências

Aqui estão as referências usadas no projeto.

1. https://mayayamamoto.com.br/
2. https://www.toptal.com/developers/gitignore
