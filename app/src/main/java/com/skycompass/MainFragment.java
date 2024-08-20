package com.skycompass;

import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

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

        binding.changeLocation.setOnClickListener(v -> binding.mapCardView.show());
        binding.changeDate.setOnClickListener(v -> binding.calendarCardView.show());
        binding.changeTime.setOnClickListener(v -> binding.clockCardView.show());

        binding.right.setOnClickListener(onClickRight);
        binding.left.setOnClickListener(onClickLeft);

        switch (viewModel.currentFragment) {
            case COMPASS:
                setFragmentCompass();
                break;
            case INFO:
                setFragmentInfo();
                break;
        }

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

    private void setLocation(double latitude, double longitude, @Nullable String location) {

        Debug.log(String.format("Location: %.2f %.2f %s", latitude, longitude, location));

        binding.locationText.setText(Format.LatitudeLongitude(latitude, longitude));

        if (location != null) {
            binding.locationAddress.setText(location);
        } else {
            binding.locationAddress.setText(R.string.tap_to_change_location);
        }

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
        if (isCurrentDate) {
            binding.dateSubscript.setText(R.string.using_system_date);
        } else {
            binding.dateSubscript.setText(R.string.tap_to_change_date);
        }

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
        if (location != null) {
            binding.timeLocation.setText(location);
        } else {
            binding.timeLocation.setText(R.string.tap_to_change_time);
        }

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

    private void setFragmentInfo() {

        Debug.log("Focus: Info");

        viewModel.currentFragment = MainViewModel.FragmentView.INFO;

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