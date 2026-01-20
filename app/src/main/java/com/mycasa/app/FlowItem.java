package com.mycasa.app;

public class FlowItem {
    public String id;
    public String name;
    public int iconRes;
    public double amount;
    public boolean isActive;

    public FlowItem(String id, String name, int iconRes) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
        this.amount = 0;
        this.isActive = false;
    }
}
