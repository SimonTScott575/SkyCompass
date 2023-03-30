package com.icarus1.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.icarus1.R;

public class Foreground {

    private static final String TEXT_N = "N";
    private static final String TEXT_S = "S";
    private static final String TEXT_E = "E";
    private static final String TEXT_W = "W";

    private float innerRadius;
    private float outerRadius;
    private float thickness;
    private final Paint backgroundRingPaint;
    private final Paint NSEWPaint;
    private final int NColor;

    public Foreground(Context context, float innerRadius, float outerRadius) {

        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();

        backgroundRingPaint = new Paint();
        backgroundRingPaint.setColor(resources.getColor(R.color.compass_ring, theme));
        backgroundRingPaint.setStyle(Paint.Style.STROKE);

        NSEWPaint = new Paint();
        NSEWPaint.setColor(resources.getColor(R.color.compass_SEW, theme));
        NSEWPaint.setStyle(Paint.Style.FILL);
        NSEWPaint.setFakeBoldText(true);

        NColor = resources.getColor(R.color.compass_N, theme);

        setRing(innerRadius, outerRadius);

    }

    public void setRing(float innerRadius, float outerRadius) {

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        thickness = outerRadius - innerRadius;
        backgroundRingPaint.setStrokeWidth(thickness);
        NSEWPaint.setTextSize(thickness);

    }

    public void draw(Canvas canvas) {

        canvas.drawCircle(outerRadius, outerRadius, innerRadius+thickness/2f, backgroundRingPaint);
        drawNSEW(canvas);

    }

    private void drawNSEW(Canvas canvas) {

        float textSize = NSEWPaint.getTextSize();
        float halfThickness = thickness/2f;
        float outerDiameter = 2f*outerRadius;

        float sizeN = NSEWPaint.measureText(TEXT_N);
        float sizeS = NSEWPaint.measureText(TEXT_S);
        float sizeE = NSEWPaint.measureText(TEXT_E);
        float sizeW = NSEWPaint.measureText(TEXT_W);

        canvas.drawText(
            TEXT_S,
            outerRadius - sizeS/2f,
            outerDiameter - halfThickness + textSize/2f,
            NSEWPaint
        );
        canvas.drawText(
            TEXT_E,
            outerDiameter - halfThickness - sizeE/2f,
            outerRadius + textSize/2f,
            NSEWPaint
        );
        canvas.drawText(
            TEXT_W,
            halfThickness - sizeW/2f,
            outerRadius + textSize/2f,
            NSEWPaint
        );

        int SEWColor = NSEWPaint.getColor();
        NSEWPaint.setColor(NColor);

        canvas.drawText(
            TEXT_N,
            outerRadius - sizeN/2f,
            halfThickness + textSize/2f,
            NSEWPaint
        );

        NSEWPaint.setColor(SEWColor);

    }

}
