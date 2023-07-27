package com.skycompass.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.skycompass.R;
import com.skycompass.util.Format;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimeZonePickerAdapter extends RecyclerView.Adapter<TimeZonePickerAdapter.ViewHolder> {

    private final Context context;

    private String[] timeZones;
    private LocalDateTime dateTime = LocalDateTime.of(2000,1,1,0,0,0);

    private SelectTimeZoneListener selectTimeZoneListener;
    private int selected = -1;
    private String selectedID = "";

    private Color highlight = Color.valueOf(Color.parseColor("#f0c02e"));
    private int bgColorRedId;

    public TimeZonePickerAdapter(Context context) {

        this.context = context;
        timeZones = TimeZone.getAvailableIDs();

        if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            highlight = Color.valueOf(Color.parseColor("#414141"));
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        bgColorRedId = outValue.resourceId;

    }

    public void setSelectTimeZoneListener(SelectTimeZoneListener selectTimeZoneListener) {
        this.selectTimeZoneListener = selectTimeZoneListener;
    }

    public void setSelectedID(@Nullable String selectedID) {
        this.selectedID = (selectedID != null ? selectedID : "");
        this.selected = -1;
        notifyDataSetChanged();
    }

    public void setTimeZones(String[] timeZones) {
        this.timeZones = timeZones;
        selected = -1;
        notifyDataSetChanged();
    }

    public void setDate(LocalDate localDate) {
        dateTime = LocalDateTime.of(localDate, dateTime.toLocalTime());
        notifyDataSetChanged();
    }
    public void setTime(LocalTime time) {
        dateTime = LocalDateTime.of(dateTime.toLocalDate(), time);
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

        ZoneId zoneId = ZoneId.of(timeZones[position]);
        int offset = ZonedDateTime.of(dateTime, zoneId).getOffset().getTotalSeconds()*1000;
        String offsetText = "UTC" + Format.TimeZoneOffset(offset);

        holder.getTimeZoneOffset().setText(offsetText);
        holder.getTimeZoneName().setText(zoneId.getId());
        holder.setPos(position);
        holder.setId(zoneId.getId());

        if (position == selected || zoneId.getId().equals(selectedID)) {
            holder.itemView.setBackgroundColor(highlight.toArgb());
        } else {
            holder.itemView.setBackgroundResource(bgColorRedId);
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
        private String id = null;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeZoneName = itemView.findViewById(R.id.time_zone_name);
            timeZoneOffset = itemView.findViewById(R.id.time_zone_offset);

            itemView.setOnClickListener(v -> {
                if (selectTimeZoneListener != null) {
                    selectTimeZoneListener.onSelectTimeZone(id);
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

        public void setId(String id) {
            this.id = id;
        }

    }

    public interface SelectTimeZoneListener {
        void onSelectTimeZone(String id);
    }

}
