package com.skycompass.compass;

import static com.skycompass.compass.Values.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CompassView extends View {

    private float offsetHorizontal;
    private float offsetVertical;
    private float innerRadius;
    private float ringThickness;
    private float northRotation;

    private final Background background = new Background(innerRadius);
    private final Foreground foreground = new Foreground(innerRadius, innerRadius + ringThickness);
    private final Track track = new Track(innerRadius);

    private double latitude, longitude;
    private LocalDateTime dateTime = LocalDateTime.of(2000,1,1,0,0,0);

    public CompassView(Context context) {
        super(context);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        invalidate();
    }

    public int getYear() {
        return dateTime.getYear();
    }

    public int getMonth() {
        return dateTime.getMonth().getValue();
    }

    public int getDayOfMonth() {
        return dateTime.getDayOfMonth();
    }

    public void setDate(LocalDate date) {
        dateTime = LocalDateTime.of(date, dateTime.toLocalTime());
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

    public void setTime(LocalTime time) {
        dateTime = LocalDateTime.of(dateTime.toLocalDate(), time);
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
            track.drawTracks(body, latitude, longitude, dateTime, canvas);
        }

        for (CelestialObject body : bodies) {
            track.drawCurrentPosition(body, latitude, longitude, dateTime, canvas);
        }

        canvas.restore();

    }

}
