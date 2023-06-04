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
import com.icarus1.util.TimeZone;

import java.util.Calendar;

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
        binding.numberEditText.setText("0");

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

    private void setTimeZoneWithoutNotification(TimeZone timeZone, UseDST useDST) {

        this.useDST = useDST;

        int offset = timeZone.getRawOffset();
        switch (useDST) {
            case DATE:
                if (timeZone.getID() == null) {
                    break;
                }
                int dstOffset = TimeZone.getDSTOffset(
                    java.util.TimeZone.getTimeZone(timeZone.getID()),
                    year, month, day,
                    hour, minute, second
                );
                offset += dstOffset;
                this.timeZone = new TimeZone(timeZone, dstOffset > 0);
                break;
            case ALWAYS:
                offset += timeZone.getDST();
                this.timeZone = new TimeZone(timeZone, true);
                break;
            default:
                this.timeZone = new TimeZone(timeZone, false);
        }

        binding.numberEditText.removeTextChangedListener(watcher);
        binding.numberEditText.setText(String.valueOf(offset/com.icarus1.util.TimeZone.MILLISECONDS_IN_HOUR));
        binding.numberEditText.addTextChangedListener(watcher);
        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, this.timeZone);
        }

    }
    public void setTimeZone(TimeZone timeZone, UseDST useDST) {

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

        int offset = timeZone.getRawOffset();
        switch (useDST) {
            case DATE:
                if (timeZone.getID() == null) {
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

        binding.numberEditText.removeTextChangedListener(watcher);
        binding.numberEditText.setText(String.valueOf(offset/com.icarus1.util.TimeZone.MILLISECONDS_IN_HOUR));
        binding.numberEditText.addTextChangedListener(watcher);
        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, timeZone);
        }

    }

    private class OnCheckedListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (binding.useDstDate.isChecked()) {
                useDST = UseDST.DATE;
            } else if (binding.useDstAlways.isChecked()) {
                useDST = UseDST.ALWAYS;
            } else if (binding.useDstNever.isChecked()) {
                useDST = UseDST.NEVER;
            }
            adapter.setUseDST(useDST);
            setTimeZoneWithoutNotification(timeZone, useDST);
        }
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

    private class ShiftNumber implements View.OnClickListener {

        private final int shift;

        public ShiftNumber(int shift) {
            this.shift = shift;
        }

        @Override
        public void onClick(View v) {

            int n;
            try {
                n = Integer.parseInt(binding.numberEditText.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }

            n += shift;
            if (n < -12) {
                n = 12;
            } else if (n > 12) {
                n = -12;
            }

            binding.numberEditText.setText(String.valueOf(n));

        }

    }

    private class RangeWatcher implements TextWatcher {

        private String prevText = "0";

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            prevText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            int value;

            try {
                value = Integer.parseInt(s.toString());
            } catch (NumberFormatException e) {
                return;
            }

            if (value < -12 || 12 < value) {

                int prevValue;

                try {
                    prevValue = Integer.parseInt(prevText);
                } catch (NumberFormatException e) {
                    return;
                }

                s.replace(0, s.length(), String.valueOf(prevValue));

            }

            try {
                timeZone = new TimeZone(Integer.parseInt(s.toString()) * 3600000);
                if (onTimeZoneChanged != null) {
                    onTimeZoneChanged.onTimeZoneChanged(TimeZonePicker.this, timeZone);
                }
            } catch (NumberFormatException e) {
            }

        }
    }

    public enum UseDST {
        DATE,
        ALWAYS,
        NEVER
    }

}
