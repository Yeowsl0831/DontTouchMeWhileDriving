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
    public abstract void processOnTickEvent(long l);

    @Override
    public void onFinish() {
        Log.e(TAG, "Timer Handle:onFinish");
        processTimeOutEvent();
        //timer expired
        if(mTimerState == true){
            mTimerState = false;
        }
    }

    @Override
    public void onTick(long l) {
        Log.e(TAG, "Timer Handle:onTick:"+l);
        processOnTickEvent(l);
    }

    public void onStart(){
        Log.e(TAG, "Timer Handle:onStart");
        if(mTimerState == false){
            this.start();
            mTimerState = true;
        }
    }

    public void onCancel(boolean isUiUpdate){
        Log.e(TAG, "Timer Handle:onCancel");
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
