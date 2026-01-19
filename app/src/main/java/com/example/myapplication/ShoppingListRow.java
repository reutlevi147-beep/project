package com.example.myapplication;

public class ShoppingListRow {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_CLEAR_PURCHASED = 2;

    private int type;
    private String title;
    private ShoppingItem item;

    private ShoppingListRow(int type) {
        this.type = type;
    }

    public static ShoppingListRow header(String title) {
        ShoppingListRow r = new ShoppingListRow(TYPE_HEADER);
        r.title = title;
        return r;
    }

    public static ShoppingListRow item(ShoppingItem item) {
        ShoppingListRow r = new ShoppingListRow(TYPE_ITEM);
        r.item = item;
        return r;
    }

    public static ShoppingListRow clearPurchased() {
        return new ShoppingListRow(TYPE_CLEAR_PURCHASED);
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public ShoppingItem getItem() { return item; }



}
