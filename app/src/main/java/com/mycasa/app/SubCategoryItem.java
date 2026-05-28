package com.mycasa.app;

public class SubCategoryItem {

    // מחלקת מודל המייצגת תת־קטגוריה עם סכום משויך

    public String id;
    public String title;
    public double amount;

    public SubCategoryItem(String id, String title, double amount) {
        this.id = id;
        this.title = title;
        this.amount = amount;
    }
}
