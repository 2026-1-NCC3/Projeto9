package com.example.pi_maya.presentation.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.pi_maya.MayaApp;
import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.User;
import com.example.pi_maya.domain.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public AuthViewModel() {
        this.authRepository = MayaApp.get().getAuthRepository();
    }

    public LiveData<Resource<User>> signIn(String email, String password) {
        return authRepository.signIn(email.trim(), password);
    }

    public LiveData<Resource<User>> signUp(String email, String password,
                                           String fullName, String phone,
                                           boolean lgpdConsent) {
        return authRepository.signUp(email.trim(), password, fullName.trim(), phone, lgpdConsent);
    }

    public LiveData<Resource<Void>> requestPasswordRecovery(String email) {
        return authRepository.requestPasswordRecovery(email.trim());
    }

    public LiveData<Resource<Void>> signOut() {
        return authRepository.signOut();
    }
}
