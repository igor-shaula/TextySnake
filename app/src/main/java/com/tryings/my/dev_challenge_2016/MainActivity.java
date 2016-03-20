package com.tryings.my.dev_challenge_2016;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private final char SNAKE = '@';
    private final char SPACE = ' ';
    private final char FOOD = '$'; // +1 to length
    private final char LENGTH_PLUS = '^'; // +2 to length
    private final char LENGTH_MINUS = '*'; // -1 from length
    private final char SPEED_SLOW = '-'; // slows by 25%
    private final char SPEED_UP = '+'; // speeds up by 25%

    private Snake snake;

    private int fieldPixelWidth, fieldPixelHeight; // in pixels
    private int symbolsInFieldLine, fieldLinesCount; // in items - for arrays \

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

                        // moving everything to separate methods \
                        prepareTextField();

                        setInitialSnake();
                        setInitialFood();
                    }
                });

        // TODO: 20.03.2016 make method to randomly choose initial direction \

        // TODO: 20.03.2016 make method to add the snake to the field \
    }

    private void prepareTextField() {

        // here we get pixel width of a single symbol - initial TextView has only one symbol \
        actvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measuredSymbolWidth = actvMainField.getMeasuredWidth();
        MyLog.i("measuredSymbolWidth " + measuredSymbolWidth);

        // getting our first valuable parameter - the size of main array \
        symbolsInFieldLine = fieldPixelWidth / measuredSymbolWidth;
        MyLog.i("symbolsInFieldLine " + symbolsInFieldLine);

        // clearing the text field to properly initialize it for game \
        actvMainField.setText(null);

        // now preparing our model and initializing text field \
        int i = 0, measuredTextHeight;
        mainCharArrayList = new ArrayList<>();
        do {
            // filling up out TextView to measure its lines and set initial state at once \
            char[] charArray = new char[symbolsInFieldLine + 1];
            // setting end element for a new line on the next array \
            charArray[symbolsInFieldLine] = '\n';
            // setting other elements to their default values \
            for (int j = 0; j < symbolsInFieldLine; j++) {
                charArray[j] = SPACE;
            }
            // now single char array is ready and has to be added to the list \
            mainCharArrayList.add(i, charArray);
//            MyLog.i("added " + new String(mainCharArrayList.get(i)));

            String previousText = actvMainField.getText().toString();
            String newText = previousText + new String(mainCharArrayList.get(i));
            actvMainField.setText(newText);
//            MyLog.i("newText \n" + newText);

            i++;

            actvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measuredTextHeight = actvMainField.getMeasuredHeight();
//            MyLog.i("measuredTextHeight " + measuredTextHeight);

        } while (measuredTextHeight <= fieldPixelHeight);

        fieldLinesCount = i;
        MyLog.i("fieldLinesCount " + fieldLinesCount);
    }

    private Snake setInitialSnake() {

        int snakeLength = 3;
        snake = new Snake(snakeLength);

        // TODO: 20.03.2016 determine snake's direction here \

        // now i assume that the snake is placed into right-direction \
        for (int i = 0; i <snakeLength; i++){
            mainCharArrayList.get(fieldLinesCount/2)[symbolsInFieldLine/2 + i] = SNAKE;
        }

        updateTextView(mainCharArrayList);

        return snake;
    }

    private void setInitialFood() {

        Random random = new Random();

        // this method gets called after the snake is initialized - so we have to check collisions \
        int foodPositionRow, foodPositionSymbol;
        // i decided to do all in one cycle because of low probability of collisions \
        do {
            // increased by one to include the whole range of values \
            foodPositionRow = random.nextInt(fieldLinesCount) + 1;
            foodPositionSymbol = random.nextInt(symbolsInFieldLine) + 1;
            MyLog.i("random foodPositionRow " + foodPositionRow);
            MyLog.i("random foodPositionSymbol " + foodPositionSymbol);
        } while (mainCharArrayList.get(foodPositionRow - 1)[foodPositionSymbol-1] == SNAKE);

        // substracting 1 because we know that these are indexes - counted from zero \
        mainCharArrayList.get(foodPositionRow - 1)[foodPositionSymbol - 1] = FOOD;
        updateTextView(mainCharArrayList);

        new Timer("timer", true);
    }

    /**
     * @param charArrayList model to set into our main text field \
     */
    private void updateTextView(ArrayList<char[]> charArrayList) {

        StringBuilder newStringToSet = new StringBuilder();
        for (int i = 0; i < fieldLinesCount; i++) {
            newStringToSet.append(charArrayList.get(i));
        }
        actvMainField.setText(newStringToSet);
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