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
    private final OnClickLeft onClickLeft = new OnClickLeft();
    private final OnClickRight onClickRight = new OnClickRight();

    private Bundle location;
    private Bundle date;
    private Bundle time;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentMainBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.changeLocation.setOnClickListener(view1 -> binding.mapCardView.show());
        binding.changeDate.setOnClickListener(view1 -> binding.calendarCardView.show());
        binding.changeTime.setOnClickListener(view1 -> binding.clockCardView.show());

        binding.right.setOnClickListener(onClickRight);
        binding.left.setOnClickListener(onClickLeft);

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

    private void setLocation(double longitude, double latitude, @Nullable String location) {

        binding.locationText.setText(Format.LatitudeLongitude(latitude, longitude));

        if (location != null) {
            binding.locationAddress.setText(location);
        } else {
            binding.locationAddress.setText(R.string.tap_to_change_location);
        }

        try {
            CompassFragment fragment = getCompassFragment();
            fragment.setLocation(longitude, latitude);
        } catch (Debug.Exception e) {
        }
        try {
            InfoFragment fragment = getInfoFragment();
            fragment.setLocation(longitude, latitude);
        } catch (Debug.Exception e) {
        }

    }

    private class OnChangeLocationListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            location = result;
            double longitude = result.getDouble("Longitude");
            double latitude = result.getDouble("Latitude");
            String location = result.getString("Location");

            setLocation(longitude, latitude, location);

        }
    }

    private void setDate(int year, int month, int day, boolean currentDate) {

        binding.dateText.setText(Format.Date(year, month, day));
        if (currentDate) {
            binding.dateSubscript.setText(R.string.using_system_date);
        } else {
            binding.dateSubscript.setText(R.string.tap_to_change_date);
        }


        try {
            CompassFragment compassFragment = getCompassFragment();
            compassFragment.setDate(year, month, day);
        } catch (Debug.Exception e) {
        }
        try {
            InfoFragment fragment = getInfoFragment();
            fragment.setDate(year, month, day);
        } catch (Debug.Exception e) {
        }
        try {
            ClockFragment clockFragment = getClockFragment();
            clockFragment.setDate(year, month, day, currentDate);
        } catch (Debug.Exception e) {
            Debug.error(e);
            return;
        }

    }

    private class OnChangeDateListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            date = result;
            int year = result.getInt("Y");
            int month = result.getInt("M");
            int day = result.getInt("D");
            boolean currentDate = result.getBoolean("CURRENT DATE");

            setDate(year, month, day, currentDate);

        }
    }

    private void setTime(Time time, int offset, String location) {

        String text = Format.Time(time.getHour(), time.getMinute(), time.getSecond());
        text += " (" + Format.UTCOffset(offset) + ")";

        binding.timeText.setText(text);
        if (location != null) {
            binding.timeLocation.setText(location);
        } else {
            binding.timeLocation.setText(R.string.tap_to_change_time);
        }

        TimeZone timeZone = new TimeZone(offset);

        try {
            CompassFragment compassFragment = getCompassFragment();
            compassFragment.setTime(
                time.getHour() - timeZone.getRawHourOffset(),
                time.getMinute() - timeZone.getRawMinuteOffset(),
                time.getSecond() - timeZone.getRawSecondOffset() - timeZone.getRawMillisecondOffset()/1000f
            );
        } catch (Debug.Exception e) {
        }
        try {
            InfoFragment fragment = getInfoFragment();
            fragment.setTime(
                time.getHour() - timeZone.getRawHourOffset(),
                time.getMinute() - timeZone.getRawMinuteOffset(),
                time.getSecond() - timeZone.getRawSecondOffset() - timeZone.getRawMillisecondOffset()/1000f,
                offset
            );
        } catch (Debug.Exception e) {
        }

    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            time = result;
            int hour = result.getInt("HOUR");
            int minute = result.getInt("MINUTE");
            int seconds = result.getInt("SECOND");
            int UTCOffset = result.getInt("OFFSET");
            String location = result.getString("LOCATION");

            setTime(new Time(hour, minute, seconds), UTCOffset, location);

        }
    }

    private class OnClickLeft implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();
            bundle.putAll(location);
            bundle.putAll(date);
            bundle.putAll(time);

            getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_compass, CompassFragment.class, bundle)
                .commit();

            binding.textView.setText(R.string.compass);

        }

    }
    private class OnClickRight implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();
            bundle.putAll(location);
            bundle.putAll(date);
            bundle.putAll(time);

            getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_compass, InfoFragment.class, bundle)
                .commit();

            binding.textView.setText(R.string.info);

        }

    }

    private CompassFragment getCompassFragment()
            throws FragmentNotFound, NoChildFragmentManagerAttached {

        Fragment fragment2 = getChildFragmentManagerOrThrowException()
                .findFragmentById(R.id.fragment_compass);

        CompassFragment fragment;
        if (fragment2 instanceof CompassFragment) {
            fragment = (CompassFragment) fragment2;
        } else {
            throw new FragmentNotFound();
        }

        return fragment;

    }

    private InfoFragment getInfoFragment()
            throws FragmentNotFound, NoChildFragmentManagerAttached {

        Fragment fragment2 = getChildFragmentManagerOrThrowException()
                .findFragmentById(R.id.fragment_compass);

        InfoFragment fragment;
        if (fragment2 instanceof InfoFragment) {
            fragment = (InfoFragment) fragment2;
        } else {
            throw new FragmentNotFound();
        }

        return fragment;

    }

    private ClockFragment getClockFragment()
            throws FragmentNotFound, NoChildFragmentManagerAttached {

        ClockFragment fragment = (ClockFragment) getChildFragmentManagerOrThrowException()
                .findFragmentById(R.id.clock_fragment_container);

        if (fragment == null) {
            throw new FragmentNotFound();
        }

        return fragment;

    }

    private FragmentManager getChildFragmentManagerOrThrowException()
            throws NoChildFragmentManagerAttached {

        try {
            return getChildFragmentManager();
        } catch (IllegalStateException e) {
            throw new NoChildFragmentManagerAttached();
        }

    }


    private static class FragmentNotFound extends Debug.Exception {
        private FragmentNotFound() {
            super("Fragment Not Found.");
        }
    }

    private static class NoChildFragmentManagerAttached extends Debug.Exception {
        private NoChildFragmentManagerAttached() {
            super("No child fragment manager.");
        }
    }

}