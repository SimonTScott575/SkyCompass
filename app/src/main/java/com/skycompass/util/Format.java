package com.skycompass.util;

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

    public static String TimeZoneOffset(int offset) {

        TimeZone timeZone = new TimeZone(offset);

        int hoursOffset = timeZone.getRawHourOffset();
        int absHourOffset = Math.abs(hoursOffset);
        String hours = absHourOffset < 10 ? "0" : "";
        hours += String.valueOf(absHourOffset);

        int minutesOffset = timeZone.getRawMinuteOffset();
        int absMinutesOffset = Math.abs(minutesOffset);
        String minutes = absMinutesOffset < 10 ? "0" : "";
        minutes += String.valueOf(absMinutesOffset);

        String result = "";
        result += (hoursOffset < 0 || minutesOffset < 0) ? "-" : "+";
        result += hours;
        result += ":";
        result += minutes;

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
