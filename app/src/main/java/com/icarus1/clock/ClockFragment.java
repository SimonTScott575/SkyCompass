package com.icarus1.clock;

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
    //TODO

    private ClockViewModel viewModel;
    private FragmentClockBinding binding;
    private Handler handler;
    private RetrieveSystemTime retrieveSystemTime;
    private final TimePicker.OnTimeChangedListener onTimeChangedListener;
    private final TimeZonePicker.OnTimeZoneChanged onTimeZoneChanged;

    public ClockFragment() {
        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChanged = new OnTimeZoneChange();
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

        binding.useSystemTime.setOnClickListener(v -> setUseSystemTime(true));

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnUTCOffsetChanged(onTimeZoneChanged);

        setTimeAndTimeZoneWithoutNotification(
            viewModel.getHour(), viewModel.getMinute(), viewModel.getSecond(),
            viewModel.getUTCOffset(),
            viewModel.getLocation()
        );
        setUseSystemTime(viewModel.isUseSystemTime());

        startRetrieveSystemTime();

    }

    @Override
    public void onPause() { //TODO onPause ? but when new Retrieve* ?

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnUTCOffsetChanged(null);

        endRetrieveSystemTime();

        super.onPause();
    }

    private void setTimeAndTimeZoneWithoutNotification(int hour, int minute, int second, int UTCOffset, @Nullable String location) {

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnUTCOffsetChanged(null);

        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);
        binding.timeZonePicker.setUTCOffset(UTCOffset);

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnUTCOffsetChanged(onTimeZoneChanged);

        viewModel.setTime(hour, minute, second, UTCOffset, location);

        onTimeAndTimeZoneChanged(hour, minute, second, UTCOffset, location);

    }

    public void setUseSystemTime(boolean useSystemTime) {

        if (useSystemTime) {

            binding.useSystemTime.setVisibility(View.INVISIBLE);

            Time systemTime = Time.fromSystem();
            TimeZone timeZone = TimeZone.getDefault();
            int UTCOffset = timeZone.getRawOffset();
            String location = Format.Location(timeZone);

            setTimeAndTimeZoneWithoutNotification(systemTime.getHour(), systemTime.getMinute(), systemTime.getSecond(), UTCOffset, location);

        } else {

            binding.useSystemTime.setVisibility(View.VISIBLE);

        }

        viewModel.setUseSystemTime(useSystemTime);

    }

    public void onTimeAndTimeZoneChanged(int hour, int minute, int second, int UTCOffset, String location) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", minute);
        bundle.putInt("SECOND", second);
        bundle.putInt("OFFSET", UTCOffset);
        bundle.putString("LOCATION", location);
        try {
            requireActivity().getSupportFragmentManager().setFragmentResult("C", bundle);
        } catch (IllegalStateException e) {
            Debug.log("ClockFragment requireActivity exception.");
        }

    }

    public class OnTimeChanged implements TimePicker.OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

            setUseSystemTime(false);

            setTimeAndTimeZoneWithoutNotification(
                hourOfDay, minute, 0,
                binding.timeZonePicker.getUTCOffset(),
                binding.timeZonePicker.getLocation()
            );

        }
    }

    public class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {
        @Override
        public void onUTCOffsetChanged(TimeZonePicker timeZonePicker, String location, int UTCOffset) {

            setUseSystemTime(false);

            setTimeAndTimeZoneWithoutNotification(
                binding.timePicker.getHour(), binding.timePicker.getMinute(), 0,
                UTCOffset,
                location
            );

        }
    }

    private void startRetrieveSystemTime() {

        retrieveSystemTime = new RetrieveSystemTime();
        handler = new Handler(requireActivity().getMainLooper());
        boolean success = handler.post(retrieveSystemTime);
        if (!success) {
            Debug.error("ClockFragment no post");
            setUseSystemTime(false);
        }

    }

    private void endRetrieveSystemTime() {

        retrieveSystemTime.end();

    }

    private class RetrieveSystemTime implements Runnable {

        private boolean end = false;

        @Override
        public void run() {

            if (viewModel.isUseSystemTime()) {

                Time systemTime = Time.fromSystem();
                TimeZone timeZone = TimeZone.getDefault();
                int UTCOffset = timeZone.getRawOffset();
                String location = Format.Location(timeZone);

                setTimeAndTimeZoneWithoutNotification(systemTime.getHour(), systemTime.getMinute(), systemTime.getSecond(), UTCOffset, location);

            }

            if (handler == null) {
                Debug.error("ClockFragment handler null.");
                return;
            }

            if (!end) {
                boolean success = handler.postDelayed(this, 10);
                if (!success) {
                    Debug.error("ClockFragment handler failed post.");
                }
            }

        }

        public void end() {
            end = true;
        }

    }

}