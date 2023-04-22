package com.icarus1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.icarus1.util.Debug;

import java.util.Map;

public class LocationRequester {

    private ActivityResultLauncher<String[]> register;
    private ResultCallback resultCallback;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private OnPermissionResult onPermissionResult;
    private OnLocationChanged onLocationChanged;

    private PermissionState state = PermissionState.UNREQUESTED;
    private boolean resumed;

    public LocationRequester(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    public void resume() {
        resumed = true;
        if (locationManager != null) {
            requestLocationUpdatesFromLocationManager();
        }
    }

    public void pause() {
        resumed = false;
        if (locationManager != null) {
            cancelLocationUpdatesFromLocationManager();
        }
    }

    public void setOnPermissionResult(OnPermissionResult onPermissionResult) {
        this.onPermissionResult = onPermissionResult;
    }

    public void setOnLocationChanged(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    public void register(Fragment fragment) {

        if (register != null) {
            throw new RuntimeException("Must unregister previous register.");
        }

        resultCallback = new ResultCallback(fragment.requireActivity());
        register = fragment.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), resultCallback
        );

    }

    public void unregister() {

        if (register == null) {
            throw new RuntimeException("Must register before unregister.");
        }

        register.unregister();
        register = null;
        resultCallback = null;

    }

    public void request(Fragment fragment) {

        if (register == null) {
            throw new RuntimeException("Must register before request.");
        }

        state = PermissionState.REQUESTED;

        if (!permissionsGranted(fragment.requireContext()) || locationManager == null) {
            register.launch(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            });
        } else {
            requestLocationUpdatesFromLocationManager();
        }

    }

    public EnabledState on() {
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ? EnabledState.ENABLED : EnabledState.DISABLED;
        } else {
            return EnabledState.UNKNOWN;
        }
    }

    public PermissionState permissionState() {
        return state;
    }

    private boolean permissionsGranted(Context context) {

        boolean coarseLocationPermissionGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean fineLocationPermissionGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return coarseLocationPermissionGranted && fineLocationPermissionGranted;

    }

    private void requestLocationUpdatesFromLocationManager() {

        try {
            cancelLocationUpdatesFromLocationManager();
            locationListener = new LocationListener();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
        } catch (SecurityException e) {
            Debug.error(new Debug.Exception("LocationRequester SecurityException."));
        }

    }

    private void cancelLocationUpdatesFromLocationManager() {

        if (locationListener != null) {
            locationListener.cancel();
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }

    }

    private class ResultCallback implements ActivityResultCallback<Map<String,Boolean>> {

        private final FragmentActivity activity;

        public ResultCallback(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onActivityResult(Map<String,Boolean> result) {

            if (result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == Boolean.TRUE
                || result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) == Boolean.TRUE) {

                try {

                    state = PermissionState.GRANTED;

                    locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

                    if (onPermissionResult != null) {
                        onPermissionResult.onResult(true);
                    }

                    requestLocationUpdatesFromLocationManager();

                } catch (SecurityException e) {
                    Debug.error(new Debug.Exception("LocationRequester SecurityException."));
                }

            } else {
                state = PermissionState.DENIED;

                if (onPermissionResult != null) {
                    onPermissionResult.onResult(false);
                }

                Debug.log("Failed to get permission ACCESS_FINE_LOCATION.");
            }

        }

    }

    private class LocationListener implements android.location.LocationListener {

        private boolean cancelled;

        @Override
        public void onLocationChanged(@NonNull Location location) {

            if (cancelled || !resumed) {
                return;
            }

            if (LocationRequester.this.onLocationChanged != null) {
                LocationRequester.this.onLocationChanged.onLocationChanged(location.getLatitude(), location.getLongitude());
            }

        }

        private void cancel() {
            cancelled = true;
        }

    }

    public enum PermissionState {
        UNREQUESTED,
        REQUESTED,
        GRANTED,
        DENIED
    }

    public enum EnabledState {
        DISABLED,
        ENABLED,
        UNKNOWN
    }

    public interface OnPermissionResult {
        void onResult(boolean granted);
    }

    public interface OnLocationChanged {
        void onLocationChanged(double latitude, double longitude);
    }

}
