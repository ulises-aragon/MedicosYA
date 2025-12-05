package com.aragon.medicosya.models;

import com.aragon.medicosya.enums.UserRole;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class User {
    @Exclude
    private String uid;

    private String name;
    private String email;
    private String role;
    private Timestamp createdAt;


    public User() {}


    @Exclude
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    @Exclude
    public UserRole getRoleEnum() {
        if (role == null) return null;
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public void setRoleEnum(UserRole role) { this.role = role == null ? null : role.toString(); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
