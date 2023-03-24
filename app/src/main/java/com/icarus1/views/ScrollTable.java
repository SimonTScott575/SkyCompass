package com.icarus1.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.Nullable;

import com.google.android.material.divider.MaterialDivider;
import com.icarus1.R;

public class ScrollTable extends RelativeLayout {

    MaterialDivider materialDivider;
    TableLayout tableLayout;

    public ScrollTable(Context context) {
        super(context);
        init(context);
    }

    public ScrollTable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollTable(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ScrollTable(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        LayoutInflater.from(context).inflate(R.layout.view_scroll_table, this, true);

        addView(materialDivider = new MaterialDivider(context));
        materialDivider.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        tableLayout = findViewById(R.id.bodies_table);

    }

    public void setHeader(int layoutID) {

        View header = LayoutInflater.from(getContext()).inflate(layoutID, tableLayout, false);
        tableLayout.addView(header,0);
        tableLayout.removeView(header);
        addView(header, getChildCount());

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(BELOW, header.getId());
        materialDivider.setLayoutParams(params2);

//        invalidate();

        View v = LayoutInflater.from(getContext()).inflate(layoutID, tableLayout, false);
//        v.setScaleY(0);
        tableLayout.addView(v,0);

/*        addView(view, getChildCount());

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(BELOW, view.getId());
        materialDivider.setLayoutParams(params2);

        invalidate();*/
    }

}
