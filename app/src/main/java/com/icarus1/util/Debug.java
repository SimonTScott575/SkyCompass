package com.icarus1.util;

import android.util.Log;

public class Debug {

    private Debug() {
    }

    public static void log(String msg) {
        Log.d("Icarus", msg);
    }

    public static void error(String msg) {
        Log.e("Icarus", msg);
    }

}
