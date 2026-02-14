package com.kunal.healthkriya.data.model.home;

public class HomeDataModel {

    private UserSummary user;
    private MoodSummary mood;
    private MedicineSummary medicine;
    private EmergencySummary emergency;

    public HomeDataModel(
            UserSummary user,
            MoodSummary mood,
            MedicineSummary medicine,
            EmergencySummary emergency
    ) {
        this.user = user;
        this.mood = mood;
        this.medicine = medicine;
        this.emergency = emergency;
    }

    public UserSummary getUser() {
        return user;
    }

    public MoodSummary getMood() {
        return mood;
    }

    public MedicineSummary getMedicine() {
        return medicine;
    }

    public EmergencySummary getEmergency() {
        return emergency;
    }
}
