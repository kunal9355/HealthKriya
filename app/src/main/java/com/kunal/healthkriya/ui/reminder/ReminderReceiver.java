package com.kunal.healthkriya.ui.reminder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.repository.ReminderRepository;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String clientId = intent.getStringExtra("client_id");
        String medicineName = intent.getStringExtra("medicine_name");
        String medicineDosage = intent.getStringExtra("medicine_dosage");
        int alarmHour = intent.getIntExtra("alarm_hour", -1);
        int alarmMinute = intent.getIntExtra("alarm_minute", -1);
        boolean repeatDaily = intent.getBooleanExtra("repeat_daily", false);

        if (medicineName == null || medicineName.trim().isEmpty()) {
            medicineName = "Your medicine";
        }

        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = medicineName + "_" + alarmHour + "_" + alarmMinute;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

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

        int notificationId = AlarmScheduler.buildRequestCode(clientId);

        Intent takenIntent = new Intent(context, ReminderActionReceiver.class);
        takenIntent.setAction(ReminderActionReceiver.ACTION_MARK_TAKEN);
        takenIntent.putExtra("client_id", clientId);
        takenIntent.putExtra("notification_id", notificationId);

        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                takenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        StringBuilder message = new StringBuilder(medicineName);
        if (medicineDosage != null && !medicineDosage.trim().isEmpty()) {
            message.append(" • ").append(medicineDosage.trim());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle("Time to take medicine")
                .setContentText(message.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .addAction(android.R.drawable.checkbox_on_background, "Taken", takenPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            new ReminderRepository(context).onReminderTriggered(clientId, alarmHour, alarmMinute, repeatDaily);
            return;
        }

        manager.notify(notificationId, builder.build());
        new ReminderRepository(context).onReminderTriggered(clientId, alarmHour, alarmMinute, repeatDaily);
    }
}
