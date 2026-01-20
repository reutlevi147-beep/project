package com.mycasa.app;

public class Task {

    private String id;          // מזהה Firestore
    private String title;
    private String category;
    private String priority;
    private boolean completed;

    // חובה ל-Firestore
    public Task() {}

    public Task(String title, String category, String priority, boolean completed) {
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.completed = completed;
    }

    // ===== Getters =====
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    // ===== Setters =====
    public void setId(String id) {
        this.id = id;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}


