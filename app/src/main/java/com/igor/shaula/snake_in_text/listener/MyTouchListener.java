package com.igor.shaula.snake_in_text.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.igor.shaula.snake_in_text.util.MyLog;

public class MyTouchListener implements View.OnTouchListener {

    private Context context;
    GestureDetector mGestureDetector;

    // universal constructor for lists \
    public MyTouchListener(Context context) {
        this.context = context;
        mGestureDetector = new GestureDetector(
                context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        MyLog.i("onFling");

                        int detectedDirection = 0;

                        // detecting move along X axis \
                        if (velocityY < 10) {
                            int directionX = (int) (e2.getAxisValue(MotionEvent.AXIS_X) - e1.getAxisValue(MotionEvent.AXIS_X));
                            MyLog.i("directionX = " + directionX);
                            if (directionX > 0) detectedDirection = 0;
                            else detectedDirection = 2;
                        }
                        // detecting move along Y axis \
                        if (velocityX < 10) {
                            int directionY = (int) (e2.getAxisValue(MotionEvent.AXIS_Y) - e1.getAxisValue(MotionEvent.AXIS_Y));
                            MyLog.i("directionY = " + directionY);
                            if (directionY > 0) detectedDirection = 1;
                            else detectedDirection = 3;
                        }
                        MyLog.i("detectedDirection = " + detectedDirection);

                        return true;
//                    return super.onFling(e1, e2, velocityX, velocityY);
                    }

/*
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    MyLog.i("onSingleTapUp");
                    return false;
                }
*/
                });
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        MyLog.i("onGenericMotion");

        return mGestureDetector.onTouchEvent(motionEvent);
    }

    // preparing the gesture detector for onItemTouchListener \


    /*
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (mGestureDetector.onTouchEvent(motionEvent)) {

                return true;
            } else return false;
        }
    */

}