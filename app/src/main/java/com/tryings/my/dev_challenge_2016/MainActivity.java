package com.tryings.my.dev_challenge_2016;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private char food = '$'; // +1 to length
    private char foodBonus = '^'; // +2 to length
    private char foodMinus = '*'; // -1 from length
    private char foodSpeedSlow = '-'; // slows by 25%
    private char foodSpeedUp = '+'; // speeds up by 25%
    private Snake snake;

    private int fieldPixelWidth, fieldPixelHeight; // in pixels
    private int mainFieldSymbolsInLine, mainFieldLineCount; // in items - for arrays \

    private ArrayList<char[]> mainCharArrayList;

    private AppCompatTextView actvMainField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actvMainField = (AppCompatTextView) findViewById(R.id.tvMainField);

        assert actvMainField != null;
        actvMainField.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        // getting current screen size in pixels \
                        fieldPixelWidth = actvMainField.getWidth();
                        MyLog.d("fieldPixelWidth " + fieldPixelWidth);

                        FrameLayout flMain = (FrameLayout) findViewById(R.id.flMain);
                        assert flMain != null;
                        fieldPixelHeight = flMain.getHeight();
//                        fieldPixelHeight = actvMainField.getHeight();
                        MyLog.d("fieldPixelHeight " + fieldPixelHeight);

                        // now removing the listener - it's not needed any more \
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                            actvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        else
                            actvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        // moving on to separate method \
                        prepareTextField();
                    }
                });

        // TODO: 20.03.2016 make method to set food and bonuses initially on screen \

        // TODO: 20.03.2016 make method to randomly choose initial direction \

        // TODO: 20.03.2016 make method to add the snake to the field \
    }

    private void prepareTextField() {

        // here we get pixel width of a single symbol - initial TextView has only one symbol \
        actvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measuredSymbolWidth = actvMainField.getMeasuredWidth();
        MyLog.i("measuredSymbolWidth " + measuredSymbolWidth);

        // getting our first valuable parameter - the size of main array \
        mainFieldSymbolsInLine = fieldPixelWidth / measuredSymbolWidth;
        MyLog.i("mainFieldSymbolsInLine " + mainFieldSymbolsInLine);

        // clearing the text field to properly initialize it for game \
        actvMainField.setText(null);

        // now preparing our model and initializing text field \
        int i = 0, measuredTextHeight;
        mainCharArrayList = new ArrayList<>();
        do {
            // filling up out TextView to measure its lines and set initial state at once \
            char[] charArray = new char[mainFieldSymbolsInLine + 1];
            // setting end element for a new line on the next array \
            charArray[mainFieldSymbolsInLine] = '\n';
            // setting other elements to their default values \
            for (int j = 0; j < mainFieldSymbolsInLine; j++) {
                charArray[j] = '+';
            }
            // now single char array is ready and has to be added to the list \
            mainCharArrayList.add(i, charArray);
            MyLog.i("added " + new String(mainCharArrayList.get(i)));

            String previousText = actvMainField.getText().toString();
            String newText = previousText + new String(mainCharArrayList.get(i));
            actvMainField.setText(newText);
            MyLog.i("newText \n" + newText);

            i++;

            actvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measuredTextHeight = actvMainField.getMeasuredHeight();
            MyLog.i("measuredTextHeight " + measuredTextHeight);

        } while (measuredTextHeight <= fieldPixelHeight);

        mainFieldLineCount = i;
        MyLog.i("mainFieldLineCount " + mainFieldLineCount);
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