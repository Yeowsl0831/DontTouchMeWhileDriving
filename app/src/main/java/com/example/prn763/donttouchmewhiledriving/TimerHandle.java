package com.example.prn763.donttouchmewhiledriving;

import android.os.CountDownTimer;

/**
 * Created by PRN763 on 1/22/2018.
 */

public abstract class TimerHandle extends CountDownTimer{
    private boolean mTimerState;

    public TimerHandle(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        mTimerState = false;
    }

    public abstract void processTimeOutEvent();

    @Override
    public void onFinish() {
        processTimeOutEvent();
    }

    @Override
    public void onTick(long l) {

    }

    public void onStart(){
        this.start();
        mTimerState = true;
    }

    public void onCancel(){
        this.cancel();
        mTimerState = false;
    }

    public boolean getTimerEnableState(){
        return mTimerState;
    }
}
