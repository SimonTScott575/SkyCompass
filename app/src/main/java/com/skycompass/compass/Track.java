package com.skycompass.compass;

import static com.skycompass.compass.Values.*;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.time.LocalDateTime;

class Track {

    private static final Paint GLOW_PAINT = new Paint(CelestialObject.SUN.getPaint().getColor());
    static {
        GLOW_PAINT.setStyle(Paint.Style.FILL);
        GLOW_PAINT.setShader(new RadialGradient(0,0,1000,GLOW_PAINT.getColor(), Color.parseColor("#FF00FF"), Shader.TileMode.CLAMP));
    }

    private float radius;
    private float diameter;
    private float trackWidth;
    private float objectRadius;

    public Track(float radius) {
        setDimensions(radius);
    }

    public final void setDimensions(float radius) {
        this.radius = radius;
        diameter = 2*radius;
        trackWidth = radius * TRACK_WIDTH_FRACTION;
        objectRadius = radius * TRACK_MARKER_FRACTION;
    }

    public void drawTracks(CelestialObject body, CompassModel compass, int currentHour, Canvas canvas) {

        Coordinate[] coordinates = AstronomyUtil.coordinateRangeAndHorizon(
            body,
            compass.getLatitude(), compass.getLongitude(),
            LocalDateTime.of(
                compass.getYear(), compass.getMonth(), compass.getDayOfMonth(),
                currentHour, 0, 0
            ).minusHours(12),
            LocalDateTime.of(
                compass.getYear(), compass.getMonth(), compass.getDayOfMonth(),
                currentHour, 0, 0
            ).plusHours(13),
            60*60
        );

        for (int i = 0; i < coordinates.length-1; i++) {

            int hour = currentHour - 12 + i;

            double startX = coordinates[i].getX();
            double startY = coordinates[i].getY();
            double endX = coordinates[i+1].getX();
            double endY = coordinates[i+1].getY();

            if (coordinates[i].getAltitude() < 0 || coordinates[i+1].getAltitude() < 0) {
                continue;
            }

            float length = (float) (Math.sqrt( Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) ));
            float padding = length * TRACK_SPACE_FRACTION / 2f;
            float angle = (float) Math.atan2(endY - startY, endX - startX);

            length *= radius;
            padding *= radius;
            startX = startX*radius + radius;
            startY = startY*radius + radius;
            startY = diameter - startY;

            canvas.save();
            canvas.rotate(-angle*360f/(2*(float)Math.PI), (float) startX, (float) startY); // (float)(startX + (endX - startX)/2f), (float)(startY + (endY - startY)/2f));

            int prevAlpha = body.getPaint().getAlpha();
            body.getPaint().setAlpha( alphaOfTrack(hour, currentHour) );
            canvas.drawRect(
                (float) startX + padding,
                (float) startY,
                (float) startX + length - padding,
                (float) startY + trackWidth, body.getPaint()
            );
            body.getPaint().setAlpha(prevAlpha);

            canvas.restore();

        }

    }

    public void drawCurrentPosition(CelestialObject body, CompassModel compass, double seconds, int hour, int minute, Canvas canvas) {

        Coordinate coordinate = compass.getCoordinate(body, hour, minute, seconds);

        if (coordinate.getAltitude() < 0) {
            return;
        }

        float x = radius + (float) coordinate.getX()*radius;
        float y = diameter - radius - (float) coordinate.getY()*radius;
        float r = objectRadius;

        if (body == CelestialObject.SUN || body == CelestialObject.MOON) {

            int[] paints = new int[]{50, 25, 12, 0};
            for (int i = 0; i < 4; i++) {
                Paint paint = new Paint(body.getPaint());
                paint.setAlpha(paints[i]);
                paints[i] = paint.getColor();
            }

            GLOW_PAINT.setShader(new RadialGradient(
                x, y, r*6,
                paints,
                new float[]{0,1/4f,1/2f,1f},
                Shader.TileMode.CLAMP
            ));

            canvas.drawCircle(x, y, r*6, GLOW_PAINT);

        }

        canvas.drawCircle(x, y, r, body.getPaint());

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
