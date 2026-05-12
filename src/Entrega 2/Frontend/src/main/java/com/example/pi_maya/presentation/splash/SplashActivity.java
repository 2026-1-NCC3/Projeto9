package com.example.pi_maya.presentation.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.presentation.auth.LoginActivity;
import com.example.pi_maya.presentation.main.MainActivity;
import com.example.pi_maya.presentation.onboarding.OnboardingActivity;

/**
 * Tela inicial. Decide para onde mandar o usuário:
 *  - Onboarding não visto -> OnboardingActivity
 *  - Sem sessão -> LoginActivity
 *  - Com sessão -> MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MIN_MS = 800L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_MIN_MS);
    }

    private void route() {
        SessionManager session = MayaApp.get().getSessionManager();

        Intent intent;
        if (!session.isOnboardingDone()) {
            intent = new Intent(this, OnboardingActivity.class);
        } else if (!session.isLoggedIn()) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
