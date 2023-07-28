package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class Comms {

    public static class Location {

        private double latitude;
        private double longitude;
        private String location;

        public static Bundle putInto(double latitude, double longitude, String location, Bundle bundle) {

            bundle.putDouble("Longitude", longitude);
            bundle.putDouble("Latitude", latitude);
            bundle.putString("Location", location);

            return bundle;

        }

        public static Location from(@NonNull Bundle bundle) {

            Location location = new Location();
            location.latitude = bundle.getDouble("Longitude");
            location.longitude = bundle.getDouble("Latitude");
            location.location = bundle.getString("Location");

            return location;

        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getLocation() {
            return location;
        }
    }

    public static class Date {

        private LocalDate date;
        private boolean currentDate;

        private Date() {
        }

        public static Bundle putInto(int year, int month, int day, boolean currentDate, @NonNull Bundle  bundle) {

            bundle.putInt("Y", year);
            bundle.putInt("M", month);
            bundle.putInt("D", day);
            bundle.putBoolean("CURRENT DATE", currentDate);

            return bundle;

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
