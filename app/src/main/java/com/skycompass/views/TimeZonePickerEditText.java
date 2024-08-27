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
import com.skycompass.util.Debug;
import com.skycompass.util.Format;

public class TimeZonePickerEditText extends LinearLayoutCompat {

    private ViewTimeZonePickerEditTextBinding binding;
    private OnTimeZoneChanged onTimeZoneChanged;
    private OffsetWatcher offsetWatcher;

    private static final int MILLISECONDS_IN_MINUTE = 60*1000;
    private static final int MILLISECONDS_IN_HOUR = 60*MILLISECONDS_IN_MINUTE;

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

        if (!Format.TimeZoneOffset(offset).equals(binding.numberEditText.getText().toString())) {
            binding.numberEditText.removeTextChangedListener(offsetWatcher);
            binding.numberEditText.setText(Format.TimeZoneOffset(offset));
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

            if (times == null) {
                Debug.warn(String.format("Invalid time: %s", text));
                return;
            }

            int hours = times[0];
            int minutes = times[1];

            hours += shift;

            int offset = hours * MILLISECONDS_IN_HOUR + (hours >= 0 ? 1 : -1)  * minutes * MILLISECONDS_IN_MINUTE;

            text = Format.TimeZoneOffset(offset);

            binding.numberEditText.setText(text);

        }

    }

    private class OffsetWatcher implements TextWatcher {

        private String textPrevious = "+00:00";
        private int selectionPrevious = 0;
        private int previousHours = 0;
        private int previousMinutes = 0;
        private boolean first = true;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            if (parseTimes(s.toString()) != null) {
                textPrevious = s.toString();
                selectionPrevious = start;
            }

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {

            int[] times = parseTimes(s.toString());

            if (times == null) {

                binding.numberEditText.setText(textPrevious);
                binding.numberEditText.setSelection(selectionPrevious);

                return;
            }


            int hours = times[0];
            int minutes = times[1];

            Debug.log(String.format("Hours: %s Minutes: %s", hours, minutes));

            if (first || hours != previousHours || minutes != previousMinutes) {

                previousHours = hours;
                previousMinutes = minutes;
                first = false;

                if (onTimeZoneChanged != null)
                    onTimeZoneChanged.onTimeZoneChanged(hours, minutes);

            }

        }
    }

    private static int[] parseTimes(String input) {

        if (!input.contains(":"))
            return null;

        String[] parts = input.split(":");

        String first = "";
        String second = "";

        if (parts.length >= 1)
            first = parts[0];
        if (parts.length >= 2)
            second = parts[1];

        int hours = 0;
        int minutes = 0;

        try {

            hours = first.length() > 0 && !(first.equals("-") || first.equals("+")) ? Integer.parseInt(first) : 0;
            minutes = second.length() > 0 ? Integer.parseInt(second) : 0;

            if (Math.abs(hours) > 18 || minutes < 0 || 59 < minutes || (Math.abs(hours) == 18 && minutes != 0))
                return null;

        } catch (NumberFormatException e) {
            return null;
        }

        return new int[]{hours, minutes};

    }

}
