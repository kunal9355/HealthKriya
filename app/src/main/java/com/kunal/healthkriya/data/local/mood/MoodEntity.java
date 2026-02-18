package com.kunal.healthkriya.data.local.mood;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "mood_table")
public class MoodEntity {

    @PrimaryKey
    @NonNull
    public String date;   // yyyy-MM-dd (PRIMARY KEY)

    public int moodLevel; // 1–5
    public String note;
    public long updatedAt;

    @Ignore
    public MoodEntity(@NonNull String date, int moodLevel, String note) {
        this.date = date;
        this.moodLevel = moodLevel;
        this.note = note;
        this.updatedAt = System.currentTimeMillis();
    }

    public MoodEntity(@NonNull String date, int moodLevel, String note, long updatedAt) {
        this.date = date;
        this.moodLevel = moodLevel;
        this.note = note;
        this.updatedAt = updatedAt;
    }
}
