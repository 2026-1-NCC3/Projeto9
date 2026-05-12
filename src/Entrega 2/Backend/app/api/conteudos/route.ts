import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * GET /api/conteudos
 * Lista todos os conteúdos (incluindo rascunhos). Apenas fisio.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  const { supabase } = sessao;
  const { data, error } = await supabase
    .from("educational_content")
    .select("id, title, body, cover_url, video_url, type, category, published, published_at, created_at")
    .order("created_at", { ascending: false });

  if (error) return erro(error.message, 500);

  return sucesso({
    conteudos: (data ?? []).map((c) => ({
      id: c.id,
      titulo: c.title,
      corpo: c.body,
      capaUrl: c.cover_url,
      videoUrl: c.video_url,
      tipo: c.type,
      categoria: c.category,
      publicado: c.published,
      publicadoEm: c.published_at,
      criadoEm: c.created_at,
    })),
  });
}

/**
 * POST /api/conteudos
 * Body: { titulo, corpo?, tipo?, categoria?, publicado? }
 */
export async function POST(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  let body: {
    titulo?: string;
    corpo?: string;
    tipo?: string;
    categoria?: string;
    publicado?: boolean;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  if (!body.titulo) return erro("titulo é obrigatório.");

  const publicado = body.publicado ?? false;
  const { supabase, usuario } = sessao;
  const { data, error } = await supabase
    .from("educational_content")
    .insert({
      title: body.titulo,
      body: body.corpo ?? null,
      type: body.tipo ?? "post",
      category: body.categoria ?? null,
      published: publicado,
      published_at: publicado ? new Date().toISOString() : null,
      created_by: usuario.id,
    })
    .select("id")
    .single();

  if (error) return erro(error.message, 500);
  return sucesso({ id: data!.id }, 201);
}
