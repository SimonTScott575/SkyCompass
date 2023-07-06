package com.skycompass.compass;

import static com.skycompass.compass.Values.*;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

class Background {

    private float radius;
    private float diameter;

    private final Paint backgroundPaint;
    private final Paint altitudeTrackPaint;
    private final Paint altitudeTextPaint;

    public Background(float radius) {

        backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR.toArgb());
        backgroundPaint.setStyle(Paint.Style.FILL);

        altitudeTrackPaint = new Paint();
        altitudeTrackPaint.setColor(TRACK_COLOR.toArgb());
        altitudeTrackPaint.setStyle(Paint.Style.STROKE);

        altitudeTextPaint = new Paint();
        altitudeTextPaint.setColor(TRACK_COLOR.toArgb());
        altitudeTextPaint.setStyle(Paint.Style.FILL);

        setRadius(radius);

    }

    public void setColor(Color color) {
        altitudeTrackPaint.setColor(color.toArgb());
        altitudeTextPaint.setColor(color.toArgb());
    }

    public final float getRadius() {
        return radius;
    }

    public final void setRadius(float radius) {

        this.radius = radius;
        diameter = 2*radius;

        altitudeTrackPaint.setStrokeWidth(radius / 100f);

        altitudeTextPaint.setTextSize(6 * radius / 100f);
        altitudeTextPaint.setStrokeWidth(radius / 100f);
        altitudeTextPaint.setFakeBoldText(true);

    }

    public void draw(Canvas canvas) {

        canvas.drawOval(0, 0, diameter, diameter, backgroundPaint);

        for (int i = 0; i < 8; i++) {

            float trackRadius = (8-i)* radius /9f;

            canvas.drawCircle(radius, radius, trackRadius, altitudeTrackPaint);

            String text = 10*(i+1) + "\u00B0";

            canvas.drawText(
                text,
                radius - altitudeTextPaint.measureText(text)/2,
                radius - trackRadius - altitudeTextPaint.getStrokeWidth(),
                altitudeTextPaint
            );

        }

        canvas.drawCircle(radius, radius, altitudeTrackPaint.getStrokeWidth()/2f, altitudeTrackPaint);

    }

}
