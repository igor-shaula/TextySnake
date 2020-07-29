package com.igor_shaula.texty_snake.v1.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by igor_shaula texty_snake - to set custom font and maintain game field in this way \
 */
public class MyTextView extends AppCompatTextView {

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
        setTypeface(typeface);
//        setTypeface(typeface, R.style.MyText_Main);
        setKeepScreenOn(true);
        setScrollContainer(false);
        setSingleLine(false);
        setTextIsSelectable(false);
    }

    public void setSquareSymbols() {
        // fixing the total height of the line in our main field \
        setLineSpacing(0.0f, 0.6f);
    }
}