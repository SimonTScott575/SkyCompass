package com.icarus1.map;

import androidx.lifecycle.ViewModel;

import org.osmdroid.util.GeoPoint;

public class MapViewModel extends ViewModel {

    private double markerLatitude;
    private double markerLongitude;
    private String markerLocationDescription;
    private GeoPoint myLocation;

    public MapViewModel() {
        markerLatitude = markerLongitude = 0d;
    }

    public double getMarkerLatitude() {
        return markerLatitude;
    }

    public double getMarkerLongitude() {
        return markerLongitude;
    }

    public String getMarkerLocationDescription() {
        return markerLocationDescription;
    }

    public void setMarkerLocation(double latitude, double longitude, String description) {
        this.markerLatitude = latitude;
        this.markerLongitude = longitude;
        this.markerLocationDescription = description;
    }

    public GeoPoint getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(GeoPoint userLocation) {
        this.myLocation = userLocation;
    }

}
