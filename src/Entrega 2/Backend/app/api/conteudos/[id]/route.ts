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
 * GET /api/conteudos/[id]
 * Qualquer usuário pode ler (no caso de já estar publicado, RLS aprova).
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
    .from("educational_content")
    .select("id, title, body, cover_url, video_url, type, category, published, published_at")
    .eq("id", id)
    .maybeSingle();

  if (!data) return naoEncontrado();

  return sucesso({
    conteudo: {
      id: data.id,
      titulo: data.title,
      corpo: data.body,
      capaUrl: data.cover_url,
      videoUrl: data.video_url,
      tipo: data.type,
      categoria: data.category,
      publicado: data.published,
      publicadoEm: data.published_at,
    },
  });
}

/**
 * PATCH /api/conteudos/[id]
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
    corpo?: string | null;
    tipo?: string;
    categoria?: string | null;
    publicado?: boolean;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  const update: Record<string, unknown> = {};
  if (body.titulo !== undefined) update.title = body.titulo;
  if (body.corpo !== undefined) update.body = body.corpo;
  if (body.tipo !== undefined) update.type = body.tipo;
  if (body.categoria !== undefined) update.category = body.categoria;
  if (body.publicado !== undefined) {
    update.published = body.publicado;
    update.published_at = body.publicado ? new Date().toISOString() : null;
  }

  const { supabase } = sessao;
  const { error } = await supabase
    .from("educational_content")
    .update(update)
    .eq("id", id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}

/**
 * DELETE /api/conteudos/[id]
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
    .from("educational_content")
    .delete()
    .eq("id", id);

  if (error) return erro(error.message, 500);
  return sucesso({ ok: true });
}
