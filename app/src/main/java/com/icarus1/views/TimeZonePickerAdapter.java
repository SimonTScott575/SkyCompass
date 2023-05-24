package com.icarus1.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.icarus1.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeZonePickerAdapter extends RecyclerView.Adapter<TimeZonePickerAdapter.ViewHolder> {

    private final ArrayList<String> timeZones;

    public TimeZonePickerAdapter() {
        timeZones = new ArrayList<>(0);
        timeZones.addAll(Arrays.asList(TimeZone.getAvailableIDs()));
    }

    public void addTimeZone(String name) {
        timeZones.add(name);
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
        holder.getTimeZoneName().setText(timeZones.get(position));
    }

    @Override
    public int getItemCount() {
        return timeZones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView timeZoneName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeZoneName = itemView.findViewById(R.id.time_zone_name);
        }

        public TextView getTimeZoneName() {
            return timeZoneName;
        }

    }

}
