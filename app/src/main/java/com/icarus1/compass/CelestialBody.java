package com.icarus1.compass;

import android.graphics.Color;
import android.graphics.Paint;

import io.github.cosinekitty.astronomy.Body;

public enum CelestialBody {

    SUN(new SunFactory()),
    MOON(new MoonFactory()),
    MERCURY(new MercuryFactory()),
    VENUS(new VenusFactory()),
    MARS(new MarsFactory()),
    JUPITER(new JupiterFactory()),
    SATURN(new SaturnFactory()),
    URANUS(new UranusFactory()),
    NEPTUNE(new NeptuneFactory()),
    PLUTO(new PlutoFactory());

    private int index;
    private Body body;
    private Paint paint;
    private String name;

    CelestialBody(Factory factory) {
        factory.init(this);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Body getBody() {
        return body;
    }

    public Paint getPaint() {
        return paint;
    }

    private interface Factory {
        void init(CelestialBody body);
    }

    private static class SunFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 0;
            body.name = Body.Sun.toString();
            body.body = Body.Sun;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,1f,0f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class MoonFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 1;
            body.name = Body.Moon.toString();
            body.body = Body.Moon;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,1f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class MercuryFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 2;
            body.name = Body.Mercury.toString();
            body.body = Body.Mercury;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class VenusFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 3;
            body.name = Body.Venus.toString();
            body.body = Body.Venus;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class MarsFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 4;
            body.name = Body.Mars.toString();
            body.body = Body.Mars;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class JupiterFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 5;
            body.name = Body.Jupiter.toString();
            body.body = Body.Jupiter;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class SaturnFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 6;
            body.name = Body.Saturn.toString();
            body.body = Body.Saturn;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class UranusFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 7;
            body.name = Body.Uranus.toString();
            body.body = Body.Uranus;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class NeptuneFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 8;
            body.name = Body.Neptune.toString();
            body.body = Body.Neptune;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }
    private static class PlutoFactory implements Factory {
        @Override
        public void init(CelestialBody body) {
            body.index = 9;
            body.name = Body.Pluto.toString();
            body.body = Body.Pluto;
            body.paint = new Paint();
            body.paint.setColor(Color.argb(0.75f,1f,0f,1f));
            body.paint.setStyle(Paint.Style.FILL);
        }
    }

}