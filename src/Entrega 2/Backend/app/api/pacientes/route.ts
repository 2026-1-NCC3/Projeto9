import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * GET /api/pacientes
 * Lista os pacientes vinculados à fisio logada.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { supabase, usuario } = sessao;

  const [{ data: pacientes }, { data: profiles }] = await Promise.all([
    supabase
      .from("patients")
      .select("id, profile_id, primary_complaint, status, created_at")
      .eq("therapist_id", usuario.id)
      .order("created_at", { ascending: false }),
    supabase
      .from("profiles")
      .select("id, full_name, phone, avatar_url")
      .eq("role", "patient"),
  ]);

  const perfilPorId = new Map<string, { id: string; full_name: string; phone: string | null; avatar_url: string | null }>();
  for (const p of profiles ?? []) perfilPorId.set(p.id, p);

  const lista = (pacientes ?? []).map((p) => ({
    id: p.id,
    profileId: p.profile_id,
    queixaPrincipal: p.primary_complaint,
    status: p.status,
    criadoEm: p.created_at,
    nome: perfilPorId.get(p.profile_id)?.full_name ?? null,
    telefone: perfilPorId.get(p.profile_id)?.phone ?? null,
    avatarUrl: perfilPorId.get(p.profile_id)?.avatar_url ?? null,
  }));

  return sucesso({ pacientes: lista });
}
