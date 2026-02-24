package com.kunal.healthkriya.ui.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.kunal.healthkriya.data.local.reminder.ReminderEntity;

import java.util.Calendar;

public final class AlarmScheduler {
    public static final long ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L;

    private AlarmScheduler() {
    }

    public static boolean scheduleReminder(Context context, ReminderEntity reminder) {
        if (context == null || reminder == null || reminder.deleted || !reminder.active) {
            return false;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return false;
        }

        long now = System.currentTimeMillis();
        long triggerAt = reminder.triggerAt;
        if (reminder.repeatDaily) {
            triggerAt = computeNextDailyTriggerAt(reminder.hour, reminder.minute, now);
            reminder.triggerAt = triggerAt;
        } else if (triggerAt <= now) {
            return false;
        }

        PendingIntent pendingIntent = buildPendingIntent(context, reminder.clientId, reminder.name, reminder.dosage,
                reminder.hour, reminder.minute, reminder.repeatDaily);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }

        return true;
    }

    public static void cancelReminder(Context context, String clientId) {
        if (context == null || clientId == null) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                buildRequestCode(clientId),
                new Intent(context, ReminderReceiver.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static PendingIntent buildPendingIntent(
            Context context,
            String clientId,
            String name,
            String dosage,
            int hour,
            int minute,
            boolean repeatDaily
    ) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("client_id", clientId);
        intent.putExtra("medicine_name", name);
        intent.putExtra("medicine_dosage", dosage);
        intent.putExtra("alarm_hour", hour);
        intent.putExtra("alarm_minute", minute);
        intent.putExtra("repeat_daily", repeatDaily);

        return PendingIntent.getBroadcast(
                context,
                buildRequestCode(clientId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static int buildRequestCode(String clientId) {
        return Math.abs(clientId.hashCode());
    }

    public static long computeInitialTriggerAt(int hour, int minute, boolean repeatDaily) {
        long now = System.currentTimeMillis();
        long triggerAt = triggerAtForDay(now, hour, minute);
        if (triggerAt <= now) {
            triggerAt += ONE_DAY_MILLIS;
        }
        return triggerAt;
    }

    public static long computeNextDailyTriggerAt(int hour, int minute, long referenceMillis) {
        long next = triggerAtForDay(referenceMillis, hour, minute);
        if (next <= referenceMillis) {
            next += ONE_DAY_MILLIS;
        }
        return next;
    }

    public static long triggerAtForDay(long baseTimeMillis, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(baseTimeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long startOfDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
