import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * GET /api/pacientes/pendentes
 * Lista profiles role=patient que ainda não foram vinculados a nenhuma fisio.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { supabase } = sessao;

  const [{ data: profiles }, { data: vinculados }] = await Promise.all([
    supabase
      .from("profiles")
      .select("id, full_name, phone, avatar_url, created_at")
      .eq("role", "patient")
      .order("created_at", { ascending: false }),
    supabase.from("patients").select("profile_id"),
  ]);

  const idsVinculados = new Set((vinculados ?? []).map((p) => p.profile_id));
  const pendentes = (profiles ?? [])
    .filter((p) => !idsVinculados.has(p.id))
    .map((p) => ({
      id: p.id,
      nome: p.full_name,
      telefone: p.phone,
      avatarUrl: p.avatar_url,
      criadoEm: p.created_at,
    }));

  return sucesso({ pendentes });
}
