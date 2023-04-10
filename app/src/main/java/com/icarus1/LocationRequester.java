package com.icarus1;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.icarus1.util.Debug;

import java.util.Map;

public class LocationRequester {

    private ActivityResultLauncher<String[]> register;
    private LocationCallback registerCallback;
    private LocationManager lm;
    private OnLocationChanged onLocationChanged;
    private final android.location.LocationListener locationListener;

    public LocationRequester() {

        register = null;
        locationListener = new LocationListener();

    }

    public LocationRequester(OnLocationChanged onLocationChanged) {
        this();
        this.onLocationChanged = onLocationChanged;
    }

    public void setOnLocationChanged(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    public void register(FragmentActivity activity) {

        if (register != null) {
            throw new RuntimeException("Must unregister previous register.");
        }

        registerCallback = new LocationCallback(activity);
        register = activity.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), registerCallback
        );

    }

    public void unregister() {
        if (register != null) {
            register.unregister();
            register = null;
            registerCallback = null;
        }
    }

    public void request() {

        if (register == null) {
            throw new RuntimeException("Must register before request.");
        }

        register.launch(new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        });

    }

    public void cancel() {
        if (lm != null) {
            lm.removeUpdates(locationListener);
            lm = null;
        }
        if (registerCallback != null) {
            registerCallback.cancel();
        }
    }

    private class LocationCallback implements ActivityResultCallback<Map<String,Boolean>> {

        private final FragmentActivity activity;
        private boolean canceled;

        public LocationCallback(FragmentActivity activity) {
            canceled = true;
            this.activity = activity;
        }

        @Override
        public void onActivityResult(Map<String,Boolean> result) {

            if (result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == Boolean.TRUE) {

                try {

                    if (canceled) {
                        lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
                    }

                } catch (SecurityException e) {
                    Debug.error(new Debug.Exception("LocationRequester SecurityException."));
                }

            } else {
                Debug.error("Failed to get permission ACCESS_FINE_LOCATION.");
            }

        }

        public void cancel() {
            canceled = false;
        }

    }

    private class LocationListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (LocationRequester.this.onLocationChanged != null) {
                LocationRequester.this.onLocationChanged.onLocationChanged(location.getLatitude(), location.getLongitude());
            }
        }
    }

    public interface OnLocationChanged {
        void onLocationChanged(double latitude, double longitude);
    }

}
