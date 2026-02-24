package com.kunal.healthkriya.data.local.reminder;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReminderLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(ReminderLogEntity log);

    @Query("SELECT * FROM reminder_log_table WHERE reminderClientId = :clientId AND dateKey = :dateKey LIMIT 1")
    ReminderLogEntity getByReminderAndDate(String clientId, String dateKey);

    @Query("SELECT * FROM reminder_log_table WHERE dateKey BETWEEN :startDate AND :endDate")
    List<ReminderLogEntity> getBetweenDates(String startDate, String endDate);

    @Query("DELETE FROM reminder_log_table WHERE reminderClientId = :clientId")
    void deleteByReminder(String clientId);
}
