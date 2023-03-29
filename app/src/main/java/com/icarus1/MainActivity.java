package com.icarus1;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentResultListener;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.icarus1.calendar.CalendarFragment;
import com.icarus1.clock.ClockFragment;
import com.icarus1.compass.CelestialBody;
import com.icarus1.compass.CompassFragment;
import com.icarus1.databinding.ActivityMainBinding;
import com.icarus1.map.MapFragment;
import com.icarus1.selectbodies.SelectBodiesFragment;
import com.icarus1.util.Debug;
import com.icarus1.util.Format;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean restoredState;
    private final OnObjectSelection onObjectSelection = new OnObjectSelection();
    private final OnChangeLocationListener onChangeLocationListener = new OnChangeLocationListener();
    private final OnChangeDateListener onChangeDateListener = new OnChangeDateListener();
    private final OnChangeTimeListener onChangeTimeListener = new OnChangeTimeListener();
    private final OnChangeViewListener ON_CHANGE_VIEW_LISTENER = new OnChangeViewListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restoredState = savedInstanceState != null;

        initContentViewAndBinding();
        initToolbar();
        initUI();

        // Request permissions
        ActivityResultLauncher<String[]> request = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String,Boolean>>() {
            @Override
            public void onActivityResult(Map<String,Boolean> result) {

                if (result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) == Boolean.TRUE) {

                    //TODO log

                    try {

                        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                double longitude = location.getLongitude();
                                double latitude = location.getLatitude();
                                MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_container);
                                if (mapFragment == null) {
                                    //TODO log
                                    return;
                                }
                                mapFragment.setUserLocation(longitude, latitude);
                            }
                        });

                        Location location = getLastKnownLocation(); // lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_container);
                            if (mapFragment == null) {
                                //TODO log
                                return;
                            }
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

    }

    @Override
    protected void onStart() {
        super.onStart();

        SelectBodiesFragment selectBodiesFragment;
        try {
            selectBodiesFragment = getSelectBodiesFragment();
        } catch (FragmentNotFoundException e) {
            Debug.error(e);
            return;
        }

        if (!restoredState) {
            selectBodiesFragment.setViewable(CelestialBody.SUN, true);
            selectBodiesFragment.setViewable(CelestialBody.MOON, true);

            for (CelestialBody body : CelestialBody.planets()) {
                selectBodiesFragment.setViewable(body, false);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportFragmentManager()
            .setFragmentResultListener("A", this, onChangeDateListener);
        getSupportFragmentManager()
            .setFragmentResultListener("B", this, onChangeLocationListener);
        getSupportFragmentManager()
            .setFragmentResultListener("C", this, onChangeTimeListener);
        for (CelestialBody body : CelestialBody.values()) {
            getSupportFragmentManager()
                .setFragmentResultListener("E_"+body.getName(), this, ON_CHANGE_VIEW_LISTENER);
        }

    }

    private void initContentViewAndBinding() {

        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

    }

    private void initToolbar() {

        binding.toolbar.setOnMenuItemClickListener(new OnMenuClick());
        binding.toolbar.inflateMenu(R.menu.main);

        MenuItem item = binding.toolbar.getMenu().findItem(R.id.menu_dark_mode);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            item.setIcon(R.drawable.light_mode);
            item.setTitle(R.string.light_mode);
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            item.setIcon(R.drawable.dark_mode);
            item.setTitle(R.string.dark_mode);
        } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_UNDEFINED) {
            item.setIcon(R.drawable.dark_mode);
            item.setTitle(R.string.dark_mode);
        } else {
            Debug.log("Unrecognised nightModeFlags.");
        }

    }

    private void initUI() {

        binding.viewBodies.setOnClickListener(view -> binding.selectBodiesView.show());

        binding.changeLocation.setOnClickListener(view -> binding.mapCardView.show());
        binding.changeDate.setOnClickListener(view -> binding.calendarCardView.show());
        binding.changeTime.setOnClickListener(view -> binding.clockCardView.show());

        binding.toggleObjectsSunMoon.setOnClickListener(onObjectSelection);
        binding.toggleObjectsPlanets.setOnClickListener(onObjectSelection);

    }

    private CompassFragment getCompassFragment()
    throws FragmentNotFoundException {

        CompassFragment fragment = (CompassFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_compass);

        if (fragment == null) {
            throw new FragmentNotFoundException();
        }

        return fragment;

    }

    private SelectBodiesFragment getSelectBodiesFragment()
    throws FragmentNotFoundException {

        SelectBodiesFragment fragment = (SelectBodiesFragment) getSupportFragmentManager().findFragmentById(R.id.select_bodies_fragment);

        if (fragment == null) {
            throw new FragmentNotFoundException();
        }

        return fragment;

    }

    private void toggleNightMode() {

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

    }

    private void setLocation(double longitude, double latitude, @Nullable String location) {

        CompassFragment compassFragment;
        try {
            compassFragment = getCompassFragment();
        } catch (FragmentNotFoundException e) {
            Debug.error(e);
            return;
        }

        binding.locationText.setText(Format.LatitudeLongitude(latitude, longitude));

        if (location != null) {
            binding.locationAddress.setText(location);
        } else {
            binding.locationAddress.setText(R.string.tap_to_change_location);
        }

        compassFragment.setLocation(longitude, latitude);

    }

    private class OnChangeLocationListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            double longitude = result.getDouble("Longitude");
            double latitude = result.getDouble("Latitude");
            String location = result.getString("Location");

            setLocation(longitude, latitude, location);

        }
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

    private void setDate(int year, int month, int day, boolean currentDate) {

        CompassFragment compassFragment;
        try {
            compassFragment = getCompassFragment();
        } catch (FragmentNotFoundException e) {
            Debug.error(e);
            return;
        }

        binding.dateText.setText(Format.Date(year, month, day));
        if (currentDate) {
            binding.dateSubscript.setText(R.string.using_system_date);
        } else {
            binding.dateSubscript.setText(R.string.tap_to_change_date);
        }

        compassFragment.setDate(year, month, day);

    }

    private class OnChangeDateListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int year = result.getInt("Y");
            int month = result.getInt("M");
            int day = result.getInt("D");
            boolean currentDate = result.getBoolean("CURRENT DATE");

            setDate(year, month, day, currentDate);

        }
    }

    private void setTime(int hour, int minute, int seconds, int offset, @Nullable String location) {

        CompassFragment compassFragment;
        try {
            compassFragment = getCompassFragment();
        } catch (FragmentNotFoundException e) {
            Debug.error(e);
            return;
        }

        String text = Format.Time(hour, minute, seconds);
        text += " (" + Format.UTCOffset(offset, 0) + ")";

        binding.timeText.setText(text);
        if (location != null) {
            binding.timeLocation.setText(location);
        } else {
            binding.timeLocation.setText(R.string.tap_to_change_time);
        }

        compassFragment.setTime(hour - offset, minute, seconds);

    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int hour = result.getInt("HOUR");
            int minute = result.getInt("MINUTE");
            int seconds = result.getInt("SECOND");
            int timeZoneID = result.getInt("OFFSET");
            String location = result.getString("LOCATION");

            setTime(hour, minute, seconds, timeZoneID, location);

        }
    }

    private void setViewable(CelestialBody body, boolean viewable) {

        CompassFragment compassFragment;
        SelectBodiesFragment selectBodiesFragment;
        try {
            compassFragment = getCompassFragment();
            selectBodiesFragment = getSelectBodiesFragment();
        } catch (FragmentNotFoundException e) {
            Debug.error(e);
            return;
        }

        int viewID;
        CelestialBody[] objects;
        if (body == CelestialBody.SUN || body == CelestialBody.MOON) {
            viewID = R.id.toggle_objects_sun_moon;
            objects = CelestialBody.nonPlanets();
        } else {
            viewID = R.id.toggle_objects_planets;
            objects = CelestialBody.planets();
        }

        if (!viewable) {
            binding.toggleObjectsGroup.uncheck(viewID);
        } else {
            boolean allViewable = true;
            for (CelestialBody body2 : objects) {
                allViewable &= selectBodiesFragment.getViewable(body2);
            }
            if (allViewable) {
                binding.toggleObjectsGroup.check(viewID);
            }
        }

        compassFragment.setDrawBody(body, viewable);

    }

    private class OnChangeViewListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int index = result.getInt("INDEX");
            boolean checked = result.getBoolean("CHECKED");

            setViewable(CelestialBody.values()[index], checked);

        }
    }

    private class OnObjectSelection implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            SelectBodiesFragment selectBodiesFragment;
            try {
                selectBodiesFragment = getSelectBodiesFragment();
            } catch (FragmentNotFoundException e) {
                Debug.error(e);
                return;
            }

            int viewID = v.getId();

            if (viewID == R.id.toggle_objects_sun_moon) {

                boolean checked = binding.toggleObjectsGroup.getCheckedButtonIds().contains(R.id.toggle_objects_sun_moon);

                selectBodiesFragment.setViewable(CelestialBody.SUN, checked);
                selectBodiesFragment.setViewable(CelestialBody.MOON, checked);

            } else if (viewID == R.id.toggle_objects_planets) {

                boolean checked = binding.toggleObjectsGroup.getCheckedButtonIds().contains(R.id.toggle_objects_planets);

                for (CelestialBody body : CelestialBody.planets()) {
                    selectBodiesFragment.setViewable(body, checked);
                }

            }

        }
    }

    private class OnMenuClick implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (item.getItemId() == R.id.menu_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            if (item.getItemId() == R.id.menu_about) {
                //TODO switch to about fragment in MainActivity - requires rewrite of MainActivity
                return true;
            }

            if (item.getItemId() == R.id.menu_dark_mode) {
                toggleNightMode();
                return true;
            }

            return false;
        }
    }

    private static class FragmentNotFoundException extends Debug.Exception {
        private FragmentNotFoundException() {
            super("Fragment Not Found.");
        }
    }

}