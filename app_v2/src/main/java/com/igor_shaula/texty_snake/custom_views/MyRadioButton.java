package com.igor_shaula.texty_snake.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatRadioButton;

/**
 * Created by igor_shaula texty_snake - to set custom font and maintain game field in this way \
 */
public class MyRadioButton extends AppCompatRadioButton {
    public MyRadioButton(Context context) {
        super(context);
        applyMyFont(context);
    }

    public MyRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyMyFont(context);
    }

    public MyRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyMyFont(context);
    }

    private void applyMyFont(Context context) {
        Typeface typeface = MyFontCache.getTypeface(context);
        setTypeface(typeface);
    }
}