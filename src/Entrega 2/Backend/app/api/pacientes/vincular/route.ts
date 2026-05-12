import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * POST /api/pacientes/vincular
 * Body: { profileId, queixaPrincipal? }
 *
 * Vincula um profile como paciente da fisio logada. Cria também a sala de chat.
 */
export async function POST(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  let body: { profileId?: string; queixaPrincipal?: string };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  if (!body.profileId) return erro("profileId é obrigatório.");

  const { supabase, usuario } = sessao;

  const { data: paciente, error } = await supabase
    .from("patients")
    .insert({
      profile_id: body.profileId,
      therapist_id: usuario.id,
      primary_complaint: body.queixaPrincipal?.trim() || null,
      status: "active",
    })
    .select("id")
    .single();

  if (error || !paciente) {
    if (error?.message.includes("duplicate")) {
      return erro("Esse paciente já está vinculado.", 409);
    }
    return erro(error?.message ?? "Falha ao vincular.", 500);
  }

  // Cria sala de chat — não bloqueia se falhar
  await supabase.from("chat_rooms").insert({
    patient_id: paciente.id,
    therapist_id: usuario.id,
  });

  return sucesso({ pacienteId: paciente.id }, 201);
}
