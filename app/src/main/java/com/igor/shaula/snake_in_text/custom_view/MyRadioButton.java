package com.igor.shaula.snake_in_text.custom_view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by igor shaula - to set custom font and maintain game field in this way \
 */
public class MyRadioButton extends RadioButton {
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