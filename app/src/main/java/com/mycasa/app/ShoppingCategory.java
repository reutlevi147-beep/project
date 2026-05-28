package com.mycasa.app;
public class ShoppingCategory {
    private final String id;
    private final String name;
    private final String icon;

    // מחלקת מודל המייצגת קטגוריית קניות עם מזהה, שם ואייקון

    public ShoppingCategory(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
}
