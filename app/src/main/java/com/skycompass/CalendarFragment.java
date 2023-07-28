package com.skycompass;

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
import com.skycompass.views.ShowHideAnimation;

import java.time.LocalDate;

public class CalendarFragment extends Fragment {

    private CalendarViewModel viewModel;
    private FragmentCalendarBinding binding;
    private Handler handler;
    private RetrieveSystemDate retrieveSystemDate;

    private ShowHideAnimation showHideUseSystemDateAnimation;

    private final OnDateChangedListener onDateChangedListener;
    private final OnClickUseSystemDate onClickUseSystemDate;

    public CalendarFragment() {
        onDateChangedListener = new OnDateChangedListener();
        onClickUseSystemDate = new OnClickUseSystemDate();
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
        binding.useSystemDate.setOnClickListener(onClickUseSystemDate);
        showHideUseSystemDateAnimation = new ShowHideAnimation(binding.useSystemDate);
    }

    @Override
    public void onResume() {
        super.onResume();

        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        if (viewModel.isSystemDate()) {
            setDateFromSystemDate();
        } else {
            setDate(viewModel.getDate());
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
        binding.datePicker.setOnDateChangedListener(null);
        endRetrieveSystemDate();
        super.onPause();
    }

    private class OnDateChangedListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            setDateAsDatePickerDate(LocalDate.of(year, monthOfYear+1, dayOfMonth));
        }
    }

    private class OnClickUseSystemDate implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setDateFromSystemDate();
        }
    }

    private void setDate(@NonNull LocalDate date) {

        viewModel.setDate(date);
        viewModel.setSystemDate(false);

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        showUseSystemDate();

        onDateChanged(date, false);

    }

    public void setDateFromSystemDate() {

        LocalDate date = LocalDate.now();

        setDateAsSystemDate(date);

    }

    public void setDateAsSystemDate(@NonNull LocalDate date) {

        viewModel.setDate(date);
        viewModel.setSystemDate(true);

        binding.datePicker.setOnDateChangedListener(null);
        binding.datePicker.updateDate(date.getYear(), date.getMonthValue()-1, date.getDayOfMonth());
        binding.datePicker.setOnDateChangedListener(onDateChangedListener);

        hideUseSystemDate();

        onDateChanged(date, true);

    }

    private void setDateAsDatePickerDate(@NonNull LocalDate date) {

        viewModel.setDate(date);
        viewModel.setSystemDate(false);

        showUseSystemDate();

        onDateChanged(date, false);

    }

    public void onDateChanged(@NonNull LocalDate date, boolean currentDate) {

        Bundle bundle = Comms.Date.putInto(
            date.getYear(), date.getMonthValue()-1, date.getDayOfMonth()-1, currentDate,
            new Bundle()
        );
        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("A", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
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

                LocalDate date = LocalDate.now();

                boolean dateChanged = firstRun
                    || prevYear != date.getYear()
                    || prevMonth != date.getMonthValue()
                    || prevDayOfMonth != date.getDayOfMonth();

                if (dateChanged) {
                    setDateAsSystemDate(date);
                    firstRun = false;
                    prevYear = date.getYear();
                    prevMonth = date.getMonthValue();
                    prevDayOfMonth = date.getDayOfMonth();
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

    private void showUseSystemDate() {
        showHideUseSystemDateAnimation.show();
    }

    private void hideUseSystemDate() {
        showHideUseSystemDateAnimation.hide();
    }

}