import type { NextRequest } from "next/server";
import { supabaseAnonimo } from "@/lib/supabase";
import { erro, sucesso } from "@/lib/respostas";

/**
 * POST /api/auth/cadastro
 * Body: { email, senha, nome, telefone?, urlConfirmacao? }
 *
 * Cria um usuário no Supabase Auth. O profile é criado automaticamente
 * pela trigger handle_new_user. O usuário ainda precisa confirmar o email.
 */
export async function POST(req: NextRequest) {
  let body: {
    email?: string;
    senha?: string;
    nome?: string;
    telefone?: string;
    urlConfirmacao?: string;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  const email = body.email?.trim();
  const senha = body.senha;
  const nome = body.nome?.trim();

  if (!email || !senha || !nome) {
    return erro("email, senha e nome são obrigatórios.");
  }
  if (senha.length < 8) {
    return erro("Senha deve ter ao menos 8 caracteres.");
  }

  const supabase = supabaseAnonimo();
  const { data, error } = await supabase.auth.signUp({
    email,
    password: senha,
    options: {
      data: {
        full_name: nome,
        phone: body.telefone ?? null,
      },
      emailRedirectTo: body.urlConfirmacao,
    },
  });

  if (error) {
    return erro(traduzirErroAuth(error.message), 400);
  }

  return sucesso({
    usuario: data.user
      ? {
          id: data.user.id,
          email: data.user.email,
          confirmacaoEnviada: !data.session, // sem session = precisa confirmar email
        }
      : null,
  });
}

function traduzirErroAuth(msg: string): string {
  if (msg.includes("User already registered")) return "Este e-mail já está cadastrado.";
  if (msg.includes("Password should be")) return "Senha muito curta.";
  if (msg.includes("rate limit")) return "Muitas tentativas. Aguarde um pouco.";
  return msg;
}
