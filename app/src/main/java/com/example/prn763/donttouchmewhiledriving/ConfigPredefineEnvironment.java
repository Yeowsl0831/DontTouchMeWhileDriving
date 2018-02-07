package com.example.prn763.donttouchmewhiledriving;

import android.util.Log;

/**
 * Created by PRN763 on 1/30/2018.
 */

public class ConfigPredefineEnvironment {
    final static int CPE_COUNT_DOWN_TIMER_IN_MS = 3050;
    final static int CPE_COUNT_DOWN_INTERVAL_TIMER_IN_MS = 1000;
    final static int CPE_FIRE_ALERT_EVENT_TIMER_IN_MS = 6000;

    private boolean mEnabledVibrator = false;
    private boolean mEnabledAlertTone = false;
    private boolean mEnabledEmailNotification = false;
    private int mSpeedLimitNotification = 1000;



    private static ConfigPredefineEnvironment mCpe;

    ConfigPredefineEnvironment(){
        mCpe = null;
    }

    public static ConfigPredefineEnvironment getInstance(){
        if(mCpe == null){
            mCpe = new ConfigPredefineEnvironment();
        }
        return mCpe;
    }

    public int cpe_count_down_timer(){
        return CPE_COUNT_DOWN_TIMER_IN_MS;
    }

    public int cpe_count_down_interval_timer(){
        return CPE_COUNT_DOWN_INTERVAL_TIMER_IN_MS;
    }

    public int cpe_fire_alert_event_timer(){
        return CPE_FIRE_ALERT_EVENT_TIMER_IN_MS;
    }

    public boolean cpe_enabled_vibrator(){
        return mEnabledVibrator;
    }

    public boolean cpe_enable_alert_tone(){
        return mEnabledAlertTone;
    }

    public boolean cpe_enable_email_notification(){return mEnabledEmailNotification; }

    public int cpe_car_speed_limit(){
        return mSpeedLimitNotification;
    }

    public void cpe_set_enabled_vibrator(boolean isEnabled){
        mEnabledVibrator = isEnabled;
    }

    public void cpe_set_enabled_alert_tone(boolean isEnabled){
        mEnabledAlertTone = isEnabled;
    }

    public void cpe_set_enabled_email_notification(boolean isEnabled){
        mEnabledEmailNotification = isEnabled;
    }

    public void cpe_set_speed_limit(int speed){
        Log.d("CPE", "Speed:"+speed);
        mSpeedLimitNotification = speed;
    }
}
