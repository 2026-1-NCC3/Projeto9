package com.example.pi_maya.presentation.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pi_maya.BuildConfig;
import com.example.pi_maya.MayaApp;
import com.example.pi_maya.R;
import com.example.pi_maya.core.session.SessionManager;
import com.example.pi_maya.presentation.auth.AuthViewModel;
import com.example.pi_maya.presentation.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        SessionManager session = MayaApp.get().getSessionManager();
        TextView userName = view.findViewById(R.id.userName);
        TextView userEmail = view.findViewById(R.id.userEmail);
        userName.setText(session.getUserName() != null ? session.getUserName() : "Paciente");
        userEmail.setText(session.getUserEmail() != null ? session.getUserEmail() : "");

        TextView version = view.findViewById(R.id.versionText);
        version.setText(getString(R.string.profile_app_version, BuildConfig.VERSION_NAME));

        MaterialButton exportButton = view.findViewById(R.id.exportDataButton);
        exportButton.setOnClickListener(v -> Snackbar.make(v,
                "Exportação de dados será disponibilizada na próxima versão.",
                Snackbar.LENGTH_LONG).show());

        MaterialButton deleteButton = view.findViewById(R.id.deleteAccountButton);
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());

        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout)
                .setMessage("Você quer sair da sua conta?")
                .setPositiveButton(R.string.action_confirm, (d, w) -> performLogout())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_delete_account)
                .setMessage("Esta ação é irreversível. Seus dados serão excluídos conforme a LGPD. Confirma?")
                .setPositiveButton(R.string.action_confirm, (d, w) ->
                        Snackbar.make(requireView(),
                                "Solicitação de exclusão registrada. Entraremos em contato em até 7 dias úteis.",
                                Snackbar.LENGTH_LONG).show())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void performLogout() {
        authViewModel.signOut().observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }
}
