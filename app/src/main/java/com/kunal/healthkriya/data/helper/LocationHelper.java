package com.kunal.healthkriya.data.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.kunal.healthkriya.data.local.donation.DonationEntity;

public final class LocationHelper {

    private static final String PREF = "donation_runtime_prefs";
    private static final String KEY_CITY = "preferred_city";
    private static final String KEY_RADIUS_KM = "preferred_radius_km";
    private static final String KEY_BLOOD_GROUP = "preferred_blood_group";
    private static final String KEY_MEDICINE = "preferred_medicine";

    private LocationHelper() {
    }

    public static void setPreferredCity(Context context, String city) {
        String normalized = normalizeCity(city);
        if (normalized.isEmpty()) {
            return;
        }
        prefs(context).edit().putString(KEY_CITY, normalized).apply();
    }

    public static String getPreferredCity(Context context) {
        return prefs(context).getString(KEY_CITY, "");
    }

    public static void setPreferredRadiusKm(Context context, int radiusKm) {
        int bounded = Math.max(1, Math.min(radiusKm, 50));
        prefs(context).edit().putInt(KEY_RADIUS_KM, bounded).apply();
    }

    public static int getPreferredRadiusKm(Context context) {
        return prefs(context).getInt(KEY_RADIUS_KM, 10);
    }

    public static boolean isNearbyCity(Context context, String requestCity) {
        String userCity = normalizeCity(getPreferredCity(context));
        if (userCity.isEmpty()) {
            return true;
        }
        return userCity.equals(normalizeCity(requestCity));
    }

    public static void setPreferredBloodGroup(Context context, String bloodGroup) {
        String value = normalizeValue(bloodGroup);
        if (value.isEmpty()) {
            return;
        }
        prefs(context).edit().putString(KEY_BLOOD_GROUP, value).apply();
    }

    public static String getPreferredBloodGroup(Context context) {
        return prefs(context).getString(KEY_BLOOD_GROUP, "");
    }

    public static void setPreferredMedicine(Context context, String medicineName) {
        String value = normalizeValue(medicineName);
        if (value.isEmpty()) {
            return;
        }
        prefs(context).edit().putString(KEY_MEDICINE, value).apply();
    }

    public static String getPreferredMedicine(Context context) {
        return prefs(context).getString(KEY_MEDICINE, "");
    }

    public static void rememberDonationContext(Context context, DonationEntity donation) {
        if (donation == null) {
            return;
        }
        setPreferredCity(context, donation.city);
        if (DonationEntity.TYPE_BLOOD.equals(donation.type)) {
            setPreferredBloodGroup(context, donation.bloodGroup);
            return;
        }
        setPreferredMedicine(context, donation.medicineName);
    }

    public static int matchScore(Context context, DonationEntity donation) {
        if (donation == null) {
            return 0;
        }

        int score = 0;
        if (isNearbyCity(context, donation.city)) {
            score += 1;
        }

        if (DonationEntity.TYPE_BLOOD.equals(donation.type)) {
            String preferredBlood = normalizeValue(getPreferredBloodGroup(context));
            String requestBlood = normalizeValue(donation.bloodGroup);
            if (!preferredBlood.isEmpty() && preferredBlood.equals(requestBlood)) {
                score += 3;
            }
        } else {
            String preferredMedicine = normalizeValue(getPreferredMedicine(context));
            String requestMedicine = normalizeValue(donation.medicineName);
            if (!preferredMedicine.isEmpty() && requestMedicine.contains(preferredMedicine)) {
                score += 3;
            }
        }

        if (DonationEntity.URGENCY_CRITICAL.equals(donation.urgency)) {
            score += 1;
        }
        return score;
    }

    public static boolean isBestMatch(Context context, DonationEntity donation) {
        return matchScore(context, donation) >= 3;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String normalizeCity(String city) {
        return normalizeValue(city);
    }

    private static String normalizeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}
