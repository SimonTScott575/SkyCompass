package com.icarus1.util;

import java.util.Calendar;

public class Time {

    private final int hour; // Range 0 - 23
    private final int minute; // Range 0 - 59
    private final int second; // Range 0 - 59

    public Time(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public final int getHour() {
        return hour;
    }

    public final int getMinute() {
        return minute;
    }

    public final int getSecond() {
        return second;
    }

    public static Time fromSystem() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR)+(calendar.get(Calendar.AM_PM)==Calendar.PM?12:0);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return new Time(hour, minute, seconds);

    }

}
