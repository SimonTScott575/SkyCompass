package com.icarus1.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.icarus1.R;
import com.icarus1.database.Database;
import com.icarus1.util.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TimeZonePickerAdapter extends RecyclerView.Adapter<TimeZonePickerAdapter.ViewHolder> {

    private String[] timeZones;
    private int year = 1970, month = 0, day = 0;
    private int hour = 12, minute = 0, second = 0;
    private TimeZonePicker.UseDST useDST = TimeZonePicker.UseDST.NEVER;

    public TimeZonePickerAdapter() {
        timeZones = TimeZone.getAvailableIDs();
    }

    public void setTimeZones(String[] timeZones) {
        this.timeZones = timeZones;
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
                offset += com.icarus1.util.TimeZone.getDSTOffset(
                    timeZone,
                    year, month, day,
                    hour, minute, second
                );
                break;
        }

        holder.getTimeZoneName().setText(timeZone.getID());
        holder.getTimeZoneOffset().setText(Format.UTCOffset(offset));

    }

    @Override
    public int getItemCount() {
        return timeZones.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView timeZoneName;
        private final TextView timeZoneOffset;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeZoneName = itemView.findViewById(R.id.time_zone_name);
            timeZoneOffset = itemView.findViewById(R.id.time_zone_offset);
        }

        public TextView getTimeZoneName() {
            return timeZoneName;
        }

        public TextView getTimeZoneOffset() {
            return timeZoneOffset;
        }
    }

}
