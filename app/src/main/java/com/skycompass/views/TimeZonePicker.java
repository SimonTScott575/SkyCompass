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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeZonePicker extends LinearLayout {

    private ViewTimeZonePickerBinding binding;
    private Database db;

    private LocalDateTime dateTime = LocalDateTime.of(2000,1,1,0,0,0);
    private String zoneId;
    private int offset;

    private TimeZonePickerAdapter adapter;

    private OnTimeZoneChanged onTimeZoneChanged;

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

    private void init(@NonNull Context context) {

        setOrientation(VERTICAL);

        binding = ViewTimeZonePickerBinding.inflate(LayoutInflater.from(context), this, true);
        binding.numberEditText.setOnTimeZOneChanged(this::setTimeZoneAsEditText);

        adapter = new TimeZonePickerAdapter(context);
        adapter.setSelectTimeZoneListener(this::setTimeZone);
        binding.textSuggestions.setAdapter(adapter);
        binding.textSuggestions.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        binding.textSuggestions.addItemDecoration(dividerItemDecoration);

        binding.textSearch.addTextChangedListener(new SearchWatcher());

    }

    public void setDatabase(Database database) {
        db = database;
        adapter.setTimeZones(db.searchTimeZones(""));
    }

    public void setDate(@NonNull LocalDate date) {

        dateTime = LocalDateTime.of(date, dateTime.toLocalTime());

        TimeZonePickerAdapter adapter = (TimeZonePickerAdapter) binding.textSuggestions.getAdapter();
        adapter.setDate(date);

        if (zoneId != null) {
            setTimeZone(zoneId);
        } else {
            setTimeZone(offset);
        }

    }

    public void setTime(@NonNull LocalTime time) {

        dateTime = LocalDateTime.of(dateTime.toLocalDate(), time);

        TimeZonePickerAdapter adapter = (TimeZonePickerAdapter) binding.textSuggestions.getAdapter();
        adapter.setTime(time);

        if (zoneId != null) {
            setTimeZone(zoneId);
        } else {
            setTimeZone(offset);
        }

    }

    // Must have available ID.
    public void setTimeZone(@NonNull String id) {

        this.zoneId = id;
        offset = ZonedDateTime.of(dateTime, ZoneId.of(id)).getOffset().getTotalSeconds()*1000;

        adapter.setSelectedID(zoneId);
        binding.numberEditText.setOffset(offset);

        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, offset, id);
        }

    }

    public void setTimeZone(int offset) {

        this.zoneId = null;
        this.offset = offset;

        adapter.setSelectedID(null);
        binding.numberEditText.setOffset(offset);

        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, offset, null);
        }

    }

    public int getOffset() {

        if (zoneId == null) {
            return this.offset;
        } else {
            return ZonedDateTime.of(dateTime, ZoneId.of(zoneId)).getOffset().getTotalSeconds()*1000;
        }

    }

    public String getZoneId() {
        return zoneId;
    }

    private void setTimeZoneAsEditText(int hour, int minute) {

        this.zoneId = null;
        offset = hour*60*60 + minute*60;
        offset *= 1000;

        adapter.setSelectedID(null);

        if (onTimeZoneChanged != null) {
            onTimeZoneChanged.onTimeZoneChanged(this, offset, null);
        }

    }

    public void setOnTimeZoneChangedListener(OnTimeZoneChanged onTimeZoneChanged) {
        this.onTimeZoneChanged = onTimeZoneChanged;
    }

    public interface OnTimeZoneChanged {
        void onTimeZoneChanged(TimeZonePicker timeZonePicker, int offset, String id);
    }

    private class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (db != null) {
                adapter.setTimeZones(db.searchTimeZones(s.toString()));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

}
