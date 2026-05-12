import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * POST /api/exercicios-atribuidos
 * Body: { pacienteId, exercicioId, series?, repeticoes?, frequenciaSemanal?, observacoes? }
 */
export async function POST(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  let body: {
    pacienteId?: string;
    exercicioId?: string;
    series?: number;
    repeticoes?: number;
    frequenciaSemanal?: number;
    observacoes?: string;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  if (!body.pacienteId || !body.exercicioId) {
    return erro("pacienteId e exercicioId são obrigatórios.");
  }

  const { supabase, usuario } = sessao;
  const { data, error } = await supabase
    .from("exercise_assignments")
    .insert({
      patient_id: body.pacienteId,
      exercise_id: body.exercicioId,
      assigned_by: usuario.id,
      target_sets: body.series ?? null,
      target_repetitions: body.repeticoes ?? null,
      frequency_per_week: body.frequenciaSemanal ?? null,
      notes: body.observacoes ?? null,
      start_date: new Date().toISOString().slice(0, 10),
      active: true,
    })
    .select("id")
    .single();

  if (error) return erro(error.message, 500);
  return sucesso({ id: data!.id }, 201);
}
