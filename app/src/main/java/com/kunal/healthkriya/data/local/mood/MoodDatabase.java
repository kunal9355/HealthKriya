package com.kunal.healthkriya.data.local.mood;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {MoodEntity.class}, version = 2)
public abstract class MoodDatabase extends RoomDatabase {

    private static MoodDatabase INSTANCE;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE mood_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract MoodDao moodDao();

    public static synchronized MoodDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MoodDatabase.class,
                    "mood_db"
            ).addMigrations(MIGRATION_1_2).build();
        }
        return INSTANCE;
    }
}
