package com.example.pi_maya.core.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Gerencia tokens e dados de sessão usando EncryptedSharedPreferences.
 * Tudo é criptografado em repouso pelo Android Keystore.
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "maya_secure_prefs";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_TOKEN_EXPIRES_AT = "token_expires_at";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_LGPD_CONSENT_GRANTED = "lgpd_consent_granted";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        SharedPreferences temp;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            temp = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Falha ao criar EncryptedSharedPreferences, usando fallback", e);
            temp = context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE);
        }
        this.prefs = temp;
    }

    public void saveSession(String accessToken, String refreshToken, long expiresAtSeconds,
                            String userId, String email, String fullName) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_TOKEN_EXPIRES_AT, expiresAtSeconds)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, fullName)
                .apply();
    }

    public void updateTokens(String accessToken, String refreshToken, long expiresAtSeconds) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_TOKEN_EXPIRES_AT, expiresAtSeconds)
                .apply();
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRES_AT)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public long getTokenExpiresAt() {
        return prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0L);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }

    public boolean isTokenExpired() {
        long now = System.currentTimeMillis() / 1000L;
        long expiresAt = getTokenExpiresAt();
        // Considera expirado 60s antes do prazo real para evitar race conditions
        return expiresAt > 0 && now >= (expiresAt - 60);
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
    }

    public boolean isLgpdConsentGranted() {
        return prefs.getBoolean(KEY_LGPD_CONSENT_GRANTED, false);
    }

    public void setLgpdConsentGranted(boolean granted) {
        prefs.edit().putBoolean(KEY_LGPD_CONSENT_GRANTED, granted).apply();
    }
}
