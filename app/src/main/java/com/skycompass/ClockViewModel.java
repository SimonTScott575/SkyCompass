package com.skycompass;

import androidx.lifecycle.ViewModel;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ClockViewModel extends ViewModel {

    private List<String> timeZones;

    private List<String> timeZonesSearch;

    private String timeZoneSearchText;

    public ClockViewModel() {

        timeZones = new ArrayList<>();
        timeZones.addAll(ZoneId.getAvailableZoneIds());
        Collections.sort(timeZones);
/*
        ZoneId.getAvailableZoneIds().stream()
                .map(ZoneId::of)
                .map(id -> id.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                .distinct()
                .collect(Collectors.toList());
*/
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