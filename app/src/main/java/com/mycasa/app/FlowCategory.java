package com.mycasa.app;

// מחלקת מודל המייצגת קטגוריה פיננסית הכוללת מזהה, שם וסוג קטגוריה
public class FlowCategory {

    private String id;
    private String title;
    private boolean income;
    private boolean expanded;

    public FlowCategory() {}

    public FlowCategory(String id, String title, boolean income) {
        this.id = id;
        this.title = title;
        this.income = income;
        this.expanded = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public boolean isIncome() { return income; }

    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
