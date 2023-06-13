package com.skycompass.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skycompass.R;
import com.skycompass.settings.SavedSettings;
import com.skycompass.util.Format;

import java.util.TimeZone;

public class TimeZonePickerAdapter extends RecyclerView.Adapter<TimeZonePickerAdapter.ViewHolder> {
    //TODO background color when selected should respond to Day/Night theme and fit with rest of app.

    private final Context context;

    private String[] timeZones;
    private int year = 1970, month = 0, day = 0;
    private int hour = 12, minute = 0, second = 0;
    private TimeZonePicker.UseDST useDST = TimeZonePicker.UseDST.NEVER;

    private SelectTimeZoneListener selectTimeZoneListener;
    private int selected = -1;
    private String selectedID = "";

    private Color highlight = Color.valueOf(Color.parseColor("#f0c02e"));

    public TimeZonePickerAdapter(Context context) {
        this.context = context;
        timeZones = TimeZone.getAvailableIDs();

        if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            highlight = Color.valueOf(Color.parseColor("#414141"));
        }

    }

    public void setSelectTimeZoneListener(SelectTimeZoneListener selectTimeZoneListener) {
        this.selectTimeZoneListener = selectTimeZoneListener;
    }

    public void setSelectedID(String selectedID) {
        this.selectedID = (selectedID != null ? selectedID : "");
        this.selected = -1;
        notifyDataSetChanged();
    }

    public void setTimeZones(String[] timeZones) {
        this.timeZones = timeZones;
        selected = -1;
        notifyDataSetChanged();
    }

    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        notifyDataSetChanged();
    }
    public void setTime(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        notifyDataSetChanged();
    }

    public void setUseDST(TimeZonePicker.UseDST useDST) {
        this.useDST = useDST;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_time_zone_picker_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TimeZone timeZone = TimeZone.getTimeZone(timeZones[position]);
        int offset = timeZone.getRawOffset();
        switch (useDST) {
            case ALWAYS:
                offset += timeZone.getDSTSavings();
                break;
            case DATE:
                offset += com.skycompass.util.TimeZone.getDSTOffset(
                    timeZone,
                    year, month, day,
                    hour, minute, second
                );
                break;
        }

        holder.getTimeZoneName().setText(timeZone.getID());
        holder.getTimeZoneOffset().setText(Format.UTCOffset(offset));
        holder.setPos(position);
        if (position == selected || timeZone.getID().equals(selectedID)) {
            holder.itemView.setBackgroundColor(highlight.toArgb());
        } else {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.itemView.setBackgroundResource(outValue.resourceId);
        }

    }

    @Override
    public int getItemCount() {
        return timeZones.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView timeZoneName;
        private final TextView timeZoneOffset;
        private int pos = -1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeZoneName = itemView.findViewById(R.id.time_zone_name);
            timeZoneOffset = itemView.findViewById(R.id.time_zone_offset);

            itemView.setOnClickListener(v -> {
                if (selectTimeZoneListener != null) {
                    selectTimeZoneListener.onSelectTimeZone(timeZoneName.getText().toString());
                }
                selected = pos;
                selectedID = timeZones[selected];
            });

        }

        public TextView getTimeZoneName() {
            return timeZoneName;
        }

        public TextView getTimeZoneOffset() {
            return timeZoneOffset;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public int getPos() {
            return pos;
        }

    }

    public interface SelectTimeZoneListener {
        void onSelectTimeZone(String id);
    }

}
