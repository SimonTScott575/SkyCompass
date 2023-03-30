package com.icarus1.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.icarus1.R;

public class CompassView extends View {

    private Background background;
    private Foreground foreground;
    private Track track;

    private float innerRadius;
    private float ringThickness;

    private CompassModel compass = new CompassModel(0,0);
    private int hour;
    private int minutes;
    private float seconds;

    private boolean[] drawBody;

    public CompassView(Context context) {
        super(context);
        init(context);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        background = new Background(context, innerRadius);
        foreground = new Foreground(context, innerRadius, innerRadius + ringThickness);
        track = new Track(innerRadius);

        drawBody = new boolean[CelestialBody.values().length];

    }

    public void setCompassModel(CompassModel compassModel) {
        this.compass = compassModel;
        invalidate();
    }

    public void setTime(int hour, int minutes, float seconds) {
        this.hour = hour;
        this.minutes = minutes;
        this.seconds = seconds;
        invalidate();
    }

    public void setDrawBody(CelestialBody body, boolean draw) {
        drawBody[body.getIndex()] = draw;
        invalidate();
    }

    public boolean getDrawBody(CelestialBody body) {
        return drawBody[body.getIndex()];
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
        track.setRadius(innerRadius);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        for (CelestialBody body : CelestialBody.values()) {
            if (drawBody[body.getIndex()]) {
                drawTracks(hour,minutes,seconds, body, canvas);
            }
        }
        drawForeground(canvas);

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

    private void drawTracks(int hour, int minute, double seconds, CelestialBody body, Canvas canvas) {

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

        track.draw(canvas, compass, body, hour, minute, seconds);

        canvas.restore();

    }

}
