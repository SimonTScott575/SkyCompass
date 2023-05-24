package com.icarus1.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.icarus1.databinding.ViewTimeZonePickerBinding;
import com.icarus1.util.TimeZone;

public class TimeZonePicker extends ConstraintLayout {

    private ViewTimeZonePickerBinding binding;
    private TimeZone timeZone;
    private OnTimeZoneChanged onTimeZoneChanged;

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

        timeZone = new TimeZone(0);

        binding = ViewTimeZonePickerBinding.inflate(LayoutInflater.from(context), this, true);

        binding.plus.setOnClickListener(new ShiftNumber(1));
        binding.minus.setOnClickListener(new ShiftNumber(-1));
        binding.numberEditText.addTextChangedListener(new RangeWatcher());
        binding.numberEditText.setText("0");

        TimeZonePickerAdapter adapter = new TimeZonePickerAdapter();
        binding.textSuggestions.setAdapter(adapter);
        binding.textSuggestions.setLayoutManager(new LinearLayoutManager(context));


    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        binding.numberEditText.setText(String.valueOf(timeZone.getUTCOffset()/3600000));
        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, timeZone);
        }
    }

    public void setOnTimeZoneChangedListener(OnTimeZoneChanged onTimeZoneChanged) {
        this.onTimeZoneChanged = onTimeZoneChanged;
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

    public interface OnTimeZoneChanged {
        void onTimeZoneChanged(TimeZonePicker timeZonePicker, TimeZone timeZone);
    }

}
