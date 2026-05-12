import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * GET /api/agenda
 * Lista todos os agendamentos da fisio logada.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { supabase, usuario } = sessao;

  const { data, error } = await supabase
    .from("appointments")
    .select("id, patient_id, starts_at, ends_at, status, notes")
    .eq("therapist_id", usuario.id)
    .order("starts_at", { ascending: false });

  if (error) return erro(error.message, 500);

  return sucesso({
    agendamentos: (data ?? []).map((a) => ({
      id: a.id,
      pacienteId: a.patient_id,
      inicioEm: a.starts_at,
      fimEm: a.ends_at,
      status: a.status,
      observacoes: a.notes,
    })),
  });
}

/**
 * POST /api/agenda
 * Body: { pacienteId, inicioEm, fimEm, status?, observacoes? }
 */
export async function POST(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  let body: {
    pacienteId?: string;
    inicioEm?: string;
    fimEm?: string;
    status?: string;
    observacoes?: string | null;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  if (!body.pacienteId || !body.inicioEm || !body.fimEm) {
    return erro("pacienteId, inicioEm e fimEm são obrigatórios.");
  }

  const { supabase, usuario } = sessao;
  const { data, error } = await supabase
    .from("appointments")
    .insert({
      patient_id: body.pacienteId,
      therapist_id: usuario.id,
      starts_at: body.inicioEm,
      ends_at: body.fimEm,
      status: body.status ?? "scheduled",
      notes: body.observacoes ?? null,
    })
    .select("id")
    .single();

  if (error) return erro(error.message, 500);
  return sucesso({ id: data!.id }, 201);
}
