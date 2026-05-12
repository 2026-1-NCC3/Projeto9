import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * PATCH /api/agenda/[id]
 * Body: { inicioEm?, fimEm?, status?, observacoes? }
 */
export async function PATCH(
  req: NextRequest,
  ctx: { params: Promise<{ id: string }> }
) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { id } = await ctx.params;
  let body: {
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

  const { supabase, usuario } = sessao;
  const update: Record<string, unknown> = {};
  if (body.inicioEm !== undefined) update.starts_at = body.inicioEm;
  if (body.fimEm !== undefined) update.ends_at = body.fimEm;
  if (body.status !== undefined) update.status = body.status;
  if (body.observacoes !== undefined) update.notes = body.observacoes;

  const { error } = await supabase
    .from("appointments")
    .update(update)
    .eq("id", id)
    .eq("therapist_id", usuario.id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}

/**
 * DELETE /api/agenda/[id]
 */
export async function DELETE(
  req: NextRequest,
  ctx: { params: Promise<{ id: string }> }
) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { id } = await ctx.params;
  const { supabase, usuario } = sessao;

  const { error } = await supabase
    .from("appointments")
    .delete()
    .eq("id", id)
    .eq("therapist_id", usuario.id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}
