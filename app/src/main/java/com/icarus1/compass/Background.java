package com.icarus1.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.icarus1.R;

class Background {

    private static final String TEXT_N = "N";
    private static final String TEXT_S = "S";
    private static final String TEXT_E = "E";
    private static final String TEXT_W = "W";

    private float radius;
    private float diameter;

    private final Paint backgroundPaint;
    private final Paint altitudeTrackPaint;
    private final Paint altitudeTextPaint;
    private final Paint NSEWPaint;
    private final int NColor;

    public Background(Context context, float radius) {

        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(resources.getColor(R.color.compass_background, theme));
        backgroundPaint.setStyle(Paint.Style.FILL);

        altitudeTrackPaint = new Paint();
        altitudeTrackPaint.setColor(resources.getColor(R.color.compass_deg_tracks, theme));
        altitudeTrackPaint.setStyle(Paint.Style.STROKE);

        altitudeTextPaint = new Paint();
        altitudeTextPaint.setColor(resources.getColor(R.color.compass_deg_tracks, theme));
        altitudeTextPaint.setStyle(Paint.Style.FILL);

        NSEWPaint = new Paint();
        NSEWPaint.setColor(resources.getColor(R.color.compass_SEW, theme));
        NSEWPaint.setStyle(Paint.Style.FILL);
        NSEWPaint.setFakeBoldText(true);

        NColor = resources.getColor(R.color.compass_N, theme);

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

        NSEWPaint.setTextSize(radius/5f);

    }

    public void draw(Canvas canvas) {

        canvas.drawOval(0, 0, diameter, diameter, backgroundPaint);

        drawAltitudeTracks(canvas);
        drawNESW(canvas);

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

    private void drawNESW(Canvas canvas) {

        float textSize = NSEWPaint.getTextSize();
        float padding = textSize / 4f;

        float sizeN = NSEWPaint.measureText(TEXT_N);
        float sizeS = NSEWPaint.measureText(TEXT_S);
        float sizeE = NSEWPaint.measureText(TEXT_E);

        canvas.drawText(
                TEXT_S,
                radius - sizeS / 2f,
                diameter - padding,
                NSEWPaint
        );
        canvas.drawText(
                TEXT_E,
                diameter - padding - sizeE,
                radius + textSize / 2f,
                NSEWPaint
        );
        canvas.drawText(
                TEXT_W,
                padding,
                radius + textSize / 2f,
                NSEWPaint
        );

        int SEWColor = NSEWPaint.getColor();
        NSEWPaint.setColor(NColor);

        canvas.drawText(
                TEXT_N,
                radius - sizeN / 2f,
                padding + textSize,
                NSEWPaint
        );

        NSEWPaint.setColor(SEWColor);

    }

}
