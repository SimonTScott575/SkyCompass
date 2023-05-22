package com.icarus1.compass;

import android.graphics.Color;
import android.graphics.Paint;

import io.github.cosinekitty.astronomy.Body;

public enum CelestialObject {

    SUN,
    MOON,
    MERCURY,
    VENUS,
    MARS,
    JUPITER,
    SATURN,
    URANUS,
    NEPTUNE,
    PLUTO;

    private static final Body[] bodies = new Body[]{
        Body.Sun,
        Body.Moon,
        Body.Mercury,
        Body.Venus,
        Body.Mars,
        Body.Jupiter,
        Body.Saturn,
        Body.Uranus,
        Body.Neptune,
        Body.Pluto
    };

    private static final Paint[] paint = new Paint[values().length];
    static {
        paint[SUN.ordinal()] = createPaint(255,235, 235, 71);
        paint[MOON.ordinal()] = createPaint(255,150,150,150);
        paint[MERCURY.ordinal()] = createPaint(255,193,189,188);
        paint[VENUS.ordinal()] = createPaint(255,244,220,196);
        paint[MARS.ordinal()] = createPaint(255,242,122,95);
        paint[JUPITER.ordinal()] = createPaint(255,192,130,55);
        paint[SATURN.ordinal()] = createPaint(255,243,206,136);
        paint[URANUS.ordinal()] = createPaint(255,208,236,240);
        paint[NEPTUNE.ordinal()] = createPaint(255,119,158,191);
        paint[PLUTO.ordinal()] = createPaint(255,134,106,84);
    }
    private static Paint createPaint(int alpha, int red, int green, int blue) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(alpha, red, green, blue));

        return paint;

    }

    private static final CelestialObject[] planets = new CelestialObject[]{
        MERCURY,
        VENUS,
        MARS,
        JUPITER,
        SATURN,
        URANUS,
        NEPTUNE,
        PLUTO
    };
    private static final CelestialObject[] nonPlanets = new CelestialObject[]{
        SUN,
        MOON
    };

    public static CelestialObject[] planets() {
        return planets.clone();
    }
    public static CelestialObject[] nonPlanets() {
        return nonPlanets.clone();
    }

    public String getName() {
        return getBody().name();
    }

    public Body getBody() {
        return bodies[ordinal()];
    }

    public Paint getPaint() {
        return paint[ordinal()];
    }

}