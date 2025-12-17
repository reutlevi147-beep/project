package com.example.myapplication;

import java.util.List; // ✅ חדש

public class Task {

    private String id;
    private String taskName;

    // 🔹 קיים – לא נוגעים
    private String assignedTo;

    // 🔹 חדש – לכמה מבצעים
    private List<String> assignedToList;

    private boolean completed;

    // חובה ל-Firestore
    public Task() {
    }

    // בנאי קיים – לא נוגעים
    public Task(String id, String taskName, String assignedTo, boolean completed) {
        this.id = id;
        this.taskName = taskName;
        this.assignedTo = assignedTo;
        this.completed = completed;
    }

    // 🔹 בנאי חדש (לא חובה עדיין – לשלב הבא)
    public Task(String id, String taskName, List<String> assignedToList, boolean completed) {
        this.id = id;
        this.taskName = taskName;
        this.assignedToList = assignedToList;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    // קיים
    public String getAssignedTo() {
        return assignedTo;
    }

    // 🔹 חדש
    public List<String> getAssignedToList() {
        return assignedToList;
    }

    public void setAssignedToList(List<String> assignedToList) {
        this.assignedToList = assignedToList;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
