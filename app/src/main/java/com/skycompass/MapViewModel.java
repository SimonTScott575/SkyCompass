package com.skycompass;

import androidx.lifecycle.ViewModel;

public class MapViewModel extends ViewModel {

    private double latitude;
    private double longitude;

    private boolean hasMyLocation;
    private double myLatitude;
    private double myLongitude;

    private boolean useMyLocation;

    public void setLocation(double latitude, double longitude, boolean myLocation) {

        if (myLocation) {
            myLatitude = latitude;
            myLongitude = longitude;
            hasMyLocation = true;
        }

        this.latitude = latitude;
        this.longitude = longitude;

    }

    public double getLatitude() {
        if (useMyLocation && hasMyLocation)
            return myLatitude;
        else
            return latitude;
    }

    public double getLongitude() {
        if (useMyLocation && hasMyLocation)
            return myLongitude;
        else
            return longitude;
    }

    public boolean hasMyLocation() {
        return hasMyLocation;
    }

    public boolean useMyLocation() {
        return useMyLocation;
    }

    public void setUseMyLocation(boolean autoSetAsMyLocation) {
        this.useMyLocation = autoSetAsMyLocation;
    }

}
