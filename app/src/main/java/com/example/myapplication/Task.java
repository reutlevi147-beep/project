package com.example.myapplication;

public class Task {

    private String id;
    private String taskName;
    private String assignedTo;
    private boolean completed;

    public Task() {
        // נדרש לפיירסטור
    }

    public Task(String id, String taskName, String assignedTo, boolean completed) {
        this.id = id;
        this.taskName = taskName;
        this.assignedTo = assignedTo;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
