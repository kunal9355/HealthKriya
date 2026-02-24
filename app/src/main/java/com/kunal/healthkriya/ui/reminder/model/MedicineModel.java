package com.kunal.healthkriya.ui.reminder.model;

public class MedicineModel {

    private String name;
    private String dosage;
    private String time;

    public MedicineModel(String name, String dosage, String time) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTime() { return time; }
}