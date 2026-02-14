package com.kunal.healthkriya.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kunal.healthkriya.core.AppState;
import com.kunal.healthkriya.data.model.UserModel;
import com.kunal.healthkriya.data.model.home.EmergencySummary;
import com.kunal.healthkriya.data.model.home.HomeDataModel;
import com.kunal.healthkriya.data.model.home.MedicineSummary;
import com.kunal.healthkriya.data.model.home.MoodSummary;
import com.kunal.healthkriya.data.model.home.UserSummary;

public class HomeRepository {

    private static HomeRepository instance;

    private HomeRepository() {}

    public static HomeRepository getInstance() {
        if (instance == null) instance = new HomeRepository();
        return instance;
    }

    public LiveData<HomeDataModel> getHomeData() {
        MutableLiveData<HomeDataModel> liveData = new MutableLiveData<>();

        // -------- USER SUMMARY --------
        UserModel user = AppState.getInstance().getCurrentUser();
        UserSummary userSummary;

        if (user != null) {
            userSummary = new UserSummary(
                    user.getEmail(),   // name future me profile se aayega
                    user.isProfileCompleted()
            );
        } else {
            userSummary = new UserSummary("HealthKriya User", false);
        }

        // -------- MOOD SUMMARY (dummy for now) --------
        MoodSummary moodSummary = new MoodSummary(null, false);

        // -------- MEDICINE SUMMARY (dummy for now) --------
        MedicineSummary medicineSummary = new MedicineSummary(0, 0);

        // -------- EMERGENCY SUMMARY (dummy for now) --------
        EmergencySummary emergencySummary =
                new EmergencySummary(false, false);

        HomeDataModel homeData = new HomeDataModel(
                userSummary,
                moodSummary,
                medicineSummary,
                emergencySummary
        );

        liveData.postValue(homeData);
        return liveData;
    }
}
