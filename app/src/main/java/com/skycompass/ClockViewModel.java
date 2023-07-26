package com.skycompass;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class ClockViewModel extends ViewModel {

    private boolean useSystemTime;
    private LocalTime time;
    private LocalDate date;
    private ZoneOffset zoneOffset;
    private String id;

    public ClockViewModel() {
        useSystemTime = true;
    }

    public boolean isUseSystemTime() {
        return useSystemTime;
    }

    public void setUseSystemTime(boolean useSystemTime) {
        this.useSystemTime = useSystemTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    public void setTime(@NonNull LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setZoneOffset(@NonNull ZoneOffset zoneOffset) {
        this.zoneOffset = zoneOffset;
    }

    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}