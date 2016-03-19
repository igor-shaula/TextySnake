package com.tryings.my.dev_challenge_2016;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewTreeObserver;

public class MainActivity extends AppCompatActivity {

    private char food = '$';
    private char foodBonus = '^';
    private char foodSpeedSlow = '-';
    private char foodSpeedUp = '+';
    private Snake snake;

    private int mainFieldWidth, mainFieldHeight;
    private int mainFieldSymbolsInLine, mainFieldLineCount;

    private AppCompatTextView tvMainField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMainField = (AppCompatTextView) findViewById(R.id.tvMainField);

        // 1 via getViewTreeObserver().addOnGlobalLayoutListener - works
        tvMainField.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mainFieldWidth = tvMainField.getWidth();
                        MyLog.d("mainFieldWidth " + mainFieldWidth);
                        mainFieldHeight = tvMainField.getHeight();
                        MyLog.d("mainFieldHeight " + mainFieldHeight);
                        // now removing the listener - it's not needed any more \
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            tvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            tvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // task 1 - get exact width and height of the main text field - not in pixels, but in symbols \

        // 2 - here we get pixel sizes of a single symbol \
        tvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measuredWidth = tvMainField.getMeasuredWidth();
        int measuredHeight = tvMainField.getMeasuredHeight();
        MyLog.i("measuredWidth " + measuredWidth);
        MyLog.i("measuredHeight " + measuredHeight);

        mainFieldSymbolsInLine = mainFieldWidth / measuredWidth;
        MyLog.i("mainFieldSymbolsInLine " + mainFieldSymbolsInLine);


    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }
}