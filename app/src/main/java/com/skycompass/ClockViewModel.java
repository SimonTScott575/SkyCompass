package com.skycompass;

import androidx.lifecycle.ViewModel;

import com.skycompass.util.TimeZone;
import com.skycompass.views.TimeZonePicker;

import java.time.LocalTime;

public class ClockViewModel extends ViewModel {

    private boolean useSystemTime;
    private LocalTime time;
    private TimeZone timeZone;

    public ClockViewModel() {
        useSystemTime = true;
    }

    public boolean isUseSystemTime() {
        return useSystemTime;
    }

    public void setUseSystemTime(boolean useSystemTime) {
        this.useSystemTime = useSystemTime;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

}