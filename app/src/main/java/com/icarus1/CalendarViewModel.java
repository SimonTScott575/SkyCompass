package com.icarus1;

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

    public void setSystemDate(boolean systemDate) {
        this.systemDate = systemDate;
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

    public void setDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

}
