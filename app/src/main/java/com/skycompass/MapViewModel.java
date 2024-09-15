package com.skycompass;

import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private double latitude;
    private double longitude;

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
