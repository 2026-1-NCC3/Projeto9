import type { NextRequest } from "next/server";
import { exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/minha-agenda
 * Endpoint do app paciente — devolve os próprios agendamentos.
 *
 * Query opcional:
 *   ?futuro=true → só os a partir de agora
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const { supabase, usuario } = sessao;

  // descobre o patient_id do usuário logado
  const { data: paciente } = await supabase
    .from("patients")
    .select("id")
    .eq("profile_id", usuario.id)
    .maybeSingle();

  if (!paciente) return sucesso({ agendamentos: [] });

  const url = new URL(req.url);
  const futuro = url.searchParams.get("futuro") === "true";

  let q = supabase
    .from("appointments")
    .select("id, starts_at, ends_at, status, notes, therapist_id")
    .eq("patient_id", paciente.id)
    .order("starts_at", { ascending: true });

  if (futuro) {
    q = q.gte("starts_at", new Date().toISOString());
  }

  const { data } = await q;

  return sucesso({
    agendamentos: (data ?? []).map((a) => ({
      id: a.id,
      inicioEm: a.starts_at,
      fimEm: a.ends_at,
      status: a.status,
      observacoes: a.notes,
      fisioId: a.therapist_id,
    })),
  });
}
