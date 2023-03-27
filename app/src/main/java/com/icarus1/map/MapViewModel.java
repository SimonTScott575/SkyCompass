package com.icarus1.map;

import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private double latitude;
    private double longitude;
    private String location;

    public MapViewModel() {
        latitude = longitude = 0d;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLatitudeLongitude(double latitude, double longitude, String location) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }

}
