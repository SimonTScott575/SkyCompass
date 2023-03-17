package com.icarus1.util;

import java.util.TimeZone;

public class Format {

    private Format() {
    }

    public static String UTCOffset(int hoursOffset, int minutesOffset) {

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

        StringBuilder result = new StringBuilder();

        result.append(year);
        result.append("-");
        result.append(month);
        result.append("-");
        result.append(dayOfMonth);

        return result.toString();

    }

    public static String LatitudeLongitude(double latitude, double longitude) {

        String text = String.format("%.2f", Math.abs(latitude)) + "\u00B0" + (latitude < 0 ? "S" : "N");
        text += " ";
        text += String.format("%.2f", Math.abs(longitude)) + "\u00B0" + (longitude < 0 ? "W" : "E");

        return text;

    }

    public static String Location(TimeZone timeZone) {

        return timeZone.getDisplayName() + " - " + timeZone.getID();

    }

}
