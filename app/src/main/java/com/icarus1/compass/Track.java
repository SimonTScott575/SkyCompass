package com.icarus1.compass;

import android.graphics.Canvas;

public class Track {

    private float radius;
    private float diameter;

    private float width;
    private static final float fractionBlank = 0.2f;
    private float objectRadius;

    public Track(float radius) {

        setDimensions(radius);

    }

    public final void setDimensions(float radius) {

        this.radius = radius;
        diameter = 2*radius;

        width = radius*0.02f;
        objectRadius = 0.05f*radius;

    }

    public void drawTracks(int currentHour, CompassModel compass, CelestialObject body, Canvas canvas) {

        double[] x = new double[25];
        double[] y = new double[25];
        double[] altitude = new double[25];

        {
            Coordinate coordinate = compass.getCoordinate(body, 0, 0, 0);
            x[0] = coordinate.getX();
            y[0] = coordinate.getY();
            altitude[0] = coordinate.getAltitude();
            for (int hour = 1; hour < 25; hour++) {

                coordinate = compass.getCoordinate(body, hour, 0, 0);

                x[hour] = coordinate.getX();
                y[hour] = coordinate.getY();
                altitude[hour] = coordinate.getAltitude();

                if (altitude[hour] > 0 && altitude[hour-1] < 0) {

                    Coordinate c = getHorizonCoordinate(false, hour-1, compass, body);

                    x[hour-1] = c.getX();
                    y[hour-1] = c.getY();

                } else if (altitude[hour] < 0 && altitude[hour-1] > 0) {

                    Coordinate c = getHorizonCoordinate(true, hour-1, compass, body);

                    x[hour] = c.getX();
                    y[hour] = c.getY();

                }

            }
        }

        for (int hour = 0; hour < 24; hour++) {

            double startX = x[hour];
            double startY = y[hour];
            double endX = x[hour+1];
            double endY = y[hour+1];

            if (altitude[hour] < 0 && altitude[hour+1] > 0) {
                double length = Math.sqrt( Math.pow(startX, 2) + Math.pow(startY, 2) );
                startX /= length;
                startY /= length;
            } else if (altitude[hour] > 0 && altitude[hour+1] < 0) {
                double length = Math.sqrt( Math.pow(endX, 2) + Math.pow(endY, 2) );
                endX /= length;
                endY /= length;
            } else if (altitude[hour] < 0 && altitude[hour+1] < 0) {
                continue;
            }

            float length = (float) (Math.sqrt( Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) ));
            float padding = length * fractionBlank/2f;
            float angle = (float) Math.atan2(endY - startY, endX - startX);

            length *= radius;

            padding *= radius;

            startX *= radius;
            startY *= radius;

            startX += radius;
            startY += radius;

            startY = diameter - startY;

            canvas.save();
            canvas.rotate(-angle*360f/(2*(float)Math.PI), (float) startX, (float) startY); // (float)(startX + (endX - startX)/2f), (float)(startY + (endY - startY)/2f));

            int prevAlpha = body.getPaint().getAlpha();
            body.getPaint().setAlpha(alphaOfTrack(hour, currentHour));
            canvas.drawRect((float) startX + padding, (float) startY, (float) startX + length - padding, (float) startY + width, body.getPaint());
            body.getPaint().setAlpha(prevAlpha);

            canvas.restore();

        }

    }

    public void drawCurrentPosition(int hour, int minute, double seconds, CompassModel compass, CelestialObject body, Canvas canvas) {

        Coordinate coordinate = compass.getCoordinate(body, hour, minute, seconds);

        if (coordinate.getAltitude() < 0) {
            return;
        }

        float x = radius + (float) coordinate.getX()*radius;
        float y = diameter - radius - (float) coordinate.getY()*radius;
        float r = objectRadius;

        canvas.drawCircle(x, y, r, body.getPaint());

    }

    private static Coordinate getHorizonCoordinate(boolean stopWhenNegativeAltitude, int startingHour, CompassModel compass, CelestialObject body) {

        HorizonCoordinate firstSweepCoordinate = HorizonCoordinate.get(
            stopWhenNegativeAltitude,
            startingHour,
            0, 60, 10,
            compass,
            body
        );
        HorizonCoordinate secondSweepCoordinate = HorizonCoordinate.get(
            stopWhenNegativeAltitude,
            startingHour,
            firstSweepCoordinate.minute, firstSweepCoordinate.minute + 10, 1,
            compass,
            body
        );

        return secondSweepCoordinate.coordinate;

    }

    private static class HorizonCoordinate {

        public final Coordinate coordinate;
        public final int minute;

        public HorizonCoordinate(Coordinate coordinate, int minute) {
            this.coordinate = coordinate;
            this.minute = minute;
        }

        public static HorizonCoordinate get(
            boolean stopWhenNegativeAltitude,
            int startingHour,
            int startingMinute, int endingMinute, int step,
            CompassModel compass,
            CelestialObject body
        ) {

            Coordinate coordinate = null;
            int prevMinute = startingMinute;

            for (int minute = startingMinute; minute <= endingMinute; minute += step) {

                coordinate = compass.getCoordinate(body, startingHour, minute, 0);

                double altitude = coordinate.getAltitude();

                if (stopWhenNegativeAltitude ? altitude < 0 : altitude > 0) {
                    break;
                }

                prevMinute = minute;

            }

            return new HorizonCoordinate(coordinate, prevMinute);

        }

    }

    private static int alphaOfTrack(int trackHour, int currentHour) {

        final int MIN_ALPHA = 127;

        int alpha = Math.abs(currentHour - trackHour);
        alpha = Math.min(alpha, 5);
        alpha = 5 - alpha;
        alpha = MIN_ALPHA + alpha * ((255-MIN_ALPHA)/5);

        return alpha;

    }

}
