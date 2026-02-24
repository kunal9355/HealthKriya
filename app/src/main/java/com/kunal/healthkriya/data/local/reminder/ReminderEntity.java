package com.kunal.healthkriya.data.local.reminder;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reminder_table",
        indices = {@Index(value = "clientId", unique = true)}
)
public class ReminderEntity {
    public static final int SYNC_PENDING = 0;
    public static final int SYNC_SYNCED = 1;
    public static final int SYNC_ERROR = 2;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String clientId;

    public String name;
    public String dosage;
    public int hour;
    public int minute;
    public boolean repeatDaily;
    public long triggerAt;
    public boolean active;
    public long lastTakenAt;
    public long updatedAt;
    public int syncStatus;
    public boolean deleted;

    public ReminderEntity(
            long id,
            @NonNull String clientId,
            String name,
            String dosage,
            int hour,
            int minute,
            boolean repeatDaily,
            long triggerAt,
            boolean active,
            long lastTakenAt,
            long updatedAt,
            int syncStatus,
            boolean deleted
    ) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.dosage = dosage;
        this.hour = hour;
        this.minute = minute;
        this.repeatDaily = repeatDaily;
        this.triggerAt = triggerAt;
        this.active = active;
        this.lastTakenAt = lastTakenAt;
        this.updatedAt = updatedAt;
        this.syncStatus = syncStatus;
        this.deleted = deleted;
    }

    @Ignore
    public ReminderEntity(
            @NonNull String clientId,
            String name,
            String dosage,
            int hour,
            int minute,
            boolean repeatDaily,
            long triggerAt
    ) {
        this(
                0L,
                clientId,
                name,
                dosage,
                hour,
                minute,
                repeatDaily,
                triggerAt,
                true,
                0L,
                System.currentTimeMillis(),
                SYNC_PENDING,
                false
        );
    }
}
