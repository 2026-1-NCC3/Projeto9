package com.example.pi_maya.core.util;

import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isEmailValid(String email) {
        return email != null && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isNameValid(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static boolean isPhoneValid(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 10 && digits.length() <= 13;
    }
}
