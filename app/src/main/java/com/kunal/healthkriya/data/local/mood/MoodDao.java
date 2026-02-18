package com.kunal.healthkriya.data.local.mood;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(MoodEntity mood);

    @Query("SELECT * FROM mood_table WHERE date = :date LIMIT 1")
    MoodEntity getMoodByDate(String date);

    @Query("SELECT * FROM mood_table ORDER BY date DESC")
    List<MoodEntity> getAllMoods();

    @Query("SELECT date FROM mood_table ORDER BY date DESC")
    List<String> getAllMoodDates();

}
