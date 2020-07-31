package com.igor_shaula.texty_snake.v1.logic;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.igor_shaula.texty_snake.v1.R;
import com.igor_shaula.texty_snake.v1.entity.Snake;
import com.igor_shaula.texty_snake.v1.ui.MainActivity;
import com.igor_shaula.texty_snake.v1.utils.L;
import com.igor_shaula.texty_snake.v1.utils.MyPSF;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

// this class describes game engine and must not have any Android specific dependencies
public final class GameLogic {

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

    private Random mRandom = new Random();
    private Timer mTimer;

    // definition of the snake model \
    private Snake mSnake;
    private int mSnakeSpeed = MyPSF.STARTING_SNAKE_SPEED;
    private FourDirections mSnakeDirection, prohibitedDirection;

    // game parameters \
    private boolean mGameEnded = false, mGamePausedSwitch = false;
    private int mCurrentScore, bestScore;
    private long mCurrentTime, bestTime;

    @NonNull
    private MainActivity ui;

    // TODO: 31.07.2020 replace passing MainActivity here by an interface - avoid direct linking
    public GameLogic(@NonNull MainActivity ui) {
        this.ui = ui;
    }

    // SETTERS =====================================================================================

    public void onShowScoresClick() {
        ui.showScoresDialog();
    }

    public void onSetSpeedClick() {
        ui.showSetSpeedDialog();
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public void setBestTime(long bestTime) {
        this.bestTime = bestTime;
    }

    public void setFieldPixelWidth(int width) {
        mFieldPixelWidth = width;
        L.i("fieldPixelWidth " + mFieldPixelWidth);
    }

    public void setFieldPixelHeight(int height) {
        mFieldPixelHeight = height;
        L.i("mFieldPixelHeight " + mFieldPixelHeight);
    }

//    public void setSymbolsInFieldLine(int number) {
//        mSymbolsInFieldLine = number;
//        L.i("mSymbolsInFieldLine " + mSymbolsInFieldLine);
//    }

    public void setFieldLinesCount(int i) {
        mFieldLinesCount = i;
        L.i("mFieldLinesCount " + mFieldLinesCount);
    }

    public void setCharArrayList(@NonNull ArrayList<char[]> mCharsArrayList) {
        this.mCharsArrayList = mCharsArrayList;
    }

    // GETTERS =====================================================================================

    public void onUserGuideLongClick() {
        initializeGame();
    }

    // main repeatable sequence of steps \
    public void prepareGameIn4Steps() {
        step_1_prepareTextField(); // 1
        step_2_setFieldBorders(); // 2
        step_3_setInitialSnake(); // 3
        step_4_setInitialFood(); // 4
    }

    // PRIVATE =====================================================================================

    private void initializeGame() {
        L.i("initializeGame started");
        ui.changeTextFields();
        ui.measureAvailableSpace();
    }

    // 2 - from onGlobalLayout-method
    public void step_1_prepareTextField() {

        // clearing the text field to properly initialize it for game \
        ui.setMainFieldText(null);

        // fixing the total height of the line in our main field \
        ui.setMainFieldTextSquareSymbols();

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
            setCharArrayList(mCharsArrayList);
//            L.i("added " + new String(mCharsArrayList.get(i)));

            final String previousText = ui.getMainFieldText();
            final String newText = previousText + new String(mCharsArrayList.get(i));
            ui.setMainFieldText(newText);
//            L.i("newText \n" + newText);

            i++;

            ui.measureMainField();
            measuredTextHeight = ui.getMainFieldHeight();
//            L.i("measuredTextHeight " + measuredTextHeight);

        } while (measuredTextHeight <= mFieldPixelHeight);

        setFieldLinesCount(i);
    } // step_1_prepareTextField \\

    private void step_2_setFieldBorders() {
        for (int i = 0; i < mFieldLinesCount; i++)
            for (int j = 0; j < mSymbolsInFieldLine; j++)
                if (i == 0 || i == mFieldLinesCount - 1 || j == 0 || j == mSymbolsInFieldLine - 1)
                    mCharsArrayList.get(i)[j] = MyPSF.BORDER;
    }

    // 3 - from onGlobalLayout-method
    private void step_3_setInitialSnake() {

        mSnake = new Snake();

        // defining start directions to properly set up the mSnake \
        switch (mRandom.nextInt(3) + 1) {
            case 0:
                mSnakeDirection = FourDirections.RIGHT;
                break;
            case 1:
                mSnakeDirection = FourDirections.UP;
                break;
            case 2:
                mSnakeDirection = FourDirections.LEFT;
                break;
            case 3:
                mSnakeDirection = FourDirections.DOWN;
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
        updateMainField();
    } // end of step_3_setInitialSnake-method \\

    // 4 - from onGlobalLayout-method
    private void step_4_setInitialFood() {
        updateFood();
        updateMainField();
    } // end of step_4_setInitialFood-method \\

    // UTILS =======================================================================================

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
            range for mRandom:
            decreased by 2 to exclude visible field borders \
            again decreased by 2 to exclude near-border dangerous positions \
            increased by 1 to include the whole range of values because of method specifics \
            */
            mFoodPositionRow = mRandom.nextInt(mFieldLinesCount - 2 - 2) + 1;
            mFoodPositionSymbol = mRandom.nextInt(mSymbolsInFieldLine - 2 - 2) + 1;
            // -1 instead of +1 just to avoid placing food on the boards \
            L.i("mRandom mFoodPositionRow " + mFoodPositionRow);
            L.i("mRandom mFoodPositionSymbol " + mFoodPositionSymbol);
        } while (mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol] == MyPSF.SNAKE);

        mOldFoodPositionX = mFoodPositionSymbol;
        mOldFoodPositionY = mFoodPositionRow;

        mFoodType = mFoodTypeArray[mRandom.nextInt(mFoodTypeArray.length)];

        // subtracting 1 because we know that these are indexes - counted from zero \
        mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol] = mFoodType;
//        mCharsArrayList.get(mFoodPositionRow - 1)[mFoodPositionSymbol - 1] = FOOD;
    }

    private void updateMainField() {

        final StringBuilder newStringToSet = new StringBuilder();
        for (int i = 0; i < mFieldLinesCount; i++) {
            newStringToSet.append(mCharsArrayList.get(i));
        }
        ui.setMainFieldText(newStringToSet.toString());
    }

    public void saveNewBestResults() {
        ui.getSharedPreferences(MyPSF.S_P_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .putInt(MyPSF.KEY_SCORE, bestScore)
                .putLong(MyPSF.KEY_TIME, bestTime)
                .apply();
    }

    public void detectSymbolsInFieldLine(int measuredSymbolWidth) {
        mSymbolsInFieldLine = mFieldPixelWidth / measuredSymbolWidth;
    }


    public void actionPauseGame() {

        mGamePausedSwitch = true;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        ui.setMainFieldTextColor(ContextCompat.getColor(ui.getApplicationContext(), R.color.primary_light));
    }

    public void actionStartGame() {

        mGamePausedSwitch = false;
        mGameEnded = false;

        int startTextColor = ContextCompat.getColor(ui.getApplicationContext(), android.R.color.white);
        ui.setMainFieldTextColor(startTextColor);
        ui.setMFTBackgroundResource(R.color.primary_dark);
        // launching everything \
        int delay; // amount of time for game to wait = realization of speed \
        try {
            delay = 500 / mSnakeSpeed;
        } catch (ArithmeticException ae) {
            L.i("ArithmeticException with delay = 500 / 0: mSnakeSpeed = 0");
            actionEndGame();
            return;
        }
        mTimer = new Timer();
        mTimer.schedule(new SnakeMoveTimerTask(), 0, delay);
        mTimer.schedule(new TimeUpdateTimerTask(), 0, 1000);
//                    mTimer.schedule(new TimeUpdateTimerTask(), 0, 1); // requirements of dev-challenge \
        mTimer.schedule(new FoodUpdateTimerTask(),
                MyPSF.STARTING_UPDATE_FOOD_PERIOD,
                MyPSF.STARTING_UPDATE_FOOD_PERIOD);
    }

    public void actionEndGame() {

        mGameEnded = true;
        mGamePausedSwitch = true;

        // stopping all timers \
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        // checking if current result is the best \
        if (mCurrentScore > bestScore || mCurrentTime > bestTime) {
            if (mCurrentScore > bestScore)
                bestScore = mCurrentScore;
            if (mCurrentTime > bestTime)
                bestTime = mCurrentTime;
            saveNewBestResults();
        }

        // resetting the start-stop button to its primary state \
        ui.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int endTextColor = ContextCompat.getColor(ui, android.R.color.primary_text_light);
                ui.setMainFieldTextColor(endTextColor);
                ui.setMFTBackgroundResource(R.color.primary_light);
            }
        });
        L.i("game ended with mCurrentScore " + mCurrentScore);
    }

    public int getCurrentScore() {
        return mCurrentScore;
    }

    public long getCurrentTime() {
        return mCurrentTime;
    }

    public int getBestScore() {
        return bestScore;
    }

    public long getBestTime() {
        return bestTime;
    }

    public int getSnakeSpeed() {
        return mSnakeSpeed;
    }

    public void setSnakeSpeed(int newSnakeSpeed) {
        mSnakeSpeed = newSnakeSpeed;
    }

    public void onDoubleTap() {
        L.i("onDoubleTap - start / pause");
        L.i("mGamePausedSwitch = " + mGamePausedSwitch);
        L.i("mGameEnded = " + mGameEnded);

        // 1 - prepare new field after game over \
        if (mGameEnded) prepareGameIn4Steps();

        // 2 - pause or continue game \
        if (!mGamePausedSwitch)
            actionPauseGame();
        else actionStartGame();
    }

    public void onLongPress() {
        L.i("onLongPress - full restart from zero");
        L.i("mGamePausedSwitch = " + mGamePausedSwitch);
        L.i("mGameEnded = " + mGameEnded);

        ui.vibrate();

        actionEndGame();
        mCurrentScore = 0;

        // 1 - preparing the field for the new game \
        prepareGameIn4Steps();

        // 2 - restarting the game \
        actionStartGame();
    }

    public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // detecting current direction for desired move \
        if (Math.abs(e2.getX() - e1.getX()) > Math.abs(e2.getY() - e1.getY()))
            if (isLeftMove(velocityX, velocityY)) {
                L.i("turned left");
                mSnakeDirection = FourDirections.LEFT;
            } else {
                L.i("turned right");
                mSnakeDirection = FourDirections.RIGHT;
            }
        else { // moving along Y-axis was more noticeable than along X-axis \
            if (isUpMove(velocityX, velocityY)) {
                L.i("turned up");
                mSnakeDirection = FourDirections.UP;
            } else {
                L.i("turned down");
                mSnakeDirection = FourDirections.DOWN;
            }
        }

        // checking if direction is not reversed as before \
        if (mSnakeDirection == prohibitedDirection)
            mSnakeDirection = null;
        else
            // saving current last direction conditions for the next swipe \
            switch (mSnakeDirection) {
                case LEFT:
                    prohibitedDirection = FourDirections.RIGHT;
                    break;
                case RIGHT:
                    prohibitedDirection = FourDirections.LEFT;
                    break;
                case UP:
                    prohibitedDirection = FourDirections.DOWN;
                    break;
                case DOWN:
                    prohibitedDirection = FourDirections.UP;
                    break;
            }
    }

    private boolean isLeftMove(float velocityX, float velocityY) {
        return velocityX < 0 // determining the LEFT direction of the swipe \
//                    && absDeltaX > absDeltaY // assuring that this swipe was more along X-axis than along Y \
                && Math.abs(velocityX) > Math.abs(velocityY); // checking if we're right completely \
    }

    private boolean isUpMove(float velocityX, float velocityY) {
        return velocityY < 0 // determining the UP direction of the swipe \
//                    && absDeltaX < absDeltaY // assuring that this swipe was more along Y-axis than along X \
                && Math.abs(velocityX) < Math.abs(velocityY); // checking if we're right completely \
    }

    // TIMER CLASSES ===============================================================================

    // special class defined for repeating operations and usage of Timer \
    private class SnakeMoveTimerTask extends TimerTask {

        // this method handles movement of the mSnake \
        @Override
        public void run() {

            // is null when direction is prohibited - have to check it here to avoid crash \
            if (mSnakeDirection == null) return;

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
                snakeCell = mSnake.getCell(i); // to update current cell with position of previous \
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
/*
            // here we have just finished to update mSnake's model \
*/
            // now it's obvious to update model for field with mSnake's new data and display this all \
            for (int i = 0; i < mSnake.getLength(); i++) {
                snakeCell = mSnake.getCell(i);
                newCellX = snakeCell.getIndexByX();
                newCellY = snakeCell.getIndexByY();

                // setting the mSnake body \
                mCharsArrayList.get(newCellY)[newCellX] = MyPSF.SNAKE;
                // recovering the field after the mSnake's tail \
                mCharsArrayList.get(cellToFreeY)[cellToFreeX] = MyPSF.SPACE;
            }
            // updating the field with new mSnake's position \
            ui.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMainField();
                    String scoreComplex = ui.getString(R.string.score) + mCurrentScore;
                    ui.setScoreText(scoreComplex);
                }
            });

            // exit conditions check is the last thing to do \
            if (collisionHappened()) actionEndGame(); // this is the only way out from loop \
            else mCurrentScore++;

            // now handling eating of food and bonuses - it's taken by the head only \
            if (isFoodFound()) eatFood();
        } // end of run-method \\

        // VERIFICATIONS ===========================================================================

        private boolean isFoodFound() {
            return mSnake.getCell(0).getIndexByY() == mFoodPositionRow
                    && mSnake.getCell(0).getIndexByX() == mFoodPositionSymbol;
        }

        private void eatFood() {
            // user must be happy with such a vibration :)
            ui.vibrate();

            int cellPositionX, cellPositionY;
            Snake.SnakeCell currentCell;
            L.i("eaten = " + mCharsArrayList.get(mFoodPositionRow)[mFoodPositionSymbol]);

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
                    L.i("LENGTH_PLUS taken!");
                    mCurrentScore += 100;
                    break;

                case MyPSF.LENGTH_MINUS: // length -1
                    // just removing the last cell \
                    if (mSnake.getLength() > 1) { // to avoid mSnake's disappearing \
                        // first updating field to clear mSnake's tail - while it's available \
                        currentCell = mSnake.getCell(mSnake.getLength() - 1);
                        cellPositionX = currentCell.getIndexByX();
                        cellPositionY = currentCell.getIndexByY();
                        mCharsArrayList.get(cellPositionY)[cellPositionX] = MyPSF.SPACE;
                        // now it is safe to update the model \
                        mSnake.removeCell(mSnake.getLength() - 1);
                        if (mSnakeSpeed > 1) mSnakeSpeed--;
                    }
                    L.i("LENGTH_MINUS taken!");
                    mCurrentScore -= 100;
                    break;

                case MyPSF.SPEED_UP: // speed +1
                    if (mSnakeSpeed < 9) {
                        mSnakeSpeed++;
                        mCurrentScore += 50;
                    } else mCurrentScore += 150;
                    L.i("SPEED_UP taken!");
                    break;

                case MyPSF.SPEED_SLOW: // speed -1
                    if (mSnakeSpeed > 1) // to avoid falling after division by zero \
                        mSnakeSpeed--;
                    L.i("SPEED_SLOW taken!");
                    mCurrentScore -= 50;
                    break;
            }
        } // end of eatFood-method \

        private int shiftAfterFood(FourDirections direction, boolean isForX) {
            if (isForX)
                switch (direction) {
                    case RIGHT: // to right - for X
                        return 1;
                    case LEFT: // to left - for X
                        return -1;
                    default: // up and down - 1, 3 cases don't affect X
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
                    L.i("touchedItself");
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
            ui.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMainField();
                }
            });
            L.i("mFoodType updated");
        }
    }

    private class TimeUpdateTimerTask extends TimerTask {
        // code here is called every millisecond - it has to be really fast \
        private long initialSystemTime = System.currentTimeMillis();

        @Override
        public void run() {
            mCurrentTime = System.currentTimeMillis() - initialSystemTime;

            final String stringToSet = ui.getString(R.string.time)
                    + DateFormat.format("mm:ss", mCurrentTime);
            ui.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ui.setTimeText(stringToSet);
                }
            });
        } // end of run-method \\
    } // end of TimeUpdateTimerTask-class \\
}
