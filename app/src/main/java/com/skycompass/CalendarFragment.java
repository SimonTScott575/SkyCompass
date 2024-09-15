package com.skycompass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.skycompass.databinding.FragmentCalendarBinding;
import com.skycompass.util.Debug;
import com.skycompass.views.ShowHideAnimation;

import java.time.LocalDate;

public class CalendarFragment extends Fragment {

    private SystemViewModel systemViewModel;
    private FragmentCalendarBinding binding;

    private final OnDateChangedListener onDateChangedListener = new OnDateChangedListener();

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        systemViewModel = new ViewModelProvider(requireActivity()).get(SystemViewModel.class);

        binding = FragmentCalendarBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        systemViewModel.getDateLiveData().observe(getViewLifecycleOwner(), date -> {

            onDateChangedListener.isUserInput = false;
            binding.datePicker.updateDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
            onDateChangedListener.isUserInput = true;

        });

    }

    @Override
    public void onPause() {

        binding.datePicker.setOnDateChangedListener(null);

        super.onPause();
    }

    private class OnDateChangedListener implements DatePicker.OnDateChangedListener {

        public boolean isUserInput = true;

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            if (!isUserInput)
                return;

            LocalDate date = LocalDate.of(year, monthOfYear + 1, dayOfMonth);

            Debug.log(String.format("User input: %s", date.toString()));

            systemViewModel.setDate(date);

        }
    }

}