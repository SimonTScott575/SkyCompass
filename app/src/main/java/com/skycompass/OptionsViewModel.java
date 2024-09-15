package com.skycompass;

import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalTime;

public class OptionsViewModel extends ViewModel {

    private boolean isSystemLocation = false;
    private boolean isSystemDate = false;
    private boolean isSystemTime = false;

    private LocalDate date = LocalDate.of(2000, 1, 1);
    private LocalTime time = LocalTime.of(0, 0, 0);
    private int timeZoneOffset = 0;
    private String timeDescription = null;
    private MainViewModel.Location location = new MainViewModel.Location(0, 0);


    public boolean isSystemLocation() {
        return isSystemLocation;
    }

    public void setSystemLocation(boolean systemLocation) {
        isSystemLocation = systemLocation;
    }

    public boolean isSystemDate() {
        return isSystemDate;
    }

    public void setSystemDate(boolean systemDate) {
        isSystemDate = systemDate;
    }

    public boolean isSystemTime() {
        return isSystemTime;
    }

    public void setSystemTime(boolean systemTime) {
        isSystemTime = systemTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public String getTimeDescription() {
        return timeDescription;
    }

    public void setTimeDescription(String timeDescription) {
        this.timeDescription = timeDescription;
    }

    public MainViewModel.Location getLocation() {
        return location;
    }

    public void setLocation(MainViewModel.Location location) {
        this.location = location;
    }
}