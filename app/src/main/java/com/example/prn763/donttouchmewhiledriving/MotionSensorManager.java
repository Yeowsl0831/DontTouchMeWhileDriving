package com.example.prn763.donttouchmewhiledriving;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by PRN763 on 1/21/2018.
 */

public abstract class MotionSensorManager implements SensorEventListener {

    abstract public void processSensorUIUpdateEvent();

    private final static String TAG = "MotionSensorManager";
    private Context context;
    private SensorManager sensorManager;
    private boolean mSensorIsStarted;

    MotionSensorManager(Context contxt){
        context = contxt;
        mSensorIsStarted = false;
    }

    public void start(){
        if(mSensorIsStarted == false){
            sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void stop(){
        if(sensorManager != null && mSensorIsStarted == true){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.values[0] > 0.0 || sensorEvent.values[1] > 10 || sensorEvent.values[2] > 0.0){
            processSensorUIUpdateEvent();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
