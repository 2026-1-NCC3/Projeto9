import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * DELETE /api/exercicios-atribuidos/[id]
 * Desativa um exercício atribuído (soft delete via active=false).
 */
export async function DELETE(
  req: NextRequest,
  ctx: { params: Promise<{ id: string }> }
) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { id } = await ctx.params;
  const { supabase } = sessao;

  const { error } = await supabase
    .from("exercise_assignments")
    .update({ active: false })
    .eq("id", id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}
