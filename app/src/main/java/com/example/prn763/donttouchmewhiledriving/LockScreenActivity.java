package com.example.prn763.donttouchmewhiledriving;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by PRN763 on 2/8/2018.
 */

public class LockScreenActivity extends AppCompatActivity {
    public static LockScreenActivity mLockScreenActivity;
    private boolean mInformActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.locked_screen);
        //the purpose is for MainActivity get the object of this class to call finish()
        mLockScreenActivity = this;
        mInformActivity = false;

        sendMessageToMainActivity(0, service_msg_t.UPDATE_ACTIVITY_REQUEST_START_FIRE_ALERT_EVENT_TIMER);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "You Are Not Allowed to Play Phone!", Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            sendMessageToMainActivity(0, service_msg_t.UPDATE_ACTIVITY_FORCE_SHUT_DOWN);
            Toast.makeText(getApplicationContext(), "Force Engine Off", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //only allowed one pressed input to send
        if(mInformActivity ==  false){
            Toast.makeText(getApplicationContext(), "Home Security is Always Enabled!", Toast.LENGTH_LONG).show();
            //worry activity object is not remove after home button is pressed
            finish();

            sendMessageToMainActivity(0, service_msg_t.UPDATE_ACTIVITY_RECEIVED_HOME_PRESSED_ON_LOCK_SCREEN);

            mInformActivity = true;
        }
    }

    private void sendMessageToMainActivity(int speed, service_msg_t state) {
        Intent intent = new Intent ("DeviceStatus"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("state", state.ordinal());
        intent.putExtra("speed", speed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
