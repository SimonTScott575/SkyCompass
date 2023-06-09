package com.skycompass;

import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.skycompass.databinding.FragmentCalendarBinding;
import com.skycompass.util.Debug;

public class CalendarFragment extends Fragment {

    private CalendarViewModel viewModel;
    private FragmentCalendarBinding binding;
    private Handler handler;
    private RetrieveSystemDate retrieveSystemDate;
    private final OnDateChangedListener onDateChangedListener;

    public CalendarFragment() {
        onDateChangedListener = new OnDateChangedListener();
    }

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.useSystemDate.setOnClickListener(v -> setDateFromSystemDate());

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        if (viewModel.isSystemDate()) {
            setDateFromSystemDate();
        } else {
            setDate(viewModel.getYear(), viewModel.getMonth(), viewModel.getDayOfMonth());
        }

        try {
            startRetrieveSystemDate();
        } catch (HandlerNoPostException e) {
            Debug.error(e);
            setDateFromSystemDate();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        binding.datePicker.setOnDateChangedListener(null);

        endRetrieveSystemDate();

    }

    private void setDate(int year, int month, int dayOfMonth) {

        viewModel.setDate(year, month, dayOfMonth);
        viewModel.setSystemDate(false);

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(year, month, dayOfMonth);
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        binding.useSystemDate.setVisibility(View.VISIBLE);

        onDateChanged(year, month, dayOfMonth, false);

    }

    public void setDateFromSystemDate() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        viewModel.setDate(year, month, dayOfMonth);
        viewModel.setSystemDate(true);

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(year, month, dayOfMonth);
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        binding.useSystemDate.setVisibility(View.INVISIBLE);

        onDateChanged(year, month, dayOfMonth, true);

    }

    public void setDateAsSystemDate(int year, int month, int dayOfMonth) {

        viewModel.setDate(year, month, dayOfMonth);
        viewModel.setSystemDate(true);

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(year, month, dayOfMonth);
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        binding.useSystemDate.setVisibility(View.INVISIBLE);

        onDateChanged(year, month, dayOfMonth, true);

    }

    private void setDateAsDatePickerDate(int year, int month, int dayOfMonth) {

        viewModel.setDate(year, month, dayOfMonth);
        viewModel.setSystemDate(false);

        binding.useSystemDate.setVisibility(View.VISIBLE);

        onDateChanged(year, month, dayOfMonth, false);

    }

    public void onDateChanged(int year, int monthOfYear, int dayOfMonth, boolean currentDate) {

        Bundle bundle = new Bundle();
        bundle.putInt("Y", year);
        bundle.putInt("M", monthOfYear);
        bundle.putInt("D", dayOfMonth-1);
        bundle.putBoolean("CURRENT DATE", currentDate);
        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("A", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
        }

    }

    private class OnDateChangedListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            setDateAsDatePickerDate(year, monthOfYear, dayOfMonth);

        }
    }

    private void startRetrieveSystemDate()
    throws HandlerNoPostException {

        retrieveSystemDate = new RetrieveSystemDate();
        handler = new Handler(requireActivity().getMainLooper());
        boolean success = handler.post(retrieveSystemDate);
        if (!success) {
            throw new HandlerNoPostException();
        }

    }

    private void endRetrieveSystemDate() {
        retrieveSystemDate.end();
    }

    private class RetrieveSystemDate implements Runnable {

        private boolean end = false;
        private boolean firstRun = true;
        private int prevYear;
        private int prevMonth;
        private int prevDayOfMonth;

        @Override
        public void run() {

            if (viewModel.isSystemDate()) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                boolean dateChanged = firstRun
                    || prevYear != year
                    || prevMonth != month
                    || prevDayOfMonth != dayOfMonth;

                if (dateChanged) {
                    setDateAsSystemDate(year, month, dayOfMonth);
                    firstRun = false;
                    prevYear = year;
                    prevMonth = month;
                    prevDayOfMonth = dayOfMonth;
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