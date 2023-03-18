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

import com.icarus1.R;
import com.icarus1.databinding.FragmentMapBinding;
import com.icarus1.util.Format;

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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        Context ctx = requireActivity();
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
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        binding.mapView.onPause();
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
                requireActivity(),
                permissionsToRequest.toArray(new String[0]),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }

    }

    private void setUpMap() {

        binding.mapView.getSetLocationMarker().setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

                double longitude = marker.getPosition().getLongitude();
                double latitude = marker.getPosition().getLatitude();

                setLocation(longitude, latitude, null);

                if (userLocation != null) {
                    binding.useLocation.setVisibility(View.VISIBLE);
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
            setLocation(userLocation.getLongitude(), userLocation.getLatitude(), getResources().getString(R.string.current_location));
        } else {
            setLocation(0d, 0d, null);
        }

        binding.useLocation.setOnClickListener(new OnClickSetToLocation());

    }

    private void requestPermissionsIfNecessary(String[] permissions) {

        ArrayList<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {

            int permissionStatus = ContextCompat.checkSelfPermission(requireActivity(), permission);

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }

        }

        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toArray(new String[0]),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }

    }

    public void setLocation(double longitude, double latitude, String location) {

        binding.mapView.getSetLocationMarker().setPosition(new GeoPoint(latitude, longitude));
        binding.mapView.invalidate();
        binding.latLong.setText(Format.LatitudeLongitude(latitude, longitude));

        onLocationChanged(longitude, latitude, location);

    }

    public void setUserLocation(double longitude, double latitude) {

        userLocation = new GeoPoint(latitude, longitude);
        binding.useLocation.setVisibility(View.VISIBLE);

    }

    public void onLocationChanged(double longitude, double latitude, String location) {

        Bundle bundle = new Bundle();
        bundle.putDouble("Longitude", longitude);
        bundle.putDouble("Latitude", latitude);
        bundle.putString("Location", location);
        requireActivity().getSupportFragmentManager().setFragmentResult("B", bundle);

    }

    private class OnClickSetToLocation implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            binding.mapView.getSetLocationMarker().setPosition(userLocation);
            binding.mapView.invalidate();

            v.setVisibility(View.INVISIBLE);

            double longitude = userLocation.getLongitude();
            double latitude = userLocation.getLatitude();

            setLocation(longitude, latitude, getResources().getString(R.string.current_location));

        }
    }

}