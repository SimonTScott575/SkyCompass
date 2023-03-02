package com.icarus1;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentResultListener;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.icarus1.compass.CompassFragment;
import com.icarus1.databinding.ActivityMainBinding;
import com.icarus1.map.MapFragment;
import com.icarus1.views.Panel;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String abc = "ABC";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initContentViewAndBinding();
        initToolbar();

        // Request permissions
        ActivityResultLauncher<String[]> request = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String,Boolean>>() {
            @Override
            public void onActivityResult(Map<String,Boolean> result) {

                if (result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == Boolean.TRUE) {

                    //TODO log

                    try {

                        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                double longitude = location.getLongitude();
                                double latitude = location.getLatitude();
                                MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                                mapFragment.setUserLocation(longitude, latitude);
                            }
                        });

                        Location location = getLastKnownLocation(); // lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                            mapFragment.setUserLocation(longitude, latitude);
                        }

                    } catch (SecurityException e) {
                        throw e;
                    }

                } else {
                    //TODO log
                }

            }
        });
        request.launch(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});

        findViewById(R.id.change_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Panel a = (Panel) MainActivity.this.findViewById(R.id.map_card_view);
                a.show();
            }
        });

        findViewById(R.id.change_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Panel a = (Panel) MainActivity.this.findViewById(R.id.calendar_card_view);
                a.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportFragmentManager()
            .setFragmentResultListener("A", this, new OnChangeTimeListener());
        getSupportFragmentManager()
            .setFragmentResultListener("B", this, new OnChangeLocationListener());

    }

    private void initContentViewAndBinding() {

        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        binding.setTestText("XYZ");
        binding.executePendingBindings();
        setContentView(binding.getRoot());

    }

    private void initToolbar() {

        binding.toolbar.setTitle(R.string.app_name);
        binding.toolbar.inflateMenu(R.menu.main);

        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.menu_settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });


    }

    private Location getLastKnownLocation() throws SecurityException {

        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);

        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {

            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }

        }

        return bestLocation;

    }

    private void setLocation(double longitude, double latitude) {

        String text = String.format("%.2f", Math.abs(latitude)) + "\u00B0" + (latitude < 0 ? "S" : "N");
        text += " ";
        text += String.format("%.2f", Math.abs(longitude)) + "\u00B0" + (longitude < 0 ? "W" : "E");

        TextView locationText = (TextView) findViewById(R.id.location_text);
        if (locationText == null) {
            return;
        }

        locationText.setText(text);

        CompassFragment compassFragment = (CompassFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_compass);
        compassFragment.setLocation(longitude, latitude);

    }

    private void setDate(int year, int month, int day) {

        TextView dateText = (TextView) findViewById(R.id.time_text);
        if (dateText == null) {
            return;
        }

        dateText.setText(year + "-" + (month+1) + "-" + (day+1));

        CompassFragment compassFragment = (CompassFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_compass);
        compassFragment.setDate(year, month, day);

    }

    private class OnChangeLocationListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            double longitude = result.getDouble("Longitude");
            double latitude = result.getDouble("Latitude");

            setLocation(longitude, latitude);

        }
    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int year = result.getInt("Y");
            int month = result.getInt("M");
            int day = result.getInt("D");

            setDate(year, month, day);

        }
    }

}