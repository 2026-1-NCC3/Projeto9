package com.example.pi_maya.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pi_maya.R;
import com.example.pi_maya.core.util.ValidationUtils;
import com.example.pi_maya.presentation.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL_SENT_TO = "extra_email_sent_to";

    private AuthViewModel viewModel;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private ProgressBar progress;
    private MaterialCardView successBanner;
    private TextView successMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progress = findViewById(R.id.loginProgress);
        successBanner = findViewById(R.id.successBanner);
        successMessage = findViewById(R.id.successMessage);

        loginButton.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.forgotButton).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
        findViewById(R.id.goRegisterButton).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String emailSentTo = intent.getStringExtra(EXTRA_EMAIL_SENT_TO);
        if (emailSentTo != null && !emailSentTo.isEmpty()) {
            successBanner.setVisibility(View.VISIBLE);
            successMessage.setText(getString(R.string.auth_email_sent_body, emailSentTo));
            // Pré-preenche o email pra facilitar o login após confirmação
            emailInput.setText(emailSentTo);
        }
    }

    private void attemptLogin() {
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = textOf(emailInput);
        String password = textOf(passwordInput);

        boolean ok = true;
        if (!ValidationUtils.isEmailValid(email)) {
            emailLayout.setError(getString(R.string.validation_invalid_email));
            ok = false;
        }
        if (!ValidationUtils.isPasswordValid(password)) {
            passwordLayout.setError(getString(R.string.validation_password_short));
            ok = false;
        }
        if (!ok) return;

        setLoading(true);
        viewModel.signIn(email, password).observe(this, resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    goToMain();
                    break;
                case ERROR:
                    setLoading(false);
                    Snackbar.make(loginButton,
                            resource.getMessage() != null ? resource.getMessage() : getString(R.string.error_generic),
                            Snackbar.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
