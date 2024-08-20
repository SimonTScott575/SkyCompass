package com.skycompass.util;

import android.util.Log;

public class Debug {

    private static final boolean ENABLED = true;

    private enum LogType {
        LOG,
        WARN,
        ERROR
    }

    private Debug() {
    }

    public static void log(String msg) {
        if (ENABLED)
            post(LogType.LOG, msg);
    }

    public static void warn(String msg) {
        if (ENABLED)
            post(LogType.WARN, msg);
    }

    public static void error(String msg) {
        if (ENABLED)
            post(LogType.ERROR, msg);
    }

    private static void post(LogType type, String msg) {

        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];

        String fullClassName = stackTrace.getClassName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        int lineNumber = stackTrace.getLineNumber();

        String tag = String.format("Icarus/%s/%s", className, lineNumber);

        switch (type) {
            case LOG:
                Log.v(tag, msg);
                break;
            case ERROR:
                Log.e(tag, msg);
                break;
        }

    }

}