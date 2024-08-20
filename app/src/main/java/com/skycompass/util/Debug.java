package com.skycompass.util;

import android.util.Log;

public class Debug {

    private static final boolean ENABLED = true;

    private enum LogType {
        LOG,
        ERROR
    }

    private Debug() {
    }

    public static void log(String msg) {
        if (ENABLED)
            post(LogType.LOG, msg);
    }

    public static void log(Debug.Exception e) {
        if (ENABLED)
            post(LogType.LOG, e.getMessage());
    }

    public static void error(String msg) {
        if (ENABLED)
            post(LogType.ERROR, msg);
    }

    public static void error(Debug.Exception e) {
        if (ENABLED)
            post(LogType.ERROR, e.getMessage());
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

    public static class Exception extends java.lang.Exception {
        public Exception(String msg) {
            super(msg);
        }
    }

}
