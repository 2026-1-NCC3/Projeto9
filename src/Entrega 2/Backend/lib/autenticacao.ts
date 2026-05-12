import type { NextRequest } from "next/server";
import { supabaseDoUsuario } from "./supabase";

export type UsuarioAutenticado = {
  id: string;
  email: string | null;
  papel: "patient" | "therapist" | "admin";
  nome: string | null;
};

/**
 * Lê o token do header Authorization: Bearer ..., valida com o Supabase,
 * busca o profile correspondente e devolve o usuário autenticado.
 *
 * Retorna null se não houver token válido.
 */
export async function autenticarRequisicao(
  request: NextRequest
): Promise<{ usuario: UsuarioAutenticado; supabase: ReturnType<typeof supabaseDoUsuario> } | null> {
  const auth = request.headers.get("authorization") ?? request.headers.get("Authorization");
  if (!auth?.startsWith("Bearer ")) return null;

  const token = auth.slice("Bearer ".length).trim();
  if (!token) return null;

  const supabase = supabaseDoUsuario(token);

  const {
    data: { user },
    error,
  } = await supabase.auth.getUser();

  if (error || !user) return null;

  const { data: profile } = await supabase
    .from("profiles")
    .select("id, role, full_name")
    .eq("id", user.id)
    .maybeSingle();

  return {
    supabase,
    usuario: {
      id: user.id,
      email: user.email ?? null,
      papel: (profile?.role as UsuarioAutenticado["papel"]) ?? "patient",
      nome: profile?.full_name ?? null,
    },
  };
}

/**
 * Atalho que retorna 401 se não autenticado.
 * Use no início de cada endpoint protegido.
 */
export async function exigirAutenticacao(request: NextRequest) {
  const sessao = await autenticarRequisicao(request);
  if (!sessao) {
    return { erro: true as const };
  }
  return { erro: false as const, ...sessao };
}

export function ehTerapeuta(usuario: UsuarioAutenticado): boolean {
  return usuario.papel === "therapist" || usuario.papel === "admin";
}
