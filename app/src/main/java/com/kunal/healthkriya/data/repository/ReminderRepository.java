package com.kunal.healthkriya.data.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.kunal.healthkriya.data.local.reminder.ReminderDao;
import com.kunal.healthkriya.data.local.reminder.ReminderDatabase;
import com.kunal.healthkriya.data.local.reminder.ReminderEntity;
import com.kunal.healthkriya.data.local.reminder.ReminderLogDao;
import com.kunal.healthkriya.data.local.reminder.ReminderLogEntity;
import com.kunal.healthkriya.ui.reminder.AlarmScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderRepository {
    public static final String ACTION_REMINDERS_CHANGED = "com.kunal.healthkriya.action.REMINDERS_CHANGED";

    private static final long MISSED_GRACE_MILLIS = 15L * 60L * 1000L;
    private static final int WEEK_DAYS = 7;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    private final Context appContext;
    private final ReminderDao reminderDao;
    private final ReminderLogDao reminderLogDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private ListenerRegistration firebaseListener;

    public ReminderRepository(Context context) {
        appContext = context.getApplicationContext();
        ReminderDatabase db = ReminderDatabase.getInstance(appContext);
        reminderDao = db.reminderDao();
        reminderLogDao = db.reminderLogDao();
    }

    public static String newClientId() {
        return UUID.randomUUID().toString();
    }

    public void saveReminder(ReminderEntity reminder, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                reminder.updatedAt = System.currentTimeMillis();
                reminder.syncStatus = ReminderEntity.SYNC_PENDING;
                reminder.deleted = false;
                reminder.active = true;

                long rowId = reminderDao.insertOrUpdate(reminder);
                if (rowId > 0) {
                    reminder.id = rowId;
                }

                ensureUpcomingPendingLog(reminder, System.currentTimeMillis());
                pushReminderToFirebase(reminder);
                notifyReminderChanged();
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    public void getReminderByClientId(String clientId, ReminderCallback callback) {
        EXECUTOR.execute(() -> {
            ReminderEntity reminder = reminderDao.getByClientId(clientId);
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(reminder));
            }
        });
    }

    public void restoreReminder(ReminderEntity snapshot, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                ReminderEntity reminder = reminderDao.getByClientId(snapshot.clientId);
                if (reminder == null) {
                    reminder = snapshot;
                }

                reminder.deleted = false;
                reminder.active = true;
                reminder.updatedAt = System.currentTimeMillis();
                reminder.syncStatus = ReminderEntity.SYNC_PENDING;

                long now = System.currentTimeMillis();
                if (reminder.repeatDaily) {
                    reminder.triggerAt = AlarmScheduler.computeNextDailyTriggerAt(reminder.hour, reminder.minute, now);
                } else if (reminder.triggerAt <= now) {
                    reminder.triggerAt = AlarmScheduler.computeInitialTriggerAt(reminder.hour, reminder.minute, false);
                }

                reminderDao.insertOrUpdate(reminder);
                AlarmScheduler.scheduleReminder(appContext, reminder);
                ensureUpcomingPendingLog(reminder, now);
                pushReminderToFirebase(reminder);

                notifyReminderChanged();
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    public void markTaken(String clientId, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                ReminderEntity reminder = reminderDao.getByClientId(clientId);
                if (reminder == null || reminder.deleted) {
                    postSave(callback, false, new IllegalStateException("Reminder not found"));
                    return;
                }

                long now = System.currentTimeMillis();
                reminder.lastTakenAt = now;
                reminder.updatedAt = now;
                reminder.syncStatus = ReminderEntity.SYNC_PENDING;

                if (!reminder.repeatDaily) {
                    reminder.active = false;
                    AlarmScheduler.cancelReminder(appContext, reminder.clientId);
                }

                reminderDao.insertOrUpdate(reminder);
                upsertTakenLog(reminder, now);
                pushReminderToFirebase(reminder);

                notifyReminderChanged();
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    public void onReminderTriggered(String clientId, int hour, int minute, boolean repeatDaily) {
        EXECUTOR.execute(() -> {
            ReminderEntity reminder = reminderDao.getByClientId(clientId);
            if (reminder == null || reminder.deleted || !reminder.active) {
                return;
            }

            long now = System.currentTimeMillis();
            upsertTriggeredLog(reminder, hour, minute, now);

            if (repeatDaily) {
                reminder.triggerAt = AlarmScheduler.computeNextDailyTriggerAt(hour, minute, now + 1_000L);
                reminder.updatedAt = now;
                reminder.syncStatus = ReminderEntity.SYNC_PENDING;
                reminderDao.insertOrUpdate(reminder);

                boolean scheduled = AlarmScheduler.scheduleReminder(appContext, reminder);
                if (!scheduled) {
                    reminder.syncStatus = ReminderEntity.SYNC_ERROR;
                    reminderDao.insertOrUpdate(reminder);
                }
                pushReminderToFirebase(reminder);
            }

            notifyReminderChanged();
        });
    }

    public void deleteReminder(String clientId, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                ReminderEntity reminder = reminderDao.getByClientId(clientId);
                if (reminder == null) {
                    postSave(callback, false, new IllegalStateException("Reminder not found"));
                    return;
                }

                reminder.active = false;
                reminder.deleted = true;
                reminder.updatedAt = System.currentTimeMillis();
                reminder.syncStatus = ReminderEntity.SYNC_PENDING;

                reminderDao.insertOrUpdate(reminder);
                AlarmScheduler.cancelReminder(appContext, reminder.clientId);
                pushReminderToFirebase(reminder);

                notifyReminderChanged();
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    public void getReminders(RemindersCallback callback) {
        EXECUTOR.execute(() -> {
            long now = System.currentTimeMillis();
            List<ReminderEntity> reminders = reminderDao.getNotDeletedReminders();
            ensureDueLogs(now, reminders, WEEK_DAYS);
            postReminders(callback, reminders);
        });
    }

    public void getTodaySummary(SummaryCallback callback) {
        EXECUTOR.execute(() -> {
            long now = System.currentTimeMillis();
            List<ReminderEntity> reminders = reminderDao.getNotDeletedReminders();
            ensureDueLogs(now, reminders, WEEK_DAYS);

            DailyCounts counts = computeDayCounts(reminders, now, now);
            Summary summary = new Summary(counts.total, counts.taken, counts.missed, counts.pending);
            postSummary(callback, summary);
        });
    }

    public void getWeeklyOverview(WeeklyOverviewCallback callback) {
        EXECUTOR.execute(() -> {
            long now = System.currentTimeMillis();
            List<ReminderEntity> reminders = reminderDao.getNotDeletedReminders();
            ensureDueLogs(now, reminders, WEEK_DAYS);

            List<DailyOverview> days = new ArrayList<>();
            long todayStart = AlarmScheduler.startOfDay(now);
            int weeklyTotal = 0;
            int weeklyTaken = 0;

            for (int i = WEEK_DAYS - 1; i >= 0; i--) {
                long dayStart = todayStart - (i * AlarmScheduler.ONE_DAY_MILLIS);
                DailyCounts counts = computeDayCounts(reminders, dayStart, now);
                weeklyTotal += counts.total;
                weeklyTaken += counts.taken;
                days.add(new DailyOverview(
                        dateKey(dayStart),
                        dayLabel(dayStart),
                        counts.total,
                        counts.taken,
                        counts.missed,
                        counts.pending
                ));
            }

            int adherencePercent = weeklyTotal == 0 ? 0 : Math.round((weeklyTaken * 100f) / weeklyTotal);
            WeeklyOverview overview = new WeeklyOverview(days, adherencePercent);
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(overview));
            }
        });
    }

    public void restoreAlarmsAfterBoot() {
        EXECUTOR.execute(() -> {
            List<ReminderEntity> reminders = reminderDao.getActiveRemindersForBoot();
            long now = System.currentTimeMillis();

            for (ReminderEntity reminder : reminders) {
                if (reminder.deleted || !reminder.active) {
                    continue;
                }

                if (reminder.repeatDaily) {
                    long nextDaily = AlarmScheduler.computeNextDailyTriggerAt(reminder.hour, reminder.minute, now);
                    if (reminder.triggerAt != nextDaily) {
                        reminder.triggerAt = nextDaily;
                        reminder.updatedAt = System.currentTimeMillis();
                        reminderDao.insertOrUpdate(reminder);
                    }
                    AlarmScheduler.scheduleReminder(appContext, reminder);
                } else if (reminder.triggerAt > now) {
                    AlarmScheduler.scheduleReminder(appContext, reminder);
                }
            }

            notifyReminderChanged();
        });
    }

    public void restoreFromFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        firestore.collection("users")
                .document(uid)
                .collection("reminders")
                .get()
                .addOnSuccessListener(snapshots -> EXECUTOR.execute(() -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ReminderEntity incoming = fromDocument(doc);
                        if (incoming == null) {
                            continue;
                        }
                        mergeIncoming(incoming);
                    }
                    syncPendingReminders();
                    startRealtimeSync();
                    notifyReminderChanged();
                }))
                .addOnFailureListener(e -> startRealtimeSync());
    }

    public void startRealtimeSync() {
        stopRealtimeSync();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        firebaseListener = firestore.collection("users")
                .document(uid)
                .collection("reminders")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    EXECUTOR.execute(() -> {
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            ReminderEntity incoming = fromDocument(change.getDocument());
                            if (incoming == null) {
                                continue;
                            }
                            mergeIncoming(incoming);
                        }
                        notifyReminderChanged();
                    });
                });
    }

    public void stopRealtimeSync() {
        if (firebaseListener != null) {
            firebaseListener.remove();
            firebaseListener = null;
        }
    }

    private void syncPendingReminders() {
        List<ReminderEntity> pending = reminderDao.getBySyncStatus(ReminderEntity.SYNC_PENDING);
        for (ReminderEntity reminder : pending) {
            pushReminderToFirebase(reminder);
        }
    }

    private void pushReminderToFirebase(ReminderEntity reminder) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", reminder.name);
        payload.put("dosage", reminder.dosage);
        payload.put("hour", reminder.hour);
        payload.put("minute", reminder.minute);
        payload.put("repeatDaily", reminder.repeatDaily);
        payload.put("triggerAt", reminder.triggerAt);
        payload.put("active", reminder.active);
        payload.put("lastTakenAt", reminder.lastTakenAt);
        payload.put("updatedAt", reminder.updatedAt);
        payload.put("deleted", reminder.deleted);

        String clientId = reminder.clientId;
        long version = reminder.updatedAt;

        firestore.collection("users")
                .document(uid)
                .collection("reminders")
                .document(clientId)
                .set(payload)
                .addOnSuccessListener(unused -> EXECUTOR.execute(() -> markSyncedIfCurrent(clientId, version)))
                .addOnFailureListener(e -> EXECUTOR.execute(() -> markSyncErrorIfCurrent(clientId, version)));
    }

    private void markSyncedIfCurrent(String clientId, long updatedAt) {
        ReminderEntity current = reminderDao.getByClientId(clientId);
        if (current != null && current.updatedAt == updatedAt && current.syncStatus == ReminderEntity.SYNC_PENDING) {
            current.syncStatus = ReminderEntity.SYNC_SYNCED;
            reminderDao.insertOrUpdate(current);
            notifyReminderChanged();
        }
    }

    private void markSyncErrorIfCurrent(String clientId, long updatedAt) {
        ReminderEntity current = reminderDao.getByClientId(clientId);
        if (current != null && current.updatedAt == updatedAt && current.syncStatus == ReminderEntity.SYNC_PENDING) {
            current.syncStatus = ReminderEntity.SYNC_ERROR;
            reminderDao.insertOrUpdate(current);
            notifyReminderChanged();
        }
    }

    private void mergeIncoming(ReminderEntity incoming) {
        ReminderEntity local = reminderDao.getByClientId(incoming.clientId);
        if (local == null || incoming.updatedAt >= local.updatedAt) {
            reminderDao.insertOrUpdate(incoming);

            if (incoming.deleted || !incoming.active) {
                AlarmScheduler.cancelReminder(appContext, incoming.clientId);
            } else {
                AlarmScheduler.scheduleReminder(appContext, incoming);
            }
        }
    }

    private ReminderEntity fromDocument(DocumentSnapshot doc) {
        String clientId = doc.getId();
        String name = doc.getString("name");
        String dosage = doc.getString("dosage");

        Long hour = doc.getLong("hour");
        Long minute = doc.getLong("minute");
        Boolean repeatDaily = doc.getBoolean("repeatDaily");
        Long triggerAt = doc.getLong("triggerAt");
        Boolean active = doc.getBoolean("active");
        Long lastTakenAt = doc.getLong("lastTakenAt");
        Long updatedAt = doc.getLong("updatedAt");
        Boolean deleted = doc.getBoolean("deleted");

        if (hour == null || minute == null || triggerAt == null || updatedAt == null) {
            return null;
        }

        return new ReminderEntity(
                0L,
                clientId,
                name != null ? name : "Medicine",
                dosage != null ? dosage : "",
                hour.intValue(),
                minute.intValue(),
                repeatDaily != null && repeatDaily,
                triggerAt,
                active == null || active,
                lastTakenAt != null ? lastTakenAt : 0L,
                updatedAt,
                ReminderEntity.SYNC_SYNCED,
                deleted != null && deleted
        );
    }

    private void ensureUpcomingPendingLog(ReminderEntity reminder, long now) {
        long scheduledAt;
        if (reminder.repeatDaily) {
            scheduledAt = AlarmScheduler.computeNextDailyTriggerAt(reminder.hour, reminder.minute, now - 1_000L);
        } else {
            scheduledAt = reminder.triggerAt;
        }

        String dateKey = dateKey(scheduledAt);
        ReminderLogEntity log = reminderLogDao.getByReminderAndDate(reminder.clientId, dateKey);
        if (log == null) {
            reminderLogDao.insertOrUpdate(new ReminderLogEntity(
                    0L,
                    reminder.clientId,
                    dateKey,
                    scheduledAt,
                    0L,
                    ReminderLogEntity.STATUS_PENDING,
                    System.currentTimeMillis()
            ));
        }
    }

    private void upsertTriggeredLog(ReminderEntity reminder, int hour, int minute, long now) {
        long scheduledAt = reminder.repeatDaily
                ? AlarmScheduler.triggerAtForDay(now, hour, minute)
                : reminder.triggerAt;

        String dateKey = dateKey(scheduledAt);
        ReminderLogEntity log = reminderLogDao.getByReminderAndDate(reminder.clientId, dateKey);

        if (log == null) {
            reminderLogDao.insertOrUpdate(new ReminderLogEntity(
                    0L,
                    reminder.clientId,
                    dateKey,
                    scheduledAt,
                    0L,
                    ReminderLogEntity.STATUS_PENDING,
                    now
            ));
            return;
        }

        if (log.status != ReminderLogEntity.STATUS_TAKEN) {
            log.scheduledAt = scheduledAt;
            log.status = ReminderLogEntity.STATUS_PENDING;
            log.updatedAt = now;
            reminderLogDao.insertOrUpdate(log);
        }
    }

    private void upsertTakenLog(ReminderEntity reminder, long now) {
        long scheduledAt = reminder.repeatDaily
                ? AlarmScheduler.triggerAtForDay(now, reminder.hour, reminder.minute)
                : reminder.triggerAt;

        String dateKey = dateKey(scheduledAt);
        ReminderLogEntity log = reminderLogDao.getByReminderAndDate(reminder.clientId, dateKey);

        if (log == null) {
            log = new ReminderLogEntity(
                    0L,
                    reminder.clientId,
                    dateKey,
                    scheduledAt,
                    now,
                    ReminderLogEntity.STATUS_TAKEN,
                    now
            );
        } else {
            log.scheduledAt = scheduledAt;
            log.takenAt = now;
            log.status = ReminderLogEntity.STATUS_TAKEN;
            log.updatedAt = now;
        }

        reminderLogDao.insertOrUpdate(log);
    }

    private void ensureDueLogs(long now, List<ReminderEntity> reminders, int lookbackDays) {
        long todayStart = AlarmScheduler.startOfDay(now);

        for (ReminderEntity reminder : reminders) {
            if (reminder.deleted) {
                continue;
            }

            if (reminder.repeatDaily) {
                for (int i = 0; i < lookbackDays; i++) {
                    long dayStart = todayStart - (i * AlarmScheduler.ONE_DAY_MILLIS);
                    long scheduledAt = AlarmScheduler.triggerAtForDay(dayStart, reminder.hour, reminder.minute);
                    updateOrCreateLogIfDue(reminder, scheduledAt, now);
                }
            } else {
                updateOrCreateLogIfDue(reminder, reminder.triggerAt, now);
            }
        }
    }

    private void updateOrCreateLogIfDue(ReminderEntity reminder, long scheduledAt, long now) {
        if (scheduledAt > now) {
            return;
        }

        String dateKey = dateKey(scheduledAt);
        ReminderLogEntity log = reminderLogDao.getByReminderAndDate(reminder.clientId, dateKey);
        int computedStatus = computeStatusByTime(reminder, scheduledAt, now);

        if (log == null) {
            reminderLogDao.insertOrUpdate(new ReminderLogEntity(
                    0L,
                    reminder.clientId,
                    dateKey,
                    scheduledAt,
                    0L,
                    computedStatus,
                    now
            ));
            return;
        }

        if (log.status == ReminderLogEntity.STATUS_TAKEN) {
            return;
        }

        if (computedStatus != log.status) {
            log.scheduledAt = scheduledAt;
            log.status = computedStatus;
            log.updatedAt = now;
            reminderLogDao.insertOrUpdate(log);
        }
    }

    private int computeStatusByTime(ReminderEntity reminder, long scheduledAt, long now) {
        if (reminder.lastTakenAt >= scheduledAt
                && reminder.lastTakenAt < scheduledAt + AlarmScheduler.ONE_DAY_MILLIS) {
            return ReminderLogEntity.STATUS_TAKEN;
        }

        if (now > scheduledAt + MISSED_GRACE_MILLIS) {
            return ReminderLogEntity.STATUS_MISSED;
        }

        return ReminderLogEntity.STATUS_PENDING;
    }

    private DailyCounts computeDayCounts(List<ReminderEntity> reminders, long dayStart, long now) {
        String date = dateKey(dayStart);
        DailyCounts counts = new DailyCounts();

        for (ReminderEntity reminder : reminders) {
            if (reminder.deleted) {
                continue;
            }

            long scheduledAt;
            if (reminder.repeatDaily) {
                scheduledAt = AlarmScheduler.triggerAtForDay(dayStart, reminder.hour, reminder.minute);
            } else {
                if (!date.equals(dateKey(reminder.triggerAt))) {
                    continue;
                }
                scheduledAt = reminder.triggerAt;
            }

            counts.total++;

            ReminderLogEntity log = reminderLogDao.getByReminderAndDate(reminder.clientId, date);
            int status = (log != null)
                    ? log.status
                    : computeStatusByTime(reminder, scheduledAt, now);

            if (status == ReminderLogEntity.STATUS_TAKEN) {
                counts.taken++;
            } else if (status == ReminderLogEntity.STATUS_MISSED) {
                counts.missed++;
            } else {
                counts.pending++;
            }
        }

        return counts;
    }

    private String dateKey(long timeMillis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timeMillis));
    }

    private String dayLabel(long timeMillis) {
        return new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date(timeMillis));
    }

    private void notifyReminderChanged() {
        appContext.sendBroadcast(new Intent(ACTION_REMINDERS_CHANGED));
    }

    private void postSave(SaveCallback callback, boolean success, Exception error) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onComplete(success, error));
    }

    private void postReminders(RemindersCallback callback, List<ReminderEntity> reminders) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onResult(new ArrayList<>(reminders)));
    }

    private void postSummary(SummaryCallback callback, Summary summary) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onResult(summary));
    }

    private static class DailyCounts {
        int total;
        int taken;
        int missed;
        int pending;
    }

    public static class Summary {
        public final int total;
        public final int taken;
        public final int missed;
        public final int pending;
        public final int adherencePercent;

        public Summary(int total, int taken, int missed, int pending) {
            this.total = total;
            this.taken = taken;
            this.missed = missed;
            this.pending = pending;
            this.adherencePercent = total == 0 ? 0 : Math.round((taken * 100f) / total);
        }
    }

    public static class DailyOverview {
        public final String dateKey;
        public final String dayLabel;
        public final int total;
        public final int taken;
        public final int missed;
        public final int pending;
        public final int adherencePercent;

        public DailyOverview(String dateKey, String dayLabel, int total, int taken, int missed, int pending) {
            this.dateKey = dateKey;
            this.dayLabel = dayLabel;
            this.total = total;
            this.taken = taken;
            this.missed = missed;
            this.pending = pending;
            this.adherencePercent = total == 0 ? 0 : Math.round((taken * 100f) / total);
        }
    }

    public static class WeeklyOverview {
        public final List<DailyOverview> days;
        public final int adherencePercent;

        public WeeklyOverview(List<DailyOverview> days, int adherencePercent) {
            this.days = days;
            this.adherencePercent = adherencePercent;
        }
    }

    public interface SaveCallback {
        void onComplete(boolean success, Exception error);
    }

    public interface ReminderCallback {
        void onResult(ReminderEntity reminder);
    }

    public interface RemindersCallback {
        void onResult(List<ReminderEntity> reminders);
    }

    public interface SummaryCallback {
        void onResult(Summary summary);
    }

    public interface WeeklyOverviewCallback {
        void onResult(WeeklyOverview overview);
    }
}
