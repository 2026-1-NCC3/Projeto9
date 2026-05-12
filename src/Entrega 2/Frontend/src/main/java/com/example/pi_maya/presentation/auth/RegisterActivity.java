package com.example.pi_maya.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pi_maya.R;
import com.example.pi_maya.core.util.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    private TextInputLayout nameLayout, emailLayout, phoneLayout, passwordLayout;
    private TextInputEditText nameInput, emailInput, phoneInput, passwordInput;
    private MaterialCheckBox lgpdCheckbox;
    private MaterialButton registerButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);

        lgpdCheckbox = findViewById(R.id.lgpdCheckbox);
        registerButton = findViewById(R.id.registerButton);
        progress = findViewById(R.id.registerProgress);

        registerButton.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.goLoginButton).setOnClickListener(v -> finish());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        passwordLayout.setError(null);

        String name = textOf(nameInput);
        String email = textOf(emailInput);
        String phone = textOf(phoneInput);
        String password = textOf(passwordInput);

        boolean ok = true;
        if (!ValidationUtils.isNameValid(name)) {
            nameLayout.setError(getString(R.string.validation_required));
            ok = false;
        }
        if (!ValidationUtils.isEmailValid(email)) {
            emailLayout.setError(getString(R.string.validation_invalid_email));
            ok = false;
        }
        if (!ValidationUtils.isPhoneValid(phone)) {
            phoneLayout.setError(getString(R.string.validation_required));
            ok = false;
        }
        if (!ValidationUtils.isPasswordValid(password)) {
            passwordLayout.setError(getString(R.string.validation_password_short));
            ok = false;
        }
        if (!lgpdCheckbox.isChecked()) {
            Snackbar.make(registerButton, R.string.validation_must_accept_terms,
                    Snackbar.LENGTH_LONG).show();
            ok = false;
        }
        if (!ok) return;

        setLoading(true);
        viewModel.signUp(email, password, name, phone, true)
                .observe(this, resource -> {
                    switch (resource.getStatus()) {
                        case LOADING:
                            setLoading(true);
                            break;
                        case SUCCESS:
                            setLoading(false);
                            goToLoginWithSuccess(email);
                            break;
                        case ERROR:
                            setLoading(false);
                            Snackbar.make(registerButton,
                                    resource.getMessage() != null ? resource.getMessage()
                                            : getString(R.string.error_generic),
                                    Snackbar.LENGTH_LONG).show();
                            break;
                    }
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
    }

    /**
     * Após cadastro bem-sucedido o usuário fica DESLOGADO.
     * Volta para a Login passando a flag de sucesso, que mostra o banner
     * "Enviamos um e-mail de verificação" lá.
     */
    private void goToLoginWithSuccess(String email) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(LoginActivity.EXTRA_EMAIL_SENT_TO, email);
        startActivity(intent);
        finish();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
