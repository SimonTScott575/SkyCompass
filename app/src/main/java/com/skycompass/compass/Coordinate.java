package com.skycompass.compass;

class Coordinate {

    private final double altitude;
    private final double azimuth;

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
