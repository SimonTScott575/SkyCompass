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
import com.icarus1.util.Time;
import com.icarus1.util.TimeZone;
import com.icarus1.views.TimeZonePicker;

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
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        if (viewModel.isUseSystemTime()) {
            setTimeAndTimeZoneFromSystemValues();
        } else {
            setTimeAndTimeZone(viewModel.getTime(), viewModel.getTimeZone());
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
        binding.timeZonePicker.setOnTimeZoneChangedListener(null);

        endRetrieveSystemTime();

        super.onPause();
    }

    public void setTimeAndTimeZone(Time time, TimeZone timeZone) {

        viewModel.setTime(time);
        viewModel.setTimeZone(timeZone);
        viewModel.setUseSystemTime(false);

        binding.useSystemTime.setVisibility(View.VISIBLE);

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);
        binding.timeZonePicker.setTimeZone(timeZone);
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(time, timeZone);

    }

    private void setTimeAndTimeZoneAsPickerValues(Time time, TimeZone timeZone) {

        viewModel.setTime(time);
        viewModel.setTimeZone(timeZone);
        viewModel.setUseSystemTime(false);

        binding.useSystemTime.setVisibility(View.VISIBLE);

        onTimeAndTimeZoneChanged(time, timeZone);

    }

    public void setTimeAndTimeZoneFromSystemValues() {

        Time systemTime = Time.fromSystem();
        TimeZone timeZone = TimeZone.fromSystem();

        setTimeAndTimeZoneAsSystemValues(systemTime, timeZone);

    }

    private void setTimeAndTimeZoneAsSystemValues(Time time, TimeZone timeZone) {

        viewModel.setTime(time);
        viewModel.setTimeZone(timeZone);
        viewModel.setUseSystemTime(true);

        binding.useSystemTime.setVisibility(View.INVISIBLE);

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);
        binding.timeZonePicker.setTimeZone(timeZone);
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(time, timeZone);

    }

    public void onTimeAndTimeZoneChanged(Time time, TimeZone timeZone) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", time.getHour());
        bundle.putInt("MINUTE", time.getMinute());
        bundle.putInt("SECOND", time.getSecond());
        bundle.putInt("OFFSET", timeZone.getUTCOffset());
        bundle.putString("LOCATION", timeZone.getId());
        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("C", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
        }

    }

    public class OnTimeChanged implements TimePicker.OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            setTimeAndTimeZoneAsPickerValues(new Time(hourOfDay, minute, 0), viewModel.getTimeZone());
        }
    }

    public class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {
        @Override
        public void onTimeZoneChanged(TimeZonePicker timeZonePicker, TimeZone timeZone) {
            setTimeAndTimeZoneAsPickerValues(viewModel.getTime(), timeZone);
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

                Time time = Time.fromSystem();
                TimeZone timeZone = TimeZone.fromSystem();

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