package com.icarus1.compass;

import android.graphics.Canvas;

public class Track {

    private float radius;
    private float diameter;

    public Track(float radius) {

        this.radius = radius;
        diameter = 2*radius;

    }

    public final void setRadius(float radius) {
        this.radius = radius;
        diameter = 2*radius;
    }

    public final float getRadius() {
        return radius;
    }

    public void draw(Canvas canvas, CompassModel compass, CelestialBody body, int hour, int minute, double seconds) {

        drawTracks(compass, body, canvas);
        drawCurrentPosition(hour, minute, seconds, compass, body, canvas);

    }

    private void drawTracks(CompassModel compass, CelestialBody body, Canvas canvas) {

        double[] x = new double[26];
        double[] y = new double[26];
        double[] altitude = new double[26];

        {
            Coordinate coordinate = compass.getCoordinate(body.getBody(), -1, 0, 0);
            x[0] = coordinate.getX();
            y[0] = coordinate.getY();
            altitude[0] = coordinate.getAltitude();
            for (int i = 0; i < 25; i++) {

                coordinate = compass.getCoordinate(body.getBody(), i, 0, 0);

                x[i + 1] = coordinate.getX();
                y[i + 1] = coordinate.getY();
                altitude[i + 1] = coordinate.getAltitude();

                if (altitude[i+1] > 0 && altitude[i] < 0) {

                    Coordinate c = coordinate;
                    for (int j = 1; j < 7; j++) {
                        c = compass.getCoordinate(body.getBody(), i-1, j*10, 0d);
                        double altitude2 = c.getAltitude();
                        if (altitude2 > 0) {
                            break;
                        }
                    }

                    x[i] = c.getX();
                    y[i] = c.getY();

                } else if (altitude[i+1] < 0 && altitude[i] > 0) {

                    Coordinate c = coordinate;
                    for (int j = 1; j < 7; j++) {
                        c = compass.getCoordinate(body.getBody(), i-1, j*10, 0d);
                        double altitude2 = c.getAltitude();
                        if (altitude2 < 0) {
                            break;
                        }
                    }

                    x[i+1] = c.getX();
                    y[i+1] = c.getY();

                }

            }
        }

        for (int i = 0; i < 25; i++) {

            double startX = x[i];
            double startY = y[i];
            double endX = x[i+1];
            double endY = y[i+1];

            if (altitude[i] < 0 && altitude[i+1] > 0) {
                double length = Math.sqrt( Math.pow(startX, 2) + Math.pow(startY, 2) );
                startX /= length;
                startY /= length;
            } else if (altitude[i] > 0 && altitude[i+1] < 0) {
                double length = Math.sqrt( Math.pow(endX, 2) + Math.pow(endY, 2) );
                endX /= length;
                endY /= length;
            } else if (altitude[i] < 0 && altitude[i+1] < 0) {
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

            canvas.drawRect((float) startX + padding, (float) startY, (float) startX + length - padding, (float) startY + height, body.getPaint());

            canvas.restore();

        }

    }

    private void drawCurrentPosition(int hour, int minute, double seconds, CompassModel compass, CelestialBody body, Canvas canvas) {

        Coordinate coordinate = compass.getCoordinate(body.getBody(), hour, minute, seconds);

        if (coordinate.getAltitude() < 0) {
            return;
        }

        float x = radius + (float) coordinate.getX()*radius;
        float y = diameter - radius - (float) coordinate.getY()*radius;
        float r = 0.05f*radius;

        canvas.drawCircle(x, y, r, body.getPaint());

    }


}
