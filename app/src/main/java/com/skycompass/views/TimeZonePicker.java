package com.skycompass.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skycompass.R;
import com.skycompass.databinding.ViewTimeZonePickerBinding;
import com.skycompass.util.Format;

public class TimeZonePicker extends LinearLayout {

    private ViewTimeZonePickerBinding binding;

    private String selectedTimeZoneName;
    private int selectedTimeZoneOffsetMilliseconds;

    private TimeZoneListAdapter listAdapter;
    private TimeZoneAdapter timeZoneAdapter;

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
        binding.numberEditText.setOnTimeZOneChanged(this::onTimeZoneChangedEditText);

        listAdapter = new TimeZoneListAdapter(context);

        binding.textSuggestions.setAdapter(listAdapter);
        binding.textSuggestions.setLayoutManager(new LinearLayoutManager(context));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        binding.textSuggestions.addItemDecoration(dividerItemDecoration);

        binding.textSearch.addTextChangedListener(new SearchWatcher());

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

    private void onTimeZoneChangedEditText(int hour, int minute) {

        selectedTimeZoneName = null;
        selectedTimeZoneOffsetMilliseconds = hour * 60 * 60 + minute * 60;
        selectedTimeZoneOffsetMilliseconds *= 1000;

        if (onTimeZoneChanged != null)
            onTimeZoneChanged.onTimeZoneChanged(this, selectedTimeZoneOffsetMilliseconds, null);

    }

    private class TimeZoneListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Color highlight = Color.valueOf(Color.parseColor("#f0c02e"));
        private int bgColorRedId;

        public TimeZoneListAdapter(Context context) {

            if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                highlight = Color.valueOf(Color.parseColor("#414141"));
            }
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            bgColorRedId = outValue.resourceId;

        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_time_zone_picker_item, parent, false);

            ViewHolder holder = new ViewHolder(view);

            holder.highlight = highlight;

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            TimeZone meta = new TimeZone();

            if (timeZoneAdapter != null)
                timeZoneAdapter.getTimeZone(position, meta);

            String name = meta.name;
            int offset = meta.offsetMilliseconds;

            String offsetText = "UTC" + Format.TimeZoneOffset(offset);

            holder.timeZoneOffsetTextView.setText(offsetText);
            holder.timeZoneNameTextView.setText(name);
            holder.timeZoneOffset = offset;
            holder.timeZoneName = name;

            if (name.equals(selectedTimeZoneName))
                holder.itemView.setBackgroundColor(highlight.toArgb());
            else
                holder.itemView.setBackgroundResource(bgColorRedId);

        }

        @Override
        public int getItemCount() {
            return timeZoneAdapter != null ? timeZoneAdapter.getTimeZoneCount() : 0;
        }

    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public String timeZoneName = null;
        public int timeZoneOffset = 0;
        private final TextView timeZoneNameTextView;
        private final TextView timeZoneOffsetTextView;
        private Color highlight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeZoneNameTextView = itemView.findViewById(R.id.time_zone_name);
            timeZoneOffsetTextView = itemView.findViewById(R.id.time_zone_offset);

            itemView.setOnClickListener(v -> {

                selectedTimeZoneName = timeZoneName;
                selectedTimeZoneOffsetMilliseconds = timeZoneOffset;

                binding.numberEditText.setOffset(selectedTimeZoneOffsetMilliseconds);

                itemView.setBackgroundColor(highlight.toArgb());

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
            // TODO
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

        void getTimeZone(int position, TimeZone meta);

        int getTimeZoneCount();

    }

}
