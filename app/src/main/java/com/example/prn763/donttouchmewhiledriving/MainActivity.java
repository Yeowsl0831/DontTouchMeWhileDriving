package com.example.prn763.donttouchmewhiledriving;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


enum imageType{
    NO_IMAGE,
    ANGRY_EMOJI,
    HAPPY_EMOJI,
    COUNT_DOWN_1,
    COUNT_DOWN_2,
    COUNT_DOWN_3,
    SPEED_LIMIT
}

enum deviceUIUpdateState{
    UPDATE_DEVICE_NONE,
    UPDATE_DEVICE_CURRENT_SPEED,
    UPDATE_DEVICE_UI_PHONE_IS_COUNTDOWN_3_SECS,
    UPDATE_DEVICE_UI_PHONE_IS_PLAYING_BY_USERS,
    UPDATE_DEVICE_UI_PHONE_IS_IDLE,
    UPDATE_DEVICE_UI_PHONE_STOP_TONE,
    UPDATE_DEVICE_UI_CAR_EXCEED_SPEED_LIMIT
}

public class MainActivity extends AppCompatActivity{
    final static String TAG = "MainActivity";
    private ImageButton mPowerButton;
    private ImageButton mSettingButton;
    private int mCurrentVolume;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private ServiceManager mServiceBinder;
    private boolean mIsServiceStarted;
    private boolean mIsBound;
    private ImageView mCustomToastImageView;
    private Toast mCustomToast;
    private TextView mDebugSpeedTextView;
    private deviceUIUpdateState mCurrentDeviceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSettingConfiguration();

        mCurrentDeviceState = deviceUIUpdateState.UPDATE_DEVICE_NONE;

        mCurrentVolume = 0;
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mDebugSpeedTextView = findViewById(R.id.debugSpeedTextView);

        configureStartServiceButton();

        configureSettingButton();

        configureCustomToastMessage();
    }

    private void loadSettingConfiguration(){
        Cursor dbCursor = DBHandler.getInstance(this).getDataBase();

        while (dbCursor.moveToNext())
        {
            int emailSetting = dbCursor.getInt(1);
            int toneSetting = dbCursor.getInt(2);
            int vibrationSetting = dbCursor.getInt(3);
            int screenLockSetting = dbCursor.getInt(4);
            int speedSetting = dbCursor.getInt(5);

            ConfigPredefineEnvironment.getInstance().cpe_set_enabled_email_notification(emailSetting==1?true:false);
            ConfigPredefineEnvironment.getInstance().cpe_set_enabled_alert_tone(toneSetting==1?true:false);
            ConfigPredefineEnvironment.getInstance().cpe_set_enabled_vibrator(vibrationSetting==1?true:false);
            ConfigPredefineEnvironment.getInstance().cpe_set_enabled_screen_lock(screenLockSetting==1?true:false);
            ConfigPredefineEnvironment.getInstance().cpe_set_speed_limit(speedSetting);
        }
        dbCursor.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    protected void onResume(){
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DeviceStatus");
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, intentFilter);

        Intent intent = new Intent(this, ServiceManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTone();

        if(mIsBound){
            mIsBound = false;
            unbindService(mConnection);
            mConnection = null;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG,"onServiceConnected");
            ServiceManager.LocalServiceBinder binder = (ServiceManager.LocalServiceBinder) service;
            mServiceBinder = binder.getService();
            mIsBound = true;

            if(mPowerButton != null && mServiceBinder != null){

                mIsServiceStarted = mServiceBinder.getButtonToggleState();

                if(mIsServiceStarted == true){
                    mPowerButton.setImageResource(R.drawable.start_icon);
                }
                else{
                    mPowerButton.setImageResource(R.drawable.stop_icon);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
        }
    };

    private BroadcastReceiver  BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action == "DeviceStatus"){
                int uiUpdateState = intent.getIntExtra("state", -1);
                int speed = intent.getIntExtra("speed", -1);

                mCurrentDeviceState = deviceUIUpdateState.values()[uiUpdateState];

                switch(mCurrentDeviceState){
                    case UPDATE_DEVICE_UI_PHONE_IS_COUNTDOWN_3_SECS:
                        displayCustomToast(imageType.COUNT_DOWN_1);
                        break;
                    case UPDATE_DEVICE_UI_PHONE_IS_PLAYING_BY_USERS:
                        if(ConfigPredefineEnvironment.getInstance().cpe_enabled_screen_lock()){
                            Intent i = new Intent(MainActivity.this, LockScreenActivity.class);
                            startActivity(i);
                        }else{
                            displayCustomToast(imageType.ANGRY_EMOJI);
                            if(ConfigPredefineEnvironment.getInstance().cpe_enable_alert_tone()){
                                emitMaxWarmingAlertTone(R.raw.warning_tone, true);
                            }
                            playVibration(true);
                        }
                        break;
                    case UPDATE_DEVICE_UI_PHONE_IS_IDLE:
                        if(ConfigPredefineEnvironment.getInstance().cpe_enabled_screen_lock()){
                            LockScreenActivity.mLockScreenActivity.finish();
                        }else{
                            displayCustomToast(imageType.HAPPY_EMOJI);
                            stopTone();
                            playVibration(false);
                        }

                        break;
                    case UPDATE_DEVICE_UI_PHONE_STOP_TONE:
                        stopTone();
                        break;
                    case UPDATE_DEVICE_UI_CAR_EXCEED_SPEED_LIMIT:
                        displayCustomToast(imageType.SPEED_LIMIT);
                        emitMaxWarmingAlertTone(R.raw.exceed_speed_limit_tone, false);
                        break;
                    case UPDATE_DEVICE_CURRENT_SPEED:
                        mDebugSpeedTextView.setVisibility(View.VISIBLE);
                        mDebugSpeedTextView.setText(speed+"km/h");
                        break;
                }
            }
        }
    };

    private void emitMaxWarmingAlertTone(int tone_id, boolean isLoop){
        if(ConfigPredefineEnvironment.getInstance().cpe_enable_alert_tone()){
            //reset any existing tone to reduce the 2tones messed up.
            stopTone();

            if(mAudioManager != null){
                mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            }
            playTone(tone_id, isLoop);
        }
    }

    public void playTone(int tone_id, boolean isLoop){

        mMediaPlayer = MediaPlayer.create(this, tone_id);
        mMediaPlayer.setLooping(isLoop);
        mMediaPlayer.start();
        MediaPlayer.OnCompletionListener toneCompleteListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //we always want to maintain the user selected volume level after tone is done.
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
            }

        };
        mMediaPlayer.setOnCompletionListener(toneCompleteListener);
    }

    public void stopTone(){
        if(mMediaPlayer != null){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void playVibration(boolean isVibrate){
        if((mVibrator != null) &&
           (mVibrator.hasVibrator() == true) &&
           (ConfigPredefineEnvironment.getInstance().cpe_enabled_vibrator())){
            if(isVibrate == true){
                // Start without a delay
                // Vibrate for 100 milliseconds
                // Sleep for 1000 milliseconds
                long[] pattern = {0, 200, 200};

                // The '0' here means to repeat indefinitely
                // '0' is actually the index at which the pattern keeps repeating from (the start)
                // To repeat the pattern from any other point, you could increase the index, e.g. '1'
                mVibrator.vibrate(pattern, 0);
            }else{
                mVibrator.cancel();
            }
        }
    }

    private void configureStartServiceButton(){
        //start the service
        mPowerButton = findViewById(R.id.power_button);
        View.OnClickListener startServiceListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent svcIntent = new Intent(MainActivity.this, ServiceManager.class);

                if (mIsServiceStarted == true) {
                    Log.d(TAG, "Main Activity Stop Service");
                    stopService(svcIntent);

                    if (mServiceBinder != null) {
                        //TODO:Application crashed found here, maybe need to do some unbinding stuff.
                        mServiceBinder.stop();
                    }

                    mIsServiceStarted = false;
                    mPowerButton.setImageResource(R.drawable.stop_icon);
                    Toast.makeText(getApplicationContext(), "Power Off Engine", Toast.LENGTH_SHORT).show();
                } else {
                    startService(svcIntent);
                    mIsServiceStarted = true;
                    mPowerButton.setImageResource(R.drawable.start_icon);
                    Toast.makeText(getApplicationContext(), "Power On Engine", Toast.LENGTH_SHORT).show();

                    if((ConfigPredefineEnvironment.getInstance().cpe_shut_down_activity() == true) &&
                       //Android 8.0 facing the problem when shutdown the activity,
                       //the location listener not working
                       (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O)){
                        //shutdown activity
                        finish();

                    }
                }
            }
        };
        mPowerButton.setOnClickListener(startServiceListener);
    }

    private void configureSettingButton(){
        mSettingButton = findViewById(R.id.setting_button);
        View.OnClickListener startServiceListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
            }
        };
        mSettingButton.setOnClickListener(startServiceListener);
    }

    public void configureCustomToastMessage(){
        View view = getLayoutInflater().inflate(R.layout.custom_countdown_toast, (ViewGroup) findViewById(R.id.toast_root_layout));

        mCustomToastImageView = view.findViewById(R.id.customToastImage);

        mCustomToast = new Toast(getApplicationContext());
        mCustomToast.setGravity(Gravity.TOP|Gravity.RIGHT, 0, 0);
        mCustomToast.setDuration(Toast.LENGTH_SHORT);
        mCustomToast.setView(view);
    }

    public void displayCustomToast(imageType type){
        int resId = 0;

        switch (type){
            case ANGRY_EMOJI:
            case HAPPY_EMOJI:
                resId = R.drawable.x_phone_icon;
                break;
            case COUNT_DOWN_1:
            case COUNT_DOWN_2:
            case COUNT_DOWN_3:
                resId = R.drawable.precaution_icon;
                break;
            case SPEED_LIMIT:
                resId = R.drawable.speed_limit_logo;
                break;
            case NO_IMAGE:
            default:
                break;
        }

        mCustomToastImageView.setImageResource(resId);

        mCustomToast.show();
    }
}


