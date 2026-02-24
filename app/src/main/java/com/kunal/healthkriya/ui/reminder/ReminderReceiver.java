package com.kunal.healthkriya.ui.reminder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.kunal.healthkriya.R;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String medicineName = intent.getStringExtra("medicine_name");
        String medicineDosage = intent.getStringExtra("medicine_dosage");
        String medicineTime = intent.getStringExtra("medicine_time");

        if (medicineName == null || medicineName.trim().isEmpty()) {
            medicineName = "Your medicine";
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        String channelId = "medicine_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Medicine Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("HealthKriya medicine reminders");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        StringBuilder message = new StringBuilder(medicineName);
        if (medicineDosage != null && !medicineDosage.trim().isEmpty()) {
            message.append(" • ").append(medicineDosage.trim());
        }
        if (medicineTime != null && !medicineTime.trim().isEmpty()) {
            message.append(" at ").append(medicineTime.trim());
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_pill)
                        .setContentTitle("💊 Time to take medicine")
                        .setContentText(message.toString())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
