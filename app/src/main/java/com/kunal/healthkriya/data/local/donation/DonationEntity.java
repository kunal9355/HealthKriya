package com.kunal.healthkriya.data.local.donation;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "donation_table",
        indices = {
                @Index(value = "clientId", unique = true),
                @Index("userId"),
                @Index("type"),
                @Index("action"),
                @Index("status"),
                @Index("urgency"),
                @Index("isPublic"),
                @Index("updatedAt"),
                @Index(value = {"isPublic", "status"})
        }
)
public class DonationEntity {
    public static final int SYNC_PENDING = 0;
    public static final int SYNC_SYNCED = 1;
    public static final int SYNC_ERROR = 2;

    public static final String TYPE_BLOOD = "blood";
    public static final String TYPE_MEDICINE = "medicine";

    public static final String ACTION_DONATE = "donate";
    public static final String ACTION_REQUEST = "request";

    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_EXPIRED = "expired";

    public static final String URGENCY_NORMAL = "normal";
    public static final String URGENCY_HIGH = "high";
    public static final String URGENCY_CRITICAL = "critical";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String clientId;

    public String userId;

    @NonNull
    public String type;

    @NonNull
    public String action;

    @NonNull
    public String status;

    @NonNull
    public String urgency;

    public boolean isPublic;
    public boolean deleted;
    public int syncStatus;

    public long createdAt;
    public long updatedAt;

    public int helpCount;

    // Optional shared detail fields for future donation rebuild.
    public String title;
    public String name;
    public String description;
    public String city;
    public String contact;
    public String bloodGroup;
    public String medicineName;
    public String medicineExpiry;

    public DonationEntity() {
        long now = System.currentTimeMillis();
        this.clientId = "";
        this.userId = "";
        this.type = TYPE_BLOOD;
        this.action = ACTION_REQUEST;
        this.status = STATUS_ACTIVE;
        this.urgency = URGENCY_NORMAL;
        this.isPublic = true;
        this.deleted = false;
        this.syncStatus = SYNC_PENDING;
        this.createdAt = now;
        this.updatedAt = now;
        this.helpCount = 0;
        this.title = "";
        this.name = "";
        this.description = "";
        this.city = "";
        this.contact = "";
        this.bloodGroup = "";
        this.medicineName = "";
        this.medicineExpiry = "";
    }

    @Ignore
    public DonationEntity(
            @NonNull String clientId,
            @NonNull String userId,
            @NonNull String type,
            @NonNull String action
    ) {
        this();
        this.clientId = clientId;
        this.userId = userId;
        this.type = type;
        this.action = action;
    }
}
