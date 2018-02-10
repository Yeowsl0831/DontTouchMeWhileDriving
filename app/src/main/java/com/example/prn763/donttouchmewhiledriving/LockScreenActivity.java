package com.example.prn763.donttouchmewhiledriving;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by PRN763 on 2/8/2018.
 */

public class LockScreenActivity extends AppCompatActivity {
    public static LockScreenActivity mLockScreenActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.locked_screen);
        //the purpose is for MainActivity get the object of this class to call finish()
        mLockScreenActivity = this;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "You Are Not Allowed to Play Phone!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Toast.makeText(getApplicationContext(), "Home Security is Always Enabled!", Toast.LENGTH_LONG).show();
    }
}
