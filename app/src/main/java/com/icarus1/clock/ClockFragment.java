package com.icarus1.clock;

import androidx.fragment.app.FragmentManager;
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

import com.icarus1.databinding.FragmentClockBinding;
import com.icarus1.util.Debug;
import com.icarus1.util.Format;
import com.icarus1.util.Time;

import java.util.TimeZone;

public class ClockFragment extends Fragment {

    private ClockViewModel viewModel;
    private FragmentClockBinding binding;
    private Handler handler;
    private RetrieveSystemTime retrieveSystemTime;
    private final TimePicker.OnTimeChangedListener onTimeChangedListener;
    private final TimeZonePicker.OnTimeZoneChanged onTimeZoneChangedListener;

    public ClockFragment() {
        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChangedListener = new OnTimeZoneChange();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.timePicker.setIs24HourView(true);
        binding.useSystemTime.setOnClickListener(v -> setTimeAndTimeZoneFromSystemValues());
    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnUTCOffsetChanged(onTimeZoneChangedListener);

        if (viewModel.isUseSystemTime()) {
            setTimeAndTimeZoneFromSystemValues();
        } else {
            setTimeAndTimeZoneAsPickerValues(
                viewModel.getHour(), viewModel.getMinute(), viewModel.getSecond(),
                viewModel.getUTCOffset(), viewModel.getLocation()
            );
        }

        try {
            startRetrieveSystemTime();
        } catch (HandlerNoPostException e) {
            Debug.error(e);
            setTimeAndTimeZoneFromSystemValues();
        }

    }

    @Override
    public void onPause() { //TODO onPause ? but when new Retrieve* ?

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnUTCOffsetChanged(null);

        endRetrieveSystemTime();

        super.onPause();
    }

    private void setTimeAndTimeZoneAsPickerValues(int hour, int minute, int second, int UTCOffset, @Nullable String location) {

        viewModel.setTime(hour, minute, second);
        viewModel.setTimeZone(UTCOffset, location);
        viewModel.setUseSystemTime(false);

        binding.useSystemTime.setVisibility(View.VISIBLE);
        binding.useSystemTimeText.setVisibility(View.VISIBLE);

        onTimeAndTimeZoneChanged(hour, minute, second, UTCOffset, location);

    }

    public void setTimeAndTimeZoneFromSystemValues() {

        Time systemTime = Time.fromSystem();
        TimeZone timeZone = TimeZone.getDefault();
        int UTCOffset = timeZone.getRawOffset();
        String location = Format.Location(timeZone);

        setTimeAndTimeZoneAsSystemValues(
            systemTime.getHour(), systemTime.getMinute(), systemTime.getSecond(),
            UTCOffset, location
        );


    }

    private void setTimeAndTimeZoneAsSystemValues(int hour, int minute, int second, int UTCOffset, @Nullable String location) {

        viewModel.setTime(hour, minute, second);
        viewModel.setTimeZone(UTCOffset, location);
        viewModel.setUseSystemTime(true);

        binding.useSystemTime.setVisibility(View.INVISIBLE);
        binding.useSystemTimeText.setVisibility(View.INVISIBLE);

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnUTCOffsetChanged(null);
        binding.timeZonePicker.setUTCOffset(UTCOffset);
        binding.timeZonePicker.setOnUTCOffsetChanged(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(hour, minute, second, UTCOffset,location);

    }

    public void onTimeAndTimeZoneChanged(int hour, int minute, int second, int UTCOffset, @Nullable String location) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", minute);
        bundle.putInt("SECOND", second);
        bundle.putInt("OFFSET", UTCOffset);
        bundle.putString("LOCATION", location);
        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("C", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
        }

    }

    public class OnTimeChanged implements TimePicker.OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            setTimeAndTimeZoneAsPickerValues(
                hourOfDay, minute, 0,
                viewModel.getUTCOffset(), viewModel.getLocation()
            );
        }
    }

    public class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {
        @Override
        public void onUTCOffsetChanged(TimeZonePicker timeZonePicker, String location, int UTCOffset) {
            setTimeAndTimeZoneAsPickerValues(
                viewModel.getHour(), viewModel.getMinute(), viewModel.getSecond(),
                UTCOffset, location
            );
        }
    }

    private void startRetrieveSystemTime()
    throws HandlerNoPostException {

        retrieveSystemTime = new RetrieveSystemTime();
        handler = new Handler(requireActivity().getMainLooper());
        boolean success = handler.post(retrieveSystemTime);
        if (!success) {
            throw new HandlerNoPostException();
        }

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

                Time systemTime = Time.fromSystem();
                TimeZone timeZone = TimeZone.getDefault();
                int UTCOffset = timeZone.getRawOffset();
                String location = Format.Location(timeZone);

                boolean timeChanged = firstRun
                    || systemTime.getHour() != prevHour
                    || systemTime.getMinute() != prevMinute
                    || systemTime.getSecond() != prevSecond;

                if (timeChanged) {
                    setTimeAndTimeZoneAsSystemValues(
                        systemTime.getHour(), systemTime.getMinute(), systemTime.getSecond(),
                        UTCOffset, location
                    );
                    firstRun = false;
                    prevHour = systemTime.getHour();
                    prevMinute = systemTime.getMinute();
                    prevSecond = systemTime.getSecond();
                }

            }

            if (!end) {
                boolean success = handler.postDelayed(this, 10);
                if (!success) {
                    Debug.error(new HandlerNoPostException());
                }
            }

        }

        public void end() {
            end = true;
        }

    }

    private FragmentManager getParentFragmentManagerOrThrowException()
    throws NoParentFragmentManagerAttachedException {

        try {
            return getParentFragmentManager();
        } catch (IllegalStateException e) {
            throw new NoParentFragmentManagerAttachedException();
        }

    }

    private static class HandlerNoPostException extends Debug.Exception {
        public HandlerNoPostException() {
            super("Handler failed to post.");
        }
    }
    private static class NoParentFragmentManagerAttachedException extends Debug.Exception {
        public NoParentFragmentManagerAttachedException() {
            super("No parent fragment manager attached.");
        }
    }

}