package com.example.prn763.donttouchmewhiledriving;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;


/**
 * Created by PRN763 on 1/21/2018.
 */

public abstract class DeviceSpeedDetector implements LocationListener{
    private final static String TAG = "DeviceSpeedDetector";
    private Location lastLocation;
    private Context mContext;
    private DeviceStatus mDeviceStatus;
    private DeviceStatus mLastDeviceStatus;
    private LocationManager mLocationManager;
    private boolean mIsFireCarExceedLimitAlert;

    public abstract void speedEventHandle(DeviceStatus var1);
    public abstract void fireCarExceedLimitAlert();

    DeviceSpeedDetector(Context context){
        lastLocation = null;
        mContext = context;
        mDeviceStatus = DeviceStatus.DEVICE_IN_IDLE_MODE;
        mLastDeviceStatus = DeviceStatus.DEVICE_IN_IDLE_MODE;
        mIsFireCarExceedLimitAlert = false;

    }

    public void start(){
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
           Log.d(TAG, "GPS is Enabled");
        }
        else{
            //TODO: add handle to turn on the gps provider
            Log.d(TAG, "GPS not Found");
        }

        Criteria mFineCriteria = new Criteria();
        mFineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mFineCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        mFineCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        mFineCriteria.setBearingRequired(true);
        mLocationManager.getBestProvider(mFineCriteria, true);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }

    public void stop(){
        if(mLocationManager != null){
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
    }

    public DeviceStatus getDeviceMovementStatus(){
        return mDeviceStatus;
    }

    @Override
    public void onLocationChanged(Location location) {
        double timeElapsed = 0;
        double distance = 0;
        Log.d(TAG, "onLocationChanged");
        if(location != null){
            if(lastLocation != null){
                Log.d(TAG, "lastLocation not null");
                //get time elapsed in sec
                timeElapsed = (location.getTime() - lastLocation.getTime())/(1000);
                //get distance elapsed in m
                distance = lastLocation.distanceTo(location);
                //get last location of the device
                lastLocation = location;
            }
            else{
                Log.d(TAG, "lastLocation is null");
                lastLocation = location;
            }
            int speed = (int)((distance/timeElapsed)*(18/5));


            Log.d(TAG, "Speed: "+speed+"km/h");
            //get last device status
            mLastDeviceStatus = mDeviceStatus;

            if(speed >= 30){
                mDeviceStatus = DeviceStatus.DEVICE_IN_DRIVING_MODE;
            }else if(speed < 30 && speed > 0){
                mDeviceStatus = DeviceStatus.DEVICE_IN_WALKING_MODE;
            }else{
                mDeviceStatus = DeviceStatus.DEVICE_IN_IDLE_MODE;
            }

            if((speed > ConfigPredefineEnvironment.getInstance().cpe_car_speed_limit()) &&
               (ConfigPredefineEnvironment.getInstance().cpe_car_speed_limit() != 0) &&
                    (mIsFireCarExceedLimitAlert == false)){
                fireCarExceedLimitAlert();
                mIsFireCarExceedLimitAlert = true;
            } else if(speed < ConfigPredefineEnvironment.getInstance().cpe_car_speed_limit()){
                mIsFireCarExceedLimitAlert = false;
            }
            //status change only update
            if(mDeviceStatus != mLastDeviceStatus){
                speedEventHandle(mDeviceStatus);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
