import type { NextRequest } from "next/server";
import { ehTerapeuta, exigirAutenticacao } from "@/lib/autenticacao";
import { erro, naoAutenticado, semPermissao, sucesso } from "@/lib/respostas";

/**
 * GET /api/exercicios
 * Lista a biblioteca de exercícios. Qualquer usuário autenticado pode ler.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  const { supabase } = sessao;
  const { data, error } = await supabase
    .from("exercises")
    .select(
      "id, title, description, instructions, category, difficulty, duration_seconds, video_url, thumbnail_url, created_at"
    )
    .order("created_at", { ascending: false });

  if (error) return erro(error.message, 500);

  return sucesso({
    exercicios: (data ?? []).map((e) => ({
      id: e.id,
      titulo: e.title,
      descricao: e.description,
      instrucoes: e.instructions,
      categoria: e.category,
      dificuldade: e.difficulty,
      duracaoSegundos: e.duration_seconds,
      videoUrl: e.video_url,
      thumbnailUrl: e.thumbnail_url,
      criadoEm: e.created_at,
    })),
  });
}

/**
 * POST /api/exercicios
 * Body: { titulo, descricao?, instrucoes?, categoria?, dificuldade?, duracaoSegundos? }
 */
export async function POST(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();
  if (!ehTerapeuta(sessao.usuario)) return semPermissao();

  let body: {
    titulo?: string;
    descricao?: string;
    instrucoes?: string;
    categoria?: string;
    dificuldade?: number;
    duracaoSegundos?: number;
  };
  try {
    body = await req.json();
  } catch {
    return erro("Body inválido.");
  }

  if (!body.titulo) return erro("titulo é obrigatório.");

  const { supabase, usuario } = sessao;
  const { data, error } = await supabase
    .from("exercises")
    .insert({
      title: body.titulo,
      description: body.descricao ?? null,
      instructions: body.instrucoes ?? null,
      category: body.categoria ?? null,
      difficulty: body.dificuldade ?? null,
      duration_seconds: body.duracaoSegundos ?? null,
      created_by: usuario.id,
    })
    .select("id")
    .single();

  if (error) return erro(error.message, 500);
  return sucesso({ id: data!.id }, 201);
}
