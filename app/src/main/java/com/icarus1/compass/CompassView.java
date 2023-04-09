package com.icarus1.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CompassView extends View {

    private float innerRadius;
    private float ringThickness;
    private boolean rotate;
    private float northRotation;
    private float currentRotation;

    private final Background background = new Background(innerRadius);
    private final Foreground foreground = new Foreground(innerRadius, innerRadius + ringThickness);
    private final Track track = new Track(innerRadius);

    private CompassModel compass = new CompassModel(0,0);
    private int hour;
    private int minutes;
    private float seconds;

    private final boolean[] drawBody = new boolean[CelestialObject.values().length];

    public CompassView(Context context) {
        super(context);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCompassModel(CompassModel compassModel) {
        this.compass = compassModel;
        invalidate();
    }

    public void setCurrentRotation(float rotation) {
        this.currentRotation = rotation;
        invalidate();
    }

    public void setRotateToNorth(boolean rotate) {
        this.rotate = rotate;
        invalidate();
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

    public void setDrawBody(CelestialObject body, boolean draw) {
        drawBody[body.ordinal()] = draw;
        invalidate();
    }

    public boolean getDrawBody(CelestialObject body) {
        return drawBody[body.ordinal()];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = w - xpad;
        float hh = h - ypad;

        int maxLength = (int)Math.min(ww,hh);

        ringThickness = maxLength / 20f;
        float innerDiameter = maxLength - 2*ringThickness;
        innerRadius = innerDiameter / 2;

        background.setRadius(innerRadius);
        foreground.setRing(innerRadius, innerRadius + ringThickness);
        track.setDimensions(innerRadius);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rotate) {
            float diff = northRotation - currentRotation;
            currentRotation += (northRotation - currentRotation)*0.1f * (Math.abs(diff) > Math.PI ? -1f : 1f);
            currentRotation = (float) (currentRotation > Math.PI ? currentRotation - 2f*Math.PI: currentRotation);
            currentRotation = (float) (currentRotation < -Math.PI ? currentRotation + 2f*Math.PI: currentRotation);
        } else {
            currentRotation = 0f;
        }

        drawBackground(canvas);

        canvas.save();
        canvas.rotate(
            (float)Math.toDegrees(-currentRotation),
            innerRadius + ringThickness, innerRadius + ringThickness
        );

        drawBodies(canvas);
        drawForeground(canvas);

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

        for (CelestialObject body : CelestialObject.values()) {
            if (drawBody[body.ordinal()]) {
                track.drawTracks(hour, compass, body, canvas);
            }
        }

        for (CelestialObject body : CelestialObject.values()) {
            if (drawBody[body.ordinal()]) {
                track.drawCurrentPosition(hour, minutes, seconds, compass, body, canvas);
            }
        }

        canvas.restore();

    }

}
