package com.skycompass;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.skycompass.databinding.FragmentClockBinding;
import com.skycompass.util.Debug;
import com.skycompass.views.TimeZonePicker;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ClockFragment extends Fragment implements TimeZonePicker.TimeZoneAdapter {

    private ClockViewModel viewModel;
    private SystemViewModel systemViewModel;
    private FragmentClockBinding binding;

    private final OnTimeChanged onTimeChangedListener;
    private final OnTimeZoneChange onTimeZoneChangedListener;

    public ClockFragment() {

        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChangedListener = new OnTimeZoneChange();

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        viewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.timePicker.setIs24HourView(true);
        binding.timeZonePicker.setTimeZoneAdapter(this);

        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), date -> {

            updateTimeZonePicker();

        });

        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), time -> {

            updateTimePicker();
            updateTimeZonePicker();

        });

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        updateTimePicker();
        updateTimeZonePicker();

    }

    @Override
    public void onPause() {

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnTimeZoneChangedListener(null);

        super.onPause();
    }

    private void updateTimePicker() {

        LocalTime time = systemViewModel.getTimeLiveData().getValue();

        onTimeChangedListener.isUserInput = false;
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        onTimeChangedListener.isUserInput = true;

    }

    private void updateTimeZonePicker() {

        binding.timeZonePicker.notifyDataSetChanged();

        ZoneId timeZone = systemViewModel.getZoneId();

        if (timeZone != null) {
            onTimeZoneChangedListener.isUserInput = false;
            binding.timeZonePicker.setTimeZone(timeZone.getId(), systemViewModel.getZoneOffset().getTotalSeconds()*1000);
            onTimeZoneChangedListener.isUserInput = true;
        }

    }

    private class OnTimeChanged implements TimePicker.OnTimeChangedListener {

        public boolean isUserInput = true;

        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

            // TODO handle case where date/time are not compatible with time zone

            if (!isUserInput)
                return;

            LocalTime time = LocalTime.of(hourOfDay, minute);

            Debug.log(String.format("User: %s", time.toString()));

            systemViewModel.setTime(time);

        }
    }

    private class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {

        public boolean isUserInput = true;

        @Override
        public void onTimeZoneChanged(TimeZonePicker timeZonePicker, int offset, String id) {

            // TODO handle case where date/time are not compatible with time zone

            if (!isUserInput)
                return;

            ZoneId zoneId = id != null ? ZoneId.of(id) : null;
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offset/1000);

            Debug.log(String.format("User: %s %d",
                zoneId != null ? zoneId.toString() : null,
                zoneOffset.getTotalSeconds() * 1000
            ));

            if (id != null)
                systemViewModel.setZoneId(zoneId);
            else
                systemViewModel.setZoneOffset(zoneOffset);

        }
    }

    @Override
    public void getTimeZone(TimeZonePicker picker, int position, TimeZonePicker.TimeZone meta) {

        viewModel.setTimeZoneSearch(picker.getSearchText());

        meta.name = viewModel.getTimeZonesSearch().get(position);

        ZonedDateTime timeZone = ZonedDateTime.of(
            systemViewModel.getDateLiveData().getValue(),
            systemViewModel.getTimeLiveData().getValue(),
            ZoneId.of(meta.name)
        );

        meta.offsetMilliseconds = timeZone.getOffset().getTotalSeconds() * 1000;

    }

    @Override
    public int getTimeZoneCount(TimeZonePicker picker) {

        viewModel.setTimeZoneSearch(picker.getSearchText());

        return viewModel.getTimeZonesSearch().size();
    }

}