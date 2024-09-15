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

    public ClockViewModel() {

        timeZones = new ArrayList<>();
        timeZones.addAll(ZoneId.getAvailableZoneIds());
        Collections.sort(timeZones);

        timeZonesSearch = new ArrayList<>();
        timeZonesSearch.addAll(timeZones);

        timeZoneSearchText = "";

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