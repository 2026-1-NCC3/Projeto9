import type { NextRequest } from "next/server";
import { exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/meus-exercicios
 * Endpoint do APP do paciente — retorna seus exercícios atribuídos ativos.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const { supabase, usuario } = sessao;

  const { data: paciente } = await supabase
    .from("patients")
    .select("id")
    .eq("profile_id", usuario.id)
    .maybeSingle();

  if (!paciente) return sucesso({ exercicios: [] });

  const { data: atribuicoes } = await supabase
    .from("exercise_assignments")
    .select(
      "id, exercise_id, target_repetitions, target_sets, frequency_per_week, notes"
    )
    .eq("patient_id", paciente.id)
    .eq("active", true);

  const ids = (atribuicoes ?? []).map((a) => a.exercise_id).filter(Boolean);
  let exsPorId = new Map<
    string,
    {
      id: string;
      title: string;
      description: string | null;
      instructions: string | null;
      category: string | null;
      difficulty: number | null;
      duration_seconds: number | null;
      video_url: string | null;
      thumbnail_url: string | null;
    }
  >();
  if (ids.length) {
    const { data: exs } = await supabase
      .from("exercises")
      .select(
        "id, title, description, instructions, category, difficulty, duration_seconds, video_url, thumbnail_url"
      )
      .in("id", ids);
    for (const e of exs ?? []) exsPorId.set(e.id, e);
  }

  return sucesso({
    exercicios: (atribuicoes ?? []).map((a) => {
      const e = exsPorId.get(a.exercise_id);
      return {
        atribuicaoId: a.id,
        exercicioId: a.exercise_id,
        titulo: e?.title ?? "Exercício",
        descricao: e?.description ?? null,
        instrucoes: e?.instructions ?? null,
        categoria: e?.category ?? null,
        dificuldade: e?.difficulty ?? null,
        duracaoSegundos: e?.duration_seconds ?? null,
        videoUrl: e?.video_url ?? null,
        thumbnailUrl: e?.thumbnail_url ?? null,
        series: a.target_sets,
        repeticoes: a.target_repetitions,
        frequenciaSemanal: a.frequency_per_week,
        observacoes: a.notes,
      };
    }),
  });
}
