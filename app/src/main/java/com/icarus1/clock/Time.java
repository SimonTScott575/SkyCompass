package com.icarus1.clock;

import java.util.Calendar;

public class Time {

    private int hour; // Range 0 - 23
    private int minute; // Range 0 - 59
    private int second; // Range 0 - 59

    public Time(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public static Time fromSystem() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR)+(calendar.get(Calendar.AM_PM)==Calendar.PM?12:0);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return new Time(hour, minute, seconds);

    }

}
