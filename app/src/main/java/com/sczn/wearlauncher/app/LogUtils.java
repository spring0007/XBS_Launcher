package com.sczn.wearlauncher.app;

import android.util.Log;

/**
 * Log日志输出
 */
public class LogUtils {

    public static final String TAG = "lqq";
    private static final boolean DEBUG = true;

    public static void i(Object object, String string) {
        i(object.getClass().getSimpleName(), string);
    }

    public static void e(Object object, String string) {
        e(object.getClass().getSimpleName(), string);
    }

    public static void d(Object object, String string) {
        d(object.getClass().getSimpleName(), string);
    }

    public static void d(String tag, String string) {
        if (isDebug(tag)) {
            d(tag + ":" + string);
        }
    }

    public static void i(String tag, String string) {
        if (isDebug(tag)) {
            i(tag + ":" + string);
        }
    }

    public static void w(String tag, String string) {
        if (isDebug(tag)) {
            w(tag + ":" + string);
        }
    }

    public static void e(String tag, String string) {
        if (isDebug(tag)) {
            e(tag + ":" + string);
        }
    }

    private static void i(String info) {
        Log.i(TAG, info);
    }

    private static void d(String info) {
        Log.d(TAG, info);
    }

    private static void w(String info) {
        Log.w(TAG, info);
    }

    private static void e(String info) {
        Log.e(TAG, info);
    }

    private static boolean isDebug(String tag) {
       /* if (!DEBUG) {
            return false;
        }
        if ("dmm".equals(tag)) {
            return false;
        }*/
        return true;
    }
}
