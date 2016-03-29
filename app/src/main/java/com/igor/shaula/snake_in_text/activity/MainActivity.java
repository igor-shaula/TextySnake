package com.igor.shaula.snake_in_text.activity;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.igor.shaula.snake_in_text.R;
import com.igor.shaula.snake_in_text.entity.Snake;
import com.igor.shaula.snake_in_text.util.MyLog;
import com.igor.shaula.snake_in_text.util.MyPSF;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by igor shaula \
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // following fields are safely rebuilt after changing screen configuration \\\\\\\\\\\\\\\\\\\\\

    // definition of the field \
    private char foodType;
    private char[] foodTypeArray = // LENGTH_PLUS will be as often as other values in sum \
            {MyPSF.LENGTH_PLUS, MyPSF.LENGTH_PLUS, MyPSF.LENGTH_PLUS,
                    MyPSF.LENGTH_MINUS, MyPSF.SPEED_SLOW, MyPSF.SPEED_UP};
    private int foodPositionRow, foodPositionSymbol;
    private int oldFoodPositionX, oldFoodPositionY;
    private int fieldPixelWidth, fieldPixelHeight; // in pixels
    private int symbolsInFieldLine, fieldLinesCount; // in items - for arrays \

    // main data storage \
    private ArrayList<char[]> mainCharArrayList;

    // active widgets \
    private TextView tvMainField, tvTime, tvScore;
    private Button bStartPause;

    // utils from the system \
    private Vibrator vibrator;
    private Random random = new Random();
    private Timer timer;

    // following values have to be saved after changing screen orientation \\\\\\\\\\\\\\\\\\\\\\\\\

    // definition of the snake \
    private Snake snake;
    @SuppressWarnings("FieldCanBeLocal")
    private int snakeSpeed;
    private int snakeDirection;

    // game parameters \
    private int score;
    private boolean alreadyLaunched = false, wasGameOver = false;

    // LIFECYCLE ===================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        //noinspection StatementWithEmptyBody
        if (savedInstanceState != null) {

            // doesn't work fine yet - i have no time to fix it right now \

            snake = savedInstanceState.getParcelable(KEY_SNAKE);
            snakeDirection = savedInstanceState.getInt(KEY_SNAKE_DIRECTION);
            snakeSpeed = savedInstanceState.getInt(KEY_SNAKE_SPEED);
            score = savedInstanceState.getInt(KEY_SCORE);
            alreadyLaunched = savedInstanceState.getBoolean(KEY_ALREADY_LAUNCHED);
            wasGameOver = savedInstanceState.getBoolean(KEY_WAS_GAME_OVER);
        } else {}
*/
        /*
        * NOTE !!! as for now - after changing configuration everything has to be restarted from scratch
        * because the main parameters - numbers of rows and symbols in a row - are changed \
        */

        // from the very beginning we have to define available field \
        tvMainField = (TextView) findViewById(R.id.viewMainField);
        assert tvMainField != null;

/*
        // setting up our square font \
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Arcade.ttf");
        tvMainField.setTypeface(typeface);
*/

        tvMainField.getViewTreeObserver().addOnGlobalLayoutListener(
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

        tvTime = (TextView) findViewById(R.id.tvTime);
        tvScore = (TextView) findViewById(R.id.tvScore);
        tvScore.setText(String.valueOf(getString(R.string.score) + MyPSF.SCORE_STARTING_SUFFIX));

        bStartPause = (Button) findViewById(R.id.bStartPause);
        assert bStartPause != null;
        bStartPause.setOnClickListener(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    } // end of onCreate-method \\

    // SAVE_RESTORE ================================================================================

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
/*
        outState.putParcelable(KEY_SNAKE, snake);
        outState.putInt(KEY_SNAKE_DIRECTION, snakeDirection);
        outState.putInt(KEY_SNAKE_SPEED, snakeSpeed);
        outState.putInt(KEY_SCORE, score);
        outState.putBoolean(KEY_ALREADY_LAUNCHED, alreadyLaunched);
        outState.putBoolean(KEY_WAS_GAME_OVER, wasGameOver);
*/
        MyLog.i("onSaveInstanceState worked");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
/*
        snake = savedInstanceState.getParcelable(KEY_SNAKE);
        snakeDirection = savedInstanceState.getInt(KEY_SNAKE_DIRECTION);
        snakeSpeed = savedInstanceState.getInt(KEY_SNAKE_SPEED);
        score = savedInstanceState.getInt(KEY_SCORE);
        alreadyLaunched = savedInstanceState.getBoolean(KEY_ALREADY_LAUNCHED);
        wasGameOver = savedInstanceState.getBoolean(KEY_WAS_GAME_OVER);
*/
    }

    // BEFORE START ================================================================================

    // 0 - from onGlobalLayout-method
    private void detectFieldParameters(ViewTreeObserver.OnGlobalLayoutListener listener) {
        // getting current screen size in pixels \
        fieldPixelWidth = tvMainField.getWidth();
        MyLog.i("fieldPixelWidth " + fieldPixelWidth);

        FrameLayout flMain = (FrameLayout) findViewById(R.id.flMain);
        assert flMain != null;
        fieldPixelHeight = flMain.getHeight();
        MyLog.i("fieldPixelHeight " + fieldPixelHeight);

        // now removing the listener - it's not needed any more \
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
            tvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        else
            //noinspection deprecation
            tvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
    } // end of detectFieldParameters-method \\

    // 1 - from onGlobalLayout-method
    private void prepareTextField() {

        if (!wasGameOver) {
            // here we get pixel width of a single symbol - initial TextView has only one symbol \
            tvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int measuredSymbolWidth = tvMainField.getMeasuredWidth();
            MyLog.i("measuredSymbolWidth " + measuredSymbolWidth);

            // getting our first valuable parameter - the size of main array \
            symbolsInFieldLine = fieldPixelWidth / measuredSymbolWidth;
            MyLog.i("symbolsInFieldLine " + symbolsInFieldLine);
        }
        // clearing the text field to properly initialize it for game \
        tvMainField.setText(null);

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
                charArray[j] = MyPSF.SPACE;
            }
            // now single char array is ready and has to be added to the list \
            mainCharArrayList.add(i, charArray);
//            MyLog.i("added " + new String(mainCharArrayList.get(i)));

            String previousText = tvMainField.getText().toString();
            String newText = previousText + new String(mainCharArrayList.get(i));
            tvMainField.setText(newText);
//            MyLog.i("newText \n" + newText);

            i++;

            tvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measuredTextHeight = tvMainField.getMeasuredHeight();
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
                    mainCharArrayList.get(i)[j] = MyPSF.BORDER;
    }

    /**
     * 3 - from onGlobalLayout-method
     */
    private void setInitialSnake() {

        snake = new Snake();
        snakeSpeed = MyPSF.STARTING_SNAKE_SPEED;

        // 1 - defining start directions to properly set up the snake \
        snakeDirection = random.nextInt(3) + 1;
        for (int i = 0; i < MyPSF.STARTING_SNAKE_LENGTH; i++) {
            // here we define position of every snake's cell \
            int cellPositionX = 0, cellPositionY = 0;
            // setting tail in the opposite direction here - to free space for head \
            switch (snakeDirection) {
                case 0: { // right
                    cellPositionX = symbolsInFieldLine / 2 - i;
                    cellPositionY = fieldLinesCount / 2;
                    break;
                }
                case 1: { // up
                    cellPositionX = symbolsInFieldLine / 2;
                    cellPositionY = fieldLinesCount / 2 + i;
                    break;
                }
                case 2: { // left
                    cellPositionX = symbolsInFieldLine / 2 + i;
                    cellPositionY = fieldLinesCount / 2;
                    break;
                }
                case 3: { // down
                    cellPositionX = symbolsInFieldLine / 2;
                    cellPositionY = fieldLinesCount / 2 - i;
                    break;
                }
            }
            // now it is time to create new snake cell \
            Snake.SnakeCell newCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
            // updating the snake's model \
            snake.addCell(i, newCell);
            // placing the this snake cell to our field \
            mainCharArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SNAKE;
        } // end of for-loop
        updateTextView();
    } // end of setInitialSnake-method \\

    // 4 - from onGlobalLayout-method
    private void setInitialFood() {
        updateFood();
        updateTextView();
    } // end of setInitialFood-method \\

    private void updateFood() {
/*
        this method gets called after the snake is initialized -so we have to check collisions \
        i decided to do all in one cycle because of low probability of collisions \
*/
        // first of all clearing old place \
        char replaceToSpace = mainCharArrayList.get(oldFoodPositionY)[oldFoodPositionX];
        if (replaceToSpace != MyPSF.BORDER) // this might happen at the very start \
            mainCharArrayList.get(oldFoodPositionY)[oldFoodPositionX] = MyPSF.SPACE;
        // now everything is clear and we can set new food type and position \
        do {
            /*
            range for random: +1 -2 = -1
            increased by one to include the whole range of values \
            decreased by two to exclude visible field borders \
            */
            foodPositionRow = random.nextInt(fieldLinesCount - 2) + 1;
            foodPositionSymbol = random.nextInt(symbolsInFieldLine - 2) + 1;
            // -1 instead of +1 just to avoid placing food on the boards \
            MyLog.i("random foodPositionRow " + foodPositionRow);
            MyLog.i("random foodPositionSymbol " + foodPositionSymbol);
        } while (mainCharArrayList.get(foodPositionRow)[foodPositionSymbol] == MyPSF.SNAKE);

        oldFoodPositionX = foodPositionSymbol;
        oldFoodPositionY = foodPositionRow;

        foodType = foodTypeArray[random.nextInt(foodTypeArray.length)];

        // substracting 1 because we know that these are indexes - counted from zero \
        mainCharArrayList.get(foodPositionRow)[foodPositionSymbol] = foodType;
//        mainCharArrayList.get(foodPositionRow - 1)[foodPositionSymbol - 1] = FOOD;
    }

    private void updateTextView() {

        StringBuilder newStringToSet = new StringBuilder();
        for (int i = 0; i < fieldLinesCount; i++) {
            newStringToSet.append(mainCharArrayList.get(i));
        }
        tvMainField.setText(newStringToSet);
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

    // LISTENER ====================================================================================

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibRight:
                snakeDirection = 0;
                MyLog.i("turned Right");
                break;
            case R.id.ibUp:
                snakeDirection = 1;
                MyLog.i("turned Up");
                break;
            case R.id.ibLeft:
                snakeDirection = 2;
                MyLog.i("turned Left");
                break;
            case R.id.ibDown:
                snakeDirection = 3;
                MyLog.i("turned Down");
                break;
            case R.id.bStartPause:
                vibrator.vibrate(MyPSF.SHORT_VIBRATION);
                if (alreadyLaunched) { // initial value is false \
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    int stopTextColor = ContextCompat.getColor(this, R.color.primary_light);
                    tvMainField.setTextColor(stopTextColor);
                    bStartPause.setText(R.string.start);
                } else {
                    if (wasGameOver) {
                        // everything is reset to later start from scratch \
                        prepareTextField();
                        setFieldBorders();
                        setInitialSnake();
                        setInitialFood();
                        score = 0;
                        wasGameOver = false;
                    }
                    int startTextColor = ContextCompat.getColor(this, android.R.color.white);
                    tvMainField.setTextColor(startTextColor);
                    tvMainField.setBackgroundResource(R.color.primary_dark);
                    bStartPause.setText(R.string.pause);
                    // launching everything \
                    int delay = 600 / snakeSpeed;
                    timer = new Timer();
                    timer.schedule(new SnakeMoveTimerTask(), 0, delay);
                    timer.schedule(new TimeUpdateTimerTask(), 0, 1000);
//                    timer.schedule(new TimeUpdateTimerTask(), 0, 1); // requirements of dev-challenge \
                    timer.schedule(new FoodUpdateTimerTask(),
                            MyPSF.STARTING_UPDATE_FOOD_PERIOD,
                            MyPSF.STARTING_UPDATE_FOOD_PERIOD);
                }
                alreadyLaunched = !alreadyLaunched;
                break;
        }
    } // end of onClick-method \\

    private void gameOver() {
        vibrator.vibrate(MyPSF.LONG_VIBRATION);
        wasGameOver = true;
        alreadyLaunched = false;
        MyLog.i("game ended with score " + score);
        timer.cancel();
        timer.purge();
        timer = null;
        // resetting the start-stop button to its primary state \
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bStartPause.setText(R.string.start);
                int endTextColor = ContextCompat.getColor(MainActivity.this, android.R.color.primary_text_light);
                tvMainField.setTextColor(endTextColor);
                tvMainField.setBackgroundResource(R.color.primary_light);
            }
        });
    }

    // MOVEMENT ====================================================================================

    /**
     * special class defined for repeating operations and usage of Timer \
     */
    public class SnakeMoveTimerTask extends TimerTask {

        // this method handles movement of the snake \
        @Override
        public void run() {
            // defining all reusable variables here \
            int newCellX = 0, newCellY = 0;
            Snake.SnakeCell snakeCell; // to use only one object for the whole snake \

            // 1 - saving information about the last cell position - before it will be freed in the end \
            snakeCell = snake.getCell(snake.getLength() - 1);
            int cellToFreeX = snakeCell.getIndexByX();
            int cellToFreeY = snakeCell.getIndexByY();

            // 2 - making single change to every cell in the model of snake - except the head \
            for (int i = snake.getLength() - 1; i > 0; i--) {
//            for (int i = 1; i < snake.getLength(); i++) {
                snakeCell = snake.getCell(i - 1); // to get the position of every previous cell
                // shifting 1 cell at a step \
                newCellX = snakeCell.getIndexByX();
                newCellY = snakeCell.getIndexByY();
                // moving every cell of the snake's body \
                snakeCell = snake.getCell(i); // to update current cell vith position of previous \
                snakeCell.setIndexByX(newCellX);
                snakeCell.setIndexByY(newCellY);
            }

            // 3 - finally moving snake's head to selected direction \
            snakeCell = snake.getCell(0); // for head of the snake
            switch (snakeDirection) {
                case 0: // tail to right - head moves to left \
                    newCellX = snakeCell.getIndexByX() + 1;
                    newCellY = snakeCell.getIndexByY();
                    break;
                case 1: // tail is set up - head moves down \
                    newCellX = snakeCell.getIndexByX();
                    newCellY = snakeCell.getIndexByY() - 1;
                    break;
                case 2: // tail to left - head moves to right \
                    newCellX = snakeCell.getIndexByX() - 1;
                    newCellY = snakeCell.getIndexByY();
                    break;
                case 3: // tail is set down - head moves up \
                    newCellX = snakeCell.getIndexByX();
                    newCellY = snakeCell.getIndexByY() + 1;
                    break;
            }
            // saving info to the snake's model \
            snakeCell.setIndexByX(newCellX);
            snakeCell.setIndexByY(newCellY);

            // here we have just finished to update snake's model \
            MyLog.i("move done in snake's model");

            // now it's obvious to update model for field with snake's new data and display this all \
            for (int i = 0; i < snake.getLength(); i++) {
                snakeCell = snake.getCell(i);
                newCellX = snakeCell.getIndexByX();
                newCellY = snakeCell.getIndexByY();
/*
                // setting the snake body \
                mainCharArrayList.get(newCellY)[newCellX] = SNAKE;
                // recovering the field after the snake's tail \
                mainCharArrayList.get(cellToFreeY)[cellToFreeX] = SPACE;
*/
                // this is a crutch - the game somehow may fall after screen rotation \
                try {
                    // setting the snake body \
                    mainCharArrayList.get(newCellY)[newCellX] = MyPSF.SNAKE;
                    // recovering the field after the snake's tail \
                    mainCharArrayList.get(cellToFreeY)[cellToFreeX] = MyPSF.SPACE;
                } catch (IndexOutOfBoundsException ioobe) {
                    // ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException
                    MyLog.i("exception happened: " + ioobe.getMessage());
                    return;
                }
            }
            // updating the field with new snake's position \
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTextView();
                    String scoreComplex = getString(R.string.score) + ": " + score;
                    tvScore.setText(scoreComplex);
                    MyLog.i("tvScore updated");
                }
            });

            // exit conditions check is the last thing to do \
            if (collisionHappened()) gameOver(); // this is the only way out from loop \
            else score++;

            // now handling eating of food and bonuses - it's taken by the head only \
            if (isFoodFound()) {
                eatFood();
                MyLog.i("food eaten");
            }
        } // end of run-method \\

        // VERIFICATIONS ===========================================================================

        private boolean isFoodFound() {
            return snake.getCell(0).getIndexByY() == foodPositionRow
                    && snake.getCell(0).getIndexByX() == foodPositionSymbol;
        }

        private void eatFood() {
            // user must be happy with such a vibration :)
            vibrator.vibrate(MyPSF.SHORT_VIBRATION);

            int cellPositionX, cellPositionY;
            Snake.SnakeCell currentCell;
            MyLog.i("eaten = " + mainCharArrayList.get(foodPositionRow)[foodPositionSymbol]);

            switch (foodType) {

                case MyPSF.LENGTH_PLUS: // length +1
                    /*
                    i decided to add a new cell at the snake 's head - because we know the direction \
                    new cell will get visible only at the end of the move \
                    right now i 'm only updating the model - not the view \
                    */
                    cellPositionX = foodPositionSymbol + shiftAfterFood(snakeDirection, true);
                    cellPositionY = foodPositionRow + shiftAfterFood(snakeDirection, false);
                    currentCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
                    snake.addCell(snake.getLength(), currentCell);
                    snakeSpeed++;
                    MyLog.i("LENGTH_PLUS taken!");
                    break;

                case MyPSF.LENGTH_MINUS: // length -1
                    // just removing the last cell \
                    if (snake.getLength() > 1) { // to avoid snake's dissappearing \
                        // first updating field to clear snake's tail - while it's available \
                        currentCell = snake.getCell(snake.getLength() - 1);
                        cellPositionX = currentCell.getIndexByX();
                        cellPositionY = currentCell.getIndexByY();
                        mainCharArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SPACE;
                        // now it is safe to update the model \
                        snake.removeCell(snake.getLength() - 1);
                        if (snakeSpeed > 1) snakeSpeed--;
                    }
                    MyLog.i("LENGTH_MINUS taken!");
                    break;

                case MyPSF.SPEED_UP: // speed +1
                    snakeSpeed++;
                    MyLog.i("SPEED_UP taken!");
                    break;

                case MyPSF.SPEED_SLOW: // speed -1
                    if (snakeSpeed > 1) // to avoid falling after division by zero \
                        snakeSpeed--;
                    MyLog.i("SPEED_SLOW taken!");
                    break;
            }
        }

        private int shiftAfterFood(int direction, boolean isForX) {
            if (isForX)
                switch (direction) {
                    case 0: // to right - for X
                        return 1;
                    case 2: // to left - for X
                        return -1;
                    default: // up and dowm - 1, 3 cases don't affect X
                        return 0;
                }
            else
                switch (direction) {
                    case 1: // up - for Y
                        return 1;
                    case 3: // down - for Y
                        return -1;
                    default: // to right and left - 0, 2 cases don't affect Y
                        return 0;
                }
        }

        private boolean collisionHappened() {
            // we have only to check what happens to the snake's head - other cells are inactive \
            int snakeHeadY = snake.getCell(0).getIndexByY();
            int snakeHeadX = snake.getCell(0).getIndexByX();

            if (snake.getLength() <= 4) { // snake with less length cannot collide with itself \
                return touchedBounds(snakeHeadY, snakeHeadX);
            } else
                return touchedBounds(snakeHeadY, snakeHeadX) || touchedItself(snakeHeadY, snakeHeadX);
        }

        // connected to collisionHappened-method \
        private boolean touchedBounds(int snakeHeadY, int snakeHeadX) {
            // we can avoid loop here assuming that snake's head has index of 0 \
            return snakeHeadY == 0 || snakeHeadY == fieldLinesCount - 1 ||
                    snakeHeadX == 0 || snakeHeadX == symbolsInFieldLine - 1;
        }

        // connected to collisionHappened-method \
        private boolean touchedItself(int snakeHeadY, int snakeHeadX) {
            // snake can collide with itself beginning only from fifth element = fourth index \
            for (int i = 4; i < snake.getLength(); i++) {
                Snake.SnakeCell snakeCell = snake.getCell(i);
                if (snakeHeadY == snakeCell.getIndexByY() && snakeHeadX == snakeCell.getIndexByX()) {
                    MyLog.i("touchedItself");
                    return true;
                }
            }
            return false;
        }
    } // end of SnakeMoveTimerTask-class \\

    public class FoodUpdateTimerTask extends TimerTask {

        @Override
        public void run() {
            updateFood();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTextView();
                }
            });
            MyLog.i("foodType updated");
        }
    }

    public class TimeUpdateTimerTask extends TimerTask {
        // code here is called every milisecond - it has to be really fast \
        long initialSystemTime = System.currentTimeMillis();

        @Override
        public void run() {
            long elapsedTimeLong = System.currentTimeMillis() - initialSystemTime;
/*
            00 added to the beginning of the string to avoid situation <=99 difference
            that means less than three digits and ArrayOutOfBoundsException as a result
*/
/*
            String systemTimeString = String.valueOf("00" + elapsedTimeLong);
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(DateFormat.format("mm:ss", elapsedTimeLong));
            stringBuilder.append(":").append(getMilliseconds(systemTimeString));
*/
            final String stringToSet = String.valueOf("Time: " + DateFormat.format("mm:ss", elapsedTimeLong));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTime.setText(stringToSet);
//                    tvTime.setText(stringBuilder);
                }
            });
        }
/*
        private String getMilliseconds(String systemTimeString) {

            int stringIndex = systemTimeString.length() - 1;
            int innerIndex = 3 - 1;
            char[] digits = new char[3];
            for (int i = stringIndex; i > stringIndex - 3; i--) {
                digits[innerIndex] = systemTimeString.charAt(i);
                innerIndex--;
            }
            return new String(digits);
        }
*/
    } // end of TimeUpdateTimerTask-class \\
}