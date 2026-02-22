package com.kunal.healthkriya.data.local.mood;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {MoodEntity.class}, version = 3)
public abstract class MoodDatabase extends RoomDatabase {

    private static MoodDatabase INSTANCE;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE mood_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            if (!hasColumn(db, "mood_table", "syncStatus")) {
                db.execSQL("ALTER TABLE mood_table ADD COLUMN syncStatus INTEGER NOT NULL DEFAULT 0");
            }
            if (!hasColumn(db, "mood_table", "deleted")) {
                db.execSQL("ALTER TABLE mood_table ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0");
            }
        }
    };

    public abstract MoodDao moodDao();

    public static synchronized MoodDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MoodDatabase.class,
                    "mood_db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
        }
        return INSTANCE;
    }

    private static boolean hasColumn(SupportSQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.query("PRAGMA table_info(" + tableName + ")");
        try {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && columnName.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }
}
