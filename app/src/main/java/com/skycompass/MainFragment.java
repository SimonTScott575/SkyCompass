package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.databinding.FragmentMainBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;
import com.skycompass.util.Time;
import com.skycompass.util.TimeZone;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

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

    }

    private void initUI() {

        binding.changeLocation.setOnClickListener(view -> binding.mapCardView.show());
        binding.changeDate.setOnClickListener(view -> binding.calendarCardView.show());
        binding.changeTime.setOnClickListener(view -> binding.clockCardView.show());

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

    private ClockFragment getClockFragment()
    throws FragmentNotFoundException, NoChildFragmentManagerAttachedException {

        ClockFragment fragment = (ClockFragment) getChildFragmentManagerOrThrowException()
            .findFragmentById(R.id.clock_fragment_container);

        if (fragment == null) {
            throw new FragmentNotFoundException();
        }

        return fragment;

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
        ClockFragment clockFragment;
        try {
            compassFragment = getCompassFragment();
            clockFragment = getClockFragment();
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
        clockFragment.setDate(year, month, day);

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

    private void setTime(Time time, int offset, String location) {

        CompassFragment compassFragment;
        ClockFragment clockFragment;
        try {
            compassFragment = getCompassFragment();
            clockFragment = getClockFragment();
        } catch (Debug.Exception e) {
            Debug.error(e);
            return;
        }

        String text = Format.Time(time.getHour(), time.getMinute(), time.getSecond());
        text += " (" + Format.UTCOffset(offset) + ")";

        binding.timeText.setText(text);
        if (location != null) {
            binding.timeLocation.setText(location);
        } else {
            binding.timeLocation.setText(R.string.tap_to_change_time);
        }

        TimeZone timeZone = new TimeZone(offset);
        compassFragment.setTime(
            time.getHour() - timeZone.getRawHourOffset(),
            time.getMinute() - timeZone.getRawMinuteOffset(),
            time.getSecond() - timeZone.getRawSecondOffset() - timeZone.getRawMillisecondOffset()/1000f
        );

        clockFragment.setTime(time.getHour(), time.getMinute(), time.getSecond());

    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            int hour = result.getInt("HOUR");
            int minute = result.getInt("MINUTE");
            int seconds = result.getInt("SECOND");
            int UTCOffset = result.getInt("OFFSET");
            String location = result.getString("LOCATION");

            setTime(new Time(hour, minute, seconds), UTCOffset, location);

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