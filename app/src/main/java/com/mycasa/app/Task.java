package com.mycasa.app;

public class Task {

    private String id;
    private String title;
    private String category;
    private String priority;
    private Long dueDate;   // חייב להיות Long ל-Firestore
    private boolean completed;

    public Task() {}

    public Task(String title,
                String category,
                String priority,
                Long dueDate,
                boolean completed) {

        this.title = title;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    // ===== Getters =====

    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getCategory() { return category; }

    public String getPriority() { return priority; }

    public Long getDueDate() { return dueDate; }

    public boolean isCompleted() { return completed; }

    // ===== Setters =====

    public void setId(String id) { this.id = id; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }
}