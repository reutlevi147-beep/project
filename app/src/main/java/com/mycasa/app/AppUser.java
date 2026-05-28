package com.mycasa.app;

import com.google.firebase.Timestamp;

public class AppUser {

// מחלקת מודל המייצגת משתמש באפליקציה ושומרת את פרטיו עבור Firestore

    private String documentId;
    private String name;
    private String phoneNumber;
    private Timestamp createdAt;

    // 🔹 חובה ל-Firestore – קונסטרקטור ריק
    public AppUser() {
    }

    // 🔹 קונסטרקטור נוח לעבודה פנימית
    public AppUser(String name, String phoneNumber, Timestamp createdAt) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    // Getters & Setters

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
