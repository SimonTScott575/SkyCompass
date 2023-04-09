package com.icarus1.compass;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.icarus1.R;

class Background {

    private float radius;
    private float diameter;

    private final Paint backgroundPaint;
    private final Paint altitudeTrackPaint;
    private final Paint altitudeTextPaint;

    public Background(float radius) {

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#3F4E4F"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        altitudeTrackPaint = new Paint();
        altitudeTrackPaint.setColor(Color.parseColor("#DCD7C9"));
        altitudeTrackPaint.setStyle(Paint.Style.STROKE);

        altitudeTextPaint = new Paint();
        altitudeTextPaint.setColor(Color.parseColor("#DCD7C9"));
        altitudeTextPaint.setStyle(Paint.Style.FILL);
        setRadius(radius);

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

        drawAltitudeTracks(canvas);

    }

    private void drawAltitudeTracks(Canvas canvas) {

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
        canvas.drawCircle(radius, radius, 1, altitudeTrackPaint);

    }

}
