/**
 * Helpers para padronizar respostas dos endpoints.
 */
import { NextResponse } from "next/server";

export function sucesso<T>(dados: T, status = 200) {
  return NextResponse.json(dados, { status });
}

export function erro(mensagem: string, status = 400) {
  return NextResponse.json({ erro: mensagem }, { status });
}

export function naoAutenticado() {
  return erro("Não autenticado.", 401);
}

export function semPermissao() {
  return erro("Sem permissão.", 403);
}

export function naoEncontrado() {
  return erro("Não encontrado.", 404);
}
