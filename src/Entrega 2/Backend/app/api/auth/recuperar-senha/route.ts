import type { NextRequest } from "next/server";
import { supabaseAnonimo } from "@/lib/supabase";
import { erro, sucesso } from "@/lib/respostas";

/**
 * POST /api/auth/recuperar-senha
 * Body: { email, urlRedirecionamento? }
 *
 * Envia o link de redefinição. Sempre devolve 200 (não revela se o email existe).
 */
export async function POST(req: NextRequest) {
  let body: { email?: string; urlRedirecionamento?: string };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  const email = body.email?.trim();
  if (!email) return erro("email é obrigatório.");

  const supabase = supabaseAnonimo();
  await supabase.auth.resetPasswordForEmail(email, {
    redirectTo: body.urlRedirecionamento,
  });

  return sucesso({ ok: true });
}
