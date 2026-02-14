package com.kunal.healthkriya.data.model.home;

public class EmergencySummary {

    private boolean medicalIdCompleted;
    private boolean sosReady;

    public EmergencySummary(boolean medicalIdCompleted, boolean sosReady) {
        this.medicalIdCompleted = medicalIdCompleted;
        this.sosReady = sosReady;
    }

    public boolean isMedicalIdCompleted() {
        return medicalIdCompleted;
    }

    public boolean isSosReady() {
        return sosReady;
    }
}
