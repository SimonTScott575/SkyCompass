package com.skycompass;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.databinding.FragmentOptionsBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class OptionsFragment extends Fragment {

    private SystemViewModel systemViewModel;

    private FragmentOptionsBinding binding;

    private OnClickChangeOption onClickChangeOption = new OnClickChangeOption();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentOptionsBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.changeLocation.setOnClickListener(onClickChangeOption);
        binding.changeDate.setOnClickListener(onClickChangeOption);
        binding.changeTime.setOnClickListener(onClickChangeOption);

        systemViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), location -> {

            boolean isSystemLocation = systemViewModel.isSystemLocation();

            Debug.log(String.format(
                "Location: %.2f %.2f System location: %b",
                location.latitude,
                location.longitude,
                isSystemLocation
            ));

            updateLocation();

        });

        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), date -> {

            boolean isSystemDate = systemViewModel.isSystemDate();

            Debug.log(String.format("Date: %s System date: %b", date.toString(), isSystemDate));

            updateDate();

        });

        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), time -> {

            int offset = systemViewModel.getZoneOffset().getTotalSeconds() * 1000;
            String location = systemViewModel.getZoneId() != null ? systemViewModel.getZoneId().getId() : null;
            boolean isSystemTime = systemViewModel.isSystemTime();

            Debug.log(String.format(
                "Time: %s Time zone offset: %d Description: %s System time: %b",
                time.toString(),
                offset,
                location,
                isSystemTime
            ));

            updateTime();

        });

    }

    private class OnClickChangeOption implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            Class fragmentClass = null;
            String message = null;

            if (view == binding.changeLocation) {
                fragmentClass = MapFragment.class;
                message = "LOCATION";
            } else if (view == binding.changeDate) {
                fragmentClass = CalendarFragment.class;
                message = "DATE";
            } else if (view == binding.changeTime) {
                fragmentClass = ClockFragment.class;
                message = "TIME";
            }

            if (fragmentClass == null)
                return;

            getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.option_push,
                    R.anim.option_exit,
                    R.anim.option_enter,
                    R.anim.option_pop
                )
                .replace(R.id.options_fragment_container, fragmentClass, null, message)
                .commit();

        }
    }

    private void updateDate() {

        LocalDate date = systemViewModel.getDateLiveData().getValue();

        boolean isCurrentDate = systemViewModel.isSystemDate();

        binding.dateText.setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        if (isCurrentDate)
            binding.dateSubscript.setText(R.string.using_system_date);
        else
            binding.dateSubscript.setText(R.string.tap_to_change_date);

    }

    private void updateTime() {

        LocalTime time = systemViewModel.getTimeLiveData().getValue();
        int offset = systemViewModel.getZoneOffset().getTotalSeconds() * 1000;
        String description = systemViewModel.getZoneId() != null ? systemViewModel.getZoneId().getId() : null;

        String text = Format.Time(time.getHour(), time.getMinute(), time.getSecond());
        text += " (UTC" + Format.TimeZoneOffset(offset) + ")";

        binding.timeText.setText(text);

        if (description != null)
            binding.timeLocation.setText(description);
        else
            binding.timeLocation.setText(R.string.tap_to_change_time);

    }

    private void updateLocation() {

        SystemViewModel.Location location = systemViewModel.getLocationLiveData().getValue();

        binding.locationText.setText(Format.LatitudeLongitude(location.latitude, location.longitude));

        if (systemViewModel.isSystemLocation())
            binding.locationAddress.setText(R.string.current_location);
        else
            binding.locationAddress.setText(R.string.tap_to_change_location);

    }

}