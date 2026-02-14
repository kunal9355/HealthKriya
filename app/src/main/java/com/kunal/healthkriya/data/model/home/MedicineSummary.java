package com.kunal.healthkriya.data.model.home;

public class MedicineSummary {

    private int pendingTodayCount;
    private int streakDays;

    public MedicineSummary(int pendingTodayCount, int streakDays) {
        this.pendingTodayCount = pendingTodayCount;
        this.streakDays = streakDays;
    }

    public int getPendingTodayCount() {
        return pendingTodayCount;
    }

    public int getStreakDays() {
        return streakDays;
    }
}
