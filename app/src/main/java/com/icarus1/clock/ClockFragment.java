package com.icarus1.clock;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.icarus1.databinding.FragmentClockBinding;
import com.icarus1.util.Format;

import java.util.Calendar;
import java.util.TimeZone;

public class ClockFragment extends Fragment {

    private ClockViewModel mViewModel;
    private FragmentClockBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR)+(calendar.get(Calendar.AM_PM)==Calendar.PM?12:0);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        TimeZone timeZone = TimeZone.getDefault();
        int UTCOffset = timeZone.getRawOffset();
        String location = Format.Location(timeZone);

        binding.timePicker.setIs24HourView(true);
        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);
        binding.timeZonePicker.setUTCOffset(UTCOffset);

        binding.timeZonePicker.setOnUTCOffsetChanged(new TimeZonePicker.onTimeZoneChanged() {
            @Override
            public void onUTCOffsetChanged(int UTCOffset, String location) {
                onTimeChanged(binding.timePicker.getHour(), binding.timePicker.getMinute(), 0, UTCOffset, location);
            }
        });

        binding.timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                ClockFragment.this.onTimeChanged(hourOfDay, minute, 0, binding.timeZonePicker.getUTCOffset(), binding.timeZonePicker.getLocation());
            }
        });

        onTimeChanged(hour, minute, seconds, UTCOffset, location);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        // TODO: Use the ViewModel
    }

    public void onTimeChanged(int hour, int minute, int second, int UTCOffset, String location) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", minute);
        bundle.putInt("SECOND", second);
        bundle.putInt("OFFSET", UTCOffset);
        bundle.putString("LOCATION", location);
        requireActivity().getSupportFragmentManager().setFragmentResult("C", bundle);

    }

}