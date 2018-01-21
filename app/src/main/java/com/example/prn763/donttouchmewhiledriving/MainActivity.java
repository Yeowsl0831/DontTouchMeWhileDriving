package com.example.prn763.donttouchmewhiledriving;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;



public class MainActivity extends AppCompatActivity{
    final static String TAG = "MainActivity";
    private Button startServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup start button
        configureStartServiceButton();
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
                //finish();
            }
        };
        startServiceBtn.setOnClickListener(startServiceListener);
    }
}


