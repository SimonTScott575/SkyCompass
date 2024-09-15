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

    private CalendarViewModel viewModel;
    private FragmentCalendarBinding binding;

    private final OnDateChangedListener onDateChangedListener = new OnDateChangedListener();

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        binding = FragmentCalendarBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalDate date = viewModel.getDate();

        binding.datePicker.updateDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

    }

    @Override
    public void onPause() {

        binding.datePicker.setOnDateChangedListener(null);

        super.onPause();
    }

    public void setDate(LocalDate date) {

        viewModel.setDate(date);

        onDateChangedListener.isUserInput = false;
        binding.datePicker.updateDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        onDateChangedListener.isUserInput = true;

    }

    private class OnDateChangedListener implements DatePicker.OnDateChangedListener {

        public boolean isUserInput = true;

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            if (!isUserInput)
                return;

            LocalDate date = LocalDate.of(year, monthOfYear + 1, dayOfMonth);

            Debug.log(String.format("User input: %s", date.toString()));

            viewModel.setDate(date);

            notifyDateChanged(date);

        }
    }

    private void notifyDateChanged(@NonNull LocalDate date) {

        Bundle bundle = new Bundle();

        bundle.putInt("Y", date.getYear());
        bundle.putInt("M", date.getMonthValue() - 1);
        bundle.putInt("D", date.getDayOfMonth() - 1);
        bundle.putBoolean("CURRENT DATE", false);

        getParentFragmentManager().setFragmentResult("CalendarFragment/DateChanged", bundle);

    }

}