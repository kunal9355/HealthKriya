package com.kunal.healthkriya.data.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.kunal.healthkriya.data.model.EmergencyCardModel;
import com.kunal.healthkriya.core.HealthKriyaApp;
import com.kunal.healthkriya.data.model.UserModel;

public class LocalSource {

    private static final String PREF = "healthkriya_prefs";
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_COMPLETED = "profileCompleted";
    private final SharedPreferences prefs;

    public LocalSource() {
        prefs = HealthKriyaApp.getInstance()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ---------- USER ----------
    public void saveUser(UserModel user) {
        prefs.edit()
                .putString(KEY_UID, user.getUid())
                .putString(KEY_EMAIL, user.getEmail())
                .putBoolean(KEY_PROFILE_COMPLETED, user.isProfileCompleted())
                .apply();
    }

    public UserModel getUser() {
        String uid = prefs.getString(KEY_UID, null);
        if (uid == null) return null;

        UserModel user = new UserModel(uid, prefs.getString(KEY_EMAIL, ""));
        user.setProfileCompleted(prefs.getBoolean(KEY_PROFILE_COMPLETED, false));
        return user;
    }

    public void clearUser() {
        prefs.edit()
                .remove(KEY_UID)
                .remove(KEY_EMAIL)
                .remove(KEY_PROFILE_COMPLETED)
                .apply();
    }

    public void saveEmergencyCard(EmergencyCardModel card) {
        if (card == null || getCurrentUid() == null) {
            return;
        }

        prefs.edit()
                .putString(emergencyKey("name"), card.getName())
                .putString(emergencyKey("age"), card.getAge())
                .putString(emergencyKey("bloodGroup"), card.getBloodGroup())
                .putString(emergencyKey("contact"), card.getEmergencyContact())
                .putString(emergencyKey("conditions"), card.getMedicalConditions())
                .putString(emergencyKey("allergies"), card.getAllergies())
                .putString(emergencyKey("medicines"), card.getMedicines())
                .putString(emergencyKey("address"), card.getAddress())
                .apply();
    }

    public EmergencyCardModel getEmergencyCard() {
        EmergencyCardModel card = new EmergencyCardModel();
        if (getCurrentUid() == null) {
            return card;
        }

        card.setName(prefs.getString(emergencyKey("name"), ""));
        card.setAge(prefs.getString(emergencyKey("age"), ""));
        card.setBloodGroup(prefs.getString(emergencyKey("bloodGroup"), ""));
        card.setEmergencyContact(prefs.getString(emergencyKey("contact"), ""));
        card.setMedicalConditions(prefs.getString(emergencyKey("conditions"), ""));
        card.setAllergies(prefs.getString(emergencyKey("allergies"), ""));
        card.setMedicines(prefs.getString(emergencyKey("medicines"), ""));
        card.setAddress(prefs.getString(emergencyKey("address"), ""));
        return card;
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

    private String getCurrentUid() {
        String uid = prefs.getString(KEY_UID, null);
        if (uid == null || uid.trim().isEmpty()) {
            return null;
        }
        return uid.trim();
    }

    private String emergencyKey(String suffix) {
        return "emergency_" + getCurrentUid() + "_" + suffix;
    }
}
