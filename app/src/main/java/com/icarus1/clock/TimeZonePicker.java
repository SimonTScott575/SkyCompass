package com.icarus1.clock;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.icarus1.databinding.ViewTimeZonePickerBinding;

public class TimeZonePicker extends ConstraintLayout {

    private ViewTimeZonePickerBinding binding;
    private int UTCOffset;
    private String location;
    private onTimeZoneChanged onTimeZoneChanged;

    public TimeZonePicker(@NonNull Context context) {
        super(context);
        init(context);
    }

    public TimeZonePicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeZonePicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        binding = ViewTimeZonePickerBinding.inflate(LayoutInflater.from(context), this, true);

        ShiftNumber addOne = new ShiftNumber(1);
        ShiftNumber subOne = new ShiftNumber(-1);

        binding.plus.setOnClickListener(addOne);
        binding.minus.setOnClickListener(subOne);

        RangeWatcher rangeWatcher = new RangeWatcher();

        binding.numberEditText.addTextChangedListener(rangeWatcher);
        binding.numberEditText.setText("0");

    }

    public int getUTCOffset() {
        return UTCOffset;
    }

    public String getLocation() {
        return location;
    }

    public void setUTCOffset(int UTCOffset) {
        binding.numberEditText.setText(String.valueOf(UTCOffset));
        location = null;
    }

    public void setOnUTCOffsetChanged(onTimeZoneChanged onTimeZoneChanged) {
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
                UTCOffset = Integer.parseInt(s.toString());
                if (onTimeZoneChanged != null) {
                    onTimeZoneChanged.onUTCOffsetChanged(UTCOffset, null);
                }
            } catch (NumberFormatException e) {
            }

        }
    }

    public interface onTimeZoneChanged {
        void onUTCOffsetChanged(int UTCOffset, String location);
    }

}
