package com.icarus1.clock;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.icarus1.R;
import com.icarus1.databinding.ViewTimeZonePickerHorizontalBinding;
import com.icarus1.databinding.ViewTimeZonePickerVerticalBinding;

public class TimeZonePicker extends ConstraintLayout {

    private EditText numberEditText;

    private int UTCOffset;
    private String location;
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

        int orientation = 0;

        if (attrs != null) {

            try(
                TypedArray b = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.TimeZonePicker, 0, 0
                )
            ) {
                orientation = b.getInt(R.styleable.TimeZonePicker_orientation, 0);
            }

        }

        View plus;
        View minus;

        if (orientation == 1) {
            ViewTimeZonePickerVerticalBinding binding = ViewTimeZonePickerVerticalBinding.inflate(LayoutInflater.from(context), this, true);
            plus = binding.plus;
            minus = binding.minus;
            numberEditText = binding.numberEditText;
        } else {
            ViewTimeZonePickerHorizontalBinding binding = ViewTimeZonePickerHorizontalBinding.inflate(LayoutInflater.from(context), this, true);
            plus = binding.plus;
            minus = binding.minus;
            numberEditText = binding.numberEditText;
        }

        plus.setOnClickListener(new ShiftNumber(1));
        minus.setOnClickListener(new ShiftNumber(-1));
        numberEditText.addTextChangedListener(new RangeWatcher());
        numberEditText.setText("0");

    }

    public int getUTCOffset() {
        return UTCOffset;
    }

    public String getLocation() {
        return location;
    }

    public void setUTCOffset(int UTCOffset) {
        numberEditText.setText(String.valueOf(UTCOffset));
        location = null;
    }

    public void setOnUTCOffsetChanged(OnTimeZoneChanged onTimeZoneChanged) {
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
                n = Integer.parseInt(numberEditText.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }

            n += shift;
            if (n < -12) {
                n = 12;
            } else if (n > 12) {
                n = -12;
            }

            numberEditText.setText(String.valueOf(n));

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
                UTCOffset = Integer.parseInt(s.toString());
                if (onTimeZoneChanged != null) {
                    onTimeZoneChanged.onUTCOffsetChanged(TimeZonePicker.this, null, UTCOffset);
                }
            } catch (NumberFormatException e) {
            }

        }
    }

    public interface OnTimeZoneChanged {
        void onUTCOffsetChanged(TimeZonePicker timeZonePicker, String location, int UTCOffset);
    }

}
