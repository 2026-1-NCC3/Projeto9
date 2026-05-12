import type { NextRequest } from "next/server";
import { exigirAutenticacao } from "@/lib/autenticacao";
import { naoAutenticado, sucesso } from "@/lib/respostas";

/**
 * GET /api/auth/usuario
 * Header: Authorization: Bearer <token>
 *
 * Retorna o usuário autenticado. Útil pro app/web validar token.
 */
export async function GET(req: NextRequest) {
  const sessao = await exigirAutenticacao(req);
  if (sessao.erro) return naoAutenticado();

  return sucesso({ usuario: sessao.usuario });
}
