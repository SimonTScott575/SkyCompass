package com.icarus1.compass;

import android.graphics.Color;
import android.graphics.Paint;

import io.github.cosinekitty.astronomy.Body;

enum CelestialBody {

    SUN(Body.Sun),
    MOON(Body.Moon),
    MERCURY(Body.Mercury),
    VENUS(Body.Venus),
    EARTH(Body.Earth),
    MARS(Body.Mars),
    JUPITER(Body.Jupiter),
    SATURN(Body.Saturn),
    URANUS(Body.Uranus),
    NEPTUNE(Body.Neptune),
    PLUTO(Body.Pluto);

    private Body body;
    private Paint paint;

    CelestialBody(Body body) {
        this.body = body;
        switch (body) {
            case Sun :
                initSun();
                break;
            case Moon :
                initMoon();
                break;
            default :
                initPlanet();
        }
    }

    public Body getBody() {
        return body;
    }

    public Paint getPaint() {
        return paint;
    }

    private void initSun() {
        body = Body.Sun;
        paint = new Paint();
        paint.setColor(Color.argb(0.75f,1f,1f,0f));
        paint.setStyle(Paint.Style.FILL);
    }

    private void initMoon() {
        body = Body.Moon;
        paint = new Paint();
        paint.setColor(Color.argb(0.75f,1f,1f,1f));
        paint.setStyle(Paint.Style.FILL);
    }

    private void initPlanet() {
        paint = new Paint();
        paint.setColor(Color.argb(0.5f,1f,0f,1f));
        paint.setStyle(Paint.Style.FILL);
    }

}