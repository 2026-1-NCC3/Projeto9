import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import {
  naoAutenticado,
  naoEncontrado,
  semPermissao,
  sucesso,
} from "@/lib/respostas";

/**
 * GET /api/pacientes/[id]
 * Detalhes completos de um paciente da fisio logada (perfil + sessões + atribuições).
 */
export async function GET(
  req: NextRequest,
  ctx: { params: Promise<{ id: string }> }
) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { id } = await ctx.params;
  const { supabase, usuario } = sessao;

  const { data: paciente } = await supabase
    .from("patients")
    .select("id, profile_id, primary_complaint, medical_history, status, created_at")
    .eq("id", id)
    .eq("therapist_id", usuario.id)
    .maybeSingle();

  if (!paciente) return naoEncontrado();

  const [
    { data: profile },
    { data: agendamentos },
    { data: atribuicoes },
    { data: sessoesClinicas },
  ] = await Promise.all([
    supabase
      .from("profiles")
      .select("id, full_name, phone, birth_date, avatar_url")
      .eq("id", paciente.profile_id)
      .maybeSingle(),
    supabase
      .from("appointments")
      .select("id, starts_at, ends_at, status, notes")
      .eq("patient_id", id)
      .order("starts_at", { ascending: false })
      .limit(20),
    supabase
      .from("exercise_assignments")
      .select(
        "id, exercise_id, target_repetitions, target_sets, frequency_per_week, notes, active"
      )
      .eq("patient_id", id)
      .eq("active", true),
    supabase
      .from("clinical_sessions")
      .select(
        "id, evolution_notes, observations, pain_level_before, pain_level_after, created_at"
      )
      .eq("patient_id", id)
      .order("created_at", { ascending: false })
      .limit(20),
  ]);

  // resolve títulos dos exercícios atribuídos
  const idsExerc = (atribuicoes ?? []).map((a) => a.exercise_id).filter(Boolean);
  let exerciciosPorId = new Map<string, { id: string; title: string; category: string | null; difficulty: number | null }>();
  if (idsExerc.length) {
    const { data: exs } = await supabase
      .from("exercises")
      .select("id, title, category, difficulty")
      .in("id", idsExerc);
    for (const e of exs ?? []) exerciciosPorId.set(e.id, e);
  }

  return sucesso({
    paciente: {
      id: paciente.id,
      nome: profile?.full_name ?? null,
      telefone: profile?.phone ?? null,
      nascimento: profile?.birth_date ?? null,
      queixaPrincipal: paciente.primary_complaint,
      historicoMedico: paciente.medical_history,
      status: paciente.status,
      criadoEm: paciente.created_at,
    },
    agendamentos: (agendamentos ?? []).map((a) => ({
      id: a.id,
      inicioEm: a.starts_at,
      fimEm: a.ends_at,
      status: a.status,
      observacoes: a.notes,
    })),
    exerciciosAtivos: (atribuicoes ?? []).map((a) => {
      const e = exerciciosPorId.get(a.exercise_id);
      return {
        id: a.id,
        exercicioId: a.exercise_id,
        titulo: e?.title ?? "Exercício",
        categoria: e?.category ?? null,
        series: a.target_sets,
        repeticoes: a.target_repetitions,
        frequenciaSemanal: a.frequency_per_week,
        observacoes: a.notes,
      };
    }),
    sessoesClinicas: (sessoesClinicas ?? []).map((s) => ({
      id: s.id,
      evolucao: s.evolution_notes,
      observacoes: s.observations,
      dorAntes: s.pain_level_before,
      dorDepois: s.pain_level_after,
      criadoEm: s.created_at,
    })),
  });
}
