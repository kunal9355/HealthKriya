package com.kunal.healthkriya.data.local.mood;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "mood_table")
public class MoodEntity {
    public static final int SYNC_PENDING = 0;
    public static final int SYNC_SYNCED = 1;
    public static final int SYNC_ERROR = 2;

    @PrimaryKey
    @NonNull
    public String date;   // yyyy-MM-dd (PRIMARY KEY)

    public int moodLevel; // 1–5
    public String note;
    public long updatedAt;
    public int syncStatus;
    public boolean deleted;

    @Ignore
    public MoodEntity(@NonNull String date, int moodLevel, String note) {
        this(date, moodLevel, note, System.currentTimeMillis(), SYNC_PENDING, false);
    }

    @Ignore
    public MoodEntity(@NonNull String date, int moodLevel, String note, long updatedAt) {
        this(date, moodLevel, note, updatedAt, SYNC_PENDING, false);
    }

    public MoodEntity(
            @NonNull String date,
            int moodLevel,
            String note,
            long updatedAt,
            int syncStatus,
            boolean deleted
    ) {
        this.date = date;
        this.moodLevel = moodLevel;
        this.note = note;
        this.updatedAt = updatedAt;
        this.syncStatus = syncStatus;
        this.deleted = deleted;
    }
}
