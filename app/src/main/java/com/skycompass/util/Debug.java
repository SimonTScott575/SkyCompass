package com.skycompass.util;

import android.util.Log;

public class Debug {

    private static final boolean ENABLED = true;
    private static final boolean LOG_THROW_EXCEPTION = false;
    private static final boolean ERROR_THROW_EXCEPTION = true;

    private Debug() {
    }

    public static void log(String msg) {
        if (ENABLED) {
            postLog(new Debug.Exception(msg));
        }
    }

    public static void log(Exception e) {
        if (ENABLED) {
            postLog(e);
        }
    }

    public static void error(String msg) {
        if (ENABLED) {
            postError(new Debug.Exception(msg));
        }
    }

    public static void error(Exception e) {
        if (ENABLED) {
            postError(e);
        }
    }

    private static void postLog(Exception e) {
        Log.d("Icarus", e.getMessage());
        if (LOG_THROW_EXCEPTION) {
            throw new RuntimeException(e);
        }
    }

    private static void postError(Exception e) {
        Log.e("Icarus", e.getMessage());
        if (ERROR_THROW_EXCEPTION) {
            throw new RuntimeException(e);
        }
    }

    public static class Exception extends java.lang.Exception {
        public Exception(String msg) {
            super(msg);
        }
    }

}
