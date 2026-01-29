package com.mycasa.app;

import java.util.Date;

public class FlowItem {

    private String id;
    private String categoryId;   // קטגוריה ראשית (expense_housing וכו')
    private String title;

    private int amount;
    private boolean enabled;
    private boolean editing = false;
    private String frequency = "חודשי";

    private Date lastApprovedAt;

    public FlowItem() {
        // חובה ל-Firebase
    }

    public FlowItem(String id, String categoryId, String title) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = 0;
        this.enabled = true;
    }

    // ===== Getters =====
    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEditing() {
        return editing;
    }

    public String getFrequency() {
        return frequency;
    }

    public Date getLastApprovedAt() {
        return lastApprovedAt;
    }

    // ===== Setters =====
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setLastApprovedAt(Date lastApprovedAt) {
        this.lastApprovedAt = lastApprovedAt;
    }

    // ===== Logic =====
    public boolean isConfigured() {
        return amount > 0;
    }
}
