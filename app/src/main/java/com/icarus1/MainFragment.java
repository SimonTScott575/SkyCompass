package com.icarus1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icarus1.compass.CelestialObject;
import com.icarus1.compass.CompassFragment;
import com.icarus1.databinding.FragmentMainBinding;
import com.icarus1.selectbodies.SelectBodiesFragment;
import com.icarus1.util.Debug;
import com.icarus1.util.Format;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private final OnObjectSelection onObjectSelection = new OnObjectSelection();
    private final OnChangeViewListener onChangeViewListener = new OnChangeViewListener();
    private final OnChangeLocationListener onChangeLocationListener = new OnChangeLocationListener();
    private final OnChangeDateListener onChangeDateListener = new OnChangeDateListener();
    private final OnChangeTimeListener onChangeTimeListener = new OnChangeTimeListener();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initUI();

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
        for (CelestialObject body : CelestialObject.values()) {
            getChildFragmentManager()
                .setFragmentResultListener("E_"+body.getName(), this, onChangeViewListener);
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

    private FragmentManager getChildFragmentManagerOrThrowException()
            throws NoChildFragmentManagerAttachedException {

        try {
            return getChildFragmentManager();
        } catch (IllegalStateException e) {
            throw new NoChildFragmentManagerAttachedException();
        }

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

    private class OnChangeViewListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int index = result.getInt("INDEX");
            boolean checked = result.getBoolean("CHECKED");

            CelestialObject object = CelestialObject.values()[index];

            setViewableObjectGroupChecked(object, checked);

            CompassFragment compassFragment;
            try {
                compassFragment = getCompassFragment();
                compassFragment.setDrawBody(object, checked);
            } catch (Debug.Exception e) {
                Debug.error(e);
            }

        }
    }

    private void setViewableObjectGroupChecked(CelestialObject body, boolean viewable) {

        SelectBodiesFragment selectBodiesFragment;
        try {
            selectBodiesFragment = getSelectBodiesFragment();
        } catch (Debug.Exception e) {
            Debug.error(e);
            return;
        }

        int viewID;
        CelestialObject[] objects;
        if (body == CelestialObject.SUN || body == CelestialObject.MOON) {
            viewID = R.id.toggle_objects_sun_moon;
            objects = new CelestialObject[]{CelestialObject.SUN, CelestialObject.MOON};
        } else {
            viewID = R.id.toggle_objects_planets;
            objects = CelestialObject.planets();
        }

        if (!viewable) {
            binding.toggleObjectsGroup.uncheck(viewID);
        } else {
            boolean allViewable = true;
            for (CelestialObject body2 : objects) {
                allViewable &= selectBodiesFragment.getViewable(body2);
            }
            if (allViewable) {
                binding.toggleObjectsGroup.check(viewID);
            }
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

                selectBodiesFragment.setViewable(CelestialObject.SUN, checked);
                selectBodiesFragment.setViewable(CelestialObject.MOON, checked);

            } else if (viewID == R.id.toggle_objects_planets) {

                boolean checked = binding.toggleObjectsGroup.getCheckedButtonIds().contains(R.id.toggle_objects_planets);

                for (CelestialObject body : CelestialObject.planets()) {
                    selectBodiesFragment.setViewable(body, checked);
                }

            }

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

        int hourOffset = offset/3600000;
        int minuteOffset = (offset - hourOffset*3600000)/60000;
        float secondOffset = (offset - hourOffset*3600000 - minuteOffset*60000)/1000f;

        String text = Format.Time(hour, minute, seconds);
        text += " (" + Format.UTCOffset(hourOffset, minuteOffset) + ")";

        binding.timeText.setText(text);
        if (location != null) {
            binding.timeLocation.setText(location);
        } else {
            binding.timeLocation.setText(R.string.tap_to_change_time);
        }

        compassFragment.setTime(hour - hourOffset, minute - minuteOffset, seconds - secondOffset);

    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int hour = result.getInt("HOUR");
            int minute = result.getInt("MINUTE");
            int seconds = result.getInt("SECOND");
            int UTCOffset = result.getInt("OFFSET");
            String location = result.getString("LOCATION");

            setTime(hour, minute, seconds, UTCOffset, location);

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