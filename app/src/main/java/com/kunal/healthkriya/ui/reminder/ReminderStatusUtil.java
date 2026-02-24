package com.kunal.healthkriya.ui.reminder;

import com.kunal.healthkriya.data.local.reminder.ReminderEntity;

public final class ReminderStatusUtil {

    private ReminderStatusUtil() {
    }

    public static Status getStatus(ReminderEntity reminder, long now) {
        if (reminder.repeatDaily) {
            long todayTrigger = AlarmScheduler.triggerAtForDay(now, reminder.hour, reminder.minute);
            if (reminder.lastTakenAt >= todayTrigger) {
                return Status.TAKEN;
            }
            return now > todayTrigger ? Status.MISSED : Status.PENDING;
        }

        if (reminder.lastTakenAt >= reminder.triggerAt) {
            return Status.TAKEN;
        }
        return now > reminder.triggerAt ? Status.MISSED : Status.PENDING;
    }

    public static boolean isScheduledForToday(ReminderEntity reminder, long now) {
        if (reminder.deleted) {
            return false;
        }

        if (reminder.repeatDaily) {
            return true;
        }

        long startOfDay = AlarmScheduler.startOfDay(now);
        long endOfDay = startOfDay + AlarmScheduler.ONE_DAY_MILLIS;
        return reminder.triggerAt >= startOfDay && reminder.triggerAt < endOfDay;
    }

    public static String asLabel(Status status) {
        if (status == Status.TAKEN) return "Taken";
        if (status == Status.MISSED) return "Missed";
        return "Pending";
    }

    public enum Status {
        TAKEN,
        MISSED,
        PENDING
    }
}
