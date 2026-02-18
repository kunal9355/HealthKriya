package com.kunal.healthkriya.data.repository;

import android.content.Context;

import com.kunal.healthkriya.data.local.mood.MoodDao;
import com.kunal.healthkriya.data.local.mood.MoodDatabase;
import com.kunal.healthkriya.data.local.mood.MoodEntity;

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
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public MoodRepository(Context context) {
        moodDao = MoodDatabase.getInstance(context).moodDao();
    }

    public void saveMood(MoodEntity mood) {
        EXECUTOR.execute(() -> moodDao.insertOrUpdate(mood));
    }

    public void saveMood(MoodEntity mood, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                moodDao.insertOrUpdate(mood);
                if (callback != null) callback.onComplete(true, null);
            } catch (Exception e) {
                if (callback != null) callback.onComplete(false, e);
            }
        });
    }

    public void getMoodByDate(String date, MoodCallback callback) {
        EXECUTOR.execute(() -> {
            MoodEntity mood = moodDao.getMoodByDate(date);
            if (callback != null) callback.onResult(mood);
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
            List<String> dates = moodDao.getAllMoodDates();
            int streak = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);

            Date expectedDate;
            try {
                expectedDate = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                if (callback != null) callback.onResult(0);
                return;
            }

            Calendar cal = Calendar.getInstance();

            for (String dateStr : dates) {
                Date dbDate;
                try {
                    dbDate = sdf.parse(dateStr);
                } catch (ParseException e) {
                    break;
                }

                if (dbDate != null && dbDate.equals(expectedDate)) {
                    streak++;
                    cal.setTime(expectedDate);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    expectedDate = cal.getTime();
                } else {
                    break;
                }
            }

            if (callback != null) callback.onResult(streak);
        });
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
