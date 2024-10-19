package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skycompass.databinding.FragmentTimeZoneBinding;
import com.skycompass.util.Debug;
import com.skycompass.views.TimeZonePicker;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeZoneFragment extends Fragment implements TimeZonePicker.TimeZoneAdapter {

    private ClockViewModel viewModel;
    private SystemViewModel systemViewModel;

    private FragmentTimeZoneBinding binding;

    private final TimeZoneFragment.OnTimeZoneChange onTimeZoneChangedListener = new TimeZoneFragment.OnTimeZoneChange();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ClockViewModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTimeZoneBinding.inflate(inflater, null, false);

        viewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.timeZonePicker.setTimeZoneAdapter(this);

        systemViewModel.getZoneOffsetLiveData().observe(getViewLifecycleOwner(), data -> {
            onTimeZoneChangedListener.isUserInput = false;
            binding.timeZonePicker.setTimeZone(
                    systemViewModel.getZoneId() != null ? systemViewModel.getZoneId().getId() : null,
                    systemViewModel.getZoneOffset().getTotalSeconds() * 1000
            );
            onTimeZoneChangedListener.isUserInput = true;
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

    }

    @Override
    public void onPause() {

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);

        super.onPause();
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