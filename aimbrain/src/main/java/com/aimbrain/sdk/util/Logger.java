package com.aimbrain.sdk.util;

import android.util.Log;

public class Logger {

    public static final int DEBUG = Log.DEBUG;
    public static final int ERROR = Log.ERROR;
    public static final int INFO = Log.INFO;
    public static final int VERBOSE = Log.VERBOSE;
    public static final int WARN = Log.WARN;

    public static int LEVEL = Log.WARN;

    public static int v(String tag, String msg) {
        return LEVEL <= Log.VERBOSE ? Log.v(tag, msg) : 0;
    }

    public static int v(String tag, String msg, Throwable tr) {
        return LEVEL <= Log.VERBOSE ? Log.v(tag, msg, tr) : 0;
    }

    public static int d(String tag, String msg) {
        return LEVEL <= Log.DEBUG ? Log.d(tag, msg) : 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        return LEVEL <= Log.DEBUG ? Log.d(tag, msg, tr) : 0;
    }

    public static int i(String tag, String msg) {
        return LEVEL <= Log.INFO ? Log.i(tag, msg) : 0;
    }

    public static int i(String tag, String msg, Throwable tr) {
        return LEVEL <= Log.INFO ? Log.i(tag, msg, tr) : 0;
    }

    public static int w(String tag, String msg) {
        return LEVEL <= Log.WARN ? Log.w(tag, msg) : 0;
    }

    public static int w(String tag, String msg, Throwable tr) {
        return LEVEL <= Log.WARN ? Log.w(tag, msg, tr) : 0;
    }

    public static int w(String tag, Throwable tr) {
        return LEVEL <= Log.WARN ? Log.w(tag, tr) : 0;
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }
}