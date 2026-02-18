package com.kunal.healthkriya.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.kunal.healthkriya.data.repository.MoodRepository;

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

        // Sync mood data when auth user is available (works for login-after-launch flow too)
        MoodRepository repository = new MoodRepository(this);
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {
            String uid = auth.getUid();
            if (uid == null) {
                lastSyncedUid = null;
                return;
            }
            if (!uid.equals(lastSyncedUid)) {
                lastSyncedUid = uid;
                repository.restoreFromFirebase();
            }
        });
    }

    public static HealthKriyaApp getInstance() {
        return instance;
    }
}
