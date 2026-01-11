package com.example.myapplication;

public class User {

    private String id;
    private String name;
    private String role;
    private String phone;
    private String email;
    private String avatarColor;

    public User() {
        // חובה ל-Firestore
    }

    // ===== Getters =====
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    // ===== Setters =====
    public void setId(String id) {
        this.id = id;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }
}
