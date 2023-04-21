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

    public LocationRequester(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
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
        if (state != PermissionState.UNREQUESTED) {
            throw new RuntimeException("Already requested.");
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
            if (locationListener != null) {
                locationListener.cancel();
            }
            locationListener = new LocationListener();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
        } catch (SecurityException e) {
            Debug.error(new Debug.Exception("LocationRequester SecurityException."));
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

                    if (onPermissionResult != null) {
                        onPermissionResult.onResult(true);
                    }

                    locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
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

            if (cancelled) {
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

    public interface OnPermissionResult {
        void onResult(boolean granted);
    }

    public interface OnLocationChanged {
        void onLocationChanged(double latitude, double longitude);
    }

}
