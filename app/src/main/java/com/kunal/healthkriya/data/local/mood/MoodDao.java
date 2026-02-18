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

    @Query("SELECT * FROM mood_table WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    List<MoodEntity> getMoodsBetween(String startDate, String endDate);

    @Query("SELECT * FROM mood_table ORDER BY date ASC")
    List<MoodEntity> getAllMoods();

    @Query("SELECT * FROM mood_table WHERE date LIKE :monthPrefix ORDER BY date ASC")
    List<MoodEntity> getMoodsInMonth(String monthPrefix);

    @Query("SELECT date FROM mood_table ORDER BY date DESC")
    List<String> getAllMoodDates();

    @Query("SELECT date FROM mood_table WHERE date <= :today ORDER BY date DESC")
    List<String> getMoodDatesUntil(String today);

}
