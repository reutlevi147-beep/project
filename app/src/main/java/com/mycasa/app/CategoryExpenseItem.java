package com.mycasa.app;
public class CategoryExpenseItem {

    // מחלקת מודל המייצגת נתוני הוצאה לפי קטגוריה

    public String categoryId;
    public String title;
    public double amount;
    public int color;

    public CategoryExpenseItem(String categoryId, String title, double amount, int color) {
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.color = color;
    }
}
