package com.example.prn763.donttouchmewhiledriving;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by PRN763 on 2/4/2018.
 */

public class Setting extends AppCompatActivity {
    private final static int TRUE = 1;
    private final static int FALSE = 0;
    private final static String TAG = "Setting";
    private CheckBox mEmailCb;
    private CheckBox mToneCb;
    private CheckBox mVibrationCb;
    private CheckBox mLockScreenCb;
    private EditText mSpeedEt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mEmailCb = findViewById(R.id.emailCheckBox);
        mToneCb = findViewById(R.id.musicCheckBox);
        mVibrationCb = findViewById(R.id.vibrationCheckBox);
        mLockScreenCb = findViewById(R.id.lockScreenCheckBox);
        mSpeedEt = findViewById(R.id.speedEditText);
        setTitle("Setting");
        //retrieve database and set to UI
        updateUiUserSetting();
        //setup the save button
        configureSaveButton();

    }

    private void updateUiUserSetting() {
        Cursor dbCursor = DBHandler.getInstance(getApplicationContext()).getDataBase();
        while (dbCursor.moveToNext())
        {
            int emailSetting = dbCursor.getInt(1);
            int toneSetting = dbCursor.getInt(2);
            int vibrationSetting = dbCursor.getInt(3);
            int screenLockSetting = dbCursor.getInt(4);
            int speedSetting = dbCursor.getInt(5);

            //set to display on UI
            mEmailCb.setChecked((emailSetting==TRUE)?true:false);
            mToneCb.setChecked((toneSetting==TRUE)?true:false);
            mVibrationCb.setChecked((vibrationSetting==TRUE)?true:false);
            mLockScreenCb.setChecked((screenLockSetting==TRUE)?true:false);
            mSpeedEt.setText(String.valueOf(speedSetting), TextView.BufferType.EDITABLE);
        }
        dbCursor.close();
    }

    private void configureSaveButton(){
        ImageButton saveButton = findViewById(R.id.save_button);
        View.OnClickListener buttonLister = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                //set into database
                if(DBHandler.getInstance(getApplicationContext()).getProfilesCount() > 0){
                    DBHandler.getInstance(getApplicationContext()).updateDatebase(mEmailCb.isChecked()?1:0,
                                                                                  mToneCb.isChecked()?1:0,
                                                                                  mVibrationCb.isChecked()?1:0,
                                                                                  mLockScreenCb.isChecked()?1:0,
                                                                                  Integer.parseInt(mSpeedEt.getText().toString()));

                }else{
                    //create the new row entry when the table is blank
                    DBHandler.getInstance(getApplicationContext()).insertDatabase(mEmailCb.isChecked()?1:0,
                                                                                  mToneCb.isChecked()?1:0,
                                                                                  mVibrationCb.isChecked()?1:0,
                                                                                  mLockScreenCb.isChecked()?1:0,
                                                                                  Integer.parseInt(mSpeedEt.getText().toString()));
                }

                //update setting immediately
                ConfigPredefineEnvironment.getInstance().cpe_set_enabled_email_notification(mEmailCb.isChecked());
                ConfigPredefineEnvironment.getInstance().cpe_set_enabled_alert_tone(mToneCb.isChecked());
                ConfigPredefineEnvironment.getInstance().cpe_set_enabled_vibrator(mVibrationCb.isChecked());
                ConfigPredefineEnvironment.getInstance().cpe_set_enabled_screen_lock(mLockScreenCb.isChecked());
                ConfigPredefineEnvironment.getInstance().cpe_set_speed_limit(Integer.parseInt(mSpeedEt.getText().toString()));
            }
        };
        saveButton.setOnClickListener(buttonLister);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

class DBHandler extends SQLiteOpenHelper {
    public static DBHandler mDbHandler;
    public Context mContext;

    private static final String DATABASE_NAME = "ContactDB";
    private static final String TABLE_NAME = "Contact";
    private static final String COL_1_ID = "ID";
    private static final String COL_2_EMAIL = "EMAIL";
    private static final String COL_3_TONE = "TONE";
    private static final String COL_4_VIBRATION = "VIBRATION";
    private static final String COL_5_LOCK_SCREEN = "LOCKSCREEN";
    private static final String COL_6_SPEED = "SPEED";


    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    public static DBHandler getInstance(Context context){
        if(mDbHandler == null){
            mDbHandler = new DBHandler(context);
        }
        return mDbHandler;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_QUERY = "create table " + TABLE_NAME + " (" + COL_1_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_2_EMAIL +" INTEGER,"+
                                                                                                                    COL_3_TONE+" TEXT,"+
                                                                                                                    COL_4_VIBRATION +" TEXT,"+
                                                                                                                    COL_5_LOCK_SCREEN+" Text,"+
                                                                                                                    COL_6_SPEED+" TEXT)";
        sqLiteDatabase.execSQL(SQL_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String SQL_QUERY = "DROP TABLE IF EXISTS"+TABLE_NAME;
        sqLiteDatabase.execSQL(SQL_QUERY);
        onCreate(sqLiteDatabase);
    }

    public boolean insertDatabase(int email, int tone, int vibration, int screenlock, int speed){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_EMAIL, email);
        contentValues.put(COL_3_TONE, tone);
        contentValues.put(COL_4_VIBRATION, vibration);
        contentValues.put(COL_5_LOCK_SCREEN, screenlock);
        contentValues.put(COL_6_SPEED, speed);
        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        if(result >= 0){
            return true;
        }
        else{
            return false;
        }
    }

    public Cursor getDataBase(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String SQL_QUERY = "select * from "+TABLE_NAME;
        Cursor res = sqLiteDatabase.rawQuery(SQL_QUERY, null);
        return res;
    }

    public long getProfilesCount() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(sqLiteDatabase, TABLE_NAME);
        sqLiteDatabase.close();
        return count;
    }

    public int updateDatebase(int email, int tone, int vibration, int screenlock, int speed){
        SQLiteDatabase sqlHelper = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COL_2_EMAIL, email);
        cv.put(COL_3_TONE, tone);
        cv.put(COL_4_VIBRATION, vibration);
        cv.put(COL_5_LOCK_SCREEN, screenlock);
        cv.put(COL_6_SPEED, speed);

        return sqlHelper.update(TABLE_NAME, cv, COL_1_ID+"=1", null);
    }
}
