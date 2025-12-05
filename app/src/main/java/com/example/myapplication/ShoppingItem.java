package com.example.myapplication;

public class ShoppingItem {

    // 1. מזהה המסמך (חיוני למחיקה/עריכה)
    private String documentId;

    // 2. שדות הנתונים
    private String name;
    private int quantity; // הוספנו כמות
    private boolean isPurchased; // הוספנו סטטוס רכישה

    // ********** 1. קונסטרוקטור ריק (חובה ל-Firestore) **********
    public ShoppingItem() {
        // Firestore חייב קונסטרוקטור ציבורי ריק
    }

    // קונסטרוקטור מלא (לנוחות)
    public ShoppingItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.isPurchased = false;
    }

    // ********** 2. Getters ו-Setters **********

    // Getters/Setters ל-Document ID
    // נשתמש ב-documentId כדי להימנע מבלבול עם ה-ID הפנימי של האובייקט
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // Getters/Setters לשם הפריט
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getters/Setters לכמות
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getters/Setters לסטטוס הרכישה
    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }
}