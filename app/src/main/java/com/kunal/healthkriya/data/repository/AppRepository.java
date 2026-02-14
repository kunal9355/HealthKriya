package com.kunal.healthkriya.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kunal.healthkriya.core.AppState;
import com.kunal.healthkriya.data.model.UserModel;
import com.kunal.healthkriya.data.source.FirebaseSource;
import com.kunal.healthkriya.data.source.LocalSource;

public class AppRepository {

    private static AppRepository instance;

    private final FirebaseSource firebase;
    private final LocalSource local;
    private final MutableLiveData<UserModel> currentUserLive = new MutableLiveData<>();

    private AppRepository() {
        firebase = new FirebaseSource();
        local = new LocalSource();
        // Initialize with cached user
        currentUserLive.setValue(local.getUser());
    }

    public static AppRepository getInstance() {
        if (instance == null) instance = new AppRepository();
        return instance;
    }

    // ---------- AUTH ----------
    public LiveData<UserModel> login(String email, String password) {
        return firebase.login(email, password);
    }

    public LiveData<UserModel> register(String email, String password) {
        return firebase.register(email, password);
    }

    public boolean isFirebaseLoggedIn() {
        return firebase.getCurrentUser() != null;
    }

    // ---------- USER ----------
    public void saveUser(UserModel user) {
        local.saveUser(user);
        AppState.getInstance().setCurrentUser(user);
        currentUserLive.postValue(user);
    }

    public UserModel getCachedUser() {
        return local.getUser();
    }

    public LiveData<UserModel> getCurrentUserLive() {
        return currentUserLive;
    }

    // ---------- ONBOARDING ----------
    public void setOnboardingSeen() {
        local.setOnboardingSeen(true);
    }

    public boolean isOnboardingSeen() {
        return local.isOnboardingSeen();
    }

    // ---------- LOGOUT ----------
    public void logout() {
        firebase.logout();
        local.clearUser();
        AppState.getInstance().setCurrentUser(null);
        currentUserLive.postValue(null);
    }

    public MutableLiveData<Boolean> verifyEmailPhone(String email, String phone) {
        return firebase.verifyEmailPhone(email, phone);
    }

    public void sendPasswordResetEmail(String email,
                                       Runnable success,
                                       Runnable failure) {
        firebase.sendPasswordResetEmail(email, success, failure);
    }

    public MutableLiveData<Boolean> changePassword(String currentPassword, String newPassword) {
        return firebase.changePassword(currentPassword, newPassword);
    }

    // 🔥 SAVE USER TO FIRESTORE + LOCAL + APPSTATE
    public MutableLiveData<Boolean> saveUserProfile(UserModel user) {

        MutableLiveData<Boolean> result = new MutableLiveData<>();

        firebase.updateUserProfile(user)
                .observeForever(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        saveUser(user);
                        result.postValue(true);
                    } else {
                        result.postValue(false);
                    }
                });

        return result;
    }
}
