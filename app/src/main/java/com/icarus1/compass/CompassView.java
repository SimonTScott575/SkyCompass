package com.icarus1.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.icarus1.R;

public class CompassView extends View {

    private static final String TEXT_N = "N";
    private static final String TEXT_S = "S";
    private static final String TEXT_E = "E";
    private static final String TEXT_W = "W";

    private int maxLength;
    private float innerDiameter;
    private float innerRadius;
    private float ringThickness;
    private int textSize;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTrackTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CompassModel compass = new CompassModel(0,0);

    private boolean drawSunMoon;
    private boolean drawPlanets;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

        backgroundPaint.setColor(getResources().getColor(R.color.compass_background, getContext().getTheme()));
        backgroundPaint.setStyle(Paint.Style.FILL);

        backgroundRingPaint.setColor(getResources().getColor(R.color.compass_ring, getContext().getTheme()));
        backgroundRingPaint.setStyle(Paint.Style.STROKE);

        backgroundTrackPaint.setColor(getResources().getColor(R.color.compass_deg_tracks, getContext().getTheme()));
        backgroundTrackPaint.setStyle(Paint.Style.STROKE);

        backgroundTrackTextPaint.setColor(getResources().getColor(R.color.compass_deg_tracks, getContext().getTheme()));
        backgroundTrackTextPaint.setStyle(Paint.Style.FILL);

        backgroundTextPaint.setColor(getResources().getColor(R.color.compass_NSEW, getContext().getTheme()));
        backgroundTextPaint.setStyle(Paint.Style.FILL);

//        trackPaint.setColor(getResources().getColor(R.color.compass_tracks, getContext().getTheme()));
//        trackPaint.setStyle(Paint.Style.FILL);

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

        maxLength = (int)Math.min(ww,hh);

        ringThickness = maxLength / 40f;
        innerDiameter = maxLength - 2* ringThickness;
        innerRadius = innerDiameter / 2;

        textSize = maxLength / 10;

        backgroundTrackPaint.setStrokeWidth(innerRadius /100f);

        backgroundTrackTextPaint.setTextSize(6 * innerRadius/100f);
        backgroundTrackTextPaint.setStrokeWidth(innerRadius/100f);
        backgroundTrackTextPaint.setFakeBoldText(true);

        backgroundRingPaint.setStrokeWidth(ringThickness);

        backgroundTextPaint.setTextSize(textSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        for (CelestialBody body : CelestialBody.values()) {
            if (body == CelestialBody.SUN || body == CelestialBody.MOON) {
                if (drawSunMoon) {
                    drawTracks(body, canvas);
                }
            } else {
                if (drawPlanets) {
                    drawTracks(body, canvas);
                }
            }
        }
        drawForeground(canvas);

    }

    private void drawBackground(Canvas canvas) {

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

        float sizeN = backgroundTextPaint.measureText(TEXT_N);
        float sizeS = backgroundTextPaint.measureText(TEXT_S);
        float sizeE = backgroundTextPaint.measureText(TEXT_E);

        float padding = textSize / 4f;

        canvas.drawOval(0, 0, innerDiameter, innerDiameter, backgroundPaint);
        for (int i = 0; i < 8; i++) {

            float trackRadius = (8-i)* innerRadius /9f;

            canvas.drawCircle(innerRadius, innerRadius, trackRadius, backgroundTrackPaint);

            String text = 10*(i+1) + "\u00B0";
            canvas.drawText(
                text,
                innerRadius - backgroundTrackTextPaint.measureText(text)/2,
                innerRadius - trackRadius - backgroundTrackTextPaint.getStrokeWidth(),
                backgroundTrackTextPaint
            );

        }
        canvas.drawCircle(innerRadius, innerRadius, 1, backgroundTrackPaint);

        canvas.drawText(
            TEXT_N,
            innerRadius - sizeN / 2f,
            padding + textSize,
            backgroundTextPaint
        );
        canvas.drawText(
            TEXT_S,
            innerRadius - sizeS / 2f,
            innerDiameter - padding,
            backgroundTextPaint
        );
        canvas.drawText(
            TEXT_E,
            innerDiameter - padding - sizeE,
            innerRadius + textSize / 2f,
            backgroundTextPaint
        );
        canvas.drawText(
            TEXT_W,
            padding,
            innerRadius + textSize / 2f,
            backgroundTextPaint
        );

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

    private void drawTracks(CelestialBody body, Canvas canvas) {

        canvas.save();
        canvas.translate(ringThickness, ringThickness);

/*
        HourAngleInfo sunriseInfo = Astronomy.searchHourAngle(
            Body.Sun,
            new Observer(compass.getLatitude(), compass.getLongitude(), 0),
            12,
            new Time(
                compass.getYear(),
                compass.getMonth(),
                compass.getDayOfMonth(),
                0,
                0,
                0
            ),
            1
        );

        HourAngleInfo sunsetInfo = Astronomy.searchHourAngle(
            Body.Sun,
            new Observer(compass.getLatitude(), compass.getLongitude(), 0),
            12,
            new Time(
                sunriseInfo.getTime().toDateTime().getYear(),
                sunriseInfo.getTime().toDateTime().getMonth(),
                sunriseInfo.getTime().toDateTime().getDay(),
                0,
                0,
                1
            ),
            1
        );
*/

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

            float height = innerDiameter / 150f;
            float length = (float) (Math.sqrt( Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) ));
            float padding = length/20f;
            float angle = (float) Math.atan2(endY - startY, endX - startX);

            length *= innerRadius;
            padding *= innerRadius;

            startX *= innerRadius;
            startY *= innerRadius;

            startX += innerRadius;
            startY += innerRadius;

            startY = innerDiameter - startY;

            canvas.save();
            canvas.rotate(-angle*360f/(2*(float)Math.PI), (float) startX, (float) startY); // (float)(startX + (endX - startX)/2f), (float)(startY + (endY - startY)/2f));

            canvas.drawRect((float) startX + padding, (float) startY, (float) startX + length - padding, (float) startY + height, body.getPaint());

            canvas.restore();

        }

        canvas.restore();

    }

}
