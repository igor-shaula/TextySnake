package com.tryings.my.dev_challenge_2016.util;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {
    @Override
    public void run() {
        MyLog.i("move done");
    }
}