package com.mycasa.app;

public class AppSession {

    private static String groupId;

    public static void setGroupId(String id) {
        groupId = id;
    }

    public static String getGroupId() {
        return groupId;
    }
}
