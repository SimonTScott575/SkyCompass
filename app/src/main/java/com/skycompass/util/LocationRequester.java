package com.skycompass.util;

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

import java.util.Map;

public class LocationRequester {

    private ActivityResultLauncher<String[]> register;
    private RequestCallback requestCallback;
    public enum RequestState {
        UNREQUESTED,
        REQUESTED,
        GRANTED,
        DENIED
    }
    private RequestState state;
    private OnRequestResult onRequestResult;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private OnLocationChanged onLocationChanged;

    private boolean resumed;

    public LocationRequester(OnLocationChanged onLocationChanged) {
        state = RequestState.UNREQUESTED;
        this.onLocationChanged = onLocationChanged;
    }

    public final void resume() {
        resumed = true;
        if (locationManager != null) {
            requestLocationUpdatesFromLocationManager();
        }
    }

    public final void pause() {
        resumed = false;
        if (locationManager != null) {
            cancelLocationUpdatesFromLocationManager();
        }
    }

    public final void register(Fragment fragment) {

        if (register != null) {
            throw new RuntimeException("Must unregister previous register.");
        }

        requestCallback = new RequestCallback(fragment.requireActivity());
        register = fragment.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            requestCallback
        );

    }

    public final void unregister() {

        if (register == null) {
            throw new RuntimeException("Must register before unregister.");
        }

        register.unregister();
        register = null;
        requestCallback = null;

    }

    public final void request(Fragment fragment) {

        if (register == null) {
            throw new RuntimeException("Must register before request.");
        }

        state = RequestState.REQUESTED;

        if (!permissionsGranted(fragment.requireContext()) || locationManager == null) {
            register.launch(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            });
        } else {
            requestLocationUpdatesFromLocationManager();
        }

    }

    private boolean permissionsGranted(Context context) {

        boolean coarseLocationPermissionGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean fineLocationPermissionGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return coarseLocationPermissionGranted || fineLocationPermissionGranted;

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

    private class RequestCallback implements ActivityResultCallback<Map<String,Boolean>> {

        private final FragmentActivity activity;

        public RequestCallback(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onActivityResult(Map<String,Boolean> result) {

            boolean accessFineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == Boolean.TRUE;
            boolean accessCoarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) == Boolean.TRUE;

            if (accessFineLocationGranted || accessCoarseLocationGranted) {

                try {
                    locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                    state = RequestState.GRANTED;
                    if (onRequestResult != null) {
                        onRequestResult.onResult(true);
                    }
                    requestLocationUpdatesFromLocationManager();
                } catch (SecurityException e) {
                    Debug.error("LocationRequester SecurityException.");
                }

            } else {
                state = RequestState.DENIED;
                if (onRequestResult != null) {
                    onRequestResult.onResult(false);
                }
                Debug.log("Failed to get permission ACCESS_FINE_LOCATION.");
            }

        }

    }

    public final RequestState getPermissionState() {
        return state;
    }

    public final void setOnPermissionResult(OnRequestResult onRequestResult) {
        this.onRequestResult = onRequestResult;
    }

    public interface OnRequestResult {
        void onResult(boolean granted);
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

    public final void setOnLocationChanged(OnLocationChanged onLocationChanged) {
        this.onLocationChanged = onLocationChanged;
    }

    public interface OnLocationChanged {
        void onLocationChanged(double latitude, double longitude);
    }

    public enum EnabledState {
        DISABLED,
        ENABLED,
        UNKNOWN
    }

    public final EnabledState getEnabledState() {
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    ? EnabledState.ENABLED : EnabledState.DISABLED;
        } else {
            return EnabledState.UNKNOWN;
        }
    }

}
