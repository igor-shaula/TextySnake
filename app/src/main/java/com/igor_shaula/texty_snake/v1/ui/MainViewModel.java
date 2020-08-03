package com.igor_shaula.texty_snake.v1.ui;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.igor_shaula.texty_snake.v1.R;
import com.igor_shaula.texty_snake.v1.logic.FourDirections;
import com.igor_shaula.texty_snake.v1.logic.GameLogic;
import com.igor_shaula.texty_snake.v1.utils.L;
import com.igor_shaula.texty_snake.v1.utils.MyPSF;

public final class MainViewModel extends ViewModel {

    private GameLogic logic;

    // field only parameters
    private int mFieldPixelWidth, mFieldPixelHeight; // in pixels
    private int mSymbolsInFieldLine, mFieldLinesCount; // in items - for arrays \
    @NonNull
    private MutableLiveData<Integer> mainFieldTextColorId = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<Integer> mainFieldBackgroundColorId = new MutableLiveData<>();

    // snake only parameters
    private int mSnakeSpeed = MyPSF.STARTING_SNAKE_SPEED;
    @Nullable
    private FourDirections mSnakeDirection, prohibitedDirection;

    // game parameters
    private int mCurrentScore;
    private long mCurrentTime;
    @NonNull
    private MutableLiveData<Integer> mldBestScore = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<Long> mldBestTime = new MutableLiveData<>();
    private boolean mGameEnded = false, mGamePausedSwitch = false;

    // LINKING =====================================================================================

    public void initGameLogic(@NonNull MainActivity ui) {
        logic = new GameLogic(ui);
        mainFieldTextColorId.setValue(R.color.primary_dark);
        mainFieldBackgroundColorId.setValue(android.R.color.white);
        mldBestScore.setValue(0);
        mldBestTime.setValue(0L);
    }

    public void destroyGameLogic() {
        logic.clearUiLink();
        logic = null;
    }

    // SETTERS =====================================================================================

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

    public void setFieldLinesCount(int number) {
        mFieldLinesCount = number;
    }

    // GETTERS =====================================================================================

    public int getCurrentScore() {
        return mCurrentScore;
    }

    public long getCurrentTime() {
        return mCurrentTime;
    }

    public int getSnakeSpeed() {
        return mSnakeSpeed;
    }

    @NonNull
    public MutableLiveData<Integer> getMainFieldTextColorId() {
        return mainFieldTextColorId;
    }

    @NonNull
    public MutableLiveData<Integer> getMainFieldBackgroundColorId() {
        return mainFieldBackgroundColorId;
    }

    @NonNull
    public MutableLiveData<Integer> getMldBestScore() {
        return mldBestScore;
    }

    @NonNull
    public MutableLiveData<Long> getMldBestTime() {
        return mldBestTime;
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

    public void onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        // detecting current direction for desired move \
        final float deltaX = e2.getX() - e1.getX();
        final float deltaY = e2.getY() - e1.getY();
        mSnakeDirection = logic.detectSnakeDirection(deltaX, deltaY, velocityX, velocityY);
        // checking if direction is not reversed as before \
        if (mSnakeDirection == prohibitedDirection) {
            L.w("onFling ` very strange case: mSnakeDirection == prohibitedDirection");
            mSnakeDirection = null;
        } else
            // saving current last direction conditions for the next swipe \
            prohibitedDirection = logic.detectProhibitedDirection(mSnakeDirection);
    }

    public void detectSymbolsInFieldLine(int measuredSymbolWidth) {
        mSymbolsInFieldLine = mFieldPixelWidth / measuredSymbolWidth;
    }

    public void onShowDialogAction() {
        mGamePausedSwitch = true;
        logic.stopAllTimers();
        mainFieldTextColorId.setValue(R.color.primary_light);
    }

    public void actionStartGame() {

        mGamePausedSwitch = false;
        mGameEnded = false;

        mainFieldTextColorId.setValue(android.R.color.white);
        mainFieldBackgroundColorId.setValue(R.color.primary_dark);

        // launching everything \
        int delay; // amount of time for game to wait = realization of speed \
        try {
            delay = 500 / mSnakeSpeed;
        } catch (ArithmeticException ae) {
            L.i("ArithmeticException with delay = 500 / 0: mSnakeSpeed = 0");
            actionEndGame();
            return;
        }
        logic.startAllTimers(delay);
    }

    public void actionEndGame() {

        mGameEnded = true;
        mGamePausedSwitch = true;

        logic.stopAllTimers();

        // checking if current result is the best \
        if (mCurrentScore > mldBestScore.getValue() || mCurrentTime > mldBestTime.getValue()) {
            if (mCurrentScore > mldBestScore.getValue())
                mldBestScore.setValue(mCurrentScore);
            if (mCurrentTime > mldBestTime.getValue())
                mldBestTime.setValue(mCurrentTime);
//            ui.saveNewBestResults(bestScore, bestTime);
        }

        // resetting the start-stop button to its primary state \
        mainFieldTextColorId.setValue(android.R.color.primary_text_light);
        mainFieldBackgroundColorId.setValue(R.color.primary_light);
        L.i("game ended with mCurrentScore " + mCurrentScore);
    }

    // main repeatable sequence of steps \
    public void prepareGameIn4Steps() {
        logic.step_1_prepareTextField(mSymbolsInFieldLine, mFieldPixelHeight); // 1
        logic.step_2_setFieldBorders(mFieldLinesCount, mSymbolsInFieldLine); // 2
        logic.step_3_setInitialSnake(mSnakeDirection, mSymbolsInFieldLine, mFieldLinesCount); // 3
        logic.step_4_setInitialFood(mFieldLinesCount, mSymbolsInFieldLine); // 4
    }
}