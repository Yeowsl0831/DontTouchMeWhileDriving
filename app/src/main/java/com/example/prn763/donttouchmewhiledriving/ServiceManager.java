package com.example.prn763.donttouchmewhiledriving;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

enum DeviceStatus{
    DEVICE_IN_IDLE_MODE,
    DEVICE_IN_WALKING_MODE,
    DEVICE_IN_DRIVING_MODE
}

/**
 * Created by PRN763 on 1/21/2018.
 */

public class ServiceManager extends Service {
    private final static String TAG = "ServiceManager";
    private DeviceSpeedDetector mDeviceSpeedDetector = null;
    private MotionSensorManager mMotionSensorManager = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceSpeedDetector = new DeviceSpeedDetector(getApplicationContext()) {
            @Override
            public void speedEventHandle(DeviceStatus var1) {
                sendNotification(var1);

                if(mMotionSensorManager != null){
                    if(var1 == DeviceStatus.DEVICE_IN_DRIVING_MODE){
                        mMotionSensorManager.start();
                    }else{
                        mMotionSensorManager.stop();
                    }
                }
            }
        };

        mMotionSensorManager = new MotionSensorManager(getApplicationContext()) {
            @Override
            public void processSensorUIUpdateEvent() {
                Log.d(TAG, "Users play phone while driving");
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mDeviceSpeedDetector != null){
            //start the speed track service
            mDeviceSpeedDetector.start();
            //notify users device status
            sendNotification(mDeviceSpeedDetector.getDeviceMovementStatus());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void sendNotification(DeviceStatus deviceStatus) {
        NotificationCompat.Builder notify = new NotificationCompat.Builder(this);
        notify.setSmallIcon(R.drawable.ic_directions_car_black_24dp);
        notify.setContentTitle("Monitor Users Device Status");

        switch (deviceStatus){
            case DEVICE_IN_IDLE_MODE:
                notify.setContentText("Device is Idle Mode...");
                break;
            case DEVICE_IN_WALKING_MODE:
                notify.setContentText("Device is Walking Mode...");
                break;
            case DEVICE_IN_DRIVING_MODE:
                notify.setContentText("Device is Driving Mode...");
                break;
            default:
                notify.setContentText("Application Encounter Error!");
                break;
        }


        Intent resultIntent = new Intent(this, MainActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notify.setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, notify.build());
    }
}
