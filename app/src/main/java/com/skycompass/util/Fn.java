package com.skycompass.util;

public class Fn {

    private Fn() {
    }

    public static int modular(int value, int mod) {
        return ((value % mod) + mod) % mod;
    }

    public static long modular(long value, long mod) {
        return ((value % mod) + mod) % mod;
    }

    public static float modular(float value, float mod) {
        return ((value % mod) + mod) % mod;
    }

    public static double modular(double value, double mod) {
        return ((value % mod) + mod) % mod;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value,min),max);
    }

    public static long clamp(long value, long min, long max) {
        return Math.min(Math.max(value,min),max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value,min),max);
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value,min),max);
    }

    public static float clampAngle(float value) {
        value += Math.PI;
        value = (float)modular(value, 2*Math.PI);
        value -= Math.PI;
        return value;
    }
    public static double clampAngle(double value) {
        value += Math.PI;
        value = modular(value, 2*Math.PI);
        value -= Math.PI;
        return value;
    }

    public static float lerp(float start, float end, float t) {
        return start + (end - start)*t;
    }

    public static double lerp(double start, double end, double t) {
        return start + (end - start)*t;
    }

    public static float lerpAngle(float start, float end, float t) {

        start = clampAngle(start);
        end = clampAngle(end);

        float diff = end - start;
        if (diff > Math.PI) {
            diff = diff - 2*(float)Math.PI;
        } else if (diff < -Math.PI) {
            diff = 2*(float)Math.PI + diff;
        }

        return clampAngle(start + diff * t);

    }
    public static double lerpAngle(double start, double end, double t) {

        start = clampAngle(start);
        end = clampAngle(end);

        double diff = end - start;
        if (diff > Math.PI) {
            diff = diff - 2*Math.PI;
        } else if (diff < -Math.PI) {
            diff = 2*Math.PI + diff;
        }

        return clampAngle(start + diff * t);

    }

}
