package com.igor.shaula.snake_in_text.gestures;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.igor.shaula.snake_in_text.activity.MainActivity;
import com.igor.shaula.snake_in_text.utils.MyLog;

public class _MyGestureListener extends GestureDetector.SimpleOnGestureListener {

    MainActivity mainActivity;
    private OnDirectionChanged onDirectionChangedListener = mainActivity;

    public _MyGestureListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        MyLog.i("onFling");
        MyDirections detectedDirection;

        if (isRight(velocityX, velocityY)) {
            detectedDirection = MyDirections.RIGHT;
            onDirectionChangedListener.changeDirection(detectedDirection);
            return true;
        }
        if (isUp(velocityX, velocityY)) {
            detectedDirection = MyDirections.UP;
            onDirectionChangedListener.changeDirection(detectedDirection);
            return true;
        }
        if (isLeft(velocityX, velocityY)) {
            detectedDirection = MyDirections.LEFT;
            onDirectionChangedListener.changeDirection(detectedDirection);
            return true;
        }
        if (isDown(velocityX, velocityY)) {
            detectedDirection = MyDirections.DOWN;
            onDirectionChangedListener.changeDirection(detectedDirection);
            return true;
        }
        return false;
    }

    private boolean isRight(float velocityX, float velocityY) {
        return velocityX > 500.0 && (velocityY < 100.0 && velocityY > -100.0);
    }

    private boolean isUp(float velocityX, float velocityY) {
        return velocityY > 300.0 && (velocityX < 100.0 && velocityX > -100.0);
    }

    private boolean isLeft(float velocityX, float velocityY) {
        return velocityX < -500.0 && ((velocityY < 100.0 && velocityY > -100.0));
    }

    private boolean isDown(float velocityX, float velocityY) {
        return velocityY < -300.0 && (velocityX < 100.0 && velocityX > -100.0);
    }

    public interface OnDirectionChanged {
        void changeDirection(MyDirections newDirection);
    }
}
