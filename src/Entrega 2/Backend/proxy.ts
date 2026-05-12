import { NextResponse, type NextRequest } from "next/server";

/**
 * Next.js 16: era "middleware.ts", virou "proxy.ts" com função "proxy".
 *
 * Aqui adicionamos CORS headers para que o app Android e o web (em outro
 * domínio Vercel) consigam chamar a API.
 */
export function proxy(request: NextRequest) {
  // Trata preflight OPTIONS antes de qualquer coisa
  if (request.method === "OPTIONS") {
    return new NextResponse(null, {
      status: 204,
      headers: corsHeaders(),
    });
  }

  const resposta = NextResponse.next();
  for (const [k, v] of Object.entries(corsHeaders())) {
    resposta.headers.set(k, v);
  }
  return resposta;
}

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET,POST,PATCH,PUT,DELETE,OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
    "Access-Control-Max-Age": "86400",
  };
}

export const config = {
  matcher: "/api/:path*",
};
