package com.icarus1.util;

import java.util.Calendar;

public class TimeZone {

    private final int UTCOffset;
    private final String id;

    public TimeZone(int UTCOffset) {
        this.UTCOffset = UTCOffset;
        this.id = null;
    }

    public TimeZone(int UTCOffset, String id) {
        this.UTCOffset = UTCOffset;
        this.id = id;
    }

    public final int getUTCOffset() {
        return UTCOffset;
    }

    public final int getUTCHourOffset() {
        return UTCOffset/3600000;
    }

    public final int getUTCMinuteOffset() {
        return (UTCOffset - getUTCHourOffset()*3600000)/60000;
    }

    public final int getUTCSecondOffset() {
        return (UTCOffset - getUTCHourOffset()*3600000 - getUTCMinuteOffset()*60000)/1000;
    }

    public final int getUTCMillisecondOffset() {
        return (UTCOffset - getUTCHourOffset()*3600000 - getUTCMinuteOffset()*60000 - getUTCSecondOffset()*1000);
    }

    public final String getId() {
        return id;
    }

    public static TimeZone fromSystem() {

        java.util.TimeZone timeZone = java.util.TimeZone.getDefault();
        int DSTOffset = Calendar.getInstance().get(Calendar.DST_OFFSET);
        int UTCOffset = timeZone.getRawOffset() + DSTOffset;

        String location = timeZone.getID();

        return new TimeZone(UTCOffset, location);

    }

}
