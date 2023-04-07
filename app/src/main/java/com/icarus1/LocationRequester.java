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

import java.util.Map;

public class LocationRequester {
    //TODO cancel location updates

    private OnLocationChanged onLocationChanged;
    private final android.location.LocationListener locationListener = new LocationListener();

    public LocationRequester() {
    }

    public LocationRequester(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    public void setOnLocationChanged(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    public void request(FragmentActivity activity) {
        ActivityResultLauncher<String[]> request = activity.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new LocationCallback(activity)
        );
        request.launch(new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    private class LocationCallback implements ActivityResultCallback<Map<String,Boolean>> {

        private final FragmentActivity activity;

        public LocationCallback(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onActivityResult(Map<String,Boolean> result) {

            if (result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == Boolean.TRUE) {

                try {

                    LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);

                } catch (SecurityException e) {
                    //TODO log
                    throw e;
                }

            } else {
                //TODO log
            }

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
