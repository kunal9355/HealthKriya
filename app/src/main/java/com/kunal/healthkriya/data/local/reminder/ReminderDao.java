package com.kunal.healthkriya.data.local.reminder;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(ReminderEntity reminder);

    @Query("SELECT * FROM reminder_table WHERE deleted = 0 ORDER BY triggerAt ASC")
    List<ReminderEntity> getNotDeletedReminders();

    @Query("SELECT * FROM reminder_table WHERE active = 1 AND deleted = 0 ORDER BY triggerAt ASC")
    List<ReminderEntity> getActiveReminders();

    @Query("SELECT * FROM reminder_table WHERE active = 1 AND deleted = 0")
    List<ReminderEntity> getActiveRemindersForBoot();

    @Query("SELECT * FROM reminder_table WHERE clientId = :clientId LIMIT 1")
    ReminderEntity getByClientId(String clientId);

    @Query("SELECT * FROM reminder_table WHERE syncStatus = :syncStatus AND deleted = 0")
    List<ReminderEntity> getBySyncStatus(int syncStatus);
}
