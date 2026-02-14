package com.kunal.healthkriya.data.model.home;

public class MoodSummary {

    private String todayMood;   // happy, sad, neutral
    private boolean loggedToday;

    public MoodSummary(String todayMood, boolean loggedToday) {
        this.todayMood = todayMood;
        this.loggedToday = loggedToday;
    }

    public String getTodayMood() {
        return todayMood;
    }

    public boolean isLoggedToday() {
        return loggedToday;
    }
}
