package com.skycompass;

import static com.skycompass.views.TimeZonePicker.UseDST.ALWAYS;
import static com.skycompass.views.TimeZonePicker.UseDST.DATE;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.skycompass.database.Database;
import com.skycompass.databinding.FragmentClockBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Time;
import com.skycompass.util.TimeZone;
import com.skycompass.views.ShowHideAnimation;
import com.skycompass.views.TimeZonePicker;

public class ClockFragment extends Fragment {

    private ClockViewModel viewModel;
    private FragmentClockBinding binding;
    private Database db;
    private Handler handler;
    private RetrieveSystemTime retrieveSystemTime;

    private final TimePicker.OnTimeChangedListener onTimeChangedListener;
    private final TimeZonePicker.OnTimeZoneChanged onTimeZoneChangedListener;

    private ShowHideAnimation showHideUseSystemDateAnimation;

    public ClockFragment() {
        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChangedListener = new OnTimeZoneChange();
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {

        viewModel = new ViewModelProvider(this).get(ClockViewModel.class);
        binding = FragmentClockBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (viewModel.getTime() == null) {
            viewModel.setTime(new Time(0, 0, 0));
        }
        if (viewModel.getTimeZone() == null) {
            viewModel.setTimeZone(new TimeZone(0));
        }
        if (viewModel.getUseDST() == null) {
            viewModel.setUseDST(ALWAYS);
        }

        binding.timePicker.setIs24HourView(true);
        binding.useSystemTime.setOnClickListener(v -> setTimeAndTimeZoneFromSystemValues());

        showHideUseSystemDateAnimation = new ShowHideAnimation(binding.useSystemTime);

    }

    @Override
    public void onStart() {
        super.onStart();
        db = new Database(requireContext());
        db.open();
        binding.timeZonePicker.setDatabase(db);
    }

    @Override
    public void onStop() {
        db.close();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        if (viewModel.isUseSystemTime()) {
            setTimeAndTimeZoneFromSystemValues();
        } else {
            setTimeAndTimeZone(viewModel.getTime(), viewModel.getTimeZone());
        }

        try {
            startRetrieveSystemTime();
        } catch (HandlerNoPostException e) {
            Debug.error(e);
            setTimeAndTimeZoneFromSystemValues();
        }

    }

    @Override
    public void onPause() { //TODO onPause ? but when new Retrieve* ?

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timeZonePicker.setOnTimeZoneChangedListener(null);

        endRetrieveSystemTime();

        super.onPause();
    }

    public void setDate(int year, int month, int dayOfMonth, boolean currentDate) {

        if (currentDate) {

            binding.timeZonePicker.setOnTimeZoneChangedListener(null);
            binding.timeZonePicker.setDate(year, month, dayOfMonth);
            binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

//            viewModel.setTimeZone(binding.timeZonePicker.getTimeZone());

            onTimeAndTimeZoneChanged(viewModel.getTime(), viewModel.getTimeZone());

        } else {
            binding.timeZonePicker.setDate(year, month, dayOfMonth);
        }

    }

    public class OnTimeChanged implements TimePicker.OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            setTimeAndTimeZoneAsPickerValues(new Time(hourOfDay, minute, 0), viewModel.getTimeZone());
        }
    }

    public class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {
        @Override
        public void onTimeZoneChanged(TimeZonePicker timeZonePicker, TimeZone timeZone) {
            viewModel.setUseDST(binding.timeZonePicker.getUseDST());
            setTimeAndTimeZoneAsPickerValues(viewModel.getTime(), timeZone);
        }
    }

    public void setTimeAndTimeZone(Time time, TimeZone timeZone) {

        viewModel.setTime(time);
        viewModel.setTimeZone(timeZone);
        viewModel.setUseSystemTime(false);

        showHideUseSystemDateAnimation.show();

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);
        binding.timeZonePicker.setTime(time.getHour(), time.getMinute(), time.getSecond());
        binding.timeZonePicker.setTimeZoneAndUseDST(timeZone, viewModel.getUseDST());
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(time, timeZone);

    }

    private void setTimeAndTimeZoneAsPickerValues(Time time, TimeZone timeZone) {

        viewModel.setTime(time);
        viewModel.setTimeZone(timeZone);
        viewModel.setUseSystemTime(false);

        showHideUseSystemDateAnimation.show();

        onTimeAndTimeZoneChanged(time, timeZone);

    }

    public void setTimeAndTimeZoneFromSystemValues() {

        Time systemTime = Time.fromSystem();
        TimeZone timeZone = TimeZone.fromSystem();

        setTimeAndTimeZoneAsSystemValues(systemTime, timeZone);

    }

    private void setTimeAndTimeZoneAsSystemValues(Time time, TimeZone timeZone) {

        viewModel.setTime(time);
        viewModel.setTimeZone(timeZone);
        viewModel.setUseDST(DATE);
        viewModel.setUseSystemTime(true);

        showHideUseSystemDateAnimation.hide();

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);
        binding.timeZonePicker.setTime(time.getHour(), time.getMinute(), time.getSecond());
        binding.timeZonePicker.setTimeZoneAndUseDST(timeZone, viewModel.getUseDST());
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(time, binding.timeZonePicker.getTimeZone());

    }

    public void onTimeAndTimeZoneChanged(Time time, TimeZone timeZone) {

        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", time.getHour());
        bundle.putInt("MINUTE", time.getMinute());
        bundle.putInt("SECOND", time.getSecond());
        bundle.putInt("OFFSET", timeZone.getOffset());
        bundle.putString("LOCATION", timeZone.getID());
        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("C", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
        }

    }

    private void startRetrieveSystemTime()
    throws HandlerNoPostException {

        retrieveSystemTime = new RetrieveSystemTime();
        handler = new Handler(requireActivity().getMainLooper());
        boolean success = handler.post(retrieveSystemTime);
        if (!success) {
            throw new HandlerNoPostException();
        }

    }

    private void endRetrieveSystemTime() {
        retrieveSystemTime.end();
    }

    private class RetrieveSystemTime implements Runnable {

        private boolean end = false;
        private boolean firstRun = true;
        private int prevHour;
        private int prevMinute;
        private int prevSecond;

        @Override
        public void run() {

            if (viewModel.isUseSystemTime()) {

                Time time = Time.fromSystem();
                TimeZone timeZone = TimeZone.fromSystem();

                boolean timeChanged = firstRun
                    || time.getHour() != prevHour
                    || time.getMinute() != prevMinute
                    || time.getSecond() != prevSecond;

                if (timeChanged) {
                    setTimeAndTimeZoneAsSystemValues(time, timeZone);
                    firstRun = false;
                    prevHour = time.getHour();
                    prevMinute = time.getMinute();
                    prevSecond = time.getSecond();
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