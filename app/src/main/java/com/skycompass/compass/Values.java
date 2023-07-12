package com.skycompass.compass;

import android.graphics.Color;

class Values {

    public static final String TEXT_N = "N";
    public static final String TEXT_S = "S";
    public static final String TEXT_E = "E";
    public static final String TEXT_W = "W";

    public static final Color TEXT_N_COLOR = Color.valueOf(Color.parseColor("#F55353"));
    public static final Color TEXT_SEW_COLOR = Color.valueOf(Color.parseColor("#000000"));
    public static final Color BACKGROUND_COLOR = Color.valueOf(Color.parseColor("#00000000"));
    public static final Color TRACK_COLOR = Color.valueOf(Color.parseColor("#000000"));
    public static final Color RING_COLOR = Color.valueOf(Color.parseColor("#000000"));

    public static final float FACE_FRACTION = 0.875f;
    public static final float RIM_FRACTION_OF_BORDER = 0.1f;

    public static final float TRACK_SPACE_FRACTION = 0.2f;
    public static final float TRACK_WIDTH_FRACTION = 0.03f;
    public static final float TRACK_MARKER_FRACTION = 0.075f;

    private Values() {
    }

}
