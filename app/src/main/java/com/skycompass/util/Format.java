package com.skycompass.util;

public class Format {

    private Format() {
    }

    public static String Time(int hour, int minute, int seconds) {

        String result = "";

        result += hour < 10 ? "0" : "";
        result += hour;
        result += ":";
        result += minute < 10 ? "0" : "";
        result += minute;
        result += ":";
        result += seconds < 10 ? "0" : "";
        result += seconds;

        return result;

    }

    public static String UTCOffsetTime(int offset) {

        TimeZone timeZone = new TimeZone(offset);

        int hoursOffset = timeZone.getRawHourOffset();
        int minutesOffset = timeZone.getRawMinuteOffset();

        StringBuilder result = new StringBuilder();

        result.append(hoursOffset < 0 ? "-" : "");

        int absHourOffset = Math.abs(hoursOffset);
        int absMinutesOffset = Math.abs(minutesOffset);

        String hours = absHourOffset < 10 ? "0" : "";
        hours += String.valueOf(absHourOffset);

        String minutes = absMinutesOffset < 10 ? "0" : "";
        minutes += String.valueOf(absMinutesOffset);

        result.append(hours);
        result.append(":");
        result.append(minutes);

        return result.toString();
    }
    public static String UTCOffset(int offset) {

        TimeZone timeZone = new TimeZone(offset);

        int hoursOffset = timeZone.getRawHourOffset();
        int minutesOffset = timeZone.getRawMinuteOffset();

        StringBuilder result = new StringBuilder("UTC");

        result.append(hoursOffset < 0 ? "-" : "+");

        int absHourOffset = Math.abs(hoursOffset);
        int absMinutesOffset = Math.abs(minutesOffset);

        String hours = absHourOffset < 10 ? "0" : "";
        hours += String.valueOf(absHourOffset);

        String minutes = absMinutesOffset < 10 ? "0" : "";
        minutes += String.valueOf(absMinutesOffset);

        result.append(hours);
        result.append(":");
        result.append(minutes);

        return result.toString();
    }

    public static String Date(int year, int month, int dayOfMonth) {

        month += 1;
        dayOfMonth += 1;

        String result = "";

        result += year;
        result += "-";
        result += month;
        result += "-";
        result += dayOfMonth;

        return result;

    }

    public static String LatitudeLongitude(double latitude, double longitude) {

        String text = String.format("%.2f", Math.abs(latitude)) + "\u00B0" + (latitude < 0 ? "S" : "N");
        text += " ";
        text += String.format("%.2f", Math.abs(longitude)) + "\u00B0" + (longitude < 0 ? "W" : "E");

        return text;

    }

}
