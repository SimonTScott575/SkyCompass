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
    private Track track;

    private float innerDiameter;
    private float innerRadius;
    private float ringThickness;
    private final Paint backgroundRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CompassModel compass = new CompassModel(0,0);

    private boolean drawSunMoon;
    private boolean drawPlanets;

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
        track = new Track(innerRadius);

        backgroundRingPaint.setColor(getResources().getColor(R.color.compass_ring, getContext().getTheme()));
        backgroundRingPaint.setStyle(Paint.Style.STROKE);

        drawSunMoon = true;
        drawPlanets = false;

    }

    public void setCompassModel(CompassModel compassModel) {
        this.compass = compassModel;
        invalidate();
    }

    public boolean isDrawSunMoon() {
        return drawSunMoon;
    }

    public void setDrawSunMoon(boolean drawSunMoon) {
        this.drawSunMoon = drawSunMoon;
        invalidate();
    }

    public boolean isDrawPlanets() {
        return drawPlanets;
    }

    public void setDrawPlanets(boolean drawPlanets) {
        this.drawPlanets = drawPlanets;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = w - xpad;
        float hh = h - ypad;

        int maxLength = (int)Math.min(ww,hh);

        ringThickness = maxLength / 40f;
        innerDiameter = maxLength - 2*ringThickness;
        innerRadius = innerDiameter / 2;

        background.setRadius(innerRadius);
        track.setRadius(innerRadius);

        backgroundRingPaint.setStrokeWidth(ringThickness);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        for (CelestialBody body : CelestialBody.values()) {
            if (body == CelestialBody.SUN || body == CelestialBody.MOON) {
                if (drawSunMoon) {
                    drawTracks(12,0,0, body, canvas);
                }
            } else {
                if (drawPlanets) {
                    drawTracks(12,0,0, body, canvas);
                }
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

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

        canvas.drawCircle(
            innerRadius, innerRadius,
            innerRadius + ringThickness/2,
            backgroundRingPaint
        );

        canvas.restore();

    }

    private void drawTracks(int hour, int minute, double seconds, CelestialBody body, Canvas canvas) {

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

        track.draw(canvas, compass, body, hour, minute, seconds);

        canvas.restore();

    }

}
