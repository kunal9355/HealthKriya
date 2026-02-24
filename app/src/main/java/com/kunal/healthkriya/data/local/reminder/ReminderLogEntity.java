package com.kunal.healthkriya.data.local.reminder;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reminder_log_table",
        indices = {@Index(value = {"reminderClientId", "dateKey"}, unique = true)}
)
public class ReminderLogEntity {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_TAKEN = 1;
    public static final int STATUS_MISSED = 2;

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String reminderClientId;

    @NonNull
    public String dateKey;

    public long scheduledAt;
    public long takenAt;
    public int status;
    public long updatedAt;

    public ReminderLogEntity(
            long id,
            @NonNull String reminderClientId,
            @NonNull String dateKey,
            long scheduledAt,
            long takenAt,
            int status,
            long updatedAt
    ) {
        this.id = id;
        this.reminderClientId = reminderClientId;
        this.dateKey = dateKey;
        this.scheduledAt = scheduledAt;
        this.takenAt = takenAt;
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
