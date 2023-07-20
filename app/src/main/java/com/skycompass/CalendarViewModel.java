package com.skycompass;

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;

public class CalendarViewModel extends ViewModel {

    private boolean systemDate;
    private LocalDate date;

    public CalendarViewModel() {
        systemDate = true;
        date = LocalDate.of(2000, 1, 1);
    }

    public boolean isSystemDate() {
        return systemDate;
    }

    public void setSystemDate(boolean systemDate) {
        this.systemDate = systemDate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
