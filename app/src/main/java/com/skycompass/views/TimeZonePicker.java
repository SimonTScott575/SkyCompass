package com.skycompass.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.skycompass.database.Database;
import com.skycompass.databinding.ViewTimeZonePickerBinding;
import com.skycompass.util.TimeZone;

public class TimeZonePicker extends LinearLayout {

    private ViewTimeZonePickerBinding binding;
    private TimeZone timeZone;
    private Database db;

    private TimeZonePickerAdapter adapter;

    private OnTimeZoneChanged onTimeZoneChanged;

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

        setOrientation(VERTICAL);

        binding = ViewTimeZonePickerBinding.inflate(LayoutInflater.from(context), this, true);

        timeZone = new TimeZone(0);

        binding.numberEditText.setOnTimeZOneChanged((hour, minute) -> setTimeZoneAsEditText(new TimeZone(
            hour * TimeZone.MILLISECONDS_IN_HOUR + minute * TimeZone.MILLISECONDS_IN_MINUTE
        )));

        adapter = new TimeZonePickerAdapter(context);
        adapter.setSelectTimeZoneListener(id -> setTimeZone(new TimeZone(id, false)));
        binding.textSuggestions.setAdapter(adapter);
        binding.textSuggestions.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        binding.textSuggestions.addItemDecoration(dividerItemDecoration);

        binding.textSearch.addTextChangedListener(new SearchWatcher());

    }

    public void setDatabase(Database database) {
        db = database;
        adapter.setTimeZones(db.getTimeZones(""));
    }

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        TimeZonePickerAdapter adapter = (TimeZonePickerAdapter) binding.textSuggestions.getAdapter();
        adapter.setDate(year, month, day);
        setTimeZone(timeZone);
    }

    public void setTime(int hour, int second, int minute) {
        this.hour = hour;
        this.second = second;
        this.minute = minute;
        TimeZonePickerAdapter adapter = (TimeZonePickerAdapter) binding.textSuggestions.getAdapter();
        adapter.setTime(hour, second, minute);
        setTimeZone(timeZone);
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {

        int offset = timeZone.getRawOffset();

        if (timeZone.getID() == null) {
            this.timeZone = timeZone;
        } else {
            int dstOffset = TimeZone.getDSTOffset(
                java.util.TimeZone.getTimeZone(timeZone.getID()),
                year, month, day,
                hour, minute, second
            );
            offset += dstOffset;
            this.timeZone = new TimeZone(timeZone, dstOffset != 0);
        }

        adapter.setSelectedID(timeZone.getID());

        binding.numberEditText.setOffset(offset);

        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, this.timeZone);
        }

    }

    private void setTimeZoneAsEditText(TimeZone timeZone) {

        this.timeZone = timeZone;

        adapter.setSelectedID(timeZone.getID());

        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, timeZone);
        }

    }

    public void setOnTimeZoneChangedListener(OnTimeZoneChanged onTimeZoneChanged) {
        this.onTimeZoneChanged = onTimeZoneChanged;
    }

    public interface OnTimeZoneChanged {
        void onTimeZoneChanged(TimeZonePicker timeZonePicker, TimeZone timeZone);
    }

    private class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (db != null) {
                adapter.setTimeZones(db.getTimeZones(s.toString()));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

}
