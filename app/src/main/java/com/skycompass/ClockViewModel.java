package com.skycompass;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClockViewModel extends ViewModel {

    public List<String> timeZones;

    private boolean useSystemTime;
    private LocalTime time;
    private LocalDate date;
    private ZoneId zoneId;
    private ZoneOffset zoneOffset;

    public ClockViewModel() {

        timeZones = new ArrayList<>();
        timeZones.addAll(ZoneId.getAvailableZoneIds());
        Collections.sort(timeZones);

        useSystemTime = true;

        time = LocalTime.of(0, 0, 0);
        date = LocalDate.of(2000, 1, 1);
        zoneOffset = ZoneOffset.ofHours(0);

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

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        zoneOffset = null;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneOffset(ZoneOffset offset) {
        zoneOffset = offset;
        zoneId = null;
    }

    public ZoneOffset getZoneOffset() {
        if (zoneId != null)
            return ZonedDateTime.of(date, time, zoneId).getOffset();
        else
            return zoneOffset;
    }

}