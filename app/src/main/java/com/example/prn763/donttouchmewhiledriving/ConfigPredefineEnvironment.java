package com.example.prn763.donttouchmewhiledriving;

/**
 * Created by PRN763 on 1/30/2018.
 */





public class ConfigPredefineEnvironment {
    final static int CPE_COUNT_DOWN_TIMER_IN_MS = 3000;
    final static int CPE_COUNT_DOWN_INTERVAL_TIMER_IN_MS = 1000;
    final static int CPE_FIRE_ALERT_EVENT_TIMER_IN_MS = 6000;
    final static int CPE_CAR_SPEED_LIMIT = 60;



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
        return true;
    }

    public boolean cpe_enable_alert_tone(){
        return true;
    }

    public int cpe_car_speed_limit(){
        return CPE_CAR_SPEED_LIMIT;
    }
}
