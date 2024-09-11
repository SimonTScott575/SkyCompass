package com.skycompass;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarView;
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

    private final OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener();
    private final OnChangeLocationListener onChangeLocationListener = new OnChangeLocationListener();
    private final OnChangeDateListener onChangeDateListener = new OnChangeDateListener();
    private final OnChangeTimeListener onChangeTimeListener = new OnChangeTimeListener();

    private Bundle locationBundle;
    private Bundle dateBundle;
    private Bundle timeBundle;

    private MainViewModel viewModel;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentMainBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        BottomSheetBehavior.from(binding.mapBottomSheet).setHideable(true);
        BottomSheetBehavior.from(binding.calendarBottomSheet).setHideable(true);
        BottomSheetBehavior.from(binding.clockBottomSheet).setHideable(true);

        BottomSheetBehavior.from(binding.optionsBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        BottomSheetBehavior.from(binding.mapBottomSheet).setState(BottomSheetBehavior.STATE_HIDDEN);
        BottomSheetBehavior.from(binding.calendarBottomSheet).setState(BottomSheetBehavior.STATE_HIDDEN);
        BottomSheetBehavior.from(binding.clockBottomSheet).setState(BottomSheetBehavior.STATE_HIDDEN);

        BottomSheetBehavior.from(binding.optionsBottomSheet).setPeekHeight(128);

        binding.changeLocation.setOnClickListener(v -> { BottomSheetBehavior.from(binding.mapBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED); });
        binding.changeDate.setOnClickListener(v -> { BottomSheetBehavior.from(binding.calendarBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED); });
        binding.changeTime.setOnClickListener(v -> { BottomSheetBehavior.from(binding.clockBottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED); });

        switch (viewModel.currentFragment) {
            case INFO:
                setFragmentInfo();
                break;
            default:
                setFragmentCompass();
        }

        binding.bottomNavigation.setOnItemSelectedListener(onItemSelectedListener);

    }

    @Override
    public void onResume() {
        super.onResume();

        getChildFragmentManager()
            .setFragmentResultListener("CalendarFragment/DateChanged", this, onChangeDateListener);
        getChildFragmentManager()
            .setFragmentResultListener("MapFragment/LocationChanged", this, onChangeLocationListener);
        getChildFragmentManager()
            .setFragmentResultListener("ClockFragment/TimeChanged", this, onChangeTimeListener);

    }

    private class OnItemSelectedListener implements NavigationBarView.OnItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            int id = item.getItemId();

            Debug.log(String.format("Selected %d", id));

            Bundle bundle = new Bundle();
            bundle.putAll(locationBundle);
            bundle.putAll(dateBundle);
            bundle.putAll(timeBundle);

            if (id == R.id.bottom_item_compass) {

                if (viewModel.currentFragment == MainViewModel.FragmentView.COMPASS)
                    return false;

                getChildFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_compass, CompassFragment.class, bundle)
                        .commit();

                setFragmentCompass();

                return true;

            }

            if (id == R.id.bottom_item_times) {

                if (viewModel.currentFragment == MainViewModel.FragmentView.INFO)
                    return false;

                getChildFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_compass, InfoFragment.class, bundle)
                        .commit();

                setFragmentInfo();

                return true;

            }


            return false;
        }
    }

    private void setLocation(double latitude, double longitude, @Nullable String location) {

        Debug.log(String.format("Location: %.2f %.2f %s", latitude, longitude, location));

        binding.locationText.setText(Format.LatitudeLongitude(latitude, longitude));

        if (location != null)
            binding.locationAddress.setText(location);
        else
            binding.locationAddress.setText(R.string.tap_to_change_location);

        switch (viewModel.currentFragment) {
            case COMPASS:
                CompassFragment compassFragment = getChildFragment(CompassFragment.class);
                compassFragment.setLocation(longitude, latitude);
                break;
            case INFO:
                InfoFragment infoFragment = getChildFragment(InfoFragment.class);
                infoFragment.setLocation(longitude, latitude);
                break;
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

        Debug.log(String.format("Date: %s Current: %b", date.toString(), isCurrentDate));

        binding.dateText.setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        if (isCurrentDate)
            binding.dateSubscript.setText(R.string.using_system_date);
        else
            binding.dateSubscript.setText(R.string.tap_to_change_date);

        switch (viewModel.currentFragment) {
            case COMPASS:
                CompassFragment compassFragment = getChildFragment(CompassFragment.class);
                compassFragment.setDate(date);
                break;
            case INFO:
                InfoFragment infoFragment = getChildFragment(InfoFragment.class);
                infoFragment.setDate(date);
                break;
        }

        ClockFragment clockFragment = getChildFragment(ClockFragment.class);
        clockFragment.setDate(date);

    }

    private class OnChangeDateListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            dateBundle = result;

            setDate(
                LocalDate.of(result.getInt("Y"), result.getInt("M") + 1, result.getInt("D") + 1),
                result.getBoolean("CURRENT DATE")
            );

        }
    }

    private void setTime(LocalTime time, int offset, String location) {

        Debug.log(String.format("Time: %s Offset: %d Location: %s", time.toString(), offset, location));

        String text = Format.Time(time.getHour(), time.getMinute(), time.getSecond());
        text += " (UTC" + Format.TimeZoneOffset(offset) + ")";

        binding.timeText.setText(text);

        if (location != null)
            binding.timeLocation.setText(location);
        else
            binding.timeLocation.setText(R.string.tap_to_change_time);

        ZoneOffset timeZone = ZoneOffset.ofTotalSeconds(offset/1000);
        LocalDate dummy = LocalDate.of(2000,1,1);

        switch (viewModel.currentFragment) {
            case COMPASS:
                CompassFragment compassFragment = getChildFragment(CompassFragment.class);
                compassFragment.setTime(
                    ZonedDateTime.of(dummy, time, timeZone)
                        .withZoneSameInstant(ZoneOffset.ofHours(0))
                        .toLocalTime()
                );
                break;
            case INFO:
                InfoFragment infoFragment = getChildFragment(InfoFragment.class);
                infoFragment.setTime(time, timeZone);
                break;
        }

    }

    private class OnChangeTimeListener implements FragmentResultListener {
        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {

            timeBundle = result;

            LocalTime time = LocalTime.of(result.getInt("HOUR"), result.getInt("MINUTE"), result.getInt("SECOND"));
            String location = result.getString("LOCATION");
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(result.getInt("OFFSET")/1000);

            setTime(time, zoneOffset.getTotalSeconds()*1000, location);

        }
    }

    private void setFragmentCompass() {

        Debug.log("Focus: Compass");

        viewModel.currentFragment = MainViewModel.FragmentView.COMPASS;

    }

    private void setFragmentInfo() {

        Debug.log("Focus: Info");

        viewModel.currentFragment = MainViewModel.FragmentView.INFO;

    }

    private <T> T getChildFragment(Class<T> fragmentClass) {

        int id;

        if (fragmentClass == CompassFragment.class)
            id = R.id.fragment_compass;
        else if (fragmentClass == InfoFragment.class)
            id = R.id.fragment_compass;
        else if (fragmentClass == ClockFragment.class)
            id = R.id.clock_fragment_container;
        else
            throw new RuntimeException("Unrecognised fragment class.");

        FragmentManager manager = getChildFragmentManager();

        Fragment child = manager.findFragmentById(id);

        if (child != null && child.getClass() == fragmentClass)
            return (T) child;
        else
            throw new RuntimeException(String.format("Child fragment not instance of %s", fragmentClass.getSimpleName()));

    }

}