import { createClient } from "@supabase/supabase-js";

/**
 * Cliente Supabase autenticado com o token JWT do usuário.
 * Usado para que as queries respeitem o RLS do Postgres.
 */
export function supabaseDoUsuario(accessToken: string) {
  return createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      global: {
        headers: { Authorization: `Bearer ${accessToken}` },
      },
      auth: {
        persistSession: false,
        autoRefreshToken: false,
      },
    }
  );
}

/**
 * Cliente Supabase com a anon key (sem usuário).
 * Para chamar funções de Auth (signUp, signIn, recover) que não exigem token.
 */
export function supabaseAnonimo() {
  return createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      auth: {
        persistSession: false,
        autoRefreshToken: false,
      },
    }
  );
}

/**
 * Cliente Supabase ADMIN (service_role) — bypassa RLS.
 * Usar com muito cuidado e só em casos de manutenção.
 */
export function supabaseAdmin() {
  return createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.SUPABASE_SERVICE_ROLE_KEY!,
    {
      auth: {
        persistSession: false,
        autoRefreshToken: false,
      },
    }
  );
}
