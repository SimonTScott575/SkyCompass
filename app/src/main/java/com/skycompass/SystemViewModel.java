package com.skycompass;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skycompass.util.Debug;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class SystemViewModel extends ViewModel {

    private boolean isSystemLocation = false;
    private boolean isSystemDate = true;
    private boolean isSystemTime = true;

    private ZoneId zoneId;

    private final MutableLiveData<LocalDate> dateLiveData = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<LocalTime> timeLiveData = new MutableLiveData<>(LocalTime.now());
    private final MutableLiveData<SystemViewModel.Location> locationLiveData = new MutableLiveData<>(new SystemViewModel.Location(0, 0));
    private final MutableLiveData<ZoneOffset> zoneOffsetLiveData = new MutableLiveData<>(ZoneOffset.ofHours(0));

    private boolean useSystemLocation = false;
    private SystemViewModel.Location systemLocation = null;

    private RetrieveSystemValues retrieveSystemValues = null;
    private Thread thread = null;

    public void setLocation(SystemViewModel.Location location) {
        isSystemLocation = false;
        locationLiveData.setValue(location);
    }

    public LiveData<SystemViewModel.Location> getLocationLiveData() {
        return locationLiveData;
    }

    public void setDate(LocalDate date) {
        isSystemDate = false;
        dateLiveData.setValue(date);
    }

    public LiveData<LocalDate> getDateLiveData() {
        return dateLiveData;
    }

    public void setTime(LocalTime time) {
        isSystemTime = false;
        timeLiveData.setValue(time);
    }

    public LiveData<LocalTime> getTimeLiveData() {
        return timeLiveData;
    }

    public void setZoneId(ZoneId zoneId) {
        isSystemTime = false;
        this.zoneId = zoneId;
        zoneOffsetLiveData.setValue(null);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneOffset(ZoneOffset offset) {
        isSystemTime = false;
        zoneId = null;
        zoneOffsetLiveData.setValue(offset);
    }

    public ZoneOffset getZoneOffset() {
        if (zoneId != null)
            return ZonedDateTime.of(dateLiveData.getValue(), timeLiveData.getValue(), zoneId).getOffset();
        else
            return zoneOffsetLiveData.getValue();
    }

    public LiveData<ZoneOffset> getZoneOffsetLiveData() {
        return zoneOffsetLiveData;
    }

    public boolean isSystemLocation() {
        return isSystemLocation;
    }

    public boolean hasSystemLocation() {
        return systemLocation != null;
    }

    public boolean isSystemTime() {
        return isSystemTime;
    }

    public boolean isSystemDate() {
        return isSystemDate;
    }

    public void useSystemLocation() {

        useSystemLocation = true;

        if (systemLocation != null) {
            isSystemLocation = true;
            locationLiveData.setValue(systemLocation);
        }

    }

    public void useSystemTime() {

        this.isSystemTime = true;

        updateSystemValues();

    }

    public void useSystemDate() {

        this.isSystemDate = true;

        updateSystemValues();

    }

    public void updateSystemLocation(SystemViewModel.Location location) {

        systemLocation = location;

        if (useSystemLocation && systemLocation != null) {
            isSystemLocation = true;
            locationLiveData.postValue(systemLocation);
        }

    }

    public void updateSystemValues() {

        if (isSystemDate)
            dateLiveData.postValue(LocalDate.now());

        if (isSystemTime) {
            timeLiveData.postValue(LocalTime.now());
            zoneId = ZoneId.systemDefault();
            zoneOffsetLiveData.postValue(ZonedDateTime.of(dateLiveData.getValue(), timeLiveData.getValue(), zoneId).getOffset());
        }

    }

    public void startRetrieveSystemValues() {

        if (retrieveSystemValues != null)
            return;

        retrieveSystemValues = new RetrieveSystemValues();

        thread = new Thread(retrieveSystemValues);

        thread.start();

    }

    public void endRetrieveSystemValues() {
        if (retrieveSystemValues != null) {
            retrieveSystemValues.end = true;
            retrieveSystemValues = null;
        }
    }

    private class RetrieveSystemValues implements Runnable {

        private boolean end = false;

        @Override
        public void run() {

            while (!end) {

                updateSystemValues();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Debug.log("Interrupted.");
                    end = true;
                }

            }

        }

    }


    public static class Location {

        public double latitude;
        public double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

    }
}
