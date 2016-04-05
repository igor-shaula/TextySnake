package com.igor.shaula.snake_in_text.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.igor.shaula.snake_in_text.R;
import com.igor.shaula.snake_in_text.custom_view.MyTextView;
import com.igor.shaula.snake_in_text.entity.Snake;
import com.igor.shaula.snake_in_text.utils.MyLog;
import com.igor.shaula.snake_in_text.utils.MyPSF;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by igor shaula \
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // following fields are safely rebuilt after changing screen configuration \\\\\\\\\\\\\\\\\\\\\

    private final char[] mFoodTypeArray = // LENGTH_PLUS will be as often as other values in sum \
            {MyPSF.LENGTH_PLUS, MyPSF.LENGTH_PLUS, MyPSF.LENGTH_PLUS,
                    MyPSF.LENGTH_MINUS, MyPSF.SPEED_SLOW, MyPSF.SPEED_UP};
    // definition of the field \
    private char mFoodType;
    private int mFoodPositionRow, mFoodPositionSymbol;
    private int mOldFoodPositionX, mOldFoodPositionY;
    private int mFieldPixelWidth, mFieldPixelHeight; // in pixels
    private int mSymbolsInFieldLine, mFieldLinesCount; // in items - for arrays \

    // main data storage \
    private ArrayList<char[]> mCharsArrayList;

    // active widgets \
    private TextView tvMainField, tvTime, tvScore;
    private Button bStartPause;

    // utils from the system \
    private Vibrator mVibrator;
    private Random mRandom = new Random();
    private Timer mTimer;

    // following values have to be saved after changing screen orientation \\\\\\\\\\\\\\\\\\\\\\\\\

    // definition of the mSnake \
    private Snake mSnake;
    @SuppressWarnings("FieldCanBeLocal")
    private int mSnakeSpeed;
    private MyDirections mSnakeDirection;

    // game parameters \
    private boolean mAlreadyLaunched = false, mWasGameOver = false;
    private int mCurrentScore, mBestScore;
    private long mCurrentTime, mBestTime;

    private GestureDetector mGestureDetector;

    // LIFECYCLE ===================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        //noinspection StatementWithEmptyBody
        if (savedInstanceState != null) {

            // doesn't work fine yet - i have no time to fix it right now \

            mSnake = savedInstanceState.getParcelable(KEY_SNAKE);
            mSnakeDirection = savedInstanceState.getInt(KEY_SNAKE_DIRECTION);
            mSnakeSpeed = savedInstanceState.getInt(KEY_SNAKE_SPEED);
            mCurrentScore = savedInstanceState.getInt(KEY_SCORE);
            mAlreadyLaunched = savedInstanceState.getBoolean(KEY_ALREADY_LAUNCHED);
            mWasGameOver = savedInstanceState.getBoolean(KEY_WAS_GAME_OVER);
        } else {}
*/
        /*
        * NOTE !!! as for now - after changing configuration everything has to be restarted from scratch
        * because the main parameters - numbers of rows and symbols in a row - are changed \
        */

        // from the very beginning we have to define available field \
        tvMainField = (TextView) findViewById(R.id.viewMainField);
        assert tvMainField != null;

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

        tvTime = (TextView) findViewById(R.id.mtvTime);
        tvScore = (TextView) findViewById(R.id.mtvScore);
        tvScore.setText(String.valueOf(getString(R.string.score) + MyPSF.SCORE_STARTING_SUFFIX));

        bStartPause = (Button) findViewById(R.id.bStartPause);
        assert bStartPause != null;
        bStartPause.setOnClickListener(this);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // now the setting the top - gesture sensetive interface \
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
//        tvMainField.setOnTouchListener(new MyTouchListener(this));

        SharedPreferences sharedPreferences = getSharedPreferences(MyPSF.S_P_NAME, MODE_PRIVATE);
        mBestScore = sharedPreferences.getInt(MyPSF.KEY_SCORE, 0);
        mBestTime = sharedPreferences.getLong(MyPSF.KEY_TIME, 0);
    } // end of onCreate-method \\

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    // SAVE_RESTORE ================================================================================

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
/*
        outState.putParcelable(KEY_SNAKE, mSnake);
        outState.putInt(KEY_SNAKE_DIRECTION, mSnakeDirection);
        outState.putInt(KEY_SNAKE_SPEED, mSnakeSpeed);
        outState.putInt(KEY_SCORE, mCurrentScore);
        outState.putBoolean(KEY_ALREADY_LAUNCHED, mAlreadyLaunched);
        outState.putBoolean(KEY_WAS_GAME_OVER, mWasGameOver);
*/
        MyLog.i("onSaveInstanceState worked");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
/*
        mSnake = savedInstanceState.getParcelable(KEY_SNAKE);
        mSnakeDirection = savedInstanceState.getInt(KEY_SNAKE_DIRECTION);
        mSnakeSpeed = savedInstanceState.getInt(KEY_SNAKE_SPEED);
        mCurrentScore = savedInstanceState.getInt(KEY_SCORE);
        mAlreadyLaunched = savedInstanceState.getBoolean(KEY_ALREADY_LAUNCHED);
        mWasGameOver = savedInstanceState.getBoolean(KEY_WAS_GAME_OVER);
*/
        MyLog.i("onRestoreInstanceState worked");
    }

    // 0 - from onGlobalLayout-method
    private void detectFieldParameters(ViewTreeObserver.OnGlobalLayoutListener listener) {
        // getting current screen size in pixels \
        mFieldPixelWidth = tvMainField.getWidth();
        MyLog.i("mFieldPixelWidth " + mFieldPixelWidth);

        FrameLayout flMain = (FrameLayout) findViewById(R.id.flMain);
        assert flMain != null;
        mFieldPixelHeight = flMain.getHeight();
        MyLog.i("mFieldPixelHeight " + mFieldPixelHeight);

        // now removing the listener - it's not needed any more \
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
            tvMainField.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        else
            //noinspection deprecation
            tvMainField.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
    } // end of detectFieldParameters-method \\

    // BEFORE START ================================================================================

    // 1 - from onGlobalLayout-method
    private void prepareTextField() {

        if (!mWasGameOver) {
            // here we get pixel width of a single symbol - initial TextView has only one symbol \
            tvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int measuredSymbolWidth = tvMainField.getMeasuredWidth();
            MyLog.i("measuredSymbolWidth " + measuredSymbolWidth);

            // getting our first valuable parameter - the size of main array \
            mSymbolsInFieldLine = mFieldPixelWidth / measuredSymbolWidth;
            MyLog.i("mSymbolsInFieldLine " + mSymbolsInFieldLine);
        }
        // clearing the text field to properly initialize it for game \
        tvMainField.setText(null);

        // now preparing our model and initializing text field \
        int i = 0, measuredTextHeight;
        mCharsArrayList = new ArrayList<>();
        do {
            // filling up out TextView to measure its lines and set initial state at once \
            char[] charArray = new char[mSymbolsInFieldLine + 1];
            // setting end element for a new line on the next array \
            charArray[mSymbolsInFieldLine] = '\n';
            // setting other elements to their default values \
            for (int j = 0; j < mSymbolsInFieldLine; j++) {
                charArray[j] = MyPSF.SPACE;
            }
            // now single char array is ready and has to be added to the list \
            mCharsArrayList.add(i, charArray);
//            MyLog.i("added " + new String(mCharsArrayList.get(i)));

            String previousText = tvMainField.getText().toString();
            String newText = previousText + new String(mCharsArrayList.get(i));
            tvMainField.setText(newText);
//            MyLog.i("newText \n" + newText);

            i++;

            tvMainField.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            measuredTextHeight = tvMainField.getMeasuredHeight();
//            MyLog.i("measuredTextHeight " + measuredTextHeight);

        } while (measuredTextHeight <= mFieldPixelHeight);

        mFieldLinesCount = i;
        MyLog.i("mFieldLinesCount " + mFieldLinesCount);
    } // end of prepareTextField-method \\

    /**
     * 2 - from onGlobalLayout-method
     */
    private void setFieldBorders() {
        for (int i = 0; i < mFieldLinesCount; i++)
            for (int j = 0; j < mSymbolsInFieldLine; j++)
                if (i == 0 || i == mFieldLinesCount - 1 || j == 0 || j == mSymbolsInFieldLine - 1)
                    mCharsArrayList.get(i)[j] = MyPSF.BORDER;
    }

    /**
     * 3 - from onGlobalLayout-method
     */
    private void setInitialSnake() {

        mSnake = new Snake();
        mSnakeSpeed = MyPSF.STARTING_SNAKE_SPEED;

        // defining start directions to properly set up the mSnake \
        switch (mRandom.nextInt(3) + 1) {
            case 0:
                mSnakeDirection = MyDirections.RIGHT;
                break;
            case 1:
                mSnakeDirection = MyDirections.UP;
                break;
            case 2:
                mSnakeDirection = MyDirections.LEFT;
                break;
            case 3:
                mSnakeDirection = MyDirections.DOWN;
        }

        for (int i = 0; i < MyPSF.STARTING_SNAKE_LENGTH; i++) {
            // here we define position of every mSnake's cell \
            int cellPositionX = 0, cellPositionY = 0;
            // setting tail in the opposite direction here - to free space for head \
            switch (mSnakeDirection) {
                case RIGHT: {
                    cellPositionX = mSymbolsInFieldLine / 2 - i;
                    cellPositionY = mFieldLinesCount / 2;
                    break;
                }
                case UP: {
                    cellPositionX = mSymbolsInFieldLine / 2;
                    cellPositionY = mFieldLinesCount / 2 + i;
                    break;
                }
                case LEFT: {
                    cellPositionX = mSymbolsInFieldLine / 2 + i;
                    cellPositionY = mFieldLinesCount / 2;
                    break;
                }
                case DOWN: {
                    cellPositionX = mSymbolsInFieldLine / 2;
                    cellPositionY = mFieldLinesCount / 2 - i;
                    break;
                }
            }
            // now it is time to create new mSnake cell \
            Snake.SnakeCell newCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
            // updating the mSnake's model \
            mSnake.addCell(i, newCell);
            // placing the this mSnake cell to our field \
            mCharsArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SNAKE;
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
        this method gets called after the mSnake is initialized -so we have to check collisions \
        i decided to do all in one cycle because of low probability of collisions \
*/
        // first of all clearing old place \
        char replaceToSpace = mCharsArrayList.get(mOldFoodPositionY)[mOldFoodPositionX];
        if (replaceToSpace != MyPSF.BORDER) // this might happen at the very start \
            mCharsArrayList.get(mOldFoodPositionY)[mOldFoodPositionX] = MyPSF.SPACE;
        // now everything is clear and we can set new food type and position \
        do {
            /*
            range for mRandom: +1 -2 = -1
            increased by one to include the whole range of values \
            decreased by two to exclude visible field borders \
            */
            mFoodPositionRow = mRandom.nextInt(mFieldLinesCount - 2) + 1;
            mFoodPositionSymbol = mRandom.nextInt(mSymbolsInFieldLine - 2) + 1;
            // -1 instead of +1 just to avoid placing food on the boards \
            MyLog.i("mRandom mFoodPositionRow " + mFoodPositionRow);
            MyLog.i("mRandom mFoodPositionSymbol " + mFoodPositionSymbol);
        } while (mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol] == MyPSF.SNAKE);

        mOldFoodPositionX = mFoodPositionSymbol;
        mOldFoodPositionY = mFoodPositionRow;

        mFoodType = mFoodTypeArray[mRandom.nextInt(mFoodTypeArray.length)];

        // substracting 1 because we know that these are indexes - counted from zero \
        mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol] = mFoodType;
//        mCharsArrayList.get(mFoodPositionRow - 1)[mFoodPositionSymbol - 1] = FOOD;
    }

    private void updateTextView() {

        StringBuilder newStringToSet = new StringBuilder();
        for (int i = 0; i < mFieldLinesCount; i++) {
            newStringToSet.append(mCharsArrayList.get(i));
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
            case R.id.viewScores:
                showScores();
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
            mSnakeSpeed = newSnakeSpeed;
        MyLog.i("newSnakeSpeed = " + newSnakeSpeed);
        return super.onOptionsItemSelected(item);
    }

    // UTILS =======================================================================================

    private boolean showScores() {

        // preparing view for the dialog \
        @SuppressLint("InflateParams")
        View dialogView = getLayoutInflater().inflate(R.layout.scores, null);

        // setting all elements for this view \
        MyTextView mtvCurrentScore = (MyTextView) dialogView.findViewById(R.id.mtvCurrentScore);
        MyTextView mtvCurrentTime = (MyTextView) dialogView.findViewById(R.id.mtvCurrentTime);
        MyTextView mtvBestScore = (MyTextView) dialogView.findViewById(R.id.mtvBestScore);
        MyTextView mtvBestTime = (MyTextView) dialogView.findViewById(R.id.mtvBestTime);
        mtvCurrentScore.setText(mCurrentScore);
        mtvCurrentTime.setText(DateFormat.format("mm:ss", mCurrentTime));
        mtvBestScore.setText(mBestScore);
        mtvBestTime.setText(DateFormat.format("mm:ss", mBestTime));

        // preparing builder for the dialog \
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // building and showing the dialog itself \
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        return true;
    }

    // LISTENER ====================================================================================

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibRight:
                mSnakeDirection = MyDirections.RIGHT;
                MyLog.i("turned Right");
                break;
            case R.id.ibUp:
                mSnakeDirection = MyDirections.UP;
                MyLog.i("turned Up");
                break;
            case R.id.ibLeft:
                mSnakeDirection = MyDirections.LEFT;
                MyLog.i("turned Left");
                break;
            case R.id.ibDown:
                mSnakeDirection = MyDirections.DOWN;
                MyLog.i("turned Down");
                break;
            case R.id.bStartPause:
                mVibrator.vibrate(MyPSF.SHORT_VIBRATION);
                if (mAlreadyLaunched) { // initial value is false \
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                    int stopTextColor = ContextCompat.getColor(this, R.color.primary_light);
                    tvMainField.setTextColor(stopTextColor);
                    bStartPause.setText(R.string.start);
                } else {
                    if (mWasGameOver) {
                        // everything is reset to later start from scratch \
                        prepareTextField();
                        setFieldBorders();
                        setInitialSnake();
                        setInitialFood();
                        mCurrentScore = 0;
                        mWasGameOver = false;
                    }
                    int startTextColor = ContextCompat.getColor(this, android.R.color.white);
                    tvMainField.setTextColor(startTextColor);
                    tvMainField.setBackgroundResource(R.color.primary_dark);
                    bStartPause.setText(R.string.pause);
                    // launching everything \
                    int delay = 600 / mSnakeSpeed;
                    mTimer = new Timer();
                    mTimer.schedule(new SnakeMoveTimerTask(), 0, delay);
                    mTimer.schedule(new TimeUpdateTimerTask(), 0, 1000);
//                    mTimer.schedule(new TimeUpdateTimerTask(), 0, 1); // requirements of dev-challenge \
                    mTimer.schedule(new FoodUpdateTimerTask(),
                            MyPSF.STARTING_UPDATE_FOOD_PERIOD,
                            MyPSF.STARTING_UPDATE_FOOD_PERIOD);
                }
                mAlreadyLaunched = !mAlreadyLaunched;
                break;
        }
    } // end of onClick-method \\

    private void gameOver() {
        mVibrator.vibrate(MyPSF.LONG_VIBRATION);
        mWasGameOver = true;
        mAlreadyLaunched = false;
        MyLog.i("game ended with mCurrentScore " + mCurrentScore);
        mTimer.cancel();
        mTimer.purge();
        mTimer = null;

        // checking if current result is the best \
        if (mCurrentScore > mBestScore) {
            mBestScore = mCurrentScore;
            saveNewBestResults();
        }

//        int rawCurrentTime = Integer.decode(String.valueOf(tvTime.getText()));
//        int currentTime = 60 * (rawCurrentTime / 100) + rawCurrentTime % 60;
        if (mCurrentTime > mBestTime) {
            mBestTime = mCurrentTime;
            saveNewBestResults();
        }

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

    private void saveNewBestResults() {
        getSharedPreferences(MyPSF.S_P_NAME, MODE_PRIVATE)
                .edit()
                .clear()
                .putInt(MyPSF.KEY_SCORE, mBestScore)
                .putLong(MyPSF.KEY_TIME, mBestTime)
                .commit();
    }

    // MOVEMENT ====================================================================================

    public enum MyDirections {RIGHT, UP, LEFT, DOWN}

    public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            MyLog.i("onFling");

            if (isRight(velocityX, velocityY)) {
                mSnakeDirection = MyDirections.RIGHT;
                return true;
            }
            if (isUp(velocityX, velocityY)) {
                mSnakeDirection = MyDirections.UP;
                return true;
            }
            if (isLeft(velocityX, velocityY)) {
                mSnakeDirection = MyDirections.LEFT;
                return true;
            }
            if (isDown(velocityX, velocityY)) {
                mSnakeDirection = MyDirections.DOWN;
                return true;
            }
            return false;
        }

        @SuppressWarnings("UnusedParameters")
        private boolean isRight(float velocityX, float velocityY) {
            return velocityX > 500;
//            return velocityX > 500 && (-100 < velocityY && velocityY < 100);
        }

        @SuppressWarnings("UnusedParameters")
        private boolean isUp(float velocityX, float velocityY) {
            return velocityY < -300;
//            return velocityY < -300 && (-100 < velocityX && velocityX < 100);
        }

        @SuppressWarnings("UnusedParameters")
        private boolean isLeft(float velocityX, float velocityY) {
            return velocityX < -500;
//            return velocityX < -500 && (-100 < velocityY && velocityY < 100);
        }

        @SuppressWarnings("UnusedParameters")
        private boolean isDown(float velocityX, float velocityY) {
            return velocityY > 300;
//            return velocityY > 300 && (-100 < velocityX && velocityX < 100);
        }
    }

    // TIMER CLASSES ===============================================================================

    /**
     * special class defined for repeating operations and usage of Timer \
     */
    private class SnakeMoveTimerTask extends TimerTask {

        // this method handles movement of the mSnake \
        @Override
        public void run() {
            // defining all reusable variables here \
            int newCellX = 0, newCellY = 0;
            Snake.SnakeCell snakeCell; // to use only one object for the whole mSnake \

            // 1 - saving information about the last cell position - before it will be freed in the end \
            snakeCell = mSnake.getCell(mSnake.getLength() - 1);
            int cellToFreeX = snakeCell.getIndexByX();
            int cellToFreeY = snakeCell.getIndexByY();

            // 2 - making single change to every cell in the model of mSnake - except the head \
            for (int i = mSnake.getLength() - 1; i > 0; i--) {
//            for (int i = 1; i < mSnake.getLength(); i++) {
                snakeCell = mSnake.getCell(i - 1); // to get the position of every previous cell
                // shifting 1 cell at a step \
                newCellX = snakeCell.getIndexByX();
                newCellY = snakeCell.getIndexByY();
                // moving every cell of the mSnake's body \
                snakeCell = mSnake.getCell(i); // to update current cell vith position of previous \
                snakeCell.setIndexByX(newCellX);
                snakeCell.setIndexByY(newCellY);
            }

            // 3 - finally moving mSnake's head to selected direction \
            snakeCell = mSnake.getCell(0); // for head of the mSnake
            switch (mSnakeDirection) {
                case RIGHT: // tail to right - head moves to left \
                    newCellX = snakeCell.getIndexByX() + 1;
                    newCellY = snakeCell.getIndexByY();
                    break;
                case UP: // tail is set up - head moves down \
                    newCellX = snakeCell.getIndexByX();
                    newCellY = snakeCell.getIndexByY() - 1;
                    break;
                case LEFT: // tail to left - head moves to right \
                    newCellX = snakeCell.getIndexByX() - 1;
                    newCellY = snakeCell.getIndexByY();
                    break;
                case DOWN: // tail is set down - head moves up \
                    newCellX = snakeCell.getIndexByX();
                    newCellY = snakeCell.getIndexByY() + 1;
                    break;
            }
            // saving info to the mSnake's model \
            snakeCell.setIndexByX(newCellX);
            snakeCell.setIndexByY(newCellY);

            // here we have just finished to update mSnake's model \
            MyLog.i("move done in mSnake's model");

            // now it's obvious to update model for field with mSnake's new data and display this all \
            for (int i = 0; i < mSnake.getLength(); i++) {
                snakeCell = mSnake.getCell(i);
                newCellX = snakeCell.getIndexByX();
                newCellY = snakeCell.getIndexByY();
/*
                // setting the mSnake body \
                mCharsArrayList.get(newCellY)[newCellX] = SNAKE;
                // recovering the field after the mSnake's tail \
                mCharsArrayList.get(cellToFreeY)[cellToFreeX] = SPACE;
*/
                // this is a crutch - the game somehow may fall after screen rotation \
                try {
                    // setting the mSnake body \
                    mCharsArrayList.get(newCellY)[newCellX] = MyPSF.SNAKE;
                    // recovering the field after the mSnake's tail \
                    mCharsArrayList.get(cellToFreeY)[cellToFreeX] = MyPSF.SPACE;
                } catch (IndexOutOfBoundsException ioobe) {
                    // ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException
                    MyLog.i("exception happened: " + ioobe.getMessage());
                    return;
                }
            }
            // updating the field with new mSnake's position \
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTextView();
                    String scoreComplex = getString(R.string.score) + " " + mCurrentScore;
                    tvScore.setText(scoreComplex);
                    MyLog.i("tvScore updated");
                }
            });

            // exit conditions check is the last thing to do \
            if (collisionHappened()) gameOver(); // this is the only way out from loop \
            else mCurrentScore++;

            // now handling eating of food and bonuses - it's taken by the head only \
            if (isFoodFound()) {
                eatFood();
                MyLog.i("food eaten");
            }
        } // end of run-method \\

        // VERIFICATIONS ===========================================================================

        private boolean isFoodFound() {
            return mSnake.getCell(0).getIndexByY() == mFoodPositionRow
                    && mSnake.getCell(0).getIndexByX() == mFoodPositionSymbol;
        }

        private void eatFood() {
            // user must be happy with such a vibration :)
            mVibrator.vibrate(MyPSF.SHORT_VIBRATION);

            int cellPositionX, cellPositionY;
            Snake.SnakeCell currentCell;
            MyLog.i("eaten = " + mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol]);

            switch (mFoodType) {

                case MyPSF.LENGTH_PLUS: // length +1
                    /*
                    i decided to add a new cell at the mSnake 's head - because we know the direction \
                    new cell will get visible only at the end of the move \
                    right now i 'm only updating the model - not the view \
                    */
                    cellPositionX = mFoodPositionSymbol + shiftAfterFood(mSnakeDirection, true);
                    cellPositionY = mFoodPositionRow + shiftAfterFood(mSnakeDirection, false);
                    currentCell = new Snake.SnakeCell(cellPositionX, cellPositionY);
                    mSnake.addCell(mSnake.getLength(), currentCell);
                    mSnakeSpeed++;
                    MyLog.i("LENGTH_PLUS taken!");
                    break;

                case MyPSF.LENGTH_MINUS: // length -1
                    // just removing the last cell \
                    if (mSnake.getLength() > 1) { // to avoid mSnake's dissappearing \
                        // first updating field to clear mSnake's tail - while it's available \
                        currentCell = mSnake.getCell(mSnake.getLength() - 1);
                        cellPositionX = currentCell.getIndexByX();
                        cellPositionY = currentCell.getIndexByY();
                        mCharsArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SPACE;
                        // now it is safe to update the model \
                        mSnake.removeCell(mSnake.getLength() - 1);
                        if (mSnakeSpeed > 1) mSnakeSpeed--;
                    }
                    MyLog.i("LENGTH_MINUS taken!");
                    break;

                case MyPSF.SPEED_UP: // speed +1
                    mSnakeSpeed++;
                    MyLog.i("SPEED_UP taken!");
                    break;

                case MyPSF.SPEED_SLOW: // speed -1
                    if (mSnakeSpeed > 1) // to avoid falling after division by zero \
                        mSnakeSpeed--;
                    MyLog.i("SPEED_SLOW taken!");
                    break;
            }
        }

        private int shiftAfterFood(MyDirections direction, boolean isForX) {
            if (isForX)
                switch (direction) {
                    case RIGHT: // to right - for X
                        return 1;
                    case LEFT: // to left - for X
                        return -1;
                    default: // up and dowm - 1, 3 cases don't affect X
                        return 0;
                }
            else
                switch (direction) {
                    case UP: // up - for Y
                        return 1;
                    case DOWN: // down - for Y
                        return -1;
                    default: // to right and left - 0, 2 cases don't affect Y
                        return 0;
                }
        }

        private boolean collisionHappened() {
            // we have only to check what happens to the mSnake's head - other cells are inactive \
            int snakeHeadY = mSnake.getCell(0).getIndexByY();
            int snakeHeadX = mSnake.getCell(0).getIndexByX();

            if (mSnake.getLength() <= 4) { // mSnake with less length cannot collide with itself \
                return touchedBounds(snakeHeadY, snakeHeadX);
            } else
                return touchedBounds(snakeHeadY, snakeHeadX) || touchedItself(snakeHeadY, snakeHeadX);
        }

        // connected to collisionHappened-method \
        private boolean touchedBounds(int snakeHeadY, int snakeHeadX) {
            // we can avoid loop here assuming that mSnake's head has index of 0 \
            return snakeHeadY == 0 || snakeHeadY == mFieldLinesCount - 1 ||
                    snakeHeadX == 0 || snakeHeadX == mSymbolsInFieldLine - 1;
        }

        // connected to collisionHappened-method \
        private boolean touchedItself(int snakeHeadY, int snakeHeadX) {
            // mSnake can collide with itself beginning only from fifth element = fourth index \
            for (int i = 4; i < mSnake.getLength(); i++) {
                Snake.SnakeCell snakeCell = mSnake.getCell(i);
                if (snakeHeadY == snakeCell.getIndexByY() && snakeHeadX == snakeCell.getIndexByX()) {
                    MyLog.i("touchedItself");
                    return true;
                }
            }
            return false;
        }
    } // end of SnakeMoveTimerTask-class \\

    private class FoodUpdateTimerTask extends TimerTask {

        @Override
        public void run() {
            updateFood();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTextView();
                }
            });
            MyLog.i("mFoodType updated");
        }
    }

    private class TimeUpdateTimerTask extends TimerTask {
        // code here is called every milisecond - it has to be really fast \
        private long initialSystemTime = System.currentTimeMillis();

        @Override
        public void run() {
            mCurrentTime = System.currentTimeMillis() - initialSystemTime;
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
            final String stringToSet = String.valueOf("Time " + DateFormat.format("mm:ss", mCurrentTime));
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