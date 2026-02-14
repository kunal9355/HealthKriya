package com.kunal.healthkriya.data.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.kunal.healthkriya.core.HealthKriyaApp;
import com.kunal.healthkriya.data.model.UserModel;

public class LocalSource {

    private static final String PREF = "healthkriya_prefs";
    private final SharedPreferences prefs;

    public LocalSource() {
        prefs = HealthKriyaApp.getInstance()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ---------- USER ----------
    public void saveUser(UserModel user) {
        prefs.edit()
                .putString("uid", user.getUid())
                .putString("email", user.getEmail())
                .putBoolean("profileCompleted", user.isProfileCompleted())
                .apply();
    }

    public UserModel getUser() {
        String uid = prefs.getString("uid", null);
        if (uid == null) return null;

        UserModel user = new UserModel(uid, prefs.getString("email", ""));
        user.setProfileCompleted(prefs.getBoolean("profileCompleted", false));
        return user;
    }

    public void clearUser() {
        prefs.edit()
                .remove("uid")
                .remove("email")
                .remove("profileCompleted")
                .apply();
    }

    // ---------- ONBOARDING ----------
    public void setOnboardingSeen(boolean seen) {
        prefs.edit().putBoolean("onboardingSeen", seen).apply();
    }

    public boolean isOnboardingSeen() {
        return prefs.getBoolean("onboardingSeen", false);
    }

    // ---------- CLEAR ALL ----------
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
