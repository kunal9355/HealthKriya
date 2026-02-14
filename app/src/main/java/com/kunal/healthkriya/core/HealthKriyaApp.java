package com.kunal.healthkriya.core;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

// core/HealthKriyaApp.java
public class HealthKriyaApp extends Application {

    private static final String TAG = "HealthKriyaApp";
    private static HealthKriyaApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
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
