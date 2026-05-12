import type { NextRequest } from "next/server";
import { supabaseAnonimo } from "@/lib/supabase";
import { erro, sucesso } from "@/lib/respostas";

/**
 * POST /api/auth/entrar
 * Body: { email, senha }
 *
 * Retorna { token, refreshToken, expiraEm, usuario }
 */
export async function POST(req: NextRequest) {
  let body: { email?: string; senha?: string };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  const email = body.email?.trim();
  const senha = body.senha;

  if (!email || !senha) {
    return erro("email e senha são obrigatórios.");
  }

  const supabase = supabaseAnonimo();
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password: senha,
  });

  if (error || !data.session || !data.user) {
    if (error?.message.includes("Invalid login")) {
      return erro("E-mail ou senha incorretos.", 401);
    }
    if (error?.message.includes("Email not confirmed")) {
      return erro("Confirme seu e-mail antes de entrar.", 401);
    }
    return erro(error?.message ?? "Não foi possível entrar.", 401);
  }

  return sucesso({
    token: data.session.access_token,
    refreshToken: data.session.refresh_token,
    expiraEm: data.session.expires_at,
    usuario: {
      id: data.user.id,
      email: data.user.email,
      nome:
        (data.user.user_metadata?.full_name as string | undefined) ?? null,
    },
  });
}
