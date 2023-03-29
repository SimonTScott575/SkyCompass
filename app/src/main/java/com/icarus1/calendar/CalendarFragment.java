package com.icarus1.calendar;

import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.icarus1.databinding.FragmentCalendarBinding;
import com.icarus1.util.Debug;

public class CalendarFragment extends Fragment {

    private CalendarViewModel viewModel;
    private FragmentCalendarBinding binding;
    private Handler handler;
    private boolean useSystemDate;
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

        binding.useSystemDate.setOnClickListener(v -> setUseSystemDate(true));

    }

    @Override
    public void onResume() {
        super.onResume();

        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        setDateWithoutNotification(
            viewModel.getYear(), viewModel.getMonth(), viewModel.getDayOfMonth(),
            viewModel.isSystemDate()
        );
        setUseSystemDate(viewModel.isSystemDate());

        try {
            startRetrieveSystemDate();
        } catch (HandlerNoPostException e) {
            Debug.error(e);
            setUseSystemDate(false);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        binding.datePicker.setOnDateChangedListener(null);

        endRetrieveSystemDate();

    }

    private void setDateWithoutNotification(int year, int month, int dayOfMonth, boolean currentDate) {

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(year, month, dayOfMonth);
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        viewModel.setDate(year, month, dayOfMonth, currentDate);

        onDateChanged(year, month, dayOfMonth, currentDate);

    }

    public void onDateChanged(int year, int monthOfYear, int dayOfMonth, boolean currentDate) {

        Bundle bundle = new Bundle();
        bundle.putInt("Y", year);
        bundle.putInt("M", monthOfYear);
        bundle.putInt("D", dayOfMonth-1);
        bundle.putBoolean("CURRENT DATE", currentDate);
        try {
            getActivityOrThrowException().getSupportFragmentManager().setFragmentResult("A", bundle);
        } catch (NoActivityAttachedExcpetion e) {
            Debug.log(e);
        }

    }

    private class OnDateChangedListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            setUseSystemDate(false);

            setDateWithoutNotification(year, monthOfYear, dayOfMonth, false);

        }
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

        @Override
        public void run() {

            if (useSystemDate) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                setDateWithoutNotification(year, month, dayOfMonth, true);

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

    private FragmentActivity getActivityOrThrowException()
    throws NoActivityAttachedExcpetion {

        try {
            return requireActivity();
        } catch (IllegalStateException e) {
            throw new NoActivityAttachedExcpetion();
        }

    }

    private static class HandlerNoPostException extends Debug.Exception {
        public HandlerNoPostException() {
            super("Handler failed to post.");
        }
    }
    private static class NoActivityAttachedExcpetion extends Debug.Exception {
        public NoActivityAttachedExcpetion() {
            super("No Activity attached.");
        }
    }

}