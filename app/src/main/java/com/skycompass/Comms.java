package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class Comms {

    public static class Date {

        private LocalDate date;
        private boolean currentDate;

        private Date() {
        }

        public static Date from(@NonNull Bundle bundle) {

            Date result = new Date();
            result.date = LocalDate.of(bundle.getInt("Y"), bundle.getInt("M")+1, bundle.getInt("D")+1);
            result.currentDate = bundle.getBoolean("CURRENT DATE");

            return result;

        }

        public LocalDate getDate() {
            return date;
        }

        public boolean isCurrentDate() {
            return currentDate;
        }

    }

    public static class Time {

        private LocalTime time;
        private ZoneOffset zoneOffset;
        private String location;

        private Time() {
        }

        public static Time from(@NonNull Bundle bundle) {

            Time time = new Time();
            time.time = LocalTime.of(bundle.getInt("HOUR"), bundle.getInt("MINUTE"), bundle.getInt("SECOND"));
            time.location = bundle.getString("LOCATION");
            time.zoneOffset = ZoneOffset.ofTotalSeconds(bundle.getInt("OFFSET")/1000);

            return time;

        }

        public LocalTime getTime() {
            return time;
        }

        public ZoneOffset getZoneOffset() {
            return zoneOffset;
        }

        public String getLocation() {
            return location;
        }

    }

}
