package com.skycompass;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.skycompass.databinding.FragmentClockBinding;
import com.skycompass.util.Debug;
import com.skycompass.views.ShowHideAnimation;
import com.skycompass.views.TimeZonePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ClockFragment extends Fragment implements TimeZonePicker.TimeZoneAdapter {

    private ClockViewModel viewModel;
    private FragmentClockBinding binding;

    private Handler handler;
    private RetrieveSystemTime retrieveSystemTime;

    private final OnTimeChanged onTimeChangedListener;
    private final OnTimeZoneChange onTimeZoneChangedListener;
    private final OnUseSystemTime onUseSystemTime;

    private ShowHideAnimation showHideUseSystemDateAnimation;

    public ClockFragment() {

        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChangedListener = new OnTimeZoneChange();
        onUseSystemTime = new OnUseSystemTime();

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

        showHideUseSystemDateAnimation = new ShowHideAnimation(binding.useSystemTime);

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);
        binding.useSystemTime.setOnClickListener(onUseSystemTime);

        if (viewModel.isUseSystemTime()) {
            setTimeAndTimeZoneAsSystemValues(LocalTime.now(), ZoneId.systemDefault());
        } else {
            updateTimePicker();
            updateTimeZonePicker();
            notifyTimeAndTimeZoneChanged();
        }

        startRetrieveSystemTime();

    }

    @Override
    public void onPause() { // TODO onPause ? but when new Retrieve* ?

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnTimeZoneChangedListener(null);

        endRetrieveSystemTime();

        super.onPause();
    }

    public void setDate(LocalDate date) {

        viewModel.setDate(date);

        updateTimeZonePicker();

        notifyTimeAndTimeZoneChanged();

    }

    private void setTimeAndTimeZoneAsSystemValues(LocalTime time, ZoneId timeZone) {

        viewModel.setTime(time);
        viewModel.setZoneId(timeZone);
        viewModel.setUseSystemTime(true);

        Debug.log(String.format("System: %s %s %d", time.toString(), timeZone.toString(), viewModel.getZoneOffset().getTotalSeconds() * 1000));

        showHideUseSystemDateAnimation.hide();
        updateTimePicker();
        updateTimeZonePicker();

        notifyTimeAndTimeZoneChanged();

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

    private class OnUseSystemTime implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setTimeAndTimeZoneAsSystemValues(LocalTime.now(), ZoneId.systemDefault());
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
            viewModel.setUseSystemTime(false);

            showHideUseSystemDateAnimation.show();
            updateTimeZonePicker();

            notifyTimeAndTimeZoneChanged();

        }
    }

    private class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {

        public boolean isUserInput = true;

        @Override
        public void onTimeZoneChanged(TimeZonePicker timeZonePicker, int offset, String id) {

            // TODO handle case where date/time are not compatible with time zone

            if (!isUserInput) {
                isUserInput = true;
                return;
            }

            if (id != null)
                viewModel.setZoneId(ZoneId.of(id));
            else
                viewModel.setZoneOffset(ZoneOffset.ofTotalSeconds(offset/1000));

            Debug.log(String.format("User: %s %d",
                viewModel.getZoneId() != null ? viewModel.getZoneId().toString() : null,
                viewModel.getZoneOffset().getTotalSeconds() * 1000
            ));

            viewModel.setUseSystemTime(false);

            showHideUseSystemDateAnimation.show();

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

    private void startRetrieveSystemTime() {

        retrieveSystemTime = new RetrieveSystemTime();

        handler = new Handler(requireActivity().getMainLooper());

        if (!handler.post(retrieveSystemTime))
            Debug.warn("Handler failed to post.");

    }

    private void endRetrieveSystemTime() {
        retrieveSystemTime.end();
    }

    private class RetrieveSystemTime implements Runnable {

        private boolean end = false;
        private boolean firstRun = true;
        private int prevHour;
        private int prevMinute;
        private int prevSecond;

        @Override
        public void run() {

            if (viewModel.isUseSystemTime()) {

                LocalTime time = LocalTime.now();
                ZoneId timeZone = ZoneId.systemDefault();

                boolean timeChanged = firstRun
                    || time.getHour() != prevHour
                    || time.getMinute() != prevMinute
                    || time.getSecond() != prevSecond;

                if (timeChanged) {
                    setTimeAndTimeZoneAsSystemValues(time, timeZone);
                    firstRun = false;
                    prevHour = time.getHour();
                    prevMinute = time.getMinute();
                    prevSecond = time.getSecond();
                }

            }

            if (!end && !handler.postDelayed(this, 10))
                Debug.warn("Handler failed to post.");

        }

        public void end() {
            end = true;
        }

    }

}