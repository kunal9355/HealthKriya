package com.kunal.healthkriya.data.local.reminder;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {ReminderEntity.class, ReminderLogEntity.class}, version = 2)
public abstract class ReminderDatabase extends RoomDatabase {

    private static volatile ReminderDatabase INSTANCE;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            if (!hasTable(db, "reminder_log_table")) {
                db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `reminder_log_table` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "`reminderClientId` TEXT NOT NULL, " +
                                "`dateKey` TEXT NOT NULL, " +
                                "`scheduledAt` INTEGER NOT NULL, " +
                                "`takenAt` INTEGER NOT NULL, " +
                                "`status` INTEGER NOT NULL, " +
                                "`updatedAt` INTEGER NOT NULL)"
                );
                db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_reminder_log_table_reminderClientId_dateKey` " +
                                "ON `reminder_log_table` (`reminderClientId`, `dateKey`)"
                );
            }
        }
    };

    public abstract ReminderDao reminderDao();
    public abstract ReminderLogDao reminderLogDao();

    public static ReminderDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReminderDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ReminderDatabase.class,
                            "reminder_db"
                    ).addMigrations(MIGRATION_1_2).build();
                }
            }
        }
        return INSTANCE;
    }

    private static boolean hasTable(SupportSQLiteDatabase db, String tableName) {
        Cursor cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }
}
