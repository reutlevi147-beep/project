package com.example.myapplication;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class ShoppingItem {

    @Exclude
    private String documentId;

    private String name;
    private int quantity;
    private boolean isPurchased;
    private String categoryId;        // 👈 חדש
    private Timestamp createdAt;      // 👈 חדש

    // חובה ל-Firestore
    public ShoppingItem() {}

    public ShoppingItem(String name, int quantity, String categoryId) {
        this.name = name;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.isPurchased = false;
        this.createdAt = Timestamp.now();
    }

    // ===== getters / setters =====

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
