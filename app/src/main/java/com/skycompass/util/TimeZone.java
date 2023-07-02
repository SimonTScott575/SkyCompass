package com.skycompass.util;

import androidx.annotation.Nullable;

import java.util.Calendar;

public class TimeZone {

    private final int rawOffset;
    private final java.util.TimeZone timeZone;
    private final boolean useDST;

    public static final int MILLISECONDS_IN_HOUR = 60*60*1000;
    public static final int MILLISECONDS_IN_MINUTE = 60*1000;
    public static final int MILLISECONDS_IN_SECOND = 1000;

    public TimeZone(int UTCOffset) {
        this.useDST = false;
        this.timeZone = null;
        this.rawOffset = UTCOffset;
    }

    public TimeZone(TimeZone timeZone, boolean useDST) {
        this.useDST = useDST;
        this.timeZone = timeZone.timeZone;
        this.rawOffset = timeZone.getRawOffset();
    }

    public TimeZone(String id, boolean useDST) {
        if (id == null) {
            throw new IllegalArgumentException("TimeZone constructor argument id can not be null.");
        }
        this.useDST = useDST;
        this.timeZone = java.util.TimeZone.getTimeZone(id);
        this.rawOffset = timeZone.getRawOffset();
    }

    public final int getOffset() {
        if (timeZone == null) {
            return rawOffset;
        }
        return timeZone.getRawOffset() + (useDST ? timeZone.getDSTSavings() : 0);
    }

    public static int getDSTOffset(
        java.util.TimeZone timeZone,
        int year, int month, int dayOfMonth,
        int hour, int minute, int second
    ) {

        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        cal.set(Calendar.AM_PM, hour >= 12 ? Calendar.PM : Calendar.AM);
        cal.set(Calendar.HOUR, hour >= 12 ? hour - 12 : hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        return cal.get(Calendar.DST_OFFSET);

    }

    public final int getRawOffset() {
        return rawOffset;
    }

    public final int getRawHourOffset() {
        return rawOffset / MILLISECONDS_IN_HOUR;
    }

    public final int getRawMinuteOffset() {
        return (rawOffset - getRawHourOffset()*MILLISECONDS_IN_HOUR) / MILLISECONDS_IN_MINUTE;
    }

    public final int getRawSecondOffset() {
        int result = rawOffset;
        result -= getRawHourOffset() * MILLISECONDS_IN_HOUR;
        result -= getRawMinuteOffset() * MILLISECONDS_IN_MINUTE;
        return result/MILLISECONDS_IN_SECOND;
    }

    public final int getRawMillisecondOffset() {
        int result = rawOffset;
        result -= getRawHourOffset() * MILLISECONDS_IN_HOUR;
        result -= getRawMinuteOffset() * MILLISECONDS_IN_MINUTE;
        return result;
    }

    public final Time timeFromUTC(Time utc) {
        return new Time(
            utc.getHour() + getRawHourOffset(),
            utc.getMinute() + getRawMinuteOffset(),
            utc.getSecond() + getRawSecondOffset()
        );
    }

    public final Time timeToUTC(Time time) {
        return new Time(
            time.getHour() - getRawHourOffset(),
            time.getMinute() - getRawMinuteOffset(),
            time.getSecond() - getRawSecondOffset()
        );
    }

    @Nullable
    public final String getID() {
        if (timeZone != null) {
            return timeZone.getID();
        }
        return null;
    }

    public int getDST() {
        if (timeZone == null) {
            return 0;
        }
        return timeZone.getDSTSavings();
    }

    public static TimeZone fromSystem() {
        return new TimeZone(
            java.util.TimeZone.getDefault().getID(),
            java.util.TimeZone.getDefault().inDaylightTime(Calendar.getInstance().getTime())
        );
    }

}
