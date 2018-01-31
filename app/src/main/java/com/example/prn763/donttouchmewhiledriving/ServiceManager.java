package com.example.prn763.donttouchmewhiledriving;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

enum DeviceStatus{
    DEVICE_IN_IDLE_MODE,
    DEVICE_IN_WALKING_MODE,
    DEVICE_IN_DRIVING_MODE
}

/**
 * Created by PRN763 on 1/21/2018.
 */

//TODO:unbind iBinder


public class ServiceManager extends Service {
    private final static String TAG = "ServiceManager";
    private final IBinder serviceBinder =  new LocalServiceBinder();
    private DeviceSpeedDetector mDeviceSpeedDetector = null;
    private MotionSensorManager mMotionSensorManager = null;
    private boolean mIsServiceStarted = false;
    private TimerHandle mCountDownTimer = null;
    private TimerHandle mFireAlertTimer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class LocalServiceBinder extends Binder{
        ServiceManager getService(){
            return ServiceManager.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceSpeedDetector = new DeviceSpeedDetector(getApplicationContext()) {
            @Override
            public void speedEventHandle(DeviceStatus var1) {
                //TODO: uncomment
                sendNotification(var1);

                if(mMotionSensorManager != null){
                    //TODO:DeviceStatus.DEVICE_IN_DRIVING_MODE
                    if(var1 == DeviceStatus.DEVICE_IN_DRIVING_MODE){
                        Log.d(TAG, "Start the accelerometer.");
                        mMotionSensorManager.start();
                    }else{
                        Log.d(TAG, "Stop the accelerometer.");
                        mMotionSensorManager.stop();
                    }
                }
            }

            @Override
            public void fireCarExceedLimitAlert() {
                sendMessageToActivity("DeviceStatus",deviceUIUpdateState.UPDATE_DEVICE_UI_CAR_EXCEED_SPEED_LIMIT);
            }
        };

        mMotionSensorManager = new MotionSensorManager(getApplicationContext()) {
            @Override
            public void processUpdateCountDownUiEvent() {
                sendMessageToActivity("DeviceStatus",deviceUIUpdateState.UPDATE_DEVICE_UI_PHONE_IS_COUNTDOWN_3_SECS);
            }

            @Override
            public void processSensorIdleEvent() {
                sendMessageToActivity("DeviceStatus",deviceUIUpdateState.UPDATE_DEVICE_UI_PHONE_IS_IDLE);
            }

            @Override
            public void updateTickUiEvent() {
                sendMessageToActivity("DeviceStatus",deviceUIUpdateState.UPDATE_DEVICE_UI_PHONE_IS_COUNTDOWN_3_SECS);
            }

            @Override
            public void updateFireAlertUiEvent() {
                sendMessageToActivity("DeviceStatus",deviceUIUpdateState.UPDATE_DEVICE_UI_PHONE_IS_PLAYING_BY_USERS);
            }
        };
    }

    private void sendMessageToActivity(String action, deviceUIUpdateState state) {
        Intent intent = new Intent ("DeviceStatus"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("state", state.ordinal());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean getButtonToggleState(){
        return mIsServiceStarted;
    }

    public void stop(){
        Log.d(TAG, "Stop Service Manager!!!");
        mMotionSensorManager.stop();
        mIsServiceStarted = false;
        mDeviceSpeedDetector.stop();

        sendMessageToActivity("DeviceStatus", deviceUIUpdateState.UPDATE_DEVICE_UI_PHONE_STOP_TONE);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mDeviceSpeedDetector != null){
            //start the speed track service
            mDeviceSpeedDetector.start();
            //notify users device status
            sendNotification(mDeviceSpeedDetector.getDeviceMovementStatus());
            //update service flag
            mIsServiceStarted = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //TODO:to be remove
    public void sendNotificationToBeRemove(int speed) {
        NotificationCompat.Builder notify = new NotificationCompat.Builder(this);
        notify.setSmallIcon(R.drawable.ic_directions_car_black_24dp);
        notify.setContentTitle("Speed Debug Usage");
        notify.setContentText("GPS SERVICE IS RUNNING");

        Intent resultIntent = new Intent(this, MainActivity.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notify.setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, notify.build());
    }

    public void sendNotification(DeviceStatus deviceStatus) {
        NotificationCompat.Builder notify = new NotificationCompat.Builder(this);

        notify.setContentTitle("Monitor Users Device Status");

        switch (deviceStatus){
            case DEVICE_IN_IDLE_MODE:
                notify.setSmallIcon(R.drawable.ic_cancel_black_24dp);
                notify.setContentText("Device is Idle Mode...");
                break;
            case DEVICE_IN_WALKING_MODE:
                notify.setSmallIcon(R.drawable.ic_directions_walk_black_24dp);
                notify.setContentText("Device is Walking Mode...");
                break;
            case DEVICE_IN_DRIVING_MODE:
                notify.setSmallIcon(R.drawable.ic_directions_car_black_24dp);
                notify.setContentText("Device is Driving Mode...");
                break;
            default:
                notify.setSmallIcon(R.drawable.ic_error_black_24dp);
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
