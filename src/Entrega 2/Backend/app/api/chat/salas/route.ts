import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/chat/salas
 * Lista as salas de chat da fisio logada.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return sucesso({ salas: [] });

  const { supabase, usuario } = sessao;

  const [{ data: salas }, { data: pacientes }, { data: profiles }] =
    await Promise.all([
      supabase
        .from("chat_rooms")
        .select("id, patient_id, last_message_at")
        .eq("therapist_id", usuario.id)
        .order("last_message_at", { ascending: false, nullsFirst: false }),
      supabase
        .from("patients")
        .select("id, profile_id")
        .eq("therapist_id", usuario.id),
      supabase.from("profiles").select("id, full_name").eq("role", "patient"),
    ]);

  const nomePorProfile = new Map<string, string>();
  for (const p of profiles ?? []) nomePorProfile.set(p.id, p.full_name);
  const nomePorPaciente = new Map<string, string>();
  for (const p of pacientes ?? []) {
    nomePorPaciente.set(p.id, nomePorProfile.get(p.profile_id) ?? "Paciente");
  }

  return sucesso({
    salas: (salas ?? []).map((s) => ({
      id: s.id,
      pacienteId: s.patient_id,
      pacienteNome: nomePorPaciente.get(s.patient_id) ?? "Paciente",
      ultimaMensagemEm: s.last_message_at,
    })),
  });
}
