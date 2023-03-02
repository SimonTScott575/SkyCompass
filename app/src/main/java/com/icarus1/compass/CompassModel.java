package com.icarus1.compass;

import io.github.cosinekitty.astronomy.Aberration;
import io.github.cosinekitty.astronomy.Astronomy;
import io.github.cosinekitty.astronomy.Body;
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
        setLongitude(longitude);
        setLatitude(latitude);
        setDate(1970, 0, 0);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
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

    public Coordinate getCoordinate(int hour, int minute, double seconds) {

        Time time = new Time(year, month, dayOfMonth, hour, minute, seconds);
        Observer observer = new Observer(latitude, longitude, 0);
        Equatorial equatorial = Astronomy.equator(
            Body.Sun,
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

/*
        double altitude;
        double azimuth = time.getSolarTime() < 0.5d ? Math.PI*3d/2d : Math.PI/2d;

        if (time.getSolarTime() < 0.5d) {
            altitude = Math.PI * time.getSolarTime()/0.5d;
        } else {
            altitude = Math.PI * (2d - time.getSolarTime()/0.5d);
        }
*/

        return new Coordinate(altitude, azimuth);

    }

}
