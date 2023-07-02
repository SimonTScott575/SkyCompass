package com.skycompass.compass;

import static com.skycompass.compass.Values.*;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Foreground {

    private float innerRadius;
    private float outerRadius;
    private float outlineThickness;
    private float NSEWPadding;
    private final Paint ringPaint;
    private final Paint NSEWPaint;
    private final int NColor;

    public Foreground(float innerRadius, float outerRadius) {

        ringPaint = new Paint();
        ringPaint.setColor(RING_COLOR.toArgb());
        ringPaint.setStyle(Paint.Style.STROKE);

        NSEWPaint = new Paint();
        NSEWPaint.setColor(TEXT_SEW_COLOR.toArgb());
        NSEWPaint.setStyle(Paint.Style.FILL);
        NSEWPaint.setFakeBoldText(true);

        NColor = TEXT_N_COLOR.toArgb();

        setRing(innerRadius, outerRadius);

    }

    public void setColor(Color color) {
        NSEWPaint.setColor(color.toArgb());
        ringPaint.setColor(color.toArgb());
    }

    public void setRing(float innerRadius, float outerRadius) {

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        outlineThickness = (outerRadius - innerRadius) * RIM_FRACTION_OF_BORDER;
        NSEWPadding = outlineThickness;
        ringPaint.setStrokeWidth(outlineThickness);
        NSEWPaint.setTextSize((outerRadius - innerRadius) - NSEWPadding);

    }

    public void draw(Canvas canvas) {

        canvas.drawCircle(outerRadius, outerRadius, innerRadius + outlineThickness /2f, ringPaint);
        drawNSEW(canvas);

    }

    private void drawNSEW(Canvas canvas) {

        String[] text = new String[]{TEXT_N, TEXT_E, TEXT_S, TEXT_W};
        float[] textWidth = new float[]{
            NSEWPaint.measureText(TEXT_N),
            NSEWPaint.measureText(TEXT_E),
            NSEWPaint.measureText(TEXT_S),
            NSEWPaint.measureText(TEXT_W)
        };

        int SEWColor = NSEWPaint.getColor();
        NSEWPaint.setColor(NColor);

        canvas.drawText(
            text[0],
            outerRadius - textWidth[0]/2f,
            (outerRadius - innerRadius) - NSEWPadding,
            NSEWPaint
        );

        NSEWPaint.setColor(SEWColor);

        for (int i = 1; i < 4; i++) {

            canvas.save();
            canvas.rotate(i*90f, outerRadius, outerRadius);

            canvas.drawText(
                text[i],
                outerRadius - textWidth[i] / 2f,
                (outerRadius - innerRadius) - NSEWPadding,
                NSEWPaint
            );

            canvas.restore();

        }

    }

}
