import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import {
  erro,
  naoAutenticado,
  naoEncontrado,
  semPermissao,
  sucesso,
} from "@/lib/respostas";

/**
 * GET /api/exercicios/[id]
 */
export async function GET(
  req: NextRequest,
  ctx: { params: Promise<{ id: string }> }
) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const { id } = await ctx.params;
  const { supabase } = sessao;

  const { data } = await supabase
    .from("exercises")
    .select(
      "id, title, description, instructions, category, difficulty, duration_seconds, video_url, thumbnail_url"
    )
    .eq("id", id)
    .maybeSingle();

  if (!data) return naoEncontrado();

  return sucesso({
    exercicio: {
      id: data.id,
      titulo: data.title,
      descricao: data.description,
      instrucoes: data.instructions,
      categoria: data.category,
      dificuldade: data.difficulty,
      duracaoSegundos: data.duration_seconds,
      videoUrl: data.video_url,
      thumbnailUrl: data.thumbnail_url,
    },
  });
}

/**
 * PATCH /api/exercicios/[id]
 * Body: campos opcionais (todos)
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
    titulo?: string;
    descricao?: string | null;
    instrucoes?: string | null;
    categoria?: string | null;
    dificuldade?: number | null;
    duracaoSegundos?: number | null;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  const update: Record<string, unknown> = {};
  if (body.titulo !== undefined) update.title = body.titulo;
  if (body.descricao !== undefined) update.description = body.descricao;
  if (body.instrucoes !== undefined) update.instructions = body.instrucoes;
  if (body.categoria !== undefined) update.category = body.categoria;
  if (body.dificuldade !== undefined) update.difficulty = body.dificuldade;
  if (body.duracaoSegundos !== undefined)
    update.duration_seconds = body.duracaoSegundos;

  const { supabase } = sessao;
  const { error } = await supabase.from("exercises").update(update).eq("id", id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}

/**
 * DELETE /api/exercicios/[id]
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
  const { error } = await supabase.from("exercises").delete().eq("id", id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}
