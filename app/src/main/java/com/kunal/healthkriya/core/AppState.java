package com.kunal.healthkriya.core;

import com.kunal.healthkriya.data.model.UserModel;

// core/AppState.java
public class AppState {

    private static AppState instance;

    private UserModel currentUser;
    private boolean isLoggedIn = false;

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserModel user) {
        this.currentUser = user;
        this.isLoggedIn = (user != null);
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
