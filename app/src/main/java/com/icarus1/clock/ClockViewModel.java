package com.icarus1.clock;

import androidx.lifecycle.ViewModel;

public class ClockViewModel extends ViewModel {

    private boolean useSystemTime;
    private int hour;
    private int minute;
    private int second;
    private int UTCOffset;
    private String location;

    public ClockViewModel() {
        useSystemTime = true;
    }

    public boolean isUseSystemTime() {
        return useSystemTime;
    }

    public void setUseSystemTime(boolean useSystemTime) {
        this.useSystemTime = useSystemTime;
    }

    public void setTime(int hour, int minute, int second, int UTCOffset, String location) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.UTCOffset = UTCOffset;
        this.location = location;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public int getUTCOffset() {
        return UTCOffset;
    }

    public String getLocation() {
        return location;
    }

}