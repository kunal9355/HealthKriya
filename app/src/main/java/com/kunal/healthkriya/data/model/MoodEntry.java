package com.kunal.healthkriya.data.model;

public class MoodEntry {

    public String date;      // yyyy-MM-dd
    public int moodLevel;    // 1–5
    public String note;

    public MoodEntry(String date, int moodLevel, String note) {
        this.date = date;
        this.moodLevel = moodLevel;
        this.note = note;
    }
}
