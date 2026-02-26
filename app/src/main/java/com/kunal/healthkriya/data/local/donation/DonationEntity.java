package com.kunal.healthkriya.data.local.donation;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "donation_table")
public class DonationEntity {

    public static final int SYNC_PENDING = 0;
    public static final int SYNC_SYNCED = 1;
    public static final int SYNC_ERROR = 2;

    public static final String CATEGORY_BLOOD = "blood";
    public static final String CATEGORY_MEDICINE = "medicine";
    public static final String ACTION_DONATE = "donate";
    public static final String ACTION_REQUEST = "request";

    @PrimaryKey
    @NonNull
    public String clientId;

    public String category;
    public String actionType;
    public String title;
    public String detail;
    public long updatedAt;
    public int syncStatus;
    public boolean deleted;

    @Ignore
    public DonationEntity(
            @NonNull String clientId,
            String category,
            String actionType,
            String title,
            String detail
    ) {
        this(
                clientId,
                category,
                actionType,
                title,
                detail,
                System.currentTimeMillis(),
                SYNC_PENDING,
                false
        );
    }

    public DonationEntity(
            @NonNull String clientId,
            String category,
            String actionType,
            String title,
            String detail,
            long updatedAt,
            int syncStatus,
            boolean deleted
    ) {
        this.clientId = clientId;
        this.category = category;
        this.actionType = actionType;
        this.title = title;
        this.detail = detail;
        this.updatedAt = updatedAt;
        this.syncStatus = syncStatus;
        this.deleted = deleted;
    }
}
