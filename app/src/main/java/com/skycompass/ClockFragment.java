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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ClockFragment extends Fragment implements TimeZonePicker.TimeZoneAdapter {

    private ClockViewModel viewModel;
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

        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.timePicker.setIs24HourView(true);
        binding.timeZonePicker.setTimeZoneAdapter(this);

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

    public void setDate(LocalDate date) {

        Debug.log(String.format("Date: %s", date.toString()));

        viewModel.setDate(date);

        updateTimeZonePicker();

    }

    public void setTime(LocalTime time) {

        Debug.log(String.format("Time: %s", time.toString()));

        viewModel.setTime(time);

        updateTimePicker();
        updateTimeZonePicker();

    }

    public void setZoneId(ZoneId zoneId) {

        Debug.log(String.format("Zone ID: %s", zoneId.getId()));

        viewModel.setZoneId(zoneId);

        updateTimeZonePicker();

    }

    public void setZoneOffset(ZoneOffset zoneOffset) {

        Debug.log(String.format("Zone offset: %s seconds", zoneOffset.getTotalSeconds()));

        viewModel.setZoneOffset(zoneOffset);

        updateTimeZonePicker();

    }

    private void updateTimePicker() {

        onTimeChangedListener.isUserInput = false;
        binding.timePicker.setHour(viewModel.getTime().getHour());
        binding.timePicker.setMinute(viewModel.getTime().getMinute());
        onTimeChangedListener.isUserInput = true;

    }

    private void updateTimeZonePicker() {

        binding.timeZonePicker.notifyDataSetChanged();

        if (viewModel.getZoneId() != null) {
            onTimeZoneChangedListener.isUserInput = false;
            binding.timeZonePicker.setTimeZone(viewModel.getZoneId().getId(), viewModel.getZoneOffset().getTotalSeconds()*1000);
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

            viewModel.setTime(time);

            updateTimeZonePicker();

            notifyTimeAndTimeZoneChanged();

        }
    }

    private class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {

        public boolean isUserInput = true;

        @Override
        public void onTimeZoneChanged(TimeZonePicker timeZonePicker, int offset, String id) {

            // TODO handle case where date/time are not compatible with time zone

            if (!isUserInput)
                return;

            if (id != null)
                viewModel.setZoneId(ZoneId.of(id));
            else
                viewModel.setZoneOffset(ZoneOffset.ofTotalSeconds(offset/1000));

            Debug.log(String.format("User: %s %d",
                viewModel.getZoneId() != null ? viewModel.getZoneId().toString() : null,
                viewModel.getZoneOffset().getTotalSeconds() * 1000
            ));

            notifyTimeAndTimeZoneChanged();

        }
    }

    private void notifyTimeAndTimeZoneChanged() {

        LocalTime time = viewModel.getTime();
        int offset = viewModel.getZoneOffset().getTotalSeconds() * 1000;
        String id = viewModel.getZoneId() != null ? viewModel.getZoneId().getId() : null;

        Bundle bundle = new Bundle();

        bundle.putInt("HOUR", time.getHour());
        bundle.putInt("MINUTE", time.getMinute());
        bundle.putInt("SECOND", time.getSecond());
        bundle.putInt("OFFSET", offset);
        bundle.putString("LOCATION", id);

        getParentFragmentManager().setFragmentResult("ClockFragment/TimeChanged", bundle);

    }

    @Override
    public void getTimeZone(TimeZonePicker picker, int position, TimeZonePicker.TimeZone meta) {

        viewModel.setTimeZoneSearch(picker.getSearchText());

        meta.name = viewModel.getTimeZonesSearch().get(position);

        ZonedDateTime timeZone = ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), ZoneId.of(meta.name));

        meta.offsetMilliseconds = timeZone.getOffset().getTotalSeconds() * 1000;

    }

    @Override
    public int getTimeZoneCount(TimeZonePicker picker) {

        viewModel.setTimeZoneSearch(picker.getSearchText());

        return viewModel.getTimeZonesSearch().size();
    }

}