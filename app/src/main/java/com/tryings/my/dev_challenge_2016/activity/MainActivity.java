package com.tryings.my.dev_challenge_2016.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.tryings.my.dev_challenge_2016.R;
import com.tryings.my.dev_challenge_2016.entity.Snake;
import com.tryings.my.dev_challenge_2016.util.MyLog;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final char SNAKE = '@';
    private final char SPACE = ' ';
    private final char FOOD = '$'; // +1 to length
    private final char LENGTH_PLUS = '^'; // +2 to length
    private final char LENGTH_MINUS = '*'; // -1 from length
    private final char SPEED_SLOW = '-'; // slows by 25%
    private final char SPEED_UP = '+'; // speeds up by 25%

    private Random random = new Random();

    // definition of the snake \
    private Snake snake;
    private int snakeSpeed = 3;
    private int snakeDirection;

    // definition of the field \
    private int fieldPixelWidth, fieldPixelHeight; // in pixels
    private int symbolsInFieldLine, fieldLinesCount; // in items - for arrays \

    // main data storage \
    private ArrayList<char[]> mainCharArrayList;

    // active widgets \
    private AppCompatTextView actvMainField;
    private Button bStart;

    // game parameters \
    private int score;
    private boolean alreadyLaunched = false;
    private Timer timer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // from the very beginning we have to define available field \
        actvMainField = (AppCompatTextView) findViewById(R.id.tvMainField);
        assert actvMainField != null;
        actvMainField.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // moving everything to separate methods \
                        detectFieldParameters(); // 0
                        prepareTextField(); // 1
                        setFieldBorders(); // 2
                        setInitialSnake(); // 3
                        setInitialFood(); // 4
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

        bStart = (Button) findViewById(R.id.bStartPause);
        assert bStart != null;
        bStart.setOnClickListener(this);
    } // end of onCreate-method \\

    // 0 - from onGlobalLayout-method
    private void detectFieldParameters() {
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
            actvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(MainActivity.this);
        else
            //noinspection deprecation
            actvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    } // end of detectFieldParameters-method \\

    // 1 - from onGlobalLayout-method
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
    } // end of prepareTextField-method \\

    /**
     * 3 - from onGlobalLayout-method
     * @return snake which is ready to start playing
     */
    private Snake setInitialSnake() {

        int snakeLength = 3;
        snake = new Snake(snakeLength);

        snakeDirection = random.nextInt(3) + 1;
        for (int i = 0; i < snakeLength; i++) {
            // here we define position of every snake's cell \
            int cellPositionX = 0, cellPositionY = 0;
            switch (snakeDirection) {
                case 0: { // right
                    cellPositionX = symbolsInFieldLine / 2 - i;
                    cellPositionY = fieldLinesCount / 2;
                    break;
                }
                case 1: { // up
                    cellPositionX = symbolsInFieldLine / 2;
                    cellPositionY = fieldLinesCount / 2 - i;
                    break;
                }
                case 2: { // left
                    cellPositionX = symbolsInFieldLine / 2 + i;
                    cellPositionY = fieldLinesCount / 2;
                    break;
                }
                case 3: { // down
                    cellPositionX = symbolsInFieldLine / 2;
                    cellPositionY = fieldLinesCount / 2 + i;
                    break;
                }
            }
            // now it is time to create new snake cell \
            Snake.SnakeCell newCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
            snake.addSnakeCell(i, newCell);
            // placing the this snake cell to our field \
            mainCharArrayList.get(cellPositionY)[cellPositionX] = SNAKE;
        }
        updateTextView(mainCharArrayList);

        return snake;
    } // end of setInitialSnake-method \\

    // 4 - from onGlobalLayout-method
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
    } // end of setInitialFood-method \\

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
     * all game is passing inside this method \
     * @param speed - needed to calculate delay inside this method \
     */
    private void moveSnake(int speed) {
        // at first we are shifting cells - the last one becomes a new head \
        int delay = 1000 / speed;
        timer = new Timer();
        timer.schedule(new MyTimerTask(), 0, delay);
    }

    private void gameOver() {

        MyLog.i("game ended with score " + score);
    }

    // MENU ========================================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.selectDistricts: {
                return showDialog();
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // SAVE_RESTORE ================================================================================

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    // LISTENER ====================================================================================

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibRight:
                snakeDirection = 0;
                MyLog.i("ibRight");
                break;
            case R.id.ibUp:
                snakeDirection = 1;
                MyLog.i("ibUp");
                break;
            case R.id.ibLeft:
                snakeDirection = 2;
                MyLog.i("ibLeft");
                break;
            case R.id.ibDown:
                snakeDirection = 3;
                MyLog.i("ibDown");
                break;
            case R.id.bStartPause:
                if (alreadyLaunched) { // initial value is false \
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    bStart.setText(R.string.start);
                } else {
                    bStart.setText(R.string.stop);
                    moveSnake(snakeSpeed);
                }
                alreadyLaunched = !alreadyLaunched;
                break;
        }
    }

    /**
     * special class defined for repeating operations and usage of Timer \
     */
    public class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            MyLog.i("move done");
            if (collisionHappened()) gameOver(); // this is the only way out from loop \
            else score++;
        }

        private boolean collisionHappened() {
            // we have only to check what happens to the snake's head - other cells are inactive \
            Snake.SnakeCell snakesHead = snake.getSnakeCell(0);
            int snakeHeadY = snakesHead.getRowIndex();
            int snakeHeadX = snakesHead.getIndexInRow();

            if (snake.getLength() <= 4) { // snake with less length cannot collide with itself \
                return touchedBounds(snakeHeadY, snakeHeadX);
            } else return touchedBounds(snakeHeadY, snakeHeadX) || touchedItelf(snakeHeadY, snakeHeadX);
        }

        // connected to collisionHappened-method \
        private boolean touchedBounds(int snakeHeadY, int snakeHeadX) {
            // we can avoid loop here assuming that snake's head has index of 0 \
            return snakeHeadY == 0 || snakeHeadY == fieldLinesCount ||
                    snakeHeadX == 0 || snakeHeadX == symbolsInFieldLine;
        }

        // connected to collisionHappened-method \
        private boolean touchedItelf(int snakeHeadY, int snakeHeadX) {
            // snake can collide with itself beginning only from fifth element = fourth index \
            for (int i = 4; i < snake.getLength(); i++) {
                Snake.SnakeCell snakeCell = snake.getSnakeCell(i);
                if (snakeHeadY == snakeCell.getRowIndex() || snakeHeadX == snakeCell.getRowIndex())
                    return true;
            }
            return false;
        }
    }
}