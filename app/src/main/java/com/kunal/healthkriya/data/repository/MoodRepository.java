package com.kunal.healthkriya.data.repository;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kunal.healthkriya.data.local.mood.MoodDao;
import com.kunal.healthkriya.data.local.mood.MoodDatabase;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.source.FirebaseMoodSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoodRepository {

    private final MoodDao moodDao;
    private final FirebaseMoodSource firebaseSource;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private ListenerRegistration firebaseListener;

    public MoodRepository(Context context) {
        moodDao = MoodDatabase.getInstance(context).moodDao();
        firebaseSource = new FirebaseMoodSource();
    }

    public void saveMood(MoodEntity mood) {
        saveMood(mood, null);
    }

    public void saveMood(MoodEntity mood, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                mood.deleted = false;
                mood.updatedAt = System.currentTimeMillis();
                mood.syncStatus = MoodEntity.SYNC_PENDING;
                moodDao.insertOrUpdate(mood);

                if (callback != null) callback.onComplete(true, null);
                pushMoodToFirebase(mood);
            } catch (Exception e) {
                if (callback != null) callback.onComplete(false, e);
            }
        });
    }

    public void softDeleteMood(String date, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            MoodEntity existing = moodDao.getMoodByDateSync(date);
            if (existing == null) {
                if (callback != null) callback.onComplete(false, new IllegalStateException("Mood not found"));
                return;
            }

            existing.deleted = true;
            existing.updatedAt = System.currentTimeMillis();
            existing.syncStatus = MoodEntity.SYNC_PENDING;
            moodDao.insertOrUpdate(existing);

            if (callback != null) callback.onComplete(true, null);
            pushMoodToFirebase(existing);
        });
    }

    public void undoDeleteMood(String date, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            MoodEntity existing = moodDao.getMoodByDateSync(date);
            if (existing == null) {
                if (callback != null) callback.onComplete(false, new IllegalStateException("Mood not found"));
                return;
            }

            existing.deleted = false;
            existing.updatedAt = System.currentTimeMillis();
            existing.syncStatus = MoodEntity.SYNC_PENDING;
            moodDao.insertOrUpdate(existing);

            if (callback != null) callback.onComplete(true, null);
            pushMoodToFirebase(existing);
        });
    }

    // Backward compatibility wrappers
    public void deleteMood(String date) {
        softDeleteMood(date, null);
    }

    public void undoDelete(String date) {
        undoDeleteMood(date, null);
    }

    private void pushMoodToFirebase(MoodEntity mood) {
        String date = mood.date;
        long version = mood.updatedAt;

        firebaseSource.syncMood(mood, success -> {
            if (success) return;
            EXECUTOR.execute(() -> markSyncErrorIfCurrent(date, version));
        });
    }

    private void markSyncErrorIfCurrent(String date, long updatedAt) {
        MoodEntity current = moodDao.getMoodByDateSync(date);
        if (current != null && current.updatedAt == updatedAt && current.syncStatus == MoodEntity.SYNC_PENDING) {
            current.syncStatus = MoodEntity.SYNC_ERROR;
            moodDao.insertOrUpdate(current);
        }
    }

    public void startRealtimeSync() {
        stopRealtimeSync();
        firebaseListener = firebaseSource.listenToMoodChanges(mood ->
                EXECUTOR.execute(() -> resolveConflict(mood))
        );
    }

    public void stopRealtimeSync() {
        if (firebaseListener != null) {
            firebaseListener.remove();
            firebaseListener = null;
        }
    }

    private void resolveConflict(MoodEntity incoming) {
        MoodEntity local = moodDao.getMoodByDateSync(incoming.date);

        if (local == null || incoming.updatedAt > local.updatedAt) {
            moodDao.insertOrUpdate(incoming);
            return;
        }

        if (incoming.updatedAt == local.updatedAt) {
            boolean changed =
                    local.syncStatus != incoming.syncStatus
                            || local.deleted != incoming.deleted
                            || local.moodLevel != incoming.moodLevel
                            || !Objects.equals(local.note, incoming.note);

            if (changed) moodDao.insertOrUpdate(incoming);
        }
    }

    public void restoreFromFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("moods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> EXECUTOR.execute(() -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String date = doc.getId();
                        Long moodLevel = doc.getLong("moodLevel");
                        String note = doc.getString("note");
                        Long updatedAt = doc.getLong("updatedAt");
                        Boolean deleted = doc.getBoolean("deleted");

                        if (moodLevel == null) continue;

                        moodDao.insertOrUpdate(
                                new MoodEntity(
                                        date,
                                        moodLevel.intValue(),
                                        note,
                                        updatedAt != null ? updatedAt : System.currentTimeMillis(),
                                        MoodEntity.SYNC_SYNCED,
                                        deleted != null && deleted
                                )
                        );
                    }
                }));

        startRealtimeSync();
    }

    public void getMoodByDate(String date, MoodCallback callback) {
        EXECUTOR.execute(() -> {
            MoodEntity mood = moodDao.getMoodByDate(date);
            if (callback != null) callback.onResult(mood);
        });
    }

    public void getWeeklyMoods(AnalyticsCallback callback) {
        EXECUTOR.execute(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            String endDate = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, -6);
            String startDate = sdf.format(cal.getTime());

            List<MoodEntity> list = moodDao.getMoodsBetween(startDate, endDate);
            if (callback != null) callback.onResult(list);
        });
    }

    public void getMonthlyMoods(AnalyticsCallback callback) {
        EXECUTOR.execute(() -> {
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            String monthPrefix = monthFormat.format(new Date()) + "-%";
            List<MoodEntity> list = moodDao.getMoodsInMonth(monthPrefix);
            if (callback != null) callback.onResult(list);
        });
    }

    public void getAllMoods(AllMoodsCallback callback) {
        EXECUTOR.execute(() -> {
            List<MoodEntity> moods = moodDao.getAllMoods();
            if (callback != null) callback.onResult(moods);
        });
    }

    public void getCurrentStreak(StreakCallback callback) {
        EXECUTOR.execute(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayStr = sdf.format(new Date());

            List<String> dates = moodDao.getMoodDatesUntil(todayStr);
            int streak = 0;

            Date expectedDate;
            try {
                expectedDate = sdf.parse(todayStr);
            } catch (ParseException e) {
                if (callback != null) callback.onResult(0);
                return;
            }

            Calendar cal = Calendar.getInstance();
            for (String dateStr : dates) {
                try {
                    Date dbDate = sdf.parse(dateStr);
                    if (dbDate != null && dbDate.equals(expectedDate)) {
                        streak++;
                        cal.setTime(expectedDate);
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                        expectedDate = cal.getTime();
                    } else {
                        break;
                    }
                } catch (ParseException e) {
                    break;
                }
            }
            if (callback != null) callback.onResult(streak);
        });
    }

    public interface AnalyticsCallback {
        void onResult(List<MoodEntity> list);
    }

    public interface MoodCallback {
        void onResult(MoodEntity mood);
    }

    public interface AllMoodsCallback {
        void onResult(List<MoodEntity> moods);
    }

    public interface SaveCallback {
        void onComplete(boolean success, Exception error);
    }

    public interface StreakCallback {
        void onResult(int streak);
    }
}
