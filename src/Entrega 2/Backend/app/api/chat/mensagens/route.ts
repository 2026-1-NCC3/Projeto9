import type { NextRequest } from "next/server";
import { exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/chat/mensagens?salaId=...
 * Histórico de mensagens de uma sala. Realtime é DIRETO Supabase ↔ cliente.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const salaId = new URL(req.url).searchParams.get("salaId");
  if (!salaId) return erro("salaId é obrigatório.");

  const { supabase } = sessao;
  const { data } = await supabase
    .from("chat_messages")
    .select("id, room_id, sender_id, content, attachment_url, attachment_type, read_at, created_at")
    .eq("room_id", salaId)
    .order("created_at", { ascending: true })
    .limit(500);

  return sucesso({
    mensagens: (data ?? []).map((m) => ({
      id: m.id,
      salaId: m.room_id,
      remetenteId: m.sender_id,
      conteudo: m.content,
      anexoUrl: m.attachment_url,
      anexoTipo: m.attachment_type,
      lidaEm: m.read_at,
      criadaEm: m.created_at,
    })),
  });
}

/**
 * POST /api/chat/mensagens
 * Body: { salaId, conteudo }
 *
 * Envia uma mensagem. Realtime no Supabase notifica os outros clientes
 * conectados.
 */
export async function POST(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  let body: { salaId?: string; conteudo?: string };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  if (!body.salaId || !body.conteudo?.trim()) {
    return erro("salaId e conteudo são obrigatórios.");
  }

  const { supabase, usuario } = sessao;
  const { data, error } = await supabase
    .from("chat_messages")
    .insert({
      room_id: body.salaId,
      sender_id: usuario.id,
      content: body.conteudo.trim(),
    })
    .select("id, room_id, sender_id, content, created_at")
    .single();

  if (error || !data) return erro(error?.message ?? "Falha ao enviar.", 500);

  return sucesso(
    {
      mensagem: {
        id: data.id,
        salaId: data.room_id,
        remetenteId: data.sender_id,
        conteudo: data.content,
        criadaEm: data.created_at,
      },
    },
    201
  );
}
