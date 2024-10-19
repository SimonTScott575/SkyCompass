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
import com.skycompass.util.Format;

import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class ClockFragment extends Fragment {

    private SystemViewModel systemViewModel;
    private FragmentClockBinding binding;

    private final OnTimeChanged onTimeChangedListener;

    public ClockFragment() {

        onTimeChangedListener = new OnTimeChanged();

    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.timePicker.setIs24HourView(true);

        systemViewModel.getZoneOffsetLiveData().observe(getViewLifecycleOwner(), zoneOffset -> {
            if (systemViewModel.getZoneId() != null)
                binding.timeZone.setText(systemViewModel.getZoneId().getDisplayName(TextStyle.FULL, Locale.getDefault()));
            else
                binding.timeZone.setText("UTC" + Format.TimeZoneOffset(systemViewModel.getZoneOffsetLiveData().getValue().getTotalSeconds() * 1000));
        });

        binding.timeZoneEdit.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.options_fragment_container, TimeZoneFragment.class, null)
                .commit();
        });

        systemViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), time -> updateTimePicker());

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        updateTimePicker();

    }

    @Override
    public void onPause() {

        binding.timePicker.setOnTimeChangedListener(null);

        super.onPause();
    }

    private void updateTimePicker() {

        LocalTime time = systemViewModel.getTimeLiveData().getValue();

        onTimeChangedListener.isUserInput = false;
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        onTimeChangedListener.isUserInput = true;

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

}