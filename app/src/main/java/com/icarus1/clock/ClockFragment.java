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

import java.util.TimeZone;

public class ClockFragment extends Fragment {

    private ClockViewModel mViewModel;
    private FragmentClockBinding binding;
    private Handler handler;
    private boolean useSystemTime;
    private TimePicker.OnTimeChangedListener onTimeChangedListener;
    private TimeZonePicker.OnTimeZoneChanged onTimeZoneChanged;
    private RetrieveSystemTime retrieveSystemTime;

    public ClockFragment() {
        useSystemTime = true;
        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChanged = new OnTimeZoneChange();
        retrieveSystemTime = new RetrieveSystemTime();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.timePicker.setIs24HourView(true);
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnUTCOffsetChanged(onTimeZoneChanged);

        binding.useSystemTime.setOnClickListener(v -> setUseSystemTime(true));

        setUseSystemTime(true);

        handler = new Handler(requireActivity().getMainLooper());
        handler.post(retrieveSystemTime);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        // TODO: Use the ViewModel
    }

    public void setTimeAndTimeZone(int hour, int minute, int second, int UTCOffset, @Nullable String location) {

        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);
        binding.timeZonePicker.setUTCOffset(UTCOffset);

        onTimeAndTimeZoneChanged(hour, minute, second, UTCOffset, location);

    }

    public void setTimeAndTimeZoneWithoutNotification(int hour, int minute, int second, int UTCOffset, @Nullable String location) {

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnUTCOffsetChanged(null);

        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);
        binding.timeZonePicker.setUTCOffset(UTCOffset);

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnUTCOffsetChanged(onTimeZoneChanged);

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

        this.useSystemTime = useSystemTime;

    }

    public void onTimeAndTimeZoneChanged(int hour, int minute, int second, int UTCOffset, String location) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", minute);
        bundle.putInt("SECOND", second);
        bundle.putInt("OFFSET", UTCOffset);
        bundle.putString("LOCATION", location);
        requireActivity().getSupportFragmentManager().setFragmentResult("C", bundle);

    }

    public class OnTimeChanged implements TimePicker.OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

            setUseSystemTime(false);

            ClockFragment.this.onTimeAndTimeZoneChanged(
                hourOfDay, minute, 0,
                binding.timeZonePicker.getUTCOffset(),
                binding.timeZonePicker.getLocation()
            );

        }
    }

    public class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {
        @Override
        public void onUTCOffsetChanged(int UTCOffset, String location) {

            setUseSystemTime(false);

            ClockFragment.this.onTimeAndTimeZoneChanged(
                binding.timePicker.getHour(), binding.timePicker.getMinute(), 0,
                UTCOffset,
                location
            );

        }
    }

    private class RetrieveSystemTime implements Runnable {
        @Override
        public void run() {

            if (useSystemTime) {

                Time systemTime = Time.fromSystem();
                TimeZone timeZone = TimeZone.getDefault();
                int UTCOffset = timeZone.getRawOffset();
                String location = Format.Location(timeZone);

                setTimeAndTimeZoneWithoutNotification(systemTime.getHour(), systemTime.getMinute(), systemTime.getSecond(), UTCOffset, location);

            }

            if (handler == null) {
                Debug.error("Clock fragment handler null.");
                return;
            }

            boolean success = handler.postDelayed(this, 10);
            if (!success) {
                Debug.error("Clock fragment handler failed post.");
            }

        }
    }

}