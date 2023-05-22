package com.icarus1.compass;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Foreground {

    private static final String TEXT_N = "N";
    private static final String TEXT_S = "S";
    private static final String TEXT_E = "E";
    private static final String TEXT_W = "W";

    private float innerRadius;
    private float outerRadius;
    private float ringThickness;
    private float NSEWPadding;
    private final Paint ringPaint;
    private final Paint NSEWPaint;
    private final int NColor;

    public Foreground(float innerRadius, float outerRadius) {

        ringPaint = new Paint();
        ringPaint.setColor(Values.RING.toArgb());
        ringPaint.setStyle(Paint.Style.STROKE);

        NSEWPaint = new Paint();
        NSEWPaint.setColor(Values.DIRECTION_SEW.toArgb());
        NSEWPaint.setStyle(Paint.Style.FILL);
        NSEWPaint.setFakeBoldText(true);

        NColor = Values.DIRECTION_N.toArgb();

        setRing(innerRadius, outerRadius);

    }

    public void setRing(float innerRadius, float outerRadius) {

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        ringThickness = outerRadius - innerRadius;
        NSEWPadding = 0;
        ringPaint.setStrokeWidth(ringThickness); // NSEWPadding);
        NSEWPaint.setTextSize(ringThickness - NSEWPadding);

    }

    public void draw(Canvas canvas) {

        canvas.drawCircle(outerRadius, outerRadius, innerRadius + ringThickness/2f, ringPaint);
        drawNSEW(canvas);

    }

    private void drawNSEW(Canvas canvas) {

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
            ringThickness - NSEWPadding,
            NSEWPaint
        );

        NSEWPaint.setColor(SEWColor);

        for (int i = 1; i < 4; i++) {

            canvas.save();
            canvas.rotate(i*90f, outerRadius, outerRadius);

            canvas.drawText(
                text[i],
                outerRadius - textWidth[i] / 2f,
                ringThickness - NSEWPadding,
                NSEWPaint
            );

            canvas.restore();

        }

    }

}
