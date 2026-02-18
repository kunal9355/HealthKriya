package com.kunal.healthkriya.data.local.mood;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MoodEntity.class}, version = 1)
public abstract class MoodDatabase extends RoomDatabase {

    private static MoodDatabase INSTANCE;

    public abstract MoodDao moodDao();

    public static synchronized MoodDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MoodDatabase.class,
                    "mood_db"
            ).build();
        }
        return INSTANCE;
    }
}

