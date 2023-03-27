package com.icarus1.calendar;

import androidx.lifecycle.ViewModel;

public class CalendarViewModel extends ViewModel {

    private boolean systemDate;
    private int year;
    private int month;
    private int dayOfMonth;

    public CalendarViewModel() {
        systemDate = true;
        year = 2000;
        month = 0;
        dayOfMonth = 0;
    }

    public boolean isSystemDate() {
        return systemDate;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDate(int year, int month, int dayOfMonth, boolean systemDate) {
        this.systemDate = systemDate;
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

}
