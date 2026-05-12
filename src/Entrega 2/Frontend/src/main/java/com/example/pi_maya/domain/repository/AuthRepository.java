package com.example.pi_maya.domain.repository;

import com.example.pi_maya.core.result.Resource;
import com.example.pi_maya.domain.model.User;

import androidx.lifecycle.LiveData;

public interface AuthRepository {

    LiveData<Resource<User>> signIn(String email, String password);

    LiveData<Resource<User>> signUp(String email, String password,
                                    String fullName, String phone,
                                    boolean lgpdConsent);

    LiveData<Resource<Void>> requestPasswordRecovery(String email);

    LiveData<Resource<Void>> signOut();

    boolean isLoggedIn();
}
