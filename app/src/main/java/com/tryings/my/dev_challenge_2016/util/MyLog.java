package com.tryings.my.dev_challenge_2016.util;

import android.util.Log;

/**
 * just a useful wrapper for system Log \
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