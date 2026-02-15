package com.kunal.healthkriya.core;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;

// core/HealthKriyaApp.java
public class HealthKriyaApp extends Application {

    private static final String TAG = "HealthKriyaApp";
    private static HealthKriyaApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        try {
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
            if (firebaseApp == null) {
                Log.w(TAG, "Firebase is not configured (google-services.json missing).");
            }
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization failed", e);
        }
    }

    public static HealthKriyaApp getInstance() {
        return instance;
    }
}
