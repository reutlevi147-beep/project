package com.mycasa.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "mycasa_reminders";

    // יצירת והצגת התראה למשתמש עם מעבר למסך מתאים בלחיצה
    public static void showNotification(
            Context context,
            String title,
            String message,
            Class<?> activity,
            int icon
    ){

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "mycasa_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "MyCasa Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        Notification notification =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        manager.notify((int) System.currentTimeMillis(), notification);

    }
}