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

public abstract class MotionSensorManager implements SensorEventListener {

    abstract public void processSensorUIUpdateEvent();
    abstract public void processSensorIdleEvent();

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
    private boolean mIsFireUIUpdateEvent;
    private TimerHandle mTimerHandle;

    MotionSensorManager(Context contxt){
        context = contxt;
        mSensorIsStarted = false;
        mCurrentHorizonAccelerometer = 0.0;
        mLastHorizonAccelerometer = 0.0;
        mCurrentVerticalAccelerometer = 0.0;
        mLastVerticalAccelerometer = 0.0;
        mFirstLaunchAccelerometer = true;
        mIsDeviceShifted = false;
        mIsFireUIUpdateEvent = false;

        mTimerHandle = new TimerHandle(5000, 1000) {
            @Override
            public void processTimeOutEvent() {
                if(mIsFireUIUpdateEvent == true){
                    mIsFireUIUpdateEvent = false;
                    processSensorIdleEvent();
                }
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
            mTimerHandle.onCancel();
        }
    }

    public boolean isDeviceTouchByUser(){
        return mIsDeviceShifted;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //Log.d(TAG, "horizontal:"+sensorEvent.values[0]+" vertical:"+sensorEvent.values[1]+" height:"+sensorEvent.values[2]);
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
            //don't always update the UI until the timer is expired and released.
            if(mIsFireUIUpdateEvent == false){
                mIsFireUIUpdateEvent = true;
                processSensorUIUpdateEvent();
            }


            //update flag to tell caller of the flag about device is touch/play by users.
            mIsDeviceShifted = true;

            if(mTimerHandle.isTimerRunning() == true){
                //if timer launched before, reset timer and relaunch again
                mTimerHandle.onCancel();
                mTimerHandle.onStart();
            }
        }else{
            if(mTimerHandle.isTimerRunning() == false){
                mTimerHandle.onStart();
            }
        }

        //update the last accelerometer value
        mLastHorizonAccelerometer = mCurrentHorizonAccelerometer;
        mLastVerticalAccelerometer = mCurrentVerticalAccelerometer;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
