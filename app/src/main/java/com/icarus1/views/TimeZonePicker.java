package com.icarus1.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.icarus1.databinding.ViewTimeZonePickerBinding;
import com.icarus1.util.Format;
import com.icarus1.util.TimeZone;

public class TimeZonePicker extends ConstraintLayout {

    private ViewTimeZonePickerBinding binding;
    private TimeZone timeZone;
    private UseDST useDST = UseDST.NEVER;
    private OnCheckedListener onCheckedListener;
    private TimeZonePickerAdapter adapter;
    private OnTimeZoneChanged onTimeZoneChanged;
    private RangeWatcher watcher;
    private int year = 1970, month = 0, day = 0;
    private int hour = 12, minute = 0, second = 0;

    public TimeZonePicker(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public TimeZonePicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimeZonePicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {

        onCheckedListener = new OnCheckedListener();

        timeZone = new TimeZone(0);

        binding = ViewTimeZonePickerBinding.inflate(LayoutInflater.from(context), this, true);

        binding.plus.setOnClickListener(new ShiftNumber(1));
        binding.minus.setOnClickListener(new ShiftNumber(-1));
        binding.numberEditText.addTextChangedListener(watcher = new RangeWatcher());
        binding.numberEditText.setText("00:00");

        adapter = new TimeZonePickerAdapter();
        binding.textSuggestions.setAdapter(adapter);
        binding.textSuggestions.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        binding.textSuggestions.addItemDecoration(dividerItemDecoration);

        binding.useDst.setOnCheckedChangeListener(onCheckedListener);
        binding.useDst.check(binding.useDstDate.getId());

    }

    public UseDST getUseDST() {
        return useDST;
    }

    public void setUseDST(UseDST useDST) {

        this.useDST = useDST;

        switch (useDST) {
            case DATE:
                binding.useDst.check(binding.useDstDate.getId());
                break;
            case ALWAYS:
                binding.useDst.check(binding.useDstAlways.getId());
                break;
            case NEVER:
                binding.useDst.check(binding.useDstNever.getId());
                break;
        }

    }
    private void setUseDSTAsDSTButton(UseDST useDST) {
        this.useDST = useDST;
    }

    private class OnCheckedListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (binding.useDstDate.isChecked()) {
                setUseDSTAsDSTButton(UseDST.DATE);
            } else if (binding.useDstAlways.isChecked()) {
                setUseDSTAsDSTButton(UseDST.ALWAYS);
            } else if (binding.useDstNever.isChecked()) {
                setUseDSTAsDSTButton(UseDST.NEVER);
            }
            adapter.setUseDST(useDST);
            setTimeZoneAsDSTButton(timeZone, useDST);
        }
    }

    public void setTimeZone(TimeZone timeZone) {

        int offset = timeZone.getRawOffset();
        switch (useDST) {
            case DATE:
                if (timeZone.getID() == null) {
                    this.timeZone = timeZone;
                    break;
                }
                int dstOffset = TimeZone.getDSTOffset(
                        java.util.TimeZone.getTimeZone(timeZone.getID()),
                        year, month, day,
                        hour, minute, second
                );
                offset += dstOffset;
                this.timeZone = new TimeZone(timeZone, dstOffset != 0);
                break;
            case ALWAYS:
                offset += timeZone.getDST();
                this.timeZone = new TimeZone(timeZone, true);
                break;
            default:
                this.timeZone = new TimeZone(timeZone, false);
        }

        if (!Format.UTCOffsetTime(offset).equals(binding.numberEditText.getText().toString())) {
            binding.numberEditText.removeTextChangedListener(watcher);
            binding.numberEditText.setText(Format.UTCOffsetTime(offset));
            binding.numberEditText.addTextChangedListener(watcher);
        }
        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, this.timeZone);
        }

    }
    private void setTimeZoneAsDSTButton(TimeZone timeZone, UseDST useDST) {

        setUseDSTAsDSTButton(useDST);
        setTimeZone(timeZone);

    }
    private void setTimeZoneAsEditText(TimeZone timeZone) {

        this.timeZone = timeZone;

        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, timeZone);
        }

    }

    public void setTimeZoneAndUseDST(TimeZone timeZone, UseDST useDST) {

        setUseDST(useDST);
        setTimeZone(timeZone);

    }

    public void setOnTimeZoneChangedListener(OnTimeZoneChanged onTimeZoneChanged) {
        this.onTimeZoneChanged = onTimeZoneChanged;
    }
    public interface OnTimeZoneChanged {
        void onTimeZoneChanged(TimeZonePicker timeZonePicker, TimeZone timeZone);
    }

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        TimeZonePickerAdapter adapter = (TimeZonePickerAdapter) binding.textSuggestions.getAdapter();
        adapter.setDate(year, month, day);
    }

    public void setTime(int hour, int second, int minute) {
        this.hour = hour;
        this.second = second;
        this.minute = minute;
        TimeZonePickerAdapter adapter = (TimeZonePickerAdapter) binding.textSuggestions.getAdapter();
        adapter.setTime(hour, second, minute);
    }

    private static int[] parseTimes(String offset) {

        String[] times = offset.split(":",2);

        int hours = 0;
        int minutes = 0;
        try {
            hours = Integer.parseInt(times[0]);
            minutes = Integer.parseInt(times[1]);
        } catch (NumberFormatException e) {
        } catch (IndexOutOfBoundsException e) {
        }

        return new int[]{hours, minutes};

    }

    private class ShiftNumber implements View.OnClickListener {

        private final int shift;

        public ShiftNumber(int shift) {
            this.shift = shift;
        }

        @Override
        public void onClick(View v) {

            String text = binding.numberEditText.getText().toString();

            int[] times = parseTimes(text);

            times[0] += shift;

            setTimeZone(new TimeZone(
                times[0] * TimeZone.MILLISECONDS_IN_HOUR + times[1] * TimeZone.MILLISECONDS_IN_MINUTE
            ));

        }

    }

    private class RangeWatcher implements TextWatcher {

        private String textToSelection = "";
        private String textAfterSelection = "";

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            textToSelection = s.toString().substring(0,start);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textAfterSelection = s.toString().substring(start+count);
        }

        @Override
        public void afterTextChanged(Editable s) {

            String text = s.toString();

            if (!text.contains(":")) {

                int start = textToSelection.length();
                String newText = textToSelection+":"+textAfterSelection;
                binding.numberEditText.setText(newText);
                binding.numberEditText.setSelection(start);

            } else {

                int[] times = parseTimes(text);

                setTimeZoneAsEditText(new TimeZone(
                    times[0] * TimeZone.MILLISECONDS_IN_HOUR + times[1] * TimeZone.MILLISECONDS_IN_MINUTE
                ));

            }

        }
    }

    public enum UseDST {
        DATE,
        ALWAYS,
        NEVER
    }

}
