package com.icarus1.calendar;

import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.icarus1.databinding.FragmentCalendarBinding;
import com.icarus1.util.Debug;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private Handler handler;
    private boolean useSystemDate;
    private final OnDateChangedListener onDateChangedListener;
    private final RetrieveSystemDate retrieveSystemDate;

    public CalendarFragment() {
        onDateChangedListener = new OnDateChangedListener();
        retrieveSystemDate = new RetrieveSystemDate();
    }

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

        binding.datePicker.setOnDateChangedListener(onDateChangedListener);
        binding.useSystemDate.setOnClickListener(v -> setUseSystemDate(true));

        setUseSystemDate(true);

        handler = new Handler(requireActivity().getMainLooper());
        boolean success = handler.post(retrieveSystemDate);
        if (!success) {
            Debug.error("CalendarFragment no post");
            setUseSystemDate(false);
        }

    }

    public void setDate(int year, int month, int dayOfMonth, boolean currentDate) {

        binding.datePicker.updateDate(year, month, dayOfMonth);
        onDateChanged(year, month, dayOfMonth, currentDate);

    }

    public void setDateWithoutNotification(int year, int month, int dayOfMonth, boolean currentDate) {

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(year, month, dayOfMonth);
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        onDateChanged(year, month, dayOfMonth, currentDate);

    }

    public void setUseSystemDate(boolean useSystemDate) {

        if (useSystemDate) {

            binding.useSystemDate.setVisibility(View.INVISIBLE);

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            setDateWithoutNotification(year, month, dayOfMonth, true);

        } else {
            binding.useSystemDate.setVisibility(View.VISIBLE);
        }

        this.useSystemDate = useSystemDate;

    }

    public void onDateChanged(int year, int monthOfYear, int dayOfMonth, boolean currentDate) {

        Bundle bundle = new Bundle();
        bundle.putInt("Y", year);
        bundle.putInt("M", monthOfYear);
        bundle.putInt("D", dayOfMonth-1);
        bundle.putBoolean("CURRENT DATE", currentDate);
        requireActivity().getSupportFragmentManager().setFragmentResult("A", bundle);

    }

    private class OnDateChangedListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            setUseSystemDate(false);

            CalendarFragment.this.onDateChanged(year, monthOfYear, dayOfMonth, false);

        }
    }

    private class RetrieveSystemDate implements Runnable {
        @Override
        public void run() {

            if (useSystemDate) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                setDateWithoutNotification(year, month, dayOfMonth, true);

            }

            if (handler == null) {
                Debug.error("CalendarFragment handler null.");
                return;
            }

            boolean success = handler.post(this);
            if (!success) {
                Debug.error("CalendarFragment no post");
            }

        }
    }

}