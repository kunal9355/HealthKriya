package com.kunal.healthkriya.ui.mood.calendar;

public class CalendarDateModel {

    public String day;
    public String date;
    public String fullDate; // yyyy-MM-dd
    public boolean isSelected;
    public Integer moodLevel;

    public CalendarDateModel(String day, String date,
                             String fullDate, boolean isSelected, Integer moodLevel) {
        this.day = day;
        this.date = date;
        this.fullDate = fullDate;
        this.isSelected = isSelected;
        this.moodLevel = moodLevel;
    }

}
