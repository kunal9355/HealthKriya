package com.kunal.healthkriya.ui.splash;

import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.repository.AppRepository;

public class SplashViewModel extends ViewModel {

    public enum Destination {
        ONBOARDING,
        AUTH,
        HOME   // future
    }

    private final AppRepository repository = AppRepository.getInstance();

    public Destination decideNext() {

        // Case 1: Firebase user exists → logged in
        if (repository.isFirebaseLoggedIn()) {
            return Destination.HOME; // future: HomeFragment
        }

        // Case 2: Not logged in
        if (repository.isOnboardingSeen()) {
            return Destination.AUTH;
        } else {
            return Destination.ONBOARDING;
        }
    }
}
