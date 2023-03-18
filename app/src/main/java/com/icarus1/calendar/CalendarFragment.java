package com.icarus1.calendar;

import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.icarus1.databinding.FragmentCalendarBinding;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {

        binding = FragmentCalendarBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();

        binding.datePicker.setOnDateChangedListener(
            (DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) -> {
                onDateChanged(year, monthOfYear, dayOfMonth);
        });

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        binding.datePicker.updateDate(year, month, dayOfMonth);
        onDateChanged(year, month, dayOfMonth);

    }

    public void onDateChanged(int year, int monthOfYear, int dayOfMonth) {

        Bundle bundle = new Bundle();
        bundle.putInt("Y", year);
        bundle.putInt("M", monthOfYear);
        bundle.putInt("D", dayOfMonth-1);
        requireActivity().getSupportFragmentManager().setFragmentResult("A", bundle);

    }

}