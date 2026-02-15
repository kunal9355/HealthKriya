package com.kunal.healthkriya.ui.mood.calendar;

public class CalendarDateModel {

    public String day;     // Mon, Tue
    public String date;    // 21
    public boolean isSelected;

    public CalendarDateModel(String day, String date, boolean isSelected) {
        this.day = day;
        this.date = date;
        this.isSelected = isSelected;
    }
}
