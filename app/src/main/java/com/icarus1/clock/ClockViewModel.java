package com.icarus1.clock;

import androidx.lifecycle.ViewModel;

import com.icarus1.util.Time;
import com.icarus1.util.TimeZone;

public class ClockViewModel extends ViewModel {

    private boolean useSystemTime;
    private Time time;
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

    public void setTime(Time time) {
        this.time = time;
    }

    public Time getTime() {
        return time;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

}