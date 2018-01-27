package com.example.prn763.donttouchmewhiledriving;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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

public class MainActivity extends AppCompatActivity{
    final static String TAG = "MainActivity";
    private Button startServiceBtn;
    private int mCurrentVolume;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;


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

    protected void onResume(){
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DeviceStatus");
        //intentFilter.addAction("ServiceStarted");
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, intentFilter);
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


    private BroadcastReceiver  BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == "DeviceStatus"){
                boolean isPlayPhone = intent.getBooleanExtra("state", false);

                if(isPlayPhone){
                    displayCustomToast(emojiType.ANGRY_EMOJI);
                    emitMaxWarmingAlertTone();
                    processVibration(true);
                    tv.setText("Don't Play While Driving");
                }else{
                    displayCustomToast(emojiType.HAPPY_EMOJI);
                    stopTone();
                    processVibration(false);
                    tv.setText("Thanks for your cooperation not playing phone.");
                }
            }/*else if(action == "isServiceStarted"){
                boolean isStarted = intent.getBooleanExtra("state", false);
                Log.e(TAG, "Received Button broadcast message");
                if(isStarted){
                    startServiceBtn.setText("Stop");
                }else{
                    startServiceBtn.setText("Start");
                }
            }*/


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
                startService(svcIntent);
                startServiceBtn.setText("Stop");

                //shutdown activity
                finish();
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


