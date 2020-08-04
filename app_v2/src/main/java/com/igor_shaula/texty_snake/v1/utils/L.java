package com.igor_shaula.texty_snake.v1.utils;

import android.util.Log;

/**
 * Created by igor_shaula texty_snake - just a useful wrapper for system Log \
 */
public class L {

    private static final String TAG = "LOG";

    public static void l(String message) {
        System.out.println(message);
    }

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