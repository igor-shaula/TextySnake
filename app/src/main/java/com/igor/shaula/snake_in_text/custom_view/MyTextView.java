package com.igor.shaula.snake_in_text.custom_view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by igor shaula - to set custom font and maintain game field in this way \
 */
public class MyTextView extends TextView {

    public MyTextView(Context context) {
        super(context);
        applyMyFont(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyMyFont(context);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyMyFont(context);
    }

    private void applyMyFont(Context context) {
        Typeface typeface = MyFontCache.getTypeface(context);
//        Typeface typeface = MyFontCache.getTypeface("RubikMonoOne-Regular.ttf", context);
        setTypeface(typeface);
//        setTypeface(typeface, R.style.MyText_Main);
        setKeepScreenOn(true);
        setScrollContainer(false);
        setSingleLine(false);
        setTextIsSelectable(false);
    }
}