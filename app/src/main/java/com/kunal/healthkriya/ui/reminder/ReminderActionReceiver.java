package com.kunal.healthkriya.ui.reminder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kunal.healthkriya.data.repository.ReminderRepository;

public class ReminderActionReceiver extends BroadcastReceiver {
    public static final String ACTION_MARK_TAKEN = "com.kunal.healthkriya.action.MARK_REMINDER_TAKEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_MARK_TAKEN.equals(intent.getAction())) {
            return;
        }

        String clientId = intent.getStringExtra("client_id");
        int notificationId = intent.getIntExtra("notification_id", -1);

        if (clientId == null || clientId.trim().isEmpty()) {
            return;
        }

        new ReminderRepository(context).markTaken(clientId, null);

        if (notificationId >= 0) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(notificationId);
            }
        }
    }
}
