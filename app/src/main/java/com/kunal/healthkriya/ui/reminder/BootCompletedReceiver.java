package com.kunal.healthkriya.ui.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kunal.healthkriya.data.repository.ReminderRepository;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        BroadcastReceiver.PendingResult pendingResult = goAsync();
        ReminderRepository repository = new ReminderRepository(context.getApplicationContext());

        new Thread(() -> {
            try {
                repository.restoreAlarmsAfterBoot();
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
