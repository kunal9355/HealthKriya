package com.kunal.healthkriya.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.kunal.healthkriya.data.repository.MoodRepository;
import com.kunal.healthkriya.data.repository.ReminderRepository;

public class HealthKriyaApp extends Application {

    private static final String TAG = "HealthKriyaApp";
    private static HealthKriyaApp instance;
    private String lastSyncedUid;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
        }

        // Night Mode
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Sync mood + reminder data when auth user is available (works for login-after-launch flow too)
        MoodRepository moodRepository = new MoodRepository(this);
        ReminderRepository reminderRepository = new ReminderRepository(this);
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            String uid = auth.getUid();
            if (uid == null) {
                lastSyncedUid = null;
                reminderRepository.stopRealtimeSync();
                moodRepository.stopRealtimeSync();
                return;
            }
            if (!uid.equals(lastSyncedUid)) {
                lastSyncedUid = uid;
                moodRepository.restoreFromFirebase();
                reminderRepository.restoreFromFirebase();
            }
        });
    }

    public static HealthKriyaApp getInstance() {
        return instance;
    }
}
