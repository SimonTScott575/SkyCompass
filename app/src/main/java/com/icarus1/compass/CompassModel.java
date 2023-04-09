package com.icarus1.compass;

import io.github.cosinekitty.astronomy.Aberration;
import io.github.cosinekitty.astronomy.Astronomy;
import io.github.cosinekitty.astronomy.EquatorEpoch;
import io.github.cosinekitty.astronomy.Equatorial;
import io.github.cosinekitty.astronomy.Observer;
import io.github.cosinekitty.astronomy.Refraction;
import io.github.cosinekitty.astronomy.Time;
import io.github.cosinekitty.astronomy.Topocentric;

public class CompassModel {

    private double longitude;
    private double latitude;
    private int year;
    private int month; // 1 to 12
    private int dayOfMonth; // 1 to 31

    public CompassModel(double longitude, double latitude) {
        setLocation(latitude, longitude);
        setDate(1970, 0, 0);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDate(int year, int month, int dayOfMonth) {

        this.year = year;
        this.month = month + 1;
        this.dayOfMonth = dayOfMonth + 1;

    }

    public Coordinate getCoordinate(CelestialObject body, int hour, int minute, double seconds) {

        Time time = new Time(year, month, dayOfMonth, hour, minute, seconds);
        Observer observer = new Observer(latitude, longitude, 0);
        Equatorial equatorial = Astronomy.equator(
            body.getBody(),
            time,
            observer,
            EquatorEpoch.OfDate,
            Aberration.None
        );

        Topocentric topocentric = Astronomy.horizon(
            time,
            observer,
            equatorial.getRa(),
            equatorial.getDec(),
            Refraction.None
        );

        double altitude = topocentric.getAltitude() * 2 * Math.PI / 360d;
        double azimuth = topocentric.getAzimuth() * 2 * Math.PI / 360d;

        return new Coordinate(altitude, azimuth);

    }

}
