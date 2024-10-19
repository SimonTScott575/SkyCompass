package com.skycompass.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skycompass.R;
import com.skycompass.databinding.ViewTimeZonePickerBinding;
import com.skycompass.util.Debug;
import com.skycompass.util.Format;

import java.time.ZoneId;

public class TimeZonePicker extends LinearLayout {

    private Color colorHighlight = Color.valueOf(Color.parseColor("#FF00FF"));

    private ViewTimeZonePickerBinding binding;

    private TimeZoneListAdapter listAdapter;
    private TimeZoneAdapter timeZoneAdapter;

    private OnTimeZoneChanged onTimeZoneChanged;

    private String selectedTimeZoneName;
    private int selectedTimeZoneOffsetMilliseconds;

    private String searchText;

    public TimeZonePicker(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public TimeZonePicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TimeZonePicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeZonePicker, defStyleAttr, 0);

        try {

            if (attributes.hasValue(R.styleable.TimeZonePicker_colorHighlight)) {

                colorHighlight = Color.valueOf(attributes.getColor(
                    R.styleable.TimeZonePicker_colorHighlight,
                    colorHighlight.toArgb()
                ));
            } else {

                TypedValue highlightValue = new TypedValue();

                context.getTheme().resolveAttribute(
                    android.R.attr.colorControlHighlight,
                    highlightValue,
                    true
                );

                colorHighlight = Color.valueOf(highlightValue.data);

            }


        } catch (UnsupportedOperationException e) {

            Debug.warn("Unsupported attribute value: colorHighlight");

        } finally {

            attributes.recycle();

        }

        setOrientation(VERTICAL);

        binding = ViewTimeZonePickerBinding.inflate(LayoutInflater.from(context), this, true);
        binding.numberEditText.setOnTimeZOneChanged(this::onTimeZoneChangedEditText);

        listAdapter = new TimeZoneListAdapter(context);

        binding.textSuggestions.setAdapter(listAdapter);
        binding.textSuggestions.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        binding.textSuggestions.addItemDecoration(dividerItemDecoration);

        binding.textSearch.addTextChangedListener(new SearchWatcher());

        binding.textSearch.setAdapter(new ArrayAdapter<String>(
            getContext(),
            android.R.layout.simple_list_item_1,
            ZoneId.getAvailableZoneIds().toArray(new String[0])
        ));

        searchText = "";

    }

    public void setTimeZone(String name, int offset) {

        selectedTimeZoneName = name;
        selectedTimeZoneOffsetMilliseconds = offset;

        binding.numberEditText.setOffset(selectedTimeZoneOffsetMilliseconds);

        notifyDataSetChanged();

        if (onTimeZoneChanged != null)
            onTimeZoneChanged.onTimeZoneChanged(this, selectedTimeZoneOffsetMilliseconds, name);

    }

    public void setOnTimeZoneChangedListener(OnTimeZoneChanged onTimeZoneChanged) {
        this.onTimeZoneChanged = onTimeZoneChanged;
    }

    public void setTimeZoneAdapter(TimeZoneAdapter onDataSetChanged) {
        this.timeZoneAdapter = onDataSetChanged;
    }

    public void notifyDataSetChanged() {
        listAdapter.notifyDataSetChanged();
    }

    public String getSearchText() {
        return searchText;
    }

    private void onTimeZoneChangedEditText(int hour, int minute) {

        selectedTimeZoneName = null;
        selectedTimeZoneOffsetMilliseconds = hour * 60 * 60 + minute * 60;
        selectedTimeZoneOffsetMilliseconds *= 1000;

        notifyDataSetChanged();

        if (onTimeZoneChanged != null)
            onTimeZoneChanged.onTimeZoneChanged(this, selectedTimeZoneOffsetMilliseconds, null);

    }

    private class TimeZoneListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int selectableItemBackgroundResourceId;

        public TimeZoneListAdapter(Context context) {

            TypedValue selectableItemBackgroundResource = new TypedValue();

            context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                selectableItemBackgroundResource,
                true
            );

            selectableItemBackgroundResourceId = selectableItemBackgroundResource.resourceId;

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_time_zone_picker_item, parent, false);

            ViewHolder holder = new ViewHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            TimeZone meta = new TimeZone();

            if (timeZoneAdapter != null)
                timeZoneAdapter.getTimeZone(TimeZonePicker.this, position, meta);

            String name = meta.name;
            int offset = meta.offsetMilliseconds;

            String offsetText = "UTC" + Format.TimeZoneOffset(offset);

            holder.timeZoneOffsetTextView.setText(offsetText);
            holder.timeZoneNameTextView.setText(name);
            holder.timeZoneOffset = offset;
            holder.timeZoneName = name;

            if (name.equals(selectedTimeZoneName))
                holder.itemView.setBackgroundColor(colorHighlight.toArgb());
            else
                holder.itemView.setBackgroundResource(selectableItemBackgroundResourceId);

        }

        @Override
        public int getItemCount() {
            return timeZoneAdapter != null ? timeZoneAdapter.getTimeZoneCount(TimeZonePicker.this) : 0;
        }

    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public String timeZoneName = null;
        public int timeZoneOffset = 0;

        private final TextView timeZoneNameTextView;
        private final TextView timeZoneOffsetTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeZoneNameTextView = itemView.findViewById(R.id.time_zone_name);
            timeZoneOffsetTextView = itemView.findViewById(R.id.time_zone_offset);

            itemView.setOnClickListener(v -> {

                selectedTimeZoneName = timeZoneName;
                selectedTimeZoneOffsetMilliseconds = timeZoneOffset;

                binding.numberEditText.setOffset(selectedTimeZoneOffsetMilliseconds);

                itemView.setBackgroundColor(colorHighlight.toArgb());

                RecyclerView.Adapter adapter = binding.textSuggestions.getAdapter();

                if (adapter == null)
                    throw new RuntimeException("Adapter null.");

                int position = getAbsoluteAdapterPosition();
                int count = binding.textSuggestions.getAdapter().getItemCount();

                adapter.notifyItemRangeChanged(0, position);
                adapter.notifyItemRangeChanged(position + 1, count - position - 1);

                if (onTimeZoneChanged != null)
                    onTimeZoneChanged.onTimeZoneChanged(TimeZonePicker.this, selectedTimeZoneOffsetMilliseconds, timeZoneName);

            });

        }

    }

    private class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchText = s.toString();
            notifyDataSetChanged();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

    public interface OnTimeZoneChanged {
        void onTimeZoneChanged(TimeZonePicker timeZonePicker, int offset, String id);
    }

    public static class TimeZone {

        public String name;
        public int offsetMilliseconds;

    }

    public interface TimeZoneAdapter {

        void getTimeZone(TimeZonePicker picker, int position, TimeZone meta);

        int getTimeZoneCount(TimeZonePicker picker);

    }

}
