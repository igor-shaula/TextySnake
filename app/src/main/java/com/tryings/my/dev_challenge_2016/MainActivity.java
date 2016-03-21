package com.tryings.my.dev_challenge_2016;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.tryings.my.dev_challenge_2016.entity.Snake;
import com.tryings.my.dev_challenge_2016.util.MyLog;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final char SNAKE = '@';
    private final char SPACE = ' ';
    private final char FOOD = '$'; // +1 to length
    private final char LENGTH_PLUS = '^'; // +2 to length
    private final char LENGTH_MINUS = '*'; // -1 from length
    private final char SPEED_SLOW = '-'; // slows by 25%
    private final char SPEED_UP = '+'; // speeds up by 25%

    private Snake snake;
    private Random random = new Random();
    private int snakeDirection;

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
//                        setFieldBorders();
                        setInitialSnake();
                        setInitialFood();
//                        startMoving();
                    }
                });

        ImageButton ibRight = (ImageButton) findViewById(R.id.ibRight);
        ImageButton ibUp = (ImageButton) findViewById(R.id.ibUp);
        ImageButton ibLeft = (ImageButton) findViewById(R.id.ibLeft);
        ImageButton ibDown = (ImageButton) findViewById(R.id.ibDown);

        if (ibRight != null)
            ibRight.setOnClickListener(this);
        if (ibUp != null)
            ibUp.setOnClickListener(this);
        if (ibLeft != null)
            ibLeft.setOnClickListener(this);
        if (ibDown != null)
            ibDown.setOnClickListener(this);
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

        snakeDirection = random.nextInt(3) + 1;
        for (int i = 0; i < snakeLength; i++) {
            switch (snakeDirection) {
                case 0: { // right
                    mainCharArrayList.get(fieldLinesCount / 2)[symbolsInFieldLine / 2 - i] = SNAKE;
                    break;
                }
                case 1: { // up
                    mainCharArrayList.get(fieldLinesCount / 2 - i)[symbolsInFieldLine / 2] = SNAKE;
                    break;
                }
                case 2: { // left
                    mainCharArrayList.get(fieldLinesCount / 2)[symbolsInFieldLine / 2 + i] = SNAKE;
                    break;
                }
                case 3: { // down
                    mainCharArrayList.get(fieldLinesCount / 2 + i)[symbolsInFieldLine / 2] = SNAKE;
                    break;
                }
            }
        }
        updateTextView(mainCharArrayList);
        return snake;
    }


    private void setInitialFood() {

        // this method gets called after the snake is initialized - so we have to check collisions \
        int foodPositionRow, foodPositionSymbol;
        // i decided to do all in one cycle because of low probability of collisions \
        do {
            // increased by one to include the whole range of values \
            foodPositionRow = random.nextInt(fieldLinesCount) + 1;
            foodPositionSymbol = random.nextInt(symbolsInFieldLine) + 1;
            MyLog.i("random foodPositionRow " + foodPositionRow);
            MyLog.i("random foodPositionSymbol " + foodPositionSymbol);
        } while (mainCharArrayList.get(foodPositionRow - 1)[foodPositionSymbol - 1] == SNAKE);

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

    /**
     * @return true if collision happened & false if it is avoided \
     */
    private boolean moveSnake() {
        // at first we are shifting cells - the last one becomes a new head \


        // than after every move we have to check situation on the field \
        if (collisionHappened()) {
            gameOver();
            return false;
        } else {
            // TODO: 21.03.2016 here some time has to be passed - in background of course \
            moveSnake();
            return true;
        }
        // now
    }

    private boolean collisionHappened() {
        // we have only to check what happens to the snake's head - other cells are inactive \
        Snake.SnakeCell snakesHead = snake.getSnakeCell(0);
        int snakeHeadY = snakesHead.getRowNumber();
        int snakeHeadX = snakesHead.getSymbolInRow();

        if (snake.getLength() <= 4) { // snake with less length cannot collide with itself \
            return touchedBounds(snakeHeadY, snakeHeadX);
        } else return touchedBounds(snakeHeadY, snakeHeadX) || touchedItelf(snakeHeadY, snakeHeadX);
    }

    private boolean touchedBounds(int snakeHeadY, int snakeHeadX) {
        // we can avoid loop here assuming that snake's head has index of 0 \
        return snakeHeadY == 0 || snakeHeadY == fieldLinesCount ||
                snakeHeadX == 0 || snakeHeadX == symbolsInFieldLine;
    }

    private boolean touchedItelf(int snakeHeadY, int snakeHeadX) {
        // snake can collide with itself beginning only from fifth element = fourth index \
        for (int i = 4; i < snake.getLength(); i++) {
            Snake.SnakeCell snakeCell = snake.getSnakeCell(i);
            if (snakeHeadY == snakeCell.getRowNumber() || snakeHeadX == snakeCell.getRowNumber())
                return true;
        }
        return false;
    }

    private void gameOver() {

    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibRight:
                snakeDirection = 0;
                break;
            case R.id.ibUp:
                snakeDirection = 1;
                break;
            case R.id.ibLeft:
                snakeDirection = 2;
                break;
            case R.id.ibDown:
                snakeDirection = 3;
                break;
        }
    }
}