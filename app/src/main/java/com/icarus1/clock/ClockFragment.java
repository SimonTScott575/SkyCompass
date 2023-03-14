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

import com.icarus1.R;
import com.icarus1.compass.CompassFragment;
import com.icarus1.databinding.FragmentClockBinding;

import java.util.Calendar;

public class ClockFragment extends Fragment {

    private ClockViewModel mViewModel;
    private FragmentClockBinding binding;

    public static ClockFragment newInstance() {
        return new ClockFragment();
    }

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
        float seconds = calendar.get(Calendar.SECOND);

        onTimeChanged(hour, minute, seconds);

        binding.timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                ClockFragment.this.onTimeChanged(hourOfDay, minute, 0);
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        // TODO: Use the ViewModel
    }

    public void onTimeChanged(int hour, int minute, float second) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", minute);
        bundle.putFloat("SECOND", second);
        requireActivity().getSupportFragmentManager().setFragmentResult("C", bundle);

    }

}