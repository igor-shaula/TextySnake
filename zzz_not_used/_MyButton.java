package com.igor.shaula.snake_in_text.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

// Created by igor shaula - to set custom font and maintain game field in this way \

public class _MyButton extends Button {
    public _MyButton(Context context) {
        super(context);
        applyMyFont(context);
    }

    public _MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyMyFont(context);
    }

    public _MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyMyFont(context);
    }

    private void applyMyFont(Context context) {
        Typeface typeface = MyFontCache.getTypeface(context);
        setTypeface(typeface);
    }
}