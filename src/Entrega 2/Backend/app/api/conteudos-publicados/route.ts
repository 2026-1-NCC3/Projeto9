import type { NextRequest } from "next/server";
import { exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/conteudos-publicados
 * Endpoint do APP — só os conteúdos publicados.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const { supabase } = sessao;
  const { data } = await supabase
    .from("educational_content")
    .select("id, title, body, cover_url, video_url, type, category, published_at")
    .eq("published", true)
    .order("published_at", { ascending: false });

  return sucesso({
    conteudos: (data ?? []).map((c) => ({
      id: c.id,
      titulo: c.title,
      corpo: c.body,
      capaUrl: c.cover_url,
      videoUrl: c.video_url,
      tipo: c.type,
      categoria: c.category,
      publicadoEm: c.published_at,
    })),
  });
}
