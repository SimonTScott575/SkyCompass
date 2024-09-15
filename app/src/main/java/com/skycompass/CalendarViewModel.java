package com.skycompass;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;

public class CalendarViewModel extends ViewModel {

    private LocalDate date;

    public CalendarViewModel() {
        date = LocalDate.of(2000, 1, 1);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
