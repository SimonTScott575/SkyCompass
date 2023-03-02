package com.icarus1.compass;

public class Coordinate {

    private double altitude;
    private double azimuth;

    public Coordinate(double altitude, double azimuth) {
        this.altitude = altitude;
        this.azimuth = azimuth;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getX() {
        return (Math.PI/2d - Math.abs(getAltitude())) / (Math.PI/2d) * Math.sin(getAzimuth());
    }

    public double getY() {
        return (Math.PI/2d - Math.abs(getAltitude())) / (Math.PI/2d) * Math.cos(getAzimuth());
    }

}
