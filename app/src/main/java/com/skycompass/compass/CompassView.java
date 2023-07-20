package com.skycompass.compass;

import static com.skycompass.compass.Values.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CompassView extends View {

    private float offsetHorizontal;
    private float offsetVertical;
    private float innerRadius;
    private float ringThickness;
    private float northRotation;

    private final Background background = new Background(innerRadius);
    private final Foreground foreground = new Foreground(innerRadius, innerRadius + ringThickness);
    private final Track track = new Track(innerRadius);

    private final CompassModel compass = new CompassModel(0,0);
    private int hour;
    private int minutes;
    private float seconds;

    public CompassView(Context context) {
        super(context);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public double getLongitude() {
        return compass.getLongitude();
    }

    public double getLatitude() {
        return compass.getLatitude();
    }

    public void setLocation(double latitude, double longitude) {
        compass.setLocation(latitude, longitude);
    }

    public int getYear() {
        return compass.getYear();
    }

    public int getMonth() {
        return compass.getMonth();
    }

    public int getDayOfMonth() {
        return compass.getDayOfMonth();
    }

    public void setDate(int year, int month, int dayOfMonth) {
        compass.setDate(year, month, dayOfMonth);
        invalidate();
    }

    public void setColor(Color color) {
        foreground.setColor(color);
        background.setColor(color);
        invalidate();
    }

    public float getNorthRotation() {
        return northRotation;
    }

    public void setNorthRotation(float rotation) {
        this.northRotation = rotation;
        invalidate();
    }

    public void setTime(int hour, int minutes, float seconds) {
        this.hour = hour;
        this.minutes = minutes;
        this.seconds = seconds;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = w - xpad;
        float hh = h - ypad;

        int maxLength = (int)Math.min(ww,hh);
        offsetHorizontal = (w - maxLength)/2f;
        offsetVertical = (h - maxLength)/2f;

        innerRadius = maxLength * FACE_FRACTION / 2f;
        ringThickness = maxLength/2f - innerRadius;

        background.setRadius(innerRadius);
        foreground.setRing(innerRadius, innerRadius + ringThickness);
        track.setDimensions(innerRadius);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(offsetHorizontal, 0);
        canvas.translate(offsetVertical, 0);

        drawBackground(canvas);

        canvas.save();
        canvas.rotate(
            (float)Math.toDegrees(-northRotation),
            innerRadius + ringThickness, innerRadius + ringThickness
        );

        drawBodies(canvas);
        drawForeground(canvas);

        canvas.restore();

        canvas.restore();

    }

    private void drawBackground(Canvas canvas) {

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

        background.draw(canvas);

        canvas.restore();

    }

    private void drawForeground(Canvas canvas) {

        foreground.draw(canvas);

    }

    private void drawBodies(Canvas canvas) {

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

        CelestialObject[] bodies = new CelestialObject[]{CelestialObject.SUN, CelestialObject.MOON};

        for (CelestialObject body : bodies) {
            track.drawTracks(hour, compass, body, canvas);
        }

        for (CelestialObject body : bodies) {
            track.drawCurrentPosition(hour, minutes, seconds, compass, body, canvas);
        }

        canvas.restore();

    }

}
