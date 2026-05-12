package com.example.pi_maya.presentation.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pi_maya.R;
import com.example.pi_maya.core.util.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private MaterialButton sendButton;
    private ProgressBar progress;
    private ImageView successIcon;
    private TextView successText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        emailLayout = findViewById(R.id.emailLayout);
        emailInput = findViewById(R.id.emailInput);
        sendButton = findViewById(R.id.sendButton);
        progress = findViewById(R.id.sendProgress);
        successIcon = findViewById(R.id.successIcon);
        successText = findViewById(R.id.successText);

        sendButton.setOnClickListener(v -> attemptRecover());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.backToLoginButton).setOnClickListener(v -> finish());
    }

    private void attemptRecover() {
        emailLayout.setError(null);
        String email = emailInput.getText() == null ? "" : emailInput.getText().toString();

        if (!ValidationUtils.isEmailValid(email)) {
            emailLayout.setError(getString(R.string.validation_invalid_email));
            return;
        }

        setLoading(true);
        viewModel.requestPasswordRecovery(email).observe(this, resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    showSuccess();
                    break;
                case ERROR:
                    setLoading(false);
                    Snackbar.make(sendButton,
                            resource.getMessage() != null ? resource.getMessage()
                                    : getString(R.string.error_generic),
                            Snackbar.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        sendButton.setEnabled(!loading);
        emailInput.setEnabled(!loading);
    }

    private void showSuccess() {
        sendButton.setVisibility(View.GONE);
        emailLayout.setVisibility(View.GONE);
        successIcon.setVisibility(View.VISIBLE);
        successText.setVisibility(View.VISIBLE);
    }
}
