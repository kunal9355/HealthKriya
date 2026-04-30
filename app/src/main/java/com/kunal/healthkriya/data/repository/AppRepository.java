package com.kunal.healthkriya.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.kunal.healthkriya.core.AppState;
import com.kunal.healthkriya.data.model.EmergencyCardModel;
import com.kunal.healthkriya.data.model.UserModel;
import com.kunal.healthkriya.data.model.auth.AuthResult;
import com.kunal.healthkriya.data.source.FirebaseSource;
import com.kunal.healthkriya.data.source.LocalSource;

public class AppRepository {

    private static AppRepository instance;

    private final FirebaseSource firebase;
    private final LocalSource local;
    private final MutableLiveData<UserModel> currentUserLive = new MutableLiveData<>();
    private final MutableLiveData<EmergencyCardModel> emergencyCardLive = new MutableLiveData<>();

    private AppRepository() {
        firebase = new FirebaseSource();
        local = new LocalSource();
        // Initialize with cached user
        currentUserLive.setValue(local.getUser());
        emergencyCardLive.setValue(local.getEmergencyCard());
    }

    public static AppRepository getInstance() {
        if (instance == null) instance = new AppRepository();
        return instance;
    }

    // ---------- AUTH ----------
    public LiveData<AuthResult> login(String email, String password) {
        return Transformations.map(firebase.login(email, password), user -> {
            if (user != null) {
                return AuthResult.success(user);
            } else {
                return AuthResult.error(
                        firebase.getLastAuthErrorCode(),
                        firebase.getLastAuthErrorMessage()
                );
            }
        });
    }

    public LiveData<AuthResult> register(String email, String password) {
        return Transformations.map(firebase.register(email, password, ""), user -> {
            if (user != null) {
                return AuthResult.success(user);
            } else {
                return AuthResult.error(
                        firebase.getLastAuthErrorCode(),
                        firebase.getLastAuthErrorMessage()
                );
            }
        });
    }

    public void validateAuthSession(SessionCallback callback) {
        firebase.validateCurrentSession(valid -> {
            if (!valid) {
                clearSessionState();
            }
            if (callback != null) {
                callback.onResult(valid);
            }
        });
    }

    public boolean isFirebaseLoggedIn() {
        return firebase.getCurrentUser() != null;
    }

    // ---------- USER ----------
    public void saveUser(UserModel user) {
        local.saveUser(user);
        AppState.getInstance().setCurrentUser(user);
        currentUserLive.postValue(user);
        emergencyCardLive.postValue(local.getEmergencyCard());
    }

    public UserModel getCachedUser() {
        return local.getUser();
    }

    public LiveData<UserModel> getCurrentUserLive() {
        return currentUserLive;
    }

    public LiveData<EmergencyCardModel> getEmergencyCardLive() {
        return emergencyCardLive;
    }

    public EmergencyCardModel getEmergencyCard() {
        EmergencyCardModel card = emergencyCardLive.getValue();
        return card != null ? card : local.getEmergencyCard();
    }

    public void saveEmergencyCard(EmergencyCardModel card) {
        local.saveEmergencyCard(card);
        emergencyCardLive.postValue(local.getEmergencyCard());
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
        clearSessionState();
    }

    public void clearSessionState() {
        local.clearUser();
        AppState.getInstance().setCurrentUser(null);
        currentUserLive.postValue(null);
        emergencyCardLive.postValue(local.getEmergencyCard());
    }

    public void deleteAccount(String currentPassword, ActionCallback callback) {
        firebase.deleteCurrentUser(currentPassword, (success, message) -> {
            if (success) {
                clearSessionState();
            }
            if (callback != null) {
                callback.onComplete(success, message);
            }
        });
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

    public interface SessionCallback {
        void onResult(boolean validSession);
    }

    public interface ActionCallback {
        void onComplete(boolean success, String message);
    }
}
