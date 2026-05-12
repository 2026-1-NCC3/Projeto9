# Maya RPG · API

Servidor backend da clínica de RPG da Maya Yamamoto. Construído em Next.js 16
(rotas serverless) e deployado na Vercel.

## Pra que serve

Centraliza a regra de negócio entre o app Android e o painel web. Ambos os
clientes consomem os mesmos endpoints `/api/*` e ficam mais simples.

A única exceção é o **chat em tempo real**: a entrega de mensagens novas usa
WebSocket direto cliente ↔ Supabase Realtime. Funções serverless da Vercel não
mantêm WebSocket aberto, então essa parte continua sem passar por aqui — mas o
envio e o histórico passam.

## Stack

- Next.js 16 (App Router) — rotas em `app/api/*/route.ts`
- TypeScript
- `@supabase/supabase-js` — cliente que respeita RLS (passa o JWT do usuário)
- CORS via `proxy.ts` (era `middleware.ts` antes do Next 16)

## Setup

```bash
cd C:\pi\api
cp .env.local.example .env.local
# preencha NEXT_PUBLIC_SUPABASE_URL, NEXT_PUBLIC_SUPABASE_ANON_KEY e
# SUPABASE_SERVICE_ROLE_KEY (mesmo projeto Supabase do app/web)
npm run dev -- -p 3001
```

A API sobe em http://localhost:3001.

## Endpoints

Todos retornam JSON. Erros têm o formato `{ "erro": "mensagem" }`.

### Autenticação (públicos)

| Método | Caminho | Body | Resposta |
|---|---|---|---|
| POST | `/api/auth/cadastro` | `{ email, senha, nome, telefone?, urlConfirmacao? }` | `{ usuario: { id, email, confirmacaoEnviada } }` |
| POST | `/api/auth/entrar` | `{ email, senha }` | `{ token, refreshToken, expiraEm, usuario }` |
| POST | `/api/auth/recuperar-senha` | `{ email, urlRedirecionamento? }` | `{ ok: true }` |
| GET | `/api/auth/usuario` | — | `{ usuario }` *(com Authorization)* |

### Pacientes (fisio)

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/pacientes` | Lista pacientes vinculados à fisio logada |
| GET | `/api/pacientes/pendentes` | Profiles role=patient ainda não vinculados |
| POST | `/api/pacientes/vincular` | Vincula um profile como paciente da fisio |
| GET | `/api/pacientes/[id]` | Detalhe (perfil + sessões + exercícios) |

### Paciente (app)

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/meu-paciente` | Devolve seu vínculo + fisio + id da sala de chat |
| GET | `/api/minha-agenda?futuro=true` | Agenda do paciente logado |
| GET | `/api/meus-exercicios` | Exercícios atribuídos ao paciente logado |
| GET | `/api/conteudos-publicados` | Posts/dicas/vídeos publicados |

### Agenda (fisio)

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/agenda` | Lista todos os agendamentos |
| POST | `/api/agenda` | Cria agendamento |
| PATCH | `/api/agenda/[id]` | Atualiza |
| DELETE | `/api/agenda/[id]` | Exclui |

### Exercícios (biblioteca)

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/exercicios` | Lista a biblioteca |
| POST | `/api/exercicios` | Cria (fisio) |
| GET | `/api/exercicios/[id]` | Detalhe |
| PATCH | `/api/exercicios/[id]` | Atualiza (fisio) |
| DELETE | `/api/exercicios/[id]` | Exclui (fisio) |
| POST | `/api/exercicios-atribuidos` | Atribui exercício a um paciente |
| DELETE | `/api/exercicios-atribuidos/[id]` | Desativa atribuição |

### Conteúdo educativo

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/conteudos` | Lista tudo (incluindo rascunhos) — fisio |
| POST | `/api/conteudos` | Cria |
| GET | `/api/conteudos/[id]` | Detalhe |
| PATCH | `/api/conteudos/[id]` | Atualiza |
| DELETE | `/api/conteudos/[id]` | Exclui |

### Chat

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/chat/salas` | Salas da fisio |
| GET | `/api/chat/mensagens?salaId=...` | Histórico |
| POST | `/api/chat/mensagens` | Envia uma mensagem |

> O **realtime** (mensagens novas em tempo real) é direto cliente ↔ Supabase.
> Veja o app Android (`SupabaseRealtimeClient.java`) e o web (`chat-room.tsx`).

## Autenticação

Todos os endpoints (exceto os 3 de auth público) exigem o header:
```
Authorization: Bearer <access_token>
```

O token vem do `/api/auth/entrar` ou do callback de confirmação de email.

A API repassa esse token ao Supabase, que aplica as policies de RLS — quem só é
paciente não consegue ler dados de outros pacientes mesmo que tente.

## Deploy na Vercel

1. Faça push do diretório `api/` (pode ser monorepo ou repo separado)
2. Em vercel.com: **Add New → Project** → seleciona o repo
3. Em "Root Directory" aponta para `api/`
4. Adicione as variáveis de ambiente:
   - `NEXT_PUBLIC_SUPABASE_URL`
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY`
   - `SUPABASE_SERVICE_ROLE_KEY`
5. Deploy. Sua URL será `https://maya-rpg-api.vercel.app` (ou similar)
6. Depois, configure `API_URL` no app Android (`local.properties`) e
   `NEXT_PUBLIC_API_URL` no web para apontar pra essa URL.
