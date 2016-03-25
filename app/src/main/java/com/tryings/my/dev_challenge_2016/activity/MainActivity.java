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
    private final char BORDER = '+';
    private final char LENGTH_PLUS = '&'; // +2 to length
    private final char LENGTH_MINUS = '*'; // -1 from length
    private final char SPEED_SLOW = '-'; // slows by 25%
    private final char SPEED_UP = '#'; // speeds up by 25%

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
                        ViewTreeObserver.OnGlobalLayoutListener listener = this;
                        detectFieldParameters(listener); // 0
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
    private void detectFieldParameters(ViewTreeObserver.OnGlobalLayoutListener listener) {
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
            actvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        else
            //noinspection deprecation
            actvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
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
     * 2 - from onGlobalLayout-method
     */
    private void setFieldBorders() {
        for (int i = 0; i < fieldLinesCount; i++)
            for (int j = 0; j < symbolsInFieldLine; j++)
                if (i == 0 || i == fieldLinesCount - 1 || j == 0 || j == symbolsInFieldLine - 1)
                    mainCharArrayList.get(i)[j] = BORDER;
    }

    /**
     * 3 - from onGlobalLayout-method
     *
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
            Snake.SnakeCell newCell = new Snake.SnakeCell(cellPositionY, cellPositionX);
            snake.addCell(i, newCell);
            // placing the this snake cell to our field \
            mainCharArrayList.get(cellPositionY)[cellPositionX] = SNAKE;

            MyLog.i("after head move: snakeCell.getIndexOfSymbol() = " + snake.getCell(0).getIndexOfSymbol());
            MyLog.i("after head move: snakeCell.getIndexOfRow() = " + snake.getCell(0).getIndexOfRow());
        } // end of for-loop
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
            foodPositionRow = random.nextInt(fieldLinesCount) - 1;
            foodPositionSymbol = random.nextInt(symbolsInFieldLine) - 1;
            // -1 instead of +1 just to avoid placing food on the boards \
            MyLog.i("random foodPositionRow " + foodPositionRow);
            MyLog.i("random foodPositionSymbol " + foodPositionSymbol);
        } while (mainCharArrayList.get(foodPositionRow)[foodPositionSymbol] == SNAKE);
//        } while (mainCharArrayList.get(foodPositionRow - 1)[foodPositionSymbol - 1] == SNAKE);
        // TODO: 25.03.2016 check this algorithm \

        // substracting 1 because we know that these are indexes - counted from zero \
        mainCharArrayList.get(foodPositionRow)[foodPositionSymbol] = FOOD;
//        mainCharArrayList.get(foodPositionRow - 1)[foodPositionSymbol - 1] = FOOD;
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
     *
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

//        if (item.getItemId() == R.id.setComplexity) {
        int newSnakeSpeed = 0;
        switch (item.getItemId()) {
            case R.id.speed_5:
                newSnakeSpeed++;
            case R.id.speed_4:
                newSnakeSpeed++;
            case R.id.speed_3:
                newSnakeSpeed++;
            case R.id.speed_2:
                newSnakeSpeed++;
            case R.id.speed_1:
                newSnakeSpeed++;
        }
        if (newSnakeSpeed != 0)
            snakeSpeed = newSnakeSpeed;
        MyLog.i("newSnakeSpeed = " + newSnakeSpeed);
        return super.onOptionsItemSelected(item);
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

    // MOVEMENT ====================================================================================

    /**
     * special class defined for repeating operations and usage of Timer \
     */
    public class MyTimerTask extends TimerTask {

        // this method handles movement of the snake \
        @Override
        public void run() {
            // defining all reusable variables here \
            int newCellX = 0, newCellY = 0;
            Snake.SnakeCell snakeCell; // to use only one object
            snakeCell = snake.getCell(0); // for head of the snake
//            Snake.SnakeCell snakesHead = snake.getCell(0);

            MyLog.i("before head move: snakeCell.getIndexOfSymbol() = " + snakeCell.getIndexOfSymbol());
            MyLog.i("before head move: snakeCell.getIndexOfRow() = " + snakeCell.getIndexOfRow());

            switch (snakeDirection) {
                case 0: // tail to right - head moves to left \
                    newCellX = snakeCell.getIndexOfSymbol() + 1;
                    newCellY = snakeCell.getIndexOfRow();
                    break;
                case 1: // tail is set up - head moves down \
                    newCellX = snakeCell.getIndexOfSymbol();
                    newCellY = snakeCell.getIndexOfRow() + 1;
                    break;
                case 2: // tail to left - head moves to right \
                    newCellX = snakeCell.getIndexOfSymbol() - 1;
                    newCellY = snakeCell.getIndexOfRow();
                    break;
                case 3: // tail is set down - head moves up \
                    newCellX = snakeCell.getIndexOfSymbol();
                    newCellY = snakeCell.getIndexOfRow() - 1;
                    break;
            }
            // moving snake's head \
            snakeCell.setIndexOfSymbol(newCellX);
            snakeCell.setIndexOfRow(newCellY);

            MyLog.i("after head move: snakeCell.getIndexOfSymbol() = " + snakeCell.getIndexOfSymbol());
            MyLog.i("after head move: snakeCell.getIndexOfRow() = " + snakeCell.getIndexOfRow());

            // saving information about the last cell position - it will be freed in the end \
            snakeCell = snake.getCell(snake.getLength() - 1);
            int cellToFreeX = snakeCell.getIndexOfSymbol();
            int cellToFreeY = snakeCell.getIndexOfRow();
            MyLog.i("cellToFree -> snake length = " + snake.getLength());
            // making single change to every cell in the model of snake \
            for (int i = 1; i < snake.getLength(); i++) {
                snakeCell = snake.getCell(i - 1); // to get position of every previous cell
                // shifting 1 cell at a step \
                newCellX = snakeCell.getIndexOfSymbol();
                newCellY = snakeCell.getIndexOfRow();
                // moving every cell of the snake's body \
                snakeCell = snake.getCell(i); // to update current cell vith position of previous \
                snakeCell.setIndexOfSymbol(newCellX);
                snakeCell.setIndexOfRow(newCellY);
            }
            // here we have just finished to update snake's model \
            MyLog.i("move done in snake's model");
            // now it's obvious to update model for field with snake's new data and display this all \
            int cellPositionX, cellPositionY;
            for (int i = 0; i < snake.getLength(); i++) {
                snakeCell = snake.getCell(i);
                cellPositionX = snakeCell.getIndexOfSymbol();
                cellPositionY = snakeCell.getIndexOfRow();
                try {
                    // setting the snake body \
                    mainCharArrayList.get(cellPositionY)[cellPositionX] = SNAKE;
                    // recovering the field after the snake's tail \
                    mainCharArrayList.get(cellToFreeY)[cellToFreeX] = SPACE;
                } catch (IndexOutOfBoundsException ioobe) {
                    // ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException
                    gameOver();
                    return;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTextView(mainCharArrayList);
                }
            });
            // exit conditions check is the last thing to do \
            if (collisionHappened()) gameOver(); // this is the only way out from loop \
            else score++;
        } // end of run-method \\

        // VERIFICATIONS ===========================================================================

        private boolean collisionHappened() {
            // we have only to check what happens to the snake's head - other cells are inactive \
            Snake.SnakeCell snakesHead = snake.getCell(0);
            int snakeHeadY = snakesHead.getIndexOfRow();
            int snakeHeadX = snakesHead.getIndexOfSymbol();

            if (snake.getLength() <= 4) { // snake with less length cannot collide with itself \
                return touchedBounds(snakeHeadY, snakeHeadX);
            } else
                return touchedBounds(snakeHeadY, snakeHeadX) || touchedItelf(snakeHeadY, snakeHeadX);
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
                Snake.SnakeCell snakeCell = snake.getCell(i);
                if (snakeHeadY == snakeCell.getIndexOfRow() || snakeHeadX == snakeCell.getIndexOfRow())
                    return true;
            }
            return false;
        }
    }
}