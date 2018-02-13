package com.example.prn763.donttouchmewhiledriving;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by PRN763 on 1/21/2018.
 */

enum UiEventTimerState{
    TIMER_STATE_IS_DO_NOTHING_EVENT,
    TIMER_STATE_IS_COUNT_DOWN_EVENT,
    TIMER_STATE_IS_FIRE_ALERT_EVENT
}

public abstract class MotionSensorManager implements SensorEventListener {

    abstract public void processUpdateCountDownUiEvent();
    abstract public void processSensorIdleEvent();
    abstract public void updateTickUiEvent();
    abstract public void updateFireAlertUiEvent();

    private final static String TAG = "MotionSensorManager";
    private Context context;
    private SensorManager sensorManager;
    private boolean mSensorIsStarted;
    private double mCurrentHorizonAccelerometer;
    private double mLastHorizonAccelerometer;
    private double mCurrentVerticalAccelerometer;
    private double mCurrentAltitudeAccelerometer;
    private double mLastVerticalAccelerometer;
    private double mLastAltitudeAccelerometer;
    private boolean mFirstLaunchAccelerometer;
    private boolean mIsFireAlertUiUpdated;
    private boolean mDeviceIsOnTouch;
    private boolean mIsExternalRequest;
    private boolean mIsTimerRequest;
    private UiEventTimerState mTimerEvent;
    private TimerHandle mTimerFireAlertEvent;



    MotionSensorManager(Context contxt){
        context = contxt;
        mSensorIsStarted = false;
        mCurrentHorizonAccelerometer = 0.0;
        mLastHorizonAccelerometer = 0.0;
        mCurrentVerticalAccelerometer = 0.0;
        mLastVerticalAccelerometer = 0.0;
        mCurrentAltitudeAccelerometer = 0.0;
        mLastAltitudeAccelerometer = 0.0;
        mFirstLaunchAccelerometer = true;
        mIsFireAlertUiUpdated = false;
        mDeviceIsOnTouch = false;
        mIsExternalRequest = false;
        mIsTimerRequest = false;
        mTimerEvent = UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT;

        mTimerFireAlertEvent = new TimerHandle(ConfigPredefineEnvironment.getInstance().cpe_fire_alert_event_timer(),
                                               ConfigPredefineEnvironment.getInstance().cpe_count_down_interval_timer()){

            @Override
            public void processTimeOutEvent() {
                if(mIsFireAlertUiUpdated == true){
                    processSensorIdleEvent();
                    //timer expired, ui is reset
                    mIsFireAlertUiUpdated = false;

                    if(mIsTimerRequest == true){
                        Log.d(TAG, "Is mIsExternalTimerRequest");
                        mIsTimerRequest = false;

                        mIsExternalRequest = false;
                    }
                }
                //reset the touch event input
                mDeviceIsOnTouch = false;
                //reset and to proceed count down event for user next touch.
                mTimerEvent = UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT;
            }

            @Override
            public void processOnTickEvent(long l) {

            }
        };
    }

    public void start(){
        if(mSensorIsStarted == false){
            mSensorIsStarted = true;
            sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stop(){
        if(sensorManager != null && mSensorIsStarted == true){
            sensorManager.unregisterListener(this);
            mSensorIsStarted = false;
        }

        mTimerEvent = UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT;
/*
        //TODO: investigate whether need to remove the object?/ previously remove caused the application clash.
        if(mTimerHandle != null){
            mTimerHandle = null;
        }

        if(mTimerFireAlertEvent != null){
            mTimerFireAlertEvent = null;
        }*/
    }


    public void requestFireAlertEvent(){
        //restart the timer
        if(mTimerFireAlertEvent.isTimerRunning() == true){
            //if timer launched before, reset timer and relaunch again
            mTimerFireAlertEvent.onCancel(false);
        }

        updateFireAlertUiEvent();

        mIsExternalRequest = true;

        Log.d(TAG, "requestFireAlertEvent");
    }

    public void requestStartFireAlertEventTimer(){
        if(mIsExternalRequest == true){
            Log.d(TAG, "requestStartFireAlertEventTimer");
            //restart the timer
            if(mTimerFireAlertEvent.isTimerRunning() == true){
                //if timer launched before, reset timer and relaunch again
                mTimerFireAlertEvent.onCancel(false);
            }
            mTimerFireAlertEvent.onStart();

            mIsTimerRequest = true;

            mIsFireAlertUiUpdated = true;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mCurrentHorizonAccelerometer = sensorEvent.values[0];
        mCurrentVerticalAccelerometer = sensorEvent.values[1];
        mCurrentAltitudeAccelerometer = sensorEvent.values[2];

        //for the 1st time launch
        if(mFirstLaunchAccelerometer == true){
            mFirstLaunchAccelerometer = false;
            mLastHorizonAccelerometer = mCurrentHorizonAccelerometer;
            mLastVerticalAccelerometer = mCurrentVerticalAccelerometer;
            mLastAltitudeAccelerometer = mCurrentAltitudeAccelerometer;
        }


        double diffHorizonAccelerometer = mCurrentHorizonAccelerometer - mLastHorizonAccelerometer;
        double diffVerticalAccelerometer = mCurrentVerticalAccelerometer - mLastVerticalAccelerometer;
        double diffAltitudeAccelerometer = mCurrentAltitudeAccelerometer - mLastAltitudeAccelerometer;

        if(Math.abs(diffHorizonAccelerometer) > 0.5 ||
           Math.abs(diffVerticalAccelerometer) > 0.5 ||
           Math.abs(diffAltitudeAccelerometer) > 2.0){
            if(mTimerEvent == UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT){
                mTimerEvent = UiEventTimerState.TIMER_STATE_IS_FIRE_ALERT_EVENT;
                processUpdateCountDownUiEvent();
                mDeviceIsOnTouch = false;
            }
        }

        if((mTimerEvent == UiEventTimerState.TIMER_STATE_IS_FIRE_ALERT_EVENT) &&
           (mIsFireAlertUiUpdated == false) && /* Update when no UI is updating before */
           (mDeviceIsOnTouch == true)){
            //restart the timer
            if(mTimerFireAlertEvent.isTimerRunning() == true){
                //if timer launched before, reset timer and relaunch again
                mTimerFireAlertEvent.onCancel(false);
            }
            mTimerFireAlertEvent.onStart();

            updateFireAlertUiEvent();
            mIsFireAlertUiUpdated = true;
        }
        //update the last accelerometer value
        mLastHorizonAccelerometer = mCurrentHorizonAccelerometer;
        mLastVerticalAccelerometer = mCurrentVerticalAccelerometer;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setDeviceIsOnTouch(boolean isTouch){
        mDeviceIsOnTouch = isTouch;
    }
}
