package com.icarus1;

import androidx.lifecycle.ViewModel;

import com.icarus1.util.Time;
import com.icarus1.util.TimeZone;
import com.icarus1.views.TimeZonePicker;

public class ClockViewModel extends ViewModel {

    private boolean useSystemTime;
    private Time time;
    private TimeZone timeZone;
    private TimeZonePicker.UseDST useDST;

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

    public TimeZonePicker.UseDST getUseDST() {
        return useDST;
    }

    public void setUseDST(TimeZonePicker.UseDST useDST) {
        this.useDST = useDST;
    }
}