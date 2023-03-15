package com.icarus1.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.icarus1.R;

public class TimeZonePicker extends ConstraintLayout {

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

    private void init(Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.view_time_zone_picker, this, true);

    }

}
