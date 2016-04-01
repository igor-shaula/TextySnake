package com.igor.shaula.snake_in_text.utils;

import android.util.Log;

/**
 * Created by igor shaula - just a useful wrapper for system Log \
 */
public class MyLog {

    private static final String TAG = "LOG";

    public static void v(String message) {
        Log.v(TAG, message);
    }

    public static void d(String message) {
        Log.d(TAG, message);
    }

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }
}