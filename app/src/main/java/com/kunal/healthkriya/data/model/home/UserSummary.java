package com.kunal.healthkriya.data.model.home;

public class UserSummary {

    private String name;
    private boolean profileCompleted;

    public UserSummary(String name, boolean profileCompleted) {
        this.name = name;
        this.profileCompleted = profileCompleted;
    }

    public String getName() {
        return name;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }
}
