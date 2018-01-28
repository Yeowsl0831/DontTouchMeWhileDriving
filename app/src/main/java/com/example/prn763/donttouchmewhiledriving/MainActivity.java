package com.example.prn763.donttouchmewhiledriving;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


enum emojiType{
    NO_EMOJI,
    ANGRY_EMOJI,
    HAPPY_EMOJI
}

enum deviceUIUpdateState{
    UPDATE_DEVICE_UI_INVALID,
    UPDATE_DEVICE_UI_PHONE_IS_PLAYING_BY_USERS,
    UPDATE_DEVICE_UI_PHONE_IS_IDLE,
    UPDATE_DEVICE_UI_PHONE_STOP_TONE
}

public class MainActivity extends AppCompatActivity{
    final static String TAG = "MainActivity";
    private Button startServiceBtn;
    private int mCurrentVolume;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private ServiceManager mServiceBinder;
    private boolean mIsServiceStarted;
    private boolean mIsBound;


    //TODO:rmv
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize
        mCurrentVolume = 0;
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        tv = findViewById(R.id.deviceStatus);
        //setup start button
        configureStartServiceButton();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    protected void onResume(){
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DeviceStatus");
        //intentFilter.addAction("ServiceStarted");
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
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG,"onServiceConnected");
            ServiceManager.LocalServiceBinder binder = (ServiceManager.LocalServiceBinder) service;
            mServiceBinder = binder.getService();
            mIsBound = true;


            Log.e(TAG,"mServiceBinder");
            if(startServiceBtn != null && mServiceBinder != null){

                mIsServiceStarted = mServiceBinder.getButtonToggleState();

                if(mIsServiceStarted == true){
                    startServiceBtn.setText("Stop");
                }
                else{
                    startServiceBtn.setText("Start");
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

                switch(deviceUIUpdateState.values()[uiUpdateState]){
                    case UPDATE_DEVICE_UI_PHONE_IS_PLAYING_BY_USERS:
                        displayCustomToast(emojiType.ANGRY_EMOJI);
                        emitMaxWarmingAlertTone();
                        processVibration(true);
                        break;
                    case UPDATE_DEVICE_UI_PHONE_IS_IDLE:
                        displayCustomToast(emojiType.HAPPY_EMOJI);
                        stopTone();
                        processVibration(false);
                        break;
                    case UPDATE_DEVICE_UI_PHONE_STOP_TONE:
                        stopTone();
                        break;
                }
            }
        }
    };

    private void emitMaxWarmingAlertTone(){
        if(mAudioManager != null){
            mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        }

        playTone();
    }

    public void playTone(){
        mMediaPlayer = MediaPlayer.create(this, R.raw.warning_tone);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    public void stopTone(){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying() == true){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
            mMediaPlayer.stop();
        }
    }

    public void processVibration(boolean isVibrate){
        if(mVibrator != null && mVibrator.hasVibrator() == true){
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
        startServiceBtn = findViewById(R.id.startButton);
        View.OnClickListener startServiceListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent svcIntent = new Intent(MainActivity.this, ServiceManager.class);

                if(mIsServiceStarted == true){
                    stopService(svcIntent);
                    mIsServiceStarted = false;
                    startServiceBtn.setText("Start");
                }else{
                    startService(svcIntent);
                    mIsServiceStarted = true;
                    startServiceBtn.setText("Stop");

                    //shutdown activity
                    finish();
                }


            }
        };
        startServiceBtn.setOnClickListener(startServiceListener);
    }

    public void displayCustomToast(emojiType type){
        int resId = 0;
        String emoText = "";
        LayoutInflater inflater = getLayoutInflater();

        switch (type){
            case ANGRY_EMOJI:
                resId = R.drawable.angry_emoji;
                emoText = "Args, No Phone";
                break;
            case HAPPY_EMOJI:
                resId = R.drawable.happy_emoji;
                emoText = "Good Boy";
                break;
            case NO_EMOJI:
            default:
                break;
        }
        View view = inflater.inflate(R.layout.custom_countdown_toast, (ViewGroup) findViewById(R.id.toast_root_layout));

        ImageView toastImage = view.findViewById(R.id.customToastImage);
        toastImage.setImageResource(resId);
        TextView emojiTextView = view.findViewById(R.id.emojiText);
        emojiTextView.setText(emoText);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
}


