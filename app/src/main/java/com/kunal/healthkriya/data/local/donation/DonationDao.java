package com.kunal.healthkriya.data.local.donation;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DonationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(DonationEntity donation);

    @Query("SELECT * FROM donation_table WHERE clientId = :clientId LIMIT 1")
    DonationEntity getByClientIdSync(String clientId);

    @Query("SELECT * FROM donation_table WHERE clientId = :clientId LIMIT 1")
    LiveData<DonationEntity> observeByClientId(String clientId);

    @Query("SELECT * FROM donation_table WHERE userId = :userId AND deleted = 0 ORDER BY updatedAt DESC")
    LiveData<List<DonationEntity>> observeByUserId(String userId);

    @Query("SELECT * FROM donation_table WHERE userId = :userId AND deleted = 0 ORDER BY updatedAt DESC")
    List<DonationEntity> getByUserIdSync(String userId);

    @Query("SELECT * FROM donation_table WHERE isPublic = 1 AND deleted = 0 AND status = :status ORDER BY updatedAt DESC")
    LiveData<List<DonationEntity>> observePublicByStatus(String status);

    @Query("SELECT * FROM donation_table WHERE isPublic = 1 AND deleted = 0 ORDER BY updatedAt DESC")
    LiveData<List<DonationEntity>> observeAllPublic();

    @Query("SELECT * FROM donation_table WHERE isPublic = 1 AND deleted = 0 ORDER BY updatedAt DESC")
    List<DonationEntity> getAllPublicSync();

    @Query("SELECT * FROM donation_table WHERE syncStatus != :syncedStatus AND deleted = 0 ORDER BY updatedAt DESC")
    List<DonationEntity> getUnsynced(int syncedStatus);

    @Query("SELECT COUNT(*) FROM donation_table WHERE userId = :userId AND action = :action AND createdAt >= :fromTs AND deleted = 0")
    int countUserRequestsSince(String userId, String action, long fromTs);

    @Query("SELECT * FROM donation_table WHERE action = :action AND status = :status AND isPublic = 1 AND deleted = 0 AND updatedAt < :olderThanTs")
    List<DonationEntity> getStalePublicRequests(String action, String status, long olderThanTs);

    @Query("DELETE FROM donation_table")
    void clearAll();
}
