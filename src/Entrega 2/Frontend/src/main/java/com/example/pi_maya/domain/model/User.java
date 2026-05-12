package com.example.pi_maya.domain.model;

public class User {
    public final String id;
    public final String email;
    public final String fullName;
    public final String phone;
    public final String role;

    public User(String id, String email, String fullName, String phone, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }
}
