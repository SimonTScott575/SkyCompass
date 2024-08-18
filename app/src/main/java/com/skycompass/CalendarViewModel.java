package com.skycompass;

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;

public class CalendarViewModel extends ViewModel {

    private boolean useSystemDate;
    private LocalDate date;
    private LocalDate systemDate;

    public CalendarViewModel() {
        useSystemDate = true;
        date = LocalDate.of(2000, 1, 1);
    }

    public LocalDate getDate() {

        if (useSystemDate)
            return systemDate;

        return date;
    }

    public void setDate(LocalDate date) {

        if (useSystemDate)
            this.systemDate = date;

        this.date = date;

    }

    public boolean useSystemDate() {
        return useSystemDate;
    }

    public void setUseSystemDate(boolean systemDate) {
        this.useSystemDate = systemDate;
    }

}
