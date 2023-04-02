package com.icarus1.compass;

import android.graphics.Canvas;

public class Track {
    //TODO speed up inefficient getHorizonCoordinate search algorithm

    private float radius;
    private float diameter;
    private float outerRadius;
    private float halfThickness;

    public Track(float radius, float thickness, float outerRadius) {

        setDimensions(radius, thickness, outerRadius);

    }

    public final void setDimensions(float radius, float thickness, float outerRadius) {
        this.radius = radius;
        diameter = 2*radius;
        this.outerRadius = outerRadius;
        halfThickness = thickness/2f;
    }

    public void drawTracks(int currentHour, CompassModel compass, CelestialBody body, Canvas canvas) {

        double[] x = new double[25];
        double[] y = new double[25];
        double[] altitude = new double[25];

        {
            Coordinate coordinate = compass.getCoordinate(body.getBody(), 0, 0, 0);
            x[0] = coordinate.getX();
            y[0] = coordinate.getY();
            altitude[0] = coordinate.getAltitude();
            for (int hour = 1; hour < 25; hour++) {

                coordinate = compass.getCoordinate(body.getBody(), hour, 0, 0);

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

            float height = diameter / 150f;
            float length = (float) (Math.sqrt( Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) ));
            float padding = length/20f;
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
            canvas.drawRect((float) startX + padding, (float) startY, (float) startX + length - padding, (float) startY + height, body.getPaint());
            body.getPaint().setAlpha(prevAlpha);

            canvas.restore();

        }

    }

    public void drawCurrentPosition(int hour, int minute, double seconds, CompassModel compass, CelestialBody body, Canvas canvas) {

        Coordinate coordinate = compass.getCoordinate(body.getBody(), hour, minute, seconds);

        if (coordinate.getAltitude() < 0) {
            return;
        }

        float x = radius + (float) coordinate.getX()*radius;
        float y = diameter - radius - (float) coordinate.getY()*radius;
        float r = 0.05f*radius;

        canvas.drawCircle(x, y, r, body.getPaint());

    }

    private static Coordinate getHorizonCoordinate(boolean stopWhenNegativeAltitude, int startingHour, CompassModel compass, CelestialBody body) {

        Coordinate c = null;

        for (int minute = 0; minute < 60; minute += 1) {

            c = compass.getCoordinate(body.getBody(), startingHour, minute, 0d);

            double altitude = c.getAltitude();

            if (stopWhenNegativeAltitude ? altitude < 0 : altitude > 0) {
                break;
            }

        }

        return c;

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
