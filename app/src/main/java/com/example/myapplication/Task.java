package com.example.myapplication;

public class Task {

    private String title;
    private String category;
    private String priority;
    private String dueDate;
    private boolean completed;

    // חובה ל-Firestore
    public Task() {}

    // בנאי מלא (לעתיד)
    public Task(String title, String category, String priority, String dueDate, boolean completed) {
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    // בנאי קצר (לדמו / בדיקות)
    public Task(String title, boolean completed) {
        this.title = title;
        this.completed = completed;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    // Setters
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
