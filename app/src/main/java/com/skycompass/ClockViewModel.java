package com.skycompass;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClockViewModel extends ViewModel {

    private List<String> timeZones;

    private List<String> timeZonesSearch;

    private String timeZoneSearchText;

    private boolean useSystemTime;
    private LocalTime time;
    private LocalDate date;
    private ZoneId zoneId;
    private ZoneOffset zoneOffset;

    public ClockViewModel() {

        timeZones = new ArrayList<>();
        timeZones.addAll(ZoneId.getAvailableZoneIds());
        Collections.sort(timeZones);

        timeZonesSearch = new ArrayList<>();
        timeZonesSearch.addAll(timeZones);

        timeZoneSearchText = "";

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

    public void setTimeZoneSearch(String searchText) {

        searchText = searchText.toLowerCase();

        if (searchText.equals(timeZoneSearchText))
            return;

        if (searchText.startsWith(timeZoneSearchText)) {

            for (int i = 0; i < timeZonesSearch.size();) {

                String name = timeZonesSearch.get(i);

                if (!name.toLowerCase().contains(searchText))
                    timeZonesSearch.remove(i);
                else
                    i++;

            }

        } else {

            timeZonesSearch.clear();

            for (String name : timeZones)
                if (name.toLowerCase().contains(searchText))
                    timeZonesSearch.add(name);

        }

        timeZoneSearchText = searchText;

    }

    public List<String> getTimeZonesSearch() {
        return timeZonesSearch;
    }

}