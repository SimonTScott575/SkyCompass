package com.icarus1;

import static android.content.Context.LOCATION_SERVICE;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.compass.CelestialBody;
import com.icarus1.compass.CompassFragment;
import com.icarus1.compass.CompassSensor;
import com.icarus1.databinding.FragmentMainBinding;
import com.icarus1.map.MapFragment;
import com.icarus1.selectbodies.SelectBodiesFragment;
import com.icarus1.util.Debug;
import com.icarus1.util.Format;

import java.util.List;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private boolean restoredState;
    private boolean started;

    private final OnObjectSelection onObjectSelection = new OnObjectSelection();

    private CompassSensor sensor;

    private final OnChangeViewListener onChangeViewListener = new OnChangeViewListener();
    private final OnChangeLocationListener onChangeLocationListener = new OnChangeLocationListener();
    private final OnChangeDateListener onChangeDateListener = new OnChangeDateListener();
    private final OnChangeTimeListener onChangeTimeListener = new OnChangeTimeListener();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationRequester locationRequester = new LocationRequester(new LocationRequester.OnLocationChanged() {
            @Override
            public void onLocationChanged(double latitude, double longitude) {
                try {
                    MapFragment mapFragment = getMapFragment();
                    mapFragment.setUserLocation(longitude, latitude);
                } catch (Debug.Exception e) {
                    Debug.log(e);
                }
            }
        });
        locationRequester.request(requireActivity());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        restoredState = savedInstanceState != null;

        initUI();

    }

    @Override
    public void onStart() {
        super.onStart();

        SelectBodiesFragment selectBodiesFragment;
        try {

            selectBodiesFragment = getSelectBodiesFragment();

            if (!restoredState && !started) {
                selectBodiesFragment.setViewable(CelestialBody.SUN, true);
                selectBodiesFragment.setViewable(CelestialBody.MOON, true);

                for (CelestialBody body : CelestialBody.planets()) {
                    selectBodiesFragment.setViewable(body, false);
                }
            }

        } catch (Debug.Exception e) {
            Debug.error(e);
        }

        started = true;

    }

    @Override
    public void onResume() {
        super.onResume();

        getChildFragmentManager()
            .setFragmentResultListener("A", this, onChangeDateListener);
        getChildFragmentManager()
            .setFragmentResultListener("B", this, onChangeLocationListener);
        getChildFragmentManager()
            .setFragmentResultListener("C", this, onChangeTimeListener);
        for (CelestialBody body : CelestialBody.values()) {
            getChildFragmentManager()
                .setFragmentResultListener("E_"+body.getName(), this, onChangeViewListener);
        }

        sensor = new CompassSensor(new CompassSensor.OnOrientationChanged() {
            @Override
            public void onOrientationChanged(float[] orientation) {
                try {
                    CompassFragment compassFragment = getCompassFragment();
                    compassFragment.setNorthRotation(orientation[0]);
                } catch (Debug.Exception e) {
                    Debug.log(e);
                }
            }
        });
        sensor.request(requireContext());

    }

    @Override
    public void onPause() {

        sensor.destroy();

        super.onPause();
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
    throws FragmentNotFoundException, NoChildFragmentManagerAttachedException {

        CompassFragment fragment = (CompassFragment) getChildFragmentManagerOrThrowException()
            .findFragmentById(R.id.fragment_compass);

        if (fragment == null) {
            throw new FragmentNotFoundException();
        }

        return fragment;

    }

    private MapFragment getMapFragment()
    throws FragmentNotFoundException, NoChildFragmentManagerAttachedException {

        MapFragment fragment = (MapFragment) getChildFragmentManagerOrThrowException()
            .findFragmentById(R.id.map_fragment_container);

        if (fragment == null) {
            throw new FragmentNotFoundException();
        }

        return fragment;

    }

    private SelectBodiesFragment getSelectBodiesFragment()
    throws FragmentNotFoundException, NoChildFragmentManagerAttachedException {

        SelectBodiesFragment fragment = (SelectBodiesFragment) getChildFragmentManagerOrThrowException()
            .findFragmentById(R.id.select_bodies_fragment);

        if (fragment == null) {
            throw new FragmentNotFoundException();
        }

        return fragment;

    }

    private void setViewable(CelestialBody body, boolean viewable) {

        CompassFragment compassFragment;
        SelectBodiesFragment selectBodiesFragment;
        try {
            compassFragment = getCompassFragment();
            selectBodiesFragment = getSelectBodiesFragment();
        } catch (Debug.Exception e) {
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

    private void setLocation(double longitude, double latitude, @Nullable String location) {

        CompassFragment compassFragment;
        try {
            compassFragment = getCompassFragment();
        } catch (Debug.Exception e) {
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

        LocationManager mLocationManager = (LocationManager)requireContext().getSystemService(LOCATION_SERVICE);

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
        } catch (Debug.Exception e) {
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
        } catch (Debug.Exception e) {
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

    private class OnObjectSelection implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            SelectBodiesFragment selectBodiesFragment;
            try {
                selectBodiesFragment = getSelectBodiesFragment();
            } catch (Debug.Exception e) {
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

    private FragmentManager getChildFragmentManagerOrThrowException()
    throws NoChildFragmentManagerAttachedException {

        try {
            return getChildFragmentManager();
        } catch (IllegalStateException e) {
            throw new NoChildFragmentManagerAttachedException();
        }

    }

    private static class FragmentNotFoundException extends Debug.Exception {
        private FragmentNotFoundException() {
            super("Fragment Not Found.");
        }
    }
    private static class NoChildFragmentManagerAttachedException extends Debug.Exception {
        private NoChildFragmentManagerAttachedException() {
            super("No child fragment manager.");
        }
    }


}