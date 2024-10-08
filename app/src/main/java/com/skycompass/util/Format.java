package com.skycompass.util;

import android.util.Log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Format {

    private Format() {
    }

    public static String Time(int hour, int minute, int seconds) {

        LocalTime time = LocalTime.of(hour, minute, seconds);

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static String Time(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static String TimeZoneOffset(int offset) {

        int hoursOffset = getRawHourOffset(offset);
        int minutesOffset = getRawMinuteOffset(offset);

        String result = String.format("%s%02d:%02d", offset < 0 ? "-" : "+", Math.abs(hoursOffset), Math.abs(minutesOffset));

        return result;
    }

    private static final int MILLISECONDS_IN_SECOND = 1000;
    private static final int MILLISECONDS_IN_MINUTE = 60 * 1000;
    private static final int MILLISECONDS_IN_HOUR = 60 * MILLISECONDS_IN_MINUTE;

    public static int getRawHourOffset(int rawOffset) {
        return rawOffset / MILLISECONDS_IN_HOUR;
    }

    public static int getRawMinuteOffset(int rawOffset) {
        return (rawOffset - getRawHourOffset(rawOffset) * MILLISECONDS_IN_HOUR) / MILLISECONDS_IN_MINUTE;
    }

    public static int getRawSecondOffset(int rawOffset) {

        int result = rawOffset;

        result -= getRawHourOffset(rawOffset) * MILLISECONDS_IN_HOUR;
        result -= getRawMinuteOffset(rawOffset) * MILLISECONDS_IN_MINUTE;

        return result/MILLISECONDS_IN_SECOND;
    }

    public static int getRawMillisecondOffset(int rawOffset) {

        int result = rawOffset;

        result -= getRawHourOffset(rawOffset) * MILLISECONDS_IN_HOUR;
        result -= getRawMinuteOffset(rawOffset) * MILLISECONDS_IN_MINUTE;

        return result;
    }

    public static String Date(int year, int month, int dayOfMonth) {

        LocalDate date = LocalDate.of(year, month+1, dayOfMonth+1);

        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String LatitudeLongitude(double latitude, double longitude) {

        String text = String.format("%.2f", Math.abs(latitude)) + "\u00B0" + (latitude < 0 ? "S" : "N");
        text += " ";
        text += String.format("%.2f", Math.abs(longitude)) + "\u00B0" + (longitude < 0 ? "W" : "E");

        return text;
    }

}
