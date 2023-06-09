package com.skycompass;

import androidx.lifecycle.ViewModel;

import com.skycompass.util.Time;
import com.skycompass.util.TimeZone;
import com.skycompass.views.TimeZonePicker;

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