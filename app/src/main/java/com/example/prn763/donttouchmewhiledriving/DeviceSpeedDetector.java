package com.example.prn763.donttouchmewhiledriving;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


/**
 * Created by PRN763 on 1/21/2018.
 */

public abstract class DeviceSpeedDetector implements LocationListener {
    private final static String TAG = "DeviceSpeedDetector";
    private Location lastLocation;
    private Context mContext;
    private DeviceStatus mDeviceStatus;

    public abstract void speedEventHandle(DeviceStatus var1);

    DeviceSpeedDetector(Context context){
        lastLocation = null;
        mContext = context;
        mDeviceStatus = DeviceStatus.DEVICE_IN_IDLE_MODE;
    }

    public void start(){
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }

    public DeviceStatus getDeviceMovementStatus(){
        return mDeviceStatus;
    }

    @Override
    public void onLocationChanged(Location location) {
        double timeElapsed = 0;
        double distance = 0;

        if(location != null){
            if(lastLocation != null){
                //get time elapsed in sec
                timeElapsed = (location.getTime() - lastLocation.getTime())/(1000);
                //get distance elapsed in m
                distance = lastLocation.distanceTo(location);
                //get last location of the device
                lastLocation = location;
            }
            else{
                lastLocation = location;
            }
            int speed = (int)((distance/timeElapsed)*(18/5));

            if(speed >= 30){
                mDeviceStatus = DeviceStatus.DEVICE_IN_DRIVING_MODE;
            }else if(speed < 30 && speed > 0){
                mDeviceStatus = DeviceStatus.DEVICE_IN_WALKING_MODE;
            }else{
                mDeviceStatus = DeviceStatus.DEVICE_IN_IDLE_MODE;
            }

            speedEventHandle(mDeviceStatus);
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
