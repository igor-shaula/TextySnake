package com.igor.shaula.snake_in_text.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MyTouchListener implements View.OnTouchListener {

    private Context context;

    // universal constructor for lists \
    public MyTouchListener(Context context) {
        this.context = context;
    }

    // preparing the gesture detector for onItemTouchListener \
    private GestureDetector mGestureDetector = new GestureDetector(
            context,
            new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    // detecting move along X axis \
                    int detectedDirection;
                    if (velocityX > velocityY)
                        e1.getAxisValue()
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (mGestureDetector.onTouchEvent(motionEvent)) {

            return true;
        } else return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}