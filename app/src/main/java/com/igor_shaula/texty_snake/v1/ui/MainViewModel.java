package com.igor_shaula.texty_snake.v1.ui;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;

import com.igor_shaula.texty_snake.v1.R;
import com.igor_shaula.texty_snake.v1.logic.FourDirections;
import com.igor_shaula.texty_snake.v1.logic.GameLogic;
import com.igor_shaula.texty_snake.v1.utils.L;
import com.igor_shaula.texty_snake.v1.utils.MyPSF;

import java.util.Timer;

import static com.igor_shaula.texty_snake.v1.logic.FourDirections.DOWN;
import static com.igor_shaula.texty_snake.v1.logic.FourDirections.LEFT;
import static com.igor_shaula.texty_snake.v1.logic.FourDirections.RIGHT;
import static com.igor_shaula.texty_snake.v1.logic.FourDirections.UP;

public final class MainViewModel extends ViewModel {

    private GameLogic logic;

    private int mFieldPixelWidth, mFieldPixelHeight; // in pixels
    private boolean mGameEnded = false, mGamePausedSwitch = false;

    // game parameters \
    private int mCurrentScore, bestScore;
    private long mCurrentTime, bestTime;

    private int mSymbolsInFieldLine, mFieldLinesCount; // in items - for arrays \
    private Timer mTimer;

    private int mSnakeSpeed = MyPSF.STARTING_SNAKE_SPEED;
    private FourDirections mSnakeDirection, prohibitedDirection;

    public void initGameLogic(@NonNull MainActivity ui) {
        logic = new GameLogic(ui);
    }

    public void destroyGameLogic() {
        logic.clearUiLink();
        logic = null;
    }

    // SETTERS =====================================================================================

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

    public void setSnakeSpeed(int newSnakeSpeed) {
        mSnakeSpeed = newSnakeSpeed;
    }

    // GETTERS =====================================================================================

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

    // REACTIONS ===================================================================================

    public void onDoubleTap() {
        L.i("onDoubleTap - start / pause");
        L.i("mGamePausedSwitch = " + mGamePausedSwitch);
        L.i("mGameEnded = " + mGameEnded);

        // 1 - prepare new field after game over \
        if (mGameEnded) prepareGameIn4Steps();

        // 2 - pause or continue game \
        if (!mGamePausedSwitch)
            onShowDialogAction();
        else actionStartGame();
    }

    public void onLongPress() {
        L.i("onLongPress - full restart from zero");
        L.i("mGamePausedSwitch = " + mGamePausedSwitch);
        L.i("mGameEnded = " + mGameEnded);

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
                mSnakeDirection = LEFT;
            } else {
                L.i("turned right");
                mSnakeDirection = RIGHT;
            }
        else { // moving along Y-axis was more noticeable than along X-axis \
            if (isUpMove(velocityX, velocityY)) {
                L.i("turned up");
                mSnakeDirection = UP;
            } else {
                L.i("turned down");
                mSnakeDirection = DOWN;
            }
        }

        // checking if direction is not reversed as before \
        if (mSnakeDirection == prohibitedDirection)
            mSnakeDirection = null;
        else
            // saving current last direction conditions for the next swipe \
            switch (mSnakeDirection) {
                case LEFT:
                    prohibitedDirection = RIGHT;
                    break;
                case RIGHT:
                    prohibitedDirection = LEFT;
                    break;
                case UP:
                    prohibitedDirection = DOWN;
                    break;
                case DOWN:
                    prohibitedDirection = UP;
                    break;
            }
    }

    public void detectSymbolsInFieldLine(int measuredSymbolWidth) {
        mSymbolsInFieldLine = mFieldPixelWidth / measuredSymbolWidth;
    }

    public void onShowDialogAction() {

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
        mTimer.schedule(new GameLogic.SnakeMoveTimerTask(), 0, delay);
        mTimer.schedule(new GameLogic.TimeUpdateTimerTask(), 0, 1000);
//                    mTimer.schedule(new TimeUpdateTimerTask(), 0, 1); // requirements of dev-challenge \
        mTimer.schedule(new GameLogic.FoodUpdateTimerTask(),
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
            ui.saveNewBestResults(bestScore, bestTime);
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

    // main repeatable sequence of steps \
    public void prepareGameIn4Steps() {
        logic.step_1_prepareTextField(); // 1
        logic.step_2_setFieldBorders(); // 2
        logic.step_3_setInitialSnake(); // 3
        logic.step_4_setInitialFood(); // 4
    }
}