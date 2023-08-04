package com.skycompass.compass;

import java.time.LocalDateTime;
import java.util.ArrayList;

import io.github.cosinekitty.astronomy.Aberration;
import io.github.cosinekitty.astronomy.Astronomy;
import io.github.cosinekitty.astronomy.EquatorEpoch;
import io.github.cosinekitty.astronomy.Equatorial;
import io.github.cosinekitty.astronomy.Observer;
import io.github.cosinekitty.astronomy.Refraction;
import io.github.cosinekitty.astronomy.Time;
import io.github.cosinekitty.astronomy.Topocentric;

public final class AstronomyUtil {

    public static Coordinate coordinate(CelestialObject body, double latitude, double longitude, LocalDateTime dateTime) {

        Time time = new Time(
            dateTime.getYear(), dateTime.getMonth().getValue(), dateTime.getDayOfMonth(),
            dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()
        );
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

    public static Coordinate[] coordinateRangeAndHorizon(
        CelestialObject body,
        double latitude, double longitude,
        LocalDateTime start, LocalDateTime end, int stepSeconds
    ) {

        ArrayList<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(coordinate(body, latitude, longitude, start));

        for (LocalDateTime date = start.plusSeconds(stepSeconds); date.isBefore(end); date = date.plusSeconds(stepSeconds)) {

            Coordinate coordinate = coordinate(body, latitude, longitude, date);

            if (
                coordinate.getAltitude() > 0 && coordinates.get(coordinates.size()-1).getAltitude() < 0
            ) {

                coordinates.add(searchHorizonCoordinate(
                    body,
                    latitude, longitude,
                    date.minusSeconds(stepSeconds), date.plusSeconds(stepSeconds), 60
                ));

            } else if (
                coordinate.getAltitude() < 0 && coordinates.get(coordinates.size()-1).getAltitude() > 0
            ) {

                coordinates.add(searchHorizonCoordinate(
                    body,
                    latitude, longitude,
                    date.minusSeconds(stepSeconds), date.plusSeconds(stepSeconds), 60
                ));

            }

            coordinates.add(coordinate);

        }

        return coordinates.toArray(new Coordinate[0]);

    }

    private static Coordinate searchHorizonCoordinate(
        CelestialObject body,
        double latitude, double longitude,
        LocalDateTime start, LocalDateTime end, int stepSeconds
    ) {

        Coordinate result = null;

        Coordinate prevCoordinate = coordinate(body, latitude, longitude, start);
        for (LocalDateTime date = start.plusSeconds(stepSeconds); date.isBefore(end); date = date.plusSeconds(stepSeconds)) {

            Coordinate coordinate = coordinate(body, latitude, longitude, date);

            if (prevCoordinate.getAltitude() < 0 && coordinate.getAltitude() >= 0) {
                result = coordinate;
                break;
            } else if (prevCoordinate.getAltitude() > 0 && coordinate.getAltitude() <= 0) {
                result = prevCoordinate;
                break;
            }

            prevCoordinate = coordinate;

        }

        return result;

    }

}
