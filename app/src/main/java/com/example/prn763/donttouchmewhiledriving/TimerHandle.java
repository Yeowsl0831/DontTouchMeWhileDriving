package com.example.prn763.donttouchmewhiledriving;

import android.os.CountDownTimer;
import android.util.Log;

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

    @Override
    public void onFinish() {
        processTimeOutEvent();
        Log.d(TAG, "onFinish");
    }

    @Override
    public void onTick(long l) {
        Log.d(TAG, "Time:"+l);
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

            Log.d(TAG, "Timer onCancel");
        }
    }

    public boolean isTimerRunning(){
        return mTimerState;
    }
}
