package com.skycompass;

import android.content.res.TypedArray;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private final OnChangeLocationListener onChangeLocationListener = new OnChangeLocationListener();
    private final OnChangeDateListener onChangeDateListener = new OnChangeDateListener();
    private final OnChangeTimeListener onChangeTimeListener = new OnChangeTimeListener();
    private final OnClickLeft onClickLeft = new OnClickLeft();
    private final OnClickRight onClickRight = new OnClickRight();

    private Bundle locationBundle;
    private Bundle dateBundle;
    private Bundle timeBundle;

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

        binding.changeLocation.setOnClickListener(v -> binding.mapCardView.show());
        binding.changeDate.setOnClickListener(v -> binding.calendarCardView.show());
        binding.changeTime.setOnClickListener(v -> binding.clockCardView.show());

        binding.right.setOnClickListener(onClickRight);
        binding.left.setOnClickListener(onClickLeft);

        try {
            getCompassFragment();
            setFragmentCompass();
        } catch (Debug.Exception e) {
        }
        try {
            getInfoFragment();
            setFragmentInfo();
        } catch (Debug.Exception e) {
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        getChildFragmentManager()
            .setFragmentResultListener("A", this, onChangeDateListener);
        getChildFragmentManager()
            .setFragmentResultListener("MapFragment/LocationChanged", this, onChangeLocationListener);
        getChildFragmentManager()
            .setFragmentResultListener("C", this, onChangeTimeListener);

    }

    private void setLocation(double latitude, double longitude, @Nullable String location) {

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

            locationBundle = result;

            setLocation(result.getDouble("Latitude"), result.getDouble("Longitude"), result.getString("Location"));

        }
    }

    private void setDate(LocalDate date, boolean isCurrentDate) {

        binding.dateText.setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (isCurrentDate) {
            binding.dateSubscript.setText(R.string.using_system_date);
        } else {
            binding.dateSubscript.setText(R.string.tap_to_change_date);
        }

        try {
            CompassFragment compassFragment = getCompassFragment();
            compassFragment.setDate(date);
        } catch (Debug.Exception e) {
        }
        try {
            InfoFragment fragment = getInfoFragment();
            fragment.setDate(date);
        } catch (Debug.Exception e) {
        }
        try {
            ClockFragment clockFragment = getClockFragment();
            clockFragment.setDate(date, isCurrentDate);
        } catch (Debug.Exception e) {
            Debug.error(e);
        }

    }

    private class OnChangeDateListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            dateBundle = result;
            Comms.Date date2 = Comms.Date.from(result);

            setDate(date2.getDate(), date2.isCurrentDate());

        }
    }

    private void setTime(LocalTime time, int offset, String location) {

        String text = Format.Time(time.getHour(), time.getMinute(), time.getSecond());
        text += " (UTC" + Format.TimeZoneOffset(offset) + ")";

        binding.timeText.setText(text);
        if (location != null) {
            binding.timeLocation.setText(location);
        } else {
            binding.timeLocation.setText(R.string.tap_to_change_time);
        }

        ZoneOffset timeZone = ZoneOffset.ofTotalSeconds(offset/1000);
        LocalDate dummy = LocalDate.of(2000,1,1);

        try {
            CompassFragment compassFragment = getCompassFragment();
            compassFragment.setTime(
                ZonedDateTime.of(dummy, time, timeZone)
                    .withZoneSameInstant(ZoneOffset.ofHours(0))
                    .toLocalTime()
            );
        } catch (Debug.Exception e) {
        }
        try {
            InfoFragment fragment = getInfoFragment();
            fragment.setTime(time, timeZone);
        } catch (Debug.Exception e) {
        }

    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            timeBundle = result;
            Comms.Time time = Comms.Time.from(result);

            setTime(time.getTime(), time.getZoneOffset().getTotalSeconds()*1000, time.getLocation());

        }
    }

    private void setFragmentInfo() {

        binding.textView.setText(R.string.info);

        binding.left.setClickable(true);
        binding.right.setClickable(false);
        try (
            TypedArray a = requireContext().obtainStyledAttributes(new int[] { R.attr.colorSecondaryVariant });
            TypedArray b = requireContext().obtainStyledAttributes(new int[] { R.attr.colorSecondary })
        ) {
            binding.left.setColorFilter(b.getColor(0, 0));
            binding.right.setColorFilter(a.getColor(0, 0));
        } catch (Exception e){
            Debug.error(e.getMessage());
        }

    }

    private void setFragmentCompass() {

        binding.textView.setText(R.string.compass);

        binding.left.setClickable(false);
        binding.right.setClickable(true);
        try (
            TypedArray a = requireContext().obtainStyledAttributes(new int[] { R.attr.colorSecondaryVariant });
            TypedArray b = requireContext().obtainStyledAttributes(new int[] { R.attr.colorSecondary })
        ) {
            binding.left.setColorFilter(a.getColor(0, 0));
            binding.right.setColorFilter(b.getColor(0, 0));
        } catch (Exception e){
            Debug.error(e.getMessage());
        }

    }

    private class OnClickLeft implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();
            bundle.putAll(locationBundle);
            bundle.putAll(dateBundle);
            bundle.putAll(timeBundle);

            getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_compass, CompassFragment.class, bundle)
                .commit();

            setFragmentCompass();

        }
    }

    private class OnClickRight implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();
            bundle.putAll(locationBundle);
            bundle.putAll(dateBundle);
            bundle.putAll(timeBundle);

            getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_compass, InfoFragment.class, bundle)
                .commit();

            setFragmentInfo();

        }
    }

    private CompassFragment getCompassFragment()
    throws FragmentNotFound, NoChildFragmentManagerAttached {

        Fragment childFragment = getChildFragmentManagerOrThrowException()
            .findFragmentById(R.id.fragment_compass);

        CompassFragment fragment;
        if (childFragment instanceof CompassFragment) {
            fragment = (CompassFragment) childFragment;
        } else {
            throw new FragmentNotFound();
        }

        return fragment;

    }

    private InfoFragment getInfoFragment()
    throws FragmentNotFound, NoChildFragmentManagerAttached {

        Fragment childFragment = getChildFragmentManagerOrThrowException()
            .findFragmentById(R.id.fragment_compass);

        InfoFragment fragment;
        if (childFragment instanceof InfoFragment) {
            fragment = (InfoFragment) childFragment;
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