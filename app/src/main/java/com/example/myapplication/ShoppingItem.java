package com.example.myapplication;

public class ShoppingItem {
    private String id;
    private String name;

    public ShoppingItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
