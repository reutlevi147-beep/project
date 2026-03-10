package com.mycasa.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // כאן נבדוק דברים באפליקציה
        checkEvents(context);
        checkTasks(context);
        checkShopping(context);
        checkFinance(context);

    }

    private void checkEvents(Context context) {

        // כאן בעתיד נבדוק אירועים שעה לפני
        NotificationHelper.showNotification(
                context,
                "MyCasa",
                "בדיקת אירועים הופעלה"
        );
    }

    private void checkTasks(Context context) {

        NotificationHelper.showNotification(
                context,
                "MyCasa",
                "בדיקת משימות הופעלה"
        );
    }

    private void checkShopping(Context context) {

        NotificationHelper.showNotification(
                context,
                "MyCasa",
                "בדיקת רשימת קניות הופעלה"
        );
    }

    private void checkFinance(Context context) {

        NotificationHelper.showNotification(
                context,
                "MyCasa",
                "בדיקת כלכלה הופעלה"
        );
    }
}