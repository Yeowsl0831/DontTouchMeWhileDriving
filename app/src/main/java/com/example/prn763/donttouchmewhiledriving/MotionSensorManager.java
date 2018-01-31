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
    private double mLastVerticalAccelerometer;
    private boolean mFirstLaunchAccelerometer;
    private boolean mIsDeviceShifted;
    private boolean mIsFireAlertUiUpdated;
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
        mFirstLaunchAccelerometer = true;
        mIsDeviceShifted = false;
        mIsFireAlertUiUpdated = false;
        mTimerEvent = UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT;

        mTimerHandle = new TimerHandle(ConfigPredefineEnvironment.getInstance().cpe_count_down_timer(),
                                       ConfigPredefineEnvironment.getInstance().cpe_count_down_interval_timer()) {
            @Override
            public void processTimeOutEvent() {
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
    }

    public boolean isDeviceTouchByUser(){
        return mIsDeviceShifted;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        mCurrentHorizonAccelerometer = sensorEvent.values[0];
        mCurrentVerticalAccelerometer = sensorEvent.values[1];

        //for the 1st time launch
        if(mFirstLaunchAccelerometer == true){
            mFirstLaunchAccelerometer = false;
            mLastHorizonAccelerometer = mCurrentHorizonAccelerometer;
            mLastVerticalAccelerometer = mCurrentVerticalAccelerometer;
        }

        double diffHorizonAccelerometer = mCurrentHorizonAccelerometer - mLastHorizonAccelerometer;
        double diffVerticalAccelerometer = mCurrentVerticalAccelerometer - mLastVerticalAccelerometer;

        if(Math.abs(diffHorizonAccelerometer) > 2.0 && Math.abs(diffVerticalAccelerometer) > 2.0 ||
          (Math.abs(diffHorizonAccelerometer) > 6.0) || (Math.abs(diffHorizonAccelerometer) > 6.0)){

            if(mTimerEvent == UiEventTimerState.TIMER_STATE_IS_COUNT_DOWN_EVENT){
                mTimerEvent = UiEventTimerState.TIMER_STATE_IS_DO_NOTHING_EVENT;
                processUpdateCountDownUiEvent();
                mTimerHandle.onStart();
            }else if(mTimerEvent == UiEventTimerState.TIMER_STATE_IS_FIRE_ALERT_EVENT){
                //don't always update the UI until the timer is expired and released.
                if(mIsFireAlertUiUpdated == false){
                    updateFireAlertUiEvent();
                    mIsFireAlertUiUpdated = true;
                }

                if(mTimerFireAlertEvent.isTimerRunning() == true){
                    //if timer launched before, reset timer and relaunch again
                    mTimerFireAlertEvent.onCancel(false);
                    mTimerFireAlertEvent.onStart();
                }
            }
            //update flag to tell caller of the flag about device is touch/play by users.
            mIsDeviceShifted = true;
        }
        //update the last accelerometer value
        mLastHorizonAccelerometer = mCurrentHorizonAccelerometer;
        mLastVerticalAccelerometer = mCurrentVerticalAccelerometer;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
