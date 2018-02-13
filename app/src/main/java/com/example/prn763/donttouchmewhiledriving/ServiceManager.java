package com.example.prn763.donttouchmewhiledriving;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

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
    private final IBinder serviceBinder =  new LocalServiceBinder();
    private SpeedLocationManager mSpeedLocationManager = null;
    private MotionSensorManager mMotionSensorManager = null;
    private boolean mServiceIsRun = false;
    private int mCurrentMovementSpeed = 0;
    private double mCurrentLatitude = 0.0;
    private double mCurrentLongitude = 0.0;
    private WindowManager mWindowManager;
    private LinearLayout mDummyView;

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
        mSpeedLocationManager = new SpeedLocationManager(getApplicationContext()) {
            @Override
            public void updateServiceGpsLocationDetails(int speed, double latitude, double longitude) {
                mCurrentMovementSpeed = speed;
                mCurrentLatitude = latitude;
                mCurrentLongitude = longitude;

                sendMessageToActivity(speed,service_msg_t.UPDATE_ACTIVITY_GPS_CURRENT_LOCATION_SPEED);
            }

            @Override
            public void updateServiceNetworkLocationDetails(int speed, double latitude, double longitude) {
                sendMessageToActivity(speed,service_msg_t.UPDATE_ACTIVITY_NETWORK_CURRENT_LOCATION_SPEED);
            }

            @Override
            public void speedEventHandle(DeviceStatus var1) {
                sendIndicatorNotification(var1);

                if(mMotionSensorManager != null){
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
                sendMessageToActivity(0,service_msg_t.UPDATE_ACTIVITY_DEVICE_EXCEEDING_SPEED_LIMIT);

                String emailContent = "Your DontTouchMeWhileDriving app caught your status as below:\n" +
                                      "Reason: Exceed Speed Limit\n" +
                                      "Speed: "+mCurrentMovementSpeed+"km'\'h\n" +
                                      "Location: "+mCurrentLatitude+"(Latitude) Longitude: "+mCurrentLongitude+"(Longitude)";
                new EmailManager().sendEmail(service_msg_t.UPDATE_ACTIVITY_DEVICE_FIRE_LOCK_ALERT_EVENT,
                                            "yslin91@hotmail.com",
                                            "DontTouchMeWhileDriving : Driving Report!",
                                             emailContent);
            }
        };

        mMotionSensorManager = new MotionSensorManager(getApplicationContext()) {
            @Override
            public void processUpdateCountDownUiEvent() {
                sendMessageToActivity(0,service_msg_t.UPDATE_ACTIVITY_DEVICE_IN_PRECAUTION_STATE);
            }

            @Override
            public void processSensorIdleEvent() {
                sendMessageToActivity(0,service_msg_t.UPDATE_ACTIVITY_DEVICE_IS_IN_IDLE);
            }

            @Override
            public void updateTickUiEvent() {
                //sendMessageToActivity(0,service_msg_t.UPDATE_ACTIVITY_DEVICE_IN_PRECAUTION_STATE);
            }

            @Override
            public void updateFireAlertUiEvent() {
                Log.d(TAG, "updateFireAlertUiEvent");
                sendMessageToActivity(0,service_msg_t.UPDATE_ACTIVITY_DEVICE_FIRE_LOCK_ALERT_EVENT);

                String emailContent = "Your DontTouchMeWhileDriving app caught your status as below:\n" +
                                      "Driving with Phone: Yes\n" +
                                      "Speed: "+mCurrentMovementSpeed+"km'\'h\n" +
                                      "Location: "+mCurrentLatitude+"(Latitude) Longitude: "+mCurrentLongitude+"(Longitude)";
                new EmailManager().sendEmail(service_msg_t.UPDATE_ACTIVITY_DEVICE_FIRE_LOCK_ALERT_EVENT,
                                                     "yslin91@hotmail.com",
                                                     "DontTouchMeWhileDriving : Driving Report!",
                                                      emailContent);
            }
        };

        //create the dummy and transparent windows for touch event
        createWindowsForOnTouchEvent();
    }

    private void sendMessageToActivity(int speed, service_msg_t state) {
        Intent intent = new Intent ("DeviceStatus"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("state", state.ordinal());
        intent.putExtra("speed", speed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean getButtonToggleState(){
        return mServiceIsRun;
    }

    public void stop(){
        Log.d(TAG, "Stop Service Manager!!!");
        mMotionSensorManager.stop();
        mServiceIsRun = false;
        mSpeedLocationManager.stop();

        //unregister touch listener
        if(mDummyView != null){
            mDummyView.setOnTouchListener(null);
            mDummyView = null;
        }

        if(mWindowManager != null){
            mWindowManager = null;
        }

        sendMessageToActivity(0, service_msg_t.UPDATE_ACTIVITY_TO_TERMINATE_TONE);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mSpeedLocationManager != null){
            mSpeedLocationManager.start();
            sendIndicatorNotification(mSpeedLocationManager.getDeviceMovementStatus());
            mServiceIsRun = true;
        }
        //added dummy view for detect touch event when activity is closed.
        addViewOnService();

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void sendIndicatorNotification(DeviceStatus deviceStatus) {
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

    //TODO:if possible move to MotionSensorManager class
    public void createWindowsForOnTouchEvent(){
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mDummyView = new LinearLayout(getApplicationContext());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, WindowManager.LayoutParams.MATCH_PARENT);
        mDummyView.setLayoutParams(params);

        View.OnTouchListener otl = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.e(TAG, "Service onTouch");
                if(mMotionSensorManager != null){
                    mMotionSensorManager.setDeviceIsOnTouch(true);
                }
                return false;
            }
        };
        mDummyView.setOnTouchListener(otl);
    }

    //TODO:if possible move to MotionSensorManager class
    public void addViewOnService(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1,
                                                                           1,
                                                                            WindowManager.LayoutParams.TYPE_PHONE,
                                                                           WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                                                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                                                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                                                            PixelFormat.TRANSPARENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        if(mWindowManager != null && mDummyView != null){
            mWindowManager.addView(mDummyView, params);
        }
    }

    public void UpdateHomeInputIsTriggered(){
        if(mMotionSensorManager != null){
            mMotionSensorManager.requestFireAlertEvent();
        }
    }

    public void requestStartFireAlertTimer(){
        if (mMotionSensorManager != null){
            mMotionSensorManager.requestStartFireAlertEventTimer();
        }
    }
}
