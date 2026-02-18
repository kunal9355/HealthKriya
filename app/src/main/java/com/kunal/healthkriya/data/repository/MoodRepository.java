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
        EXECUTOR.execute(() -> {
            mood.updatedAt = System.currentTimeMillis();
            moodDao.insertOrUpdate(mood);
            firebaseSource.syncMood(mood);
        });
    }

    public void startRealtimeSync() {
        stopRealtimeSync();

        firebaseListener = firebaseSource.listenToMoodChanges(mood -> {
            EXECUTOR.execute(() -> resolveConflict(mood));
        });
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
        }
        // else ignore (local is newer)
    }


    public void saveMood(MoodEntity mood, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                mood.updatedAt = System.currentTimeMillis();
                moodDao.insertOrUpdate(mood);
                firebaseSource.syncMood(mood);
                if (callback != null) callback.onComplete(true, null);
            } catch (Exception e) {
                if (callback != null) callback.onComplete(false, e);
            }
        });
    }

    public void restoreFromFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("moods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String date = doc.getId();
                            Long moodLevel = doc.getLong("moodLevel");
                            String note = doc.getString("note");
                            Long updatedAt = doc.getLong("updatedAt");
                            if (moodLevel != null) {
                                moodDao.insertOrUpdate(
                                        new MoodEntity(
                                                date,
                                                moodLevel.intValue(),
                                                note,
                                                updatedAt != null ? updatedAt : System.currentTimeMillis()
                                        )
                                );
                            }
                        }
                    });
                });

        stopRealtimeSync();
        firebaseListener = firebaseSource.listenToMoodChanges(mood -> {
            EXECUTOR.execute(() -> resolveConflict(mood));
        });

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
                } catch (ParseException e) { break; }
            }
            if (callback != null) callback.onResult(streak);
        });
    }

    public interface AnalyticsCallback { void onResult(List<MoodEntity> list); }
    public interface MoodCallback { void onResult(MoodEntity mood); }
    public interface AllMoodsCallback { void onResult(List<MoodEntity> moods); }
    public interface SaveCallback { void onComplete(boolean success, Exception error); }
    public interface StreakCallback { void onResult(int streak); }
}
