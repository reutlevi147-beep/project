package com.mycasa.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        checkEvents(context);
        checkTasksAndShopping(context);
        checkSavingsGoals(context);

    }

    // =========================================
    // אירועים
    // שעה לפני האירוע
    // רק למי שמיועד
    // =========================================

    private void checkEvents(Context context) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences prefs =
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String groupId = prefs.getString("groupId", null);
        String userId = prefs.getString("userId", null);

        if(groupId == null || userId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("calendar_events")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Date now = new Date();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        String date = doc.getString("date");
                        String startTime = doc.getString("startTime");
                        String title = doc.getString("title");

                        Boolean isAllUsers = doc.getBoolean("isAllUsers");
                        List<String> assignedUsers =
                                (List<String>) doc.get("assignedUserIds");

                        boolean shouldNotify = false;

                        if(isAllUsers != null && isAllUsers){
                            shouldNotify = true;
                        }
                        else if(assignedUsers != null && assignedUsers.contains(userId)){
                            shouldNotify = true;
                        }

                        if(!shouldNotify) continue;

                        try {

                            SimpleDateFormat sdf =
                                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                            Date eventDate = sdf.parse(date + " " + startTime);

                            long diff = eventDate.getTime() - now.getTime();
                            long minutes = diff / (60 * 1000);

                            if(minutes <= 60 && minutes > 0){

                                String notificationKey = "event_" + doc.getId();

                                if(wasNotificationSent(context, notificationKey)) continue;

                                NotificationHelper.showNotification(
                                        context,
                                        "אירוע בעוד שעה",
                                        title,
                                        CalendarDayActivity.class,
                                        android.R.drawable.ic_menu_my_calendar
                                );

                                markNotificationSent(context, notificationKey);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });

    }

    // =========================================
    // משימות + קניות
    // =========================================

    private void checkTasksAndShopping(Context context){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences prefs =
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String groupId = prefs.getString("groupId", null);
        String userId = prefs.getString("userId", null);
        String role = prefs.getString("role", null);

        if(groupId == null || userId == null) return;

        Date now = new Date();

        final int[] tasksCount = {0};

        db.collection("groups")
                .document(groupId)
                .collection("home_tasks")
                .whereEqualTo("isDone", false)
                .get()
                .addOnSuccessListener(tasksSnapshot -> {

                    for(DocumentSnapshot doc : tasksSnapshot.getDocuments()){

                        List<String> assignedUsers =
                                (List<String>) doc.get("assignedUserIds");

                        if(assignedUsers == null || !assignedUsers.contains(userId))
                            continue;

                        Long dueDate = doc.getLong("dueDate");

                        if(dueDate != null){

                            Date due = new Date(dueDate);

                            long diff = due.getTime() - now.getTime();
                            long hours = diff / (60 * 60 * 1000);

                            if(hours <= 24 && hours > 0){
                                tasksCount[0]++;
                            }

                        }

                    }

                    checkShoppingPart(context, db, groupId, now, tasksCount[0], role);

                });

    }

    // =========================================
    // קניות
    // רק להורים
    // =========================================

    private void checkShoppingPart(
            Context context,
            FirebaseFirestore db,
            String groupId,
            Date now,
            int tasksCount,
            String role
    ){

        final int[] shoppingCount = {0};

        db.collection("groups")
                .document(groupId)
                .collection("shopping")
                .whereEqualTo("isPurchased", false)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if("parent".equals(role)) {

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {

                            Timestamp createdAt = doc.getTimestamp("createdAt");

                            if (createdAt != null) {

                                Date created = createdAt.toDate();

                                long diff = now.getTime() - created.getTime();
                                long days = diff / (1000 * 60 * 60 * 24);

                                if (days >= 3) {
                                    shoppingCount[0]++;
                                }

                            }

                        }
                    }

                    if(tasksCount > 0 || shoppingCount[0] > 0){

                        String notificationKey = "tasks_shopping";

                        if(wasNotificationSent(context, notificationKey)) return;

                        String message =
                                "יש " + tasksCount +
                                        " משימות ו-" +
                                        shoppingCount[0] +
                                        " מוצרים שמחכים לטיפול";

                        Class<?> targetActivity;

                        if(tasksCount > 0 && shoppingCount[0] == 0){
                            targetActivity = TasksActivity.class;
                        }
                        else if(tasksCount == 0 && shoppingCount[0] > 0){
                            targetActivity = ShoppingListActivity.class;
                        }
                        else{
                            targetActivity = Home.class;
                        }

                        NotificationHelper.showNotification(
                                context,
                                "תזכורת MyCasa",
                                message,
                                targetActivity,
                                android.R.drawable.ic_menu_agenda
                        );

                        markNotificationSent(context, notificationKey);

                    }

                });

    }

    // =========================================
    // מטרות חיסכון
    // רק להורים
    // =========================================

    private void checkSavingsGoals(Context context){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences prefs =
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String groupId = prefs.getString("groupId", null);
        String role = prefs.getString("role", null);

        if(groupId == null || !"parent".equals(role)) return;

        db.collection("groups")
                .document(groupId)
                .collection("savings_goals")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Date now = new Date();

                    for(DocumentSnapshot doc : snapshot.getDocuments()){

                        Timestamp deadline = doc.getTimestamp("deadline");
                        String title = doc.getString("title");

                        Long current = doc.getLong("currentAmount");
                        Long target = doc.getLong("targetAmount");

                        if(current != null && target != null && current >= target){
                            continue;
                        }

                        if(deadline != null){

                            Date dueDate = deadline.toDate();

                            long diff = dueDate.getTime() - now.getTime();
                            long hours = diff / (60 * 60 * 1000);

                            if(hours <= 24 && hours > 0){

                                String notificationKey = "goal_" + doc.getId();

                                if(wasNotificationSent(context, notificationKey)) continue;

                                String message =
                                        "המטרה \"" + title + "\" מסתיימת מחר";

                                NotificationHelper.showNotification(
                                        context,
                                        "יעד חיסכון מתקרב",
                                        message,
                                        Finance.class,
                                        android.R.drawable.ic_dialog_info
                                );

                                markNotificationSent(context, notificationKey);

                            }

                        }

                    }

                });

    }

    // =========================================
    // מניעת שליחה כפולה של התראות
    // =========================================

    private boolean wasNotificationSent(Context context, String key){

        SharedPreferences prefs =
                context.getSharedPreferences("sent_notifications", Context.MODE_PRIVATE);

        return prefs.getBoolean(key, false);

    }

    private void markNotificationSent(Context context, String key){

        SharedPreferences prefs =
                context.getSharedPreferences("sent_notifications", Context.MODE_PRIVATE);

        prefs.edit().putBoolean(key, true).apply();

    }

}