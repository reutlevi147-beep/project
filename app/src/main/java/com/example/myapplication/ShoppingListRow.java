package com.example.myapplication;

public class ShoppingListRow {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String headerTitle;
    private ShoppingItem item;

    private ShoppingListRow(int type) {
        this.type = type;
    }

    public static ShoppingListRow header(String title) {
        ShoppingListRow row = new ShoppingListRow(TYPE_HEADER);
        row.headerTitle = title;
        return row;
    }

    public static ShoppingListRow item(ShoppingItem item) {
        ShoppingListRow row = new ShoppingListRow(TYPE_ITEM);
        row.item = item;
        return row;
    }

    public int getType() {
        return type;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public ShoppingItem getItem() {
        return item;
    }
}
