package com.example.prn763.donttouchmewhiledriving;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{
    final static String TAG = "MainActivity";
    private Button startServiceBtn;
    //TODO:rmv
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    private BroadcastReceiver  BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            //put here whatever you want your activity to do with the intent received
            //Toast.makeText(MainActivity.this, "Don't Play While Driving", Toast.LENGTH_SHORT).show();
            String action = intent.getAction();
            if(action == "DeviceStatus"){
                boolean isPlayPhone = intent.getBooleanExtra("state", false);

                if(isPlayPhone){
                    tv.setText("Don't Play While Driving");
                }else{
                    tv.setText("Thanks for your cooperation not playing phone.");
                }
            }/*else if(action == "ServiceStarted"){
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
                //finish();
            }
        };
        startServiceBtn.setOnClickListener(startServiceListener);
    }
}


