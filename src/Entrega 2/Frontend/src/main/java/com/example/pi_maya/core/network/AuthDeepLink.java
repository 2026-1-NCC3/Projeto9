package com.example.pi_maya.core.network;

/**
 * Configuração do deep link de autenticação.
 *
 * Este URI deve estar cadastrado em:
 *   Supabase Dashboard > Authentication > URL Configuration > Redirect URLs
 *
 * O esquema "pi-maya" também precisa estar declarado no AndroidManifest
 * (ver intent-filter da AuthCallbackActivity).
 */
public final class AuthDeepLink {

    public static final String SCHEME = "pi-maya";
    public static final String HOST = "auth";
    public static final String PATH = "/callback";

    public static final String REDIRECT_URL = SCHEME + "://" + HOST + PATH;

    private AuthDeepLink() {}
}
