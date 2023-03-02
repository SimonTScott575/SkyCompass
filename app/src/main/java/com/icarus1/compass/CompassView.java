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
    private float diameter;
    private float radius;
    private float ringThickness;
    private int textSize;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTextPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CompassModel compass = new CompassModel(0,0); //TODO remove ? should not depend directly on model ?

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

        backgroundTextPaint2.setColor(getResources().getColor(R.color.compass_deg_tracks, getContext().getTheme()));
        backgroundPaint.setStyle(Paint.Style.FILL);

        backgroundTextPaint.setColor(getResources().getColor(R.color.compass_NSEW, getContext().getTheme()));
        backgroundPaint.setStyle(Paint.Style.FILL);

        trackPaint.setColor(getResources().getColor(R.color.compass_tracks, getContext().getTheme()));
        backgroundPaint.setStyle(Paint.Style.FILL);

    }

    public void setCompassModel(CompassModel compassModel) {
        this.compass = compassModel;
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
        diameter = maxLength - 2* ringThickness;
        radius = diameter / 2;

        textSize = maxLength / 10;

        backgroundTrackPaint.setStrokeWidth(radius/100f);

        backgroundTextPaint2.setTextSize(4*radius/100f);
        backgroundTextPaint2.setStrokeWidth(radius/100f);
        backgroundTextPaint2.setFakeBoldText(true);

        backgroundRingPaint.setStrokeWidth(ringThickness);

        backgroundTextPaint.setTextSize(textSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawTracks(canvas);
        drawForeground(canvas);

    }

    private void drawBackground(Canvas canvas) {

        canvas.save(); //TODO use better variables/approach than "radius"
        canvas.translate(ringThickness, ringThickness);

        float sizeN = backgroundTextPaint.measureText(TEXT_N);
        float sizeS = backgroundTextPaint.measureText(TEXT_S);
        float sizeE = backgroundTextPaint.measureText(TEXT_E);

        float padding = textSize / 4f;

        canvas.drawOval(0, 0, diameter, diameter, backgroundPaint);
        for (int i = 0; i < 8; i++) {

            float trackRadius = (8-i)*radius/9f;

            canvas.drawCircle(radius, radius, trackRadius, backgroundTrackPaint);

            String text = 10*(i+1) + "\u00B0";
            canvas.drawText(
                text,
                radius - backgroundTextPaint2.measureText(text)/2,
                radius - trackRadius - backgroundTextPaint2.getStrokeWidth(),
                backgroundTextPaint2
            );

        }
        canvas.drawCircle(radius, radius, 1, backgroundTrackPaint);

        canvas.drawText(
            TEXT_N,
            radius - sizeN / 2f,
            padding + textSize,
            backgroundTextPaint
        );
        canvas.drawText(
            TEXT_S,
            radius - sizeS / 2f,
            diameter - padding,
            backgroundTextPaint
        );
        canvas.drawText(
            TEXT_E,
            diameter - padding - sizeE,
            radius + textSize / 2f,
            backgroundTextPaint
        );
        canvas.drawText(
            TEXT_W,
            padding,
            radius + textSize / 2f,
            backgroundTextPaint
        );

        canvas.restore();

    }

    private void drawForeground(Canvas canvas) {

        canvas.save(); //TODO use better variables/approach than "radius"
        canvas.translate(ringThickness, ringThickness);

        canvas.drawCircle(
            radius, radius,
            radius + ringThickness/2,
            backgroundRingPaint
        );

        canvas.restore();

    }

    private void drawTracks(Canvas canvas) {

        canvas.save(); //TODO use better variables/approach than "radius"
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
            Coordinate coordinate = compass.getCoordinate(-1, 0, 0);
            x[0] = coordinate.getX();
            y[0] = coordinate.getY();
            altitude[0] = coordinate.getAltitude();
            for (int i = 0; i < 25; i++) {

                coordinate = compass.getCoordinate(i, 0, 0);

                x[i + 1] = coordinate.getX();
                y[i + 1] = coordinate.getY();
                altitude[i + 1] = coordinate.getAltitude();

                if (altitude[i+1] > 0 && altitude[i] < 0) {

                    Coordinate c = coordinate;
                    for (int j = 1; j < 7; j++) {
                        c = compass.getCoordinate(i-1, j*10, 0d);
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
                        c = compass.getCoordinate(i-1, j*10, 0d);
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

            float height = diameter / 100f;
            float length = (float) (Math.sqrt( Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) ));
            float padding = length/20f;
            float angle = (float) Math.atan2(endY - startY, endX - startX);

            length *= radius;
            padding *= radius;

            startX *= radius;
            startY *= radius;

            startX += radius;
            startY += radius;

            startY = diameter - startY;

            canvas.save();
            canvas.rotate(-angle*360f/(2*(float)Math.PI), (float) startX, (float) startY); // (float)(startX + (endX - startX)/2f), (float)(startY + (endY - startY)/2f));

            canvas.drawRect((float) startX + padding, (float) startY, (float) startX + length - padding, (float) startY + height, trackPaint);

            canvas.restore();

        }

        canvas.restore();

    }

}
