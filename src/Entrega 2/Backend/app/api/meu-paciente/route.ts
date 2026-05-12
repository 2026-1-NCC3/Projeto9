import type { NextRequest } from "next/server";
import { exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/meu-paciente
 * Endpoint que o APP do paciente chama pra saber seu próprio vínculo:
 *   - id do registro em patients
 *   - dados da fisio
 *   - id da sala de chat
 *
 * Se ainda não foi vinculado, devolve { paciente: null }.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const { supabase, usuario } = sessao;

  const { data: paciente } = await supabase
    .from("patients")
    .select("id, therapist_id, primary_complaint, status, created_at")
    .eq("profile_id", usuario.id)
    .maybeSingle();

  if (!paciente) {
    return sucesso({ paciente: null });
  }

  const [{ data: fisio }, { data: sala }] = await Promise.all([
    supabase
      .from("profiles")
      .select("id, full_name, avatar_url, phone")
      .eq("id", paciente.therapist_id)
      .maybeSingle(),
    supabase
      .from("chat_rooms")
      .select("id")
      .eq("patient_id", paciente.id)
      .maybeSingle(),
  ]);

  return sucesso({
    paciente: {
      id: paciente.id,
      queixaPrincipal: paciente.primary_complaint,
      status: paciente.status,
      criadoEm: paciente.created_at,
      fisio: fisio
        ? { id: fisio.id, nome: fisio.full_name, avatarUrl: fisio.avatar_url, telefone: fisio.phone }
        : null,
      salaChat: sala?.id ?? null,
    },
  });
}
