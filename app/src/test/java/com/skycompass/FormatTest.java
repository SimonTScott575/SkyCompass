package com.skycompass;

import static org.junit.Assert.*;
import org.junit.Test;

import com.skycompass.util.Format;

public class FormatTest {

    @Test
    public void TimeTest() {

        assertEquals(Format.Time(0,0,0), "00:00:00");
        assertEquals(Format.Time(1,1,1), "01:01:01");
        assertEquals(Format.Time(10,10,10), "10:10:10");

    }

    @Test
    public void TimeZoneOffsetTest() {

        assertEquals(Format.TimeZoneOffset(0), "+00:00");
        assertEquals(Format.TimeZoneOffset(1), "+00:00");
        assertEquals(Format.TimeZoneOffset(1000), "+00:00");
        assertEquals(Format.TimeZoneOffset(60000), "+00:01");
        assertEquals(Format.TimeZoneOffset(3600000), "+01:00");
        assertEquals(Format.TimeZoneOffset(360000000), "+100:00");

        assertEquals(Format.TimeZoneOffset(-0), "+00:00");
        assertEquals(Format.TimeZoneOffset(-1), "+00:00");
        assertEquals(Format.TimeZoneOffset(-1000), "+00:00");
        assertEquals(Format.TimeZoneOffset(-60000), "-00:01");
        assertEquals(Format.TimeZoneOffset(-3600000), "-01:00");
        assertEquals(Format.TimeZoneOffset(-360000000), "-100:00");

    }

    @Test
    public void DateTest() {

        assertEquals(Format.Date(1,0,0), "0001-01-01");
        assertEquals(Format.Date(1,1,1), "0001-02-02");
        assertEquals(Format.Date(2000,0,0), "2000-01-01");
        assertEquals(Format.Date(2000,10,10), "2000-11-11");

    }

}
