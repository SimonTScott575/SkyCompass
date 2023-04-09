package com.icarus1.compass;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Foreground {

    private static final String TEXT_N = "N";
    private static final String TEXT_S = "S";
    private static final String TEXT_E = "E";
    private static final String TEXT_W = "W";

    private float innerRadius;
    private float outerRadius;
    private float ringThickness;
    private final Paint backgroundRingPaint;
    private final Paint NSEWPaint;
    private final int NColor;

    public Foreground(float innerRadius, float outerRadius) {

        backgroundRingPaint = new Paint();
        backgroundRingPaint.setColor(Color.parseColor("#A27B5C"));
        backgroundRingPaint.setStyle(Paint.Style.STROKE);

        NSEWPaint = new Paint();
        NSEWPaint.setColor(Color.parseColor("#F5F5F5"));
        NSEWPaint.setStyle(Paint.Style.FILL);
        NSEWPaint.setFakeBoldText(true);

        NColor = Color.parseColor("#F05454");

        setRing(innerRadius, outerRadius);

    }

    public void setRing(float innerRadius, float outerRadius) {

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        ringThickness = outerRadius - innerRadius;
        backgroundRingPaint.setStrokeWidth(ringThickness);
        NSEWPaint.setTextSize(ringThickness);

    }

    public void draw(Canvas canvas) {

        canvas.drawCircle(outerRadius, outerRadius, innerRadius+ ringThickness /2f, backgroundRingPaint);
        drawNSEW(canvas);

    }

    private void drawNSEW(Canvas canvas) {

        float textSize = NSEWPaint.getTextSize();
        float halfThickness = ringThickness/2f;

        String[] text = new String[]{TEXT_N, TEXT_E, TEXT_S, TEXT_W};
        float[] textWidth = new float[]{
            NSEWPaint.measureText(TEXT_N),
            NSEWPaint.measureText(TEXT_N),
            NSEWPaint.measureText(TEXT_N),
            NSEWPaint.measureText(TEXT_N)
        };

        int SEWColor = NSEWPaint.getColor();
        NSEWPaint.setColor(NColor);

        canvas.drawText(
                text[0],
                outerRadius - textWidth[0]/2f,
                halfThickness + textSize/2f,
                NSEWPaint
        );

        NSEWPaint.setColor(SEWColor);

        for (int i = 1; i < 4; i++) {

            canvas.save();
            canvas.rotate(i*90f, outerRadius, outerRadius);

            canvas.drawText(
                text[i],
                outerRadius - textWidth[i] / 2f,
                2*halfThickness,
                NSEWPaint
            );

            canvas.restore();

        }

    }

}
