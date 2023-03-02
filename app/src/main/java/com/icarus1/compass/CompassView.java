package com.icarus1.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

import io.github.cosinekitty.astronomy.Astronomy;
import io.github.cosinekitty.astronomy.Body;
import io.github.cosinekitty.astronomy.HourAngleInfo;
import io.github.cosinekitty.astronomy.Observer;
import io.github.cosinekitty.astronomy.Time;

public class CompassView extends View {

    private static final String TEXT_N = "N";
    private static final String TEXT_S = "S";
    private static final String TEXT_E = "E";
    private static final String TEXT_W = "W";

    private int maxLength;
    private float diameter;
    private float radius;
    private int textSize;
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

        backgroundPaint.setColor(Color.argb(1f,0.5f,0.5f,0.5f));
        backgroundTrackPaint.setColor(Color.argb(1f,0.75f,0.75f,0.75f));
        trackPaint.setColor(Color.argb(225,255,0,255));

    }

    public void setCompassModel(CompassModel compassModel) {
        this.compass = compassModel;
        invalidate();
    }

/*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);

    }
*/

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = w - xpad;
        float hh = h - ypad;

        maxLength = (int)Math.min(ww,hh);
        diameter = maxLength;
        radius = maxLength / 2f;

        textSize = maxLength / 10;

        textPaint.setTextSize(textSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawTracks(canvas);

    }

    private void drawBackground(Canvas canvas) {

        float sizeN = textPaint.measureText(TEXT_N);
        float sizeS = textPaint.measureText(TEXT_S);
        float sizeE = textPaint.measureText(TEXT_E);

        float padding = textSize / 4f;

        backgroundTrackPaint.setTextSize(4*radius/100f);
        backgroundTrackPaint.setFakeBoldText(true);

        canvas.drawOval(0, 0, diameter, diameter, backgroundPaint);
        for (int i = 0; i < 8; i++) {

            canvas.drawCircle(radius, radius, (8-i)*radius/9f + radius/100f, backgroundTrackPaint);
            canvas.drawCircle(radius, radius, (8-i)*radius/9f, backgroundPaint);

            String text = 10*(i+1) + "\u00B0";
            canvas.drawText(
                text,
                radius - backgroundTrackPaint.measureText(text)/2,
                radius - (8-i)*radius/9f - (1+0.25f)*radius/100f,
                backgroundTrackPaint
            );

        }
        canvas.drawCircle(radius, radius, radius/100f, backgroundTrackPaint);

        canvas.drawText(
            TEXT_N,
            radius - sizeN / 2f,
            padding + textSize,
            textPaint
        );
        canvas.drawText(
            TEXT_S,
            radius - sizeS / 2f,
            diameter - padding,
            textPaint
        );
        canvas.drawText(
            TEXT_E,
            diameter - padding - sizeE,
            radius + textSize / 2f,
            textPaint
        );
        canvas.drawText(
            TEXT_W,
            padding,
            radius + textSize / 2f,
            textPaint
        );

    }

    private void drawTracks(Canvas canvas) {

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
            Coordinate coordinate = compass.getCoordinate(23, 0, 0);
            x[0] = coordinate.getX();
            y[0] = coordinate.getY();
            altitude[0] = coordinate.getAltitude();
            for (int i = 0; i < 25; i++) {

                coordinate = compass.getCoordinate(i%24, 0, 0);

                x[i + 1] = coordinate.getX();
                y[i + 1] = coordinate.getY();
                altitude[i + 1] = coordinate.getAltitude();

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

    }

}
