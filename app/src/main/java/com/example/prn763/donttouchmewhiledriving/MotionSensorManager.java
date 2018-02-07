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

enum DeviceMotionStatus{
    DEVICE_MOTION_IS_ILDE,
    DEVICE_MOTION_IS_MOVE_UP,
    DEVICE_MOTION_IS_MOVE_LEFT,
    DEVICE_MOTION_IS_MOVE_RIGHT,
    DEVICE_MOTION_IS_MOVE_DOWN
}

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
    private UiEventTimerState mTimerEvent;
    private TimerHandle mTimerHandle;
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
        mTimerEvent = UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT;

        mTimerHandle = new TimerHandle(ConfigPredefineEnvironment.getInstance().cpe_count_down_timer(),
                                       ConfigPredefineEnvironment.getInstance().cpe_count_down_interval_timer()) {
            @Override
            public void processTimeOutEvent() {
                //only allowed to send a warning when after 3secs count down
                mDeviceIsOnTouch = false;
                mTimerEvent = UiEventTimerState.TIMER_STATE_IS_FIRE_ALERT_EVENT;
                mTimerFireAlertEvent.onStart();
            }

            @Override
            public void processOnTickEvent(long l) {
                if(mTimerEvent == UiEventTimerState.TIMER_STATE_IS_DO_NOTHING_EVENT){
                    updateTickUiEvent();
                }
            }
        };

        mTimerFireAlertEvent = new TimerHandle(ConfigPredefineEnvironment.getInstance().cpe_fire_alert_event_timer(),
                                               ConfigPredefineEnvironment.getInstance().cpe_count_down_interval_timer()){

            @Override
            public void processTimeOutEvent() {
                if(mIsFireAlertUiUpdated == true){
                    processSensorIdleEvent();
                    //timer expired, ui is reset
                    mIsFireAlertUiUpdated = false;
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

        //when location service is stopped, accelerometer will stop too
        //make sure stop the timer is launched before.
        if(mTimerHandle.isTimerRunning() == true){
            mTimerHandle.onCancel(true);
        }
/*
        //TODO: investigate whether need to remove the object?/ previously remove caused the application clash.
        if(mTimerHandle != null){
            mTimerHandle = null;
        }

        if(mTimerFireAlertEvent != null){
            mTimerFireAlertEvent = null;
        }*/
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
                mTimerEvent = UiEventTimerState.TIMER_STATE_IS_DO_NOTHING_EVENT;
                processUpdateCountDownUiEvent();
                mTimerHandle.onStart();
            }
        }

        if((mTimerEvent == UiEventTimerState.TIMER_STATE_IS_FIRE_ALERT_EVENT) &&
           (mIsFireAlertUiUpdated == false) &&
           (mDeviceIsOnTouch == true)){
            //restart the timer
            if(mTimerFireAlertEvent.isTimerRunning() == true){
                //if timer launched before, reset timer and relaunch again
                mTimerFireAlertEvent.onCancel(false);
                mTimerFireAlertEvent.onStart();
            }

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
