package com.icarus1.clock;

import androidx.lifecycle.ViewModel;

import com.icarus1.util.Time;

public class ClockViewModel extends ViewModel {

    private boolean useSystemTime;
    private Time time;
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

    public void setTime(int hour, int minute, int second) {
        this.time = new Time(hour, minute, second);
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Time getTime() {
        return time;
    }

    public void setTimeZone(int UTCOffset, String location) {
        this.UTCOffset = UTCOffset;
        this.location = location;
    }

    public int getUTCOffset() {
        return UTCOffset;
    }

    public String getLocation() {
        return location;
    }

}