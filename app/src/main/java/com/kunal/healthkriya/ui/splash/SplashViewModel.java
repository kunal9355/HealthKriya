package com.kunal.healthkriya.ui.splash;

import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.repository.AppRepository;

public class SplashViewModel extends ViewModel {

    public enum Destination {
        ONBOARDING,
        AUTH,
        HOME
    }

    private final AppRepository repository = AppRepository.getInstance();

    public void decideNext(DestinationCallback callback) {
        // First check if firebase thinks we are logged in
        if (repository.isFirebaseLoggedIn()) {
            // Validate the session in background to be sure
            repository.validateAuthSession(validSession -> {
                if (validSession) {
                    callback.onResult(Destination.HOME);
                } else {
                    // Session expired or invalid, go to Auth
                    checkOnboarding(callback);
                }
            });
        } else {
            // Not logged in at all
            checkOnboarding(callback);
        }
    }

    private void checkOnboarding(DestinationCallback callback) {
        if (repository.isOnboardingSeen()) {
            callback.onResult(Destination.AUTH);
        } else {
            callback.onResult(Destination.ONBOARDING);
        }
    }

    public interface DestinationCallback {
        void onResult(Destination destination);
    }
}
