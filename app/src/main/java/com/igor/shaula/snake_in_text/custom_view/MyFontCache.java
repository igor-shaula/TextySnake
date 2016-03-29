package com.igor.shaula.snake_in_text.custom_view;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created from https://futurestud.io/blog/custom-fonts-on-android-extending-textview
 * <p/>
 * This caches the fonts while minimizing the number of accesses to the assets.
 */
public class MyFontCache {

    private static HashMap<String, Typeface> fontCache = new HashMap<>();

    private static final String FONT_NAME = "Anonymous_Pro.ttf";

    public static Typeface getTypeface(Context context) {
//    public static Typeface getTypeface(String FONT_NAME, Context context) {

        Typeface typeface = fontCache.get(FONT_NAME);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), FONT_NAME);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(FONT_NAME, typeface);
        }
        return typeface;
    }
}