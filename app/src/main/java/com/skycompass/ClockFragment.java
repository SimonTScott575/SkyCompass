package com.skycompass;

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
import com.skycompass.views.ShowHideAnimation;
import com.skycompass.views.TimeZonePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

public class ClockFragment extends Fragment {

    private ClockViewModel viewModel;
    private FragmentClockBinding binding;

    private Database db;

    private Handler handler;
    private RetrieveSystemTime retrieveSystemTime;

    private final TimePicker.OnTimeChangedListener onTimeChangedListener;
    private final TimeZonePicker.OnTimeZoneChanged onTimeZoneChangedListener;
    private final OnUseSystemTime onUseSystemTime;

    private ShowHideAnimation showHideUseSystemDateAnimation;

    public ClockFragment() {
        onTimeChangedListener = new OnTimeChanged();
        onTimeZoneChangedListener = new OnTimeZoneChange();
        onUseSystemTime = new OnUseSystemTime();
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

        binding.timePicker.setIs24HourView(true);

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
        binding.useSystemTime.setOnClickListener(onUseSystemTime);

        if (viewModel.isUseSystemTime()) {
            setTimeAndTimeZoneFromSystemValues();
        } else {
            setTimeAndTimeZone(
                viewModel.getTime(),
                ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), viewModel.getZoneOffset())
                    .getOffset()
                    .getTotalSeconds()
                    * 1000,
                viewModel.getId()
            );
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

    public void setDate(LocalDate date, boolean currentDate) {

        viewModel.setDate(date);

        if (currentDate) {

            binding.timeZonePicker.setOnTimeZoneChangedListener(null);
            binding.timeZonePicker.setDate(date);
            binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

            viewModel.setId(binding.timeZonePicker.getZoneId());
            viewModel.setZoneOffset(ZoneOffset.ofTotalSeconds(binding.timeZonePicker.getOffset()/1000));

            onTimeAndTimeZoneChanged(
                viewModel.getTime(),
                ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), viewModel.getZoneOffset())
                    .getOffset()
                    .getTotalSeconds()
                    * 1000,
                viewModel.getId()
            );

        } else {

            binding.timeZonePicker.setDate(date);

        }

    }

    public class OnUseSystemTime implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setTimeAndTimeZoneFromSystemValues();
        }
    }

    public class OnTimeChanged implements TimePicker.OnTimeChangedListener {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            setTimeAndTimeZoneAsPickerValues(
                LocalTime.of(hourOfDay, minute, 0),
                ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), viewModel.getZoneOffset())
                    .get(ChronoField.OFFSET_SECONDS)
                    * 1000,
                viewModel.getId()
            );
        }
    }

    public class OnTimeZoneChange implements TimeZonePicker.OnTimeZoneChanged {
        @Override
        public void onTimeZoneChanged(TimeZonePicker timeZonePicker, int offset, String id) {
            setTimeAndTimeZoneAsPickerValues(viewModel.getTime(), offset, id);
        }
    }

    private void setTimeAndTimeZone(LocalTime time, int offset, String id) {

        viewModel.setTime(time);
        viewModel.setZoneOffset(ZoneOffset.ofTotalSeconds(offset/1000));
        viewModel.setId(id);
        viewModel.setUseSystemTime(false);

        showHideUseSystemDateAnimation.show();

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);
        binding.timeZonePicker.setTime(time);

        if (id != null)
            binding.timeZonePicker.setTimeZone(id);
        else
            binding.timeZonePicker.setTimeZone(offset);

        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(time, offset, id);

    }

    private void setTimeAndTimeZoneAsPickerValues(LocalTime time, int offset, String id) {

        viewModel.setTime(time);
        viewModel.setZoneOffset(
            id != null
            ? ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), ZoneId.of(id)).getOffset()
            : ZoneOffset.ofTotalSeconds(offset/1000)
        );
        viewModel.setId(id);
        viewModel.setUseSystemTime(false);

        showHideUseSystemDateAnimation.show();

        onTimeAndTimeZoneChanged(time, offset, id);

    }

    public void setTimeAndTimeZoneFromSystemValues() {

        LocalTime systemTime = LocalTime.now();

        ZoneId timeZone = ZoneId.systemDefault();

        setTimeAndTimeZoneAsSystemValues(systemTime, timeZone);

    }

    private void setTimeAndTimeZoneAsSystemValues(LocalTime time, ZoneId timeZone) {

        viewModel.setTime(time);
        viewModel.setZoneOffset(ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), timeZone).getOffset());
        viewModel.setUseSystemTime(true);

        showHideUseSystemDateAnimation.hide();

        binding.timePicker.setOnTimeChangedListener(null);
        binding.timePicker.setHour(time.getHour());
        binding.timePicker.setMinute(time.getMinute());
        binding.timePicker.setOnTimeChangedListener(onTimeChangedListener);

        binding.timeZonePicker.setOnTimeZoneChangedListener(null);
        binding.timeZonePicker.setTime(time);
        binding.timeZonePicker.setTimeZone(timeZone.getId());
        binding.timeZonePicker.setOnTimeZoneChangedListener(onTimeZoneChangedListener);

        onTimeAndTimeZoneChanged(
            time,
            ZonedDateTime.of(viewModel.getDate(), viewModel.getTime(), timeZone).getOffset().getTotalSeconds()*1000,
            timeZone.getId()
        );

    }

    public void onTimeAndTimeZoneChanged(LocalTime time, int offset, String id) {

        Bundle bundle = new Bundle();

        bundle.putInt("HOUR", time.getHour());
        bundle.putInt("MINUTE", time.getMinute());
        bundle.putInt("SECOND", time.getSecond());
        bundle.putInt("OFFSET", offset);
        bundle.putString("LOCATION", id);

        try {
            getParentFragmentManagerOrThrowException().setFragmentResult("ClockFragment/TimeChanged", bundle);
        } catch (NoParentFragmentManagerAttachedException e) {
            Debug.log(e);
        }

    }

    private void startRetrieveSystemTime()
    throws HandlerNoPostException {

        retrieveSystemTime = new RetrieveSystemTime();

        handler = new Handler(requireActivity().getMainLooper());

        if (!handler.post(retrieveSystemTime))
            throw new HandlerNoPostException();

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

                LocalTime time = LocalTime.now();
                ZoneId timeZone = ZoneId.systemDefault();

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

            if (!end && !handler.postDelayed(this, 10))
                Debug.error(new HandlerNoPostException());

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