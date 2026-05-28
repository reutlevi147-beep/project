package com.mycasa.app;

import com.google.firebase.firestore.Exclude;

public class ShoppingItem {

    // מחלקת מודל המייצגת פריט קנייה עם שם, קטגוריה, כמות ומצב רכישה

    @Exclude
    private String documentId;   // לא נשמר ב-Firestore

    private String name;
    private String categoryId;
    private int quantity;

    // ✅ לשמור את השדה בשם "purchased" או להשאיר isPurchased, אבל עם setter נכון
    private boolean isPurchased;

    public ShoppingItem() {}

    // ===== documentId =====
    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }


    @Exclude
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // ===== fields =====
    public String getName() { return name; }
    public String getCategoryId() { return categoryId; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ✅ חשוב: Firestore יזהה "isPurchased" רק אם יש setIsPurchased
    public boolean isPurchased() {
        return isPurchased;
    }

    public void setIsPurchased(boolean isPurchased) {
        this.isPurchased = isPurchased;
    }

    // ✅ אם את משתמשת בקוד שלך ב-setPurchased, נשאיר תאימות
    @Exclude
    public void setPurchased(boolean purchased) {
        this.isPurchased = purchased;
    }
}
