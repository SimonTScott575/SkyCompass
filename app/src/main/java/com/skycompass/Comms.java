package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class Comms {

    public static class Time {

        private LocalTime time;
        private ZoneOffset zoneOffset;
        private String location;

        private Time() {
        }

        public static Bundle putInto(LocalTime time, int offset, String id, @NonNull Bundle bundle) {

            bundle.putInt("HOUR", time.getHour());
            bundle.putInt("MINUTE", time.getMinute());
            bundle.putInt("SECOND", time.getSecond());
            bundle.putInt("OFFSET", offset);
            bundle.putString("LOCATION", id);

            return bundle;

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
