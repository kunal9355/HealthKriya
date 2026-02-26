package com.kunal.healthkriya.data.local.donation;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DonationEntity.class}, version = 1)
public abstract class DonationDatabase extends RoomDatabase {

    private static volatile DonationDatabase INSTANCE;

    public abstract DonationDao donationDao();

    public static DonationDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DonationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            DonationDatabase.class,
                            "donation_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
