package com.mycasa.app;

public class AppSession {

    // מחלקה לניהול ושמירת נתוני המשתמש והקבוצה הפעילים באפליקציה
    private static String groupId;
    private static String userId;
    private static String userRole;

    // ===== SETTERS =====
    public static void setGroupId(String id) {
        groupId = id;
    }

    public static void setUserId(String id) {
        userId = id;
    }

    public static void setUserRole(String role) {
        userRole = role;
    }

    // ===== GETTERS =====
    public static String getGroupId() {
        return groupId;
    }

    public static String getUserId() {
        return userId;
    }

    public static String getUserRole() {
        return userRole;
    }
}