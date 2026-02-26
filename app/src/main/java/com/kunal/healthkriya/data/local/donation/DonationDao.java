package com.kunal.healthkriya.data.local.donation;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DonationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(DonationEntity donation);

    @Query("SELECT * FROM donation_table WHERE clientId = :clientId LIMIT 1")
    DonationEntity getByClientIdSync(String clientId);

    @Query("SELECT * FROM donation_table WHERE deleted = 0 ORDER BY updatedAt DESC")
    List<DonationEntity> getAllActive();

    @Query("SELECT * FROM donation_table WHERE deleted = 0 AND category = :category AND actionType = :actionType ORDER BY updatedAt DESC LIMIT :limit")
    List<DonationEntity> getRecentByType(String category, String actionType, int limit);

    @Query("SELECT * FROM donation_table WHERE syncStatus = :syncStatus AND deleted = 0 ORDER BY updatedAt DESC")
    List<DonationEntity> getBySyncStatus(int syncStatus);

    @Query("SELECT * FROM donation_table WHERE syncStatus != :syncedStatus AND deleted = 0 ORDER BY updatedAt DESC")
    List<DonationEntity> getUnsynced(int syncedStatus);
}
