package com.skycompass.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.skycompass.databinding.ViewTimeZonePickerEditTextBinding;
import com.skycompass.util.Format;
import com.skycompass.util.TimeZone;

public class TimeZonePickerEditText extends LinearLayoutCompat {

    private ViewTimeZonePickerEditTextBinding binding;
    private OnTimeZoneChanged onTimeZoneChanged;
    private OffsetWatcher offsetWatcher;

    public TimeZonePickerEditText(Context context) {
        super(context);
        init(context);
    }

    public TimeZonePickerEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeZonePickerEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        binding = ViewTimeZonePickerEditTextBinding.inflate(LayoutInflater.from(context), this, true);

        binding.plus.setOnClickListener(new ShiftNumber(1));
        binding.minus.setOnClickListener(new ShiftNumber(-1));
        binding.numberEditText.addTextChangedListener(offsetWatcher = new OffsetWatcher());
        binding.numberEditText.setText("00:00");

    }

    public void setOffset(int offset) {

        if (!Format.UTCOffsetTime(offset).equals(binding.numberEditText.getText().toString())) {
            binding.numberEditText.removeTextChangedListener(offsetWatcher);
            binding.numberEditText.setText(Format.UTCOffsetTime(offset));
            binding.numberEditText.addTextChangedListener(offsetWatcher);
        }

    }

    public void setOnTimeZOneChanged(OnTimeZoneChanged onTimeZoneChanged) {
        this.onTimeZoneChanged = onTimeZoneChanged;
    }

    public interface OnTimeZoneChanged {
        void onTimeZoneChanged(int hour, int minute);
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
            int offset = times[0] * TimeZone.MILLISECONDS_IN_HOUR + times[1] * TimeZone.MILLISECONDS_IN_MINUTE;

            binding.numberEditText.setText(Format.UTCOffsetTime(offset));

        }

    }

    private class OffsetWatcher implements TextWatcher {

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

                if (onTimeZoneChanged != null) {
                    onTimeZoneChanged.onTimeZoneChanged(times[0], times[1]);
                }

            }

        }
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

}
