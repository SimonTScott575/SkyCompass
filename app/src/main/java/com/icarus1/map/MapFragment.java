package com.icarus1.map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.databinding.FragmentMapBinding;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MapFragment extends Fragment {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private FragmentMapBinding binding;
    private GeoPoint userLocation;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        binding = FragmentMapBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestPermissionsIfNecessary(new String[]{
            // if you need to show the current location, uncomment the line below
            Manifest.permission.ACCESS_FINE_LOCATION,
            // WRITE_EXTERNAL_STORAGE is required in order to show the map
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        Context ctx = getActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setUpMap();

    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        binding.map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); //TODO call super here or at end ?

        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                this.getActivity(),
                permissionsToRequest.toArray(new String[0]),
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }

    }

    private void setUpMap() {

        binding.map.getSetLocationMarker().setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

                double longitude = marker.getPosition().getLongitude();
                double latitude = marker.getPosition().getLatitude();

                setLocation(longitude, latitude);

                if (userLocation != null) {
                    binding.mapUseLocation.setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
            @Override
            public void onMarkerDragStart(Marker marker) {
            }
        });

        if (userLocation != null) {
            setLocation(userLocation.getLongitude(), userLocation.getLatitude());
        } else {
            setLocation(0d, 0d);
        }

        binding.mapUseLocation.setOnClickListener(new OnClickSetToLocation());

    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this.getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this.getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void setLocation(double longitude, double latitude) {

        String text = String.format("%.2f", Math.abs(latitude)) + "\u00B0" + (latitude < 0 ? "S" : "N");
        text += " ";
        text += String.format("%.2f", Math.abs(longitude)) + "\u00B0" + (longitude < 0 ? "W" : "E");


        binding.map.getSetLocationMarker().setPosition(new GeoPoint(latitude, longitude));
        binding.map.invalidate();
        binding.latLong.setText(text);

        onLocationChanged(longitude, latitude);

    }

    public void setUserLocation(double longitude, double latitude) {

        userLocation = new GeoPoint(latitude, longitude);
        binding.mapUseLocation.setVisibility(View.VISIBLE);

    }

    public void onLocationChanged(double longitude, double latitude) {

        Bundle bundle = new Bundle();
        bundle.putDouble("Longitude", longitude);
        bundle.putDouble("Latitude", latitude);
        requireActivity().getSupportFragmentManager().setFragmentResult("B", bundle);

    }

    private class OnClickSetToLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            binding.map.getSetLocationMarker().setPosition(userLocation);
            binding.map.invalidate();

            v.setVisibility(View.INVISIBLE);

            double longitude = userLocation.getLongitude();
            double latitude = userLocation.getLatitude();

            setLocation(longitude, latitude);

        }
    }

}