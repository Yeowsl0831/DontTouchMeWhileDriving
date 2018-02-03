package com.example.prn763.donttouchmewhiledriving;

import android.os.CountDownTimer;

/**
 * Created by PRN763 on 1/22/2018.
 */

public abstract class TimerHandle extends CountDownTimer{
    private final static String TAG = "TimerHandle";
    private boolean mTimerState;

    public TimerHandle(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        mTimerState = false;
    }

    public abstract void processTimeOutEvent();
    public abstract void processOnTickEvent(long l);

    @Override
    public void onFinish() {
        processTimeOutEvent();
        //timer expired
        if(mTimerState == true){
            mTimerState = false;
        }
    }

    @Override
    public void onTick(long l) {
        processOnTickEvent(l);
    }

    public void onStart(){
        if(mTimerState == false){
            this.start();
            mTimerState = true;
        }
    }

    public void onCancel(boolean isUiUpdate){
        if(mTimerState == true){
            this.cancel();
            mTimerState = false;

            if(isUiUpdate == true){
                processTimeOutEvent();
            }
        }
    }

    public boolean isTimerRunning(){
        return mTimerState;
    }
}
