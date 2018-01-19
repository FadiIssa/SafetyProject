package com.example.fadi.testingrx;


import android.content.ContentValues;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.MockDataProcessor;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionDBHelper;
import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.f.ble.ScanStatusCallback;
import com.example.fadi.testingrx.ui.SavedActivitiesBrowserActivity;
import com.example.fadi.testingrx.ui.uvex.NormalModeUvex;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LauncherActivity extends AppCompatActivity implements ScanStatusCallback{
    private static final String KEY_LEFT_INSOLE_MAC="leftInsoleMac";
    private static final String KEY_RIGHT_INSOLE_MAC ="rightInsoleMac";

    static final String SHARED_PREF_ENTRY="PairedDevices";

    String TAG="1Act";

    TextView textViewScanStatusLeft;
    TextView textViewScanStatusRight;

    Button buttonRealTime;
    Button buttonNormalMode;//this mode is the one Karim suggested. to hide what is in real time and what is sent after an activity.
    Button buttonScan;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_launcher);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.launcherActivity_toolbar);
        myToolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        //myToolbar.setLogo(getDrawable(R.drawable.uvex_logo_launcher_bar));
        myToolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));
        //getSupportActionBar();

        //getSupportActionBar().setLogo(R.drawable.elten_logo);

        Log.d(TAG,"Launcher activity onCreate is called");

        initUI();

        scan();
    }

    private void initUI(){

        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setEnabled(false);//because when the activity first starts, it starts scanning automatically.

        textViewScanStatusLeft = (TextView) findViewById(R.id.textViewScanStatusLeft);
        textViewScanStatusLeft.setText("");
        textViewScanStatusRight = (TextView) findViewById(R.id.textViewScanStatusRight);
        textViewScanStatusRight.setText("");

        buttonNormalMode = (Button) findViewById(R.id.buttonNormalMode);
        buttonNormalMode.setEnabled(false);

        buttonRealTime = (Button) findViewById(R.id.buttonRT);
        buttonRealTime.setEnabled(false);


        if (MyApplication.EltenMode) {
            //buttonRealTime.setBackground(getDrawable(R.drawable.elten_real_time));
            //buttonNormalMode.setBackground(getDrawable(R.drawable.elten_normal_mode));
        } else {// it is Uvex
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.uvex_logo));
            buttonRealTime.setBackground(getDrawable(R.drawable.realtime_icon0));
            buttonNormalMode.setBackground(getDrawable(R.drawable.normalmode_icon));
        }

        RxView.clicks(buttonRealTime)
                .subscribe(a-> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();//for demo mode
                });

        RxView.clicks(buttonNormalMode)
                .subscribe(a->{
                    //Intent intent = new Intent(this, NormalModeActivity.class);
                    //Intent intent = new Intent(this, Login.class);
                    Intent intent = new Intent(this, NormalModeUvex.class);
                    startActivity(intent);
                    finish();// for demo mode
                });

        // the user can make a scan if the previous scan timed out without finding any insoles for example.
        RxView.clicks(buttonScan)
                .subscribe(a->{
                    scan();
                    buttonRealTime.setEnabled(false);
                    buttonNormalMode.setEnabled(false);
                    buttonScan.setEnabled(false);
                });
    }

    @Override
    public void scanStatusLeftStopped() {
        runOnUiThread(() -> {
            textViewScanStatusLeft.setText("stopped");
        });
    }

    private void populateIntentWithSessionData(Intent intent, SessionData sessionData){
        intent.putExtra(DataProcessing.NUM_STEPS,sessionData.getNumSteps());
        intent.putExtra(DataProcessing.NUM_STAIRS,sessionData.getNumStairs());
        intent.putExtra(DataProcessing.DURATION_CROUCHING,sessionData.getDurationCrouching());
        intent.putExtra(DataProcessing.DURATION_KNEELING,sessionData.getDurationKneeling());
        intent.putExtra(DataProcessing.DURATION_TIPTOES,sessionData.getDurationTiptoes());
        intent.putExtra(DataProcessing.DURATION_WALKING,sessionData.getDurationWalking());
        intent.putExtra(DataProcessing.DURATION_STATIC,sessionData.getDurationStatic());
        intent.putExtra(DataProcessing.CALORIES,sessionData.getCalories());
        intent.putExtra(DataProcessing.DISTANCE_METERS,sessionData.getDistanceMeters());
        intent.putExtra(DataProcessing.ANGLE_LEFT,sessionData.getAngleLeft());
        intent.putExtra(DataProcessing.ANGLE_RIGHT,sessionData.getAngleRight());
        intent.putExtra(DataProcessing.FATIGUE,sessionData.getFatigueLevel());
        intent.putExtra(DataProcessing.VIBRATION_DURATION,sessionData.getVibrationDuration());

        Random myRandom= new Random(SystemClock.currentThreadTimeMillis());
        int randomVibrationIntensity;
        if (sessionData.getVibrationDuration()>=10) {
            randomVibrationIntensity = myRandom.nextInt(50);
        } else if (sessionData.getVibrationDuration()>=2){
            randomVibrationIntensity = myRandom.nextInt(30);
        } else {
            randomVibrationIntensity=1;
        }
        if (randomVibrationIntensity<0){
            randomVibrationIntensity*=-1;
        }
        // the vibration intensity is supposed to come ready from the mock object, the following line is a temporary solution until that feature is implemented in the firmware.
        intent.putExtra(DataProcessing.VIBRATION_INTENSITY,randomVibrationIntensity);
    }

    private void populateContentValuesWithSessionData(ContentValues cv, SessionData sd){
        cv.put(SessionContract.SessionTable.COLUMN_NAME_NUM_STEPS,String.valueOf(sd.getNumSteps()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_NUM_STAIRS,String.valueOf(sd.getNumStairs()));

        cv.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_CROUCHING,String.valueOf(sd.getDurationCrouching()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_KNEELING,String.valueOf(sd.getDurationKneeling()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_TIPTOES,String.valueOf(sd.getDurationTiptoes()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_STATIC,String.valueOf(sd.getDurationStatic()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_WALKING,String.valueOf(sd.getDurationWalking()));

        cv.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_VIBRATION,String.valueOf(sd.getVibrationDuration()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_VIBRATION_INTENSITY,String.valueOf(sd.getVibrationIntensity()));

        cv.put(SessionContract.SessionTable.COLUMN_NAME_ANGLE_LEFT,String.valueOf(sd.getAngleLeft()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_ANGLE_RIGHT,String.valueOf(sd.getAngleRight()));

        cv.put(SessionContract.SessionTable.COLUMN_NAME_CALORIES,String.valueOf(sd.getCalories()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_DISTANCE_METERS,String.valueOf(sd.getDistanceMeters()));
        cv.put(SessionContract.SessionTable.COLUMN_NAME_FATUGUE_LEVEL,String.valueOf(sd.getFatigueLevel()));

        cv.put(SessionContract.SessionTable.COLUMN_NAME_DATETIME,String.valueOf(sd.getCurrentDateTime()));
    }

    @Override
    public void scanStatusRightStopped() {
        runOnUiThread(() -> {
            textViewScanStatusRight.setText("stopped");
        });
    }

    @Override
    public void scanStatusLeftIsScanning() {
        runOnUiThread(() -> {
            textViewScanStatusLeft.setText("scanning..");
        });
    }

    @Override
    public void scanStatusRightIsScanning() {
        runOnUiThread(() -> {
            textViewScanStatusRight.setText("scanning..");
        });
    }

    @Override
    public void scanStatusLeftFound() {
        runOnUiThread(() -> {
            textViewScanStatusLeft.setText("found insole");
        });
    }

    @Override
    public void scanStatusRightFound() {
        runOnUiThread(() -> {
            textViewScanStatusRight.setText("found insole");
        });
    }

    @Override
    public void scanStatusFinished(String leftInsoleMac, String rightInsoleMac) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_ENTRY, Context.MODE_PRIVATE);
        Log.d(TAG,"writing to shared preferences, left mac is:"+leftInsoleMac+" right mac is:"+rightInsoleMac);
        prefs.edit().putString(KEY_LEFT_INSOLE_MAC,leftInsoleMac).putString(KEY_RIGHT_INSOLE_MAC,rightInsoleMac).commit();
        MyApplication.getBleManager().prepareBleDevices();
        runOnUiThread(() -> {
            buttonScan.setEnabled(true);
            textViewScanStatusLeft.setText("detected successfully");// this is wrong, we dont know yet if it was saved or not.
            textViewScanStatusRight.setText("detected successfully");
            buttonNormalMode.setEnabled(true);
            buttonRealTime.setEnabled(true);
        });
    }

    @Override
    public void scanStatusFinishedUnsuccessfully() {

        runOnUiThread(() -> {
            buttonScan.setEnabled(true);
            buttonNormalMode.setEnabled(false);
            buttonRealTime.setEnabled(false);
        });
    }

    // this function is called when the activity first launches, and also, if a user decides to make another scan (in case the first scan did not find the insoles due to them being disconnected for example).
    private void scan(){
        Log.d(TAG,"launching scan from LauncherActivity");// scan here means to make a completely new scan, to check for

            // we need to read from shared preferences, to decide which scan versin to call
            SharedPreferences prefs = getApplication().getSharedPreferences(
                    SHARED_PREF_ENTRY, Context.MODE_PRIVATE);

            String savedLeftMacAddress=prefs.getString("leftInsoleMac",null);
            String savedRightMacAddress=prefs.getString("rightInsoleMac",null);

            if (savedLeftMacAddress==null||savedRightMacAddress==null){
                Log.d(TAG,"start scan for discovery");
                MyApplication.getBleManager().scanForDiscovery(this);// the scan for discivery is the one responsible for writing to shared preferences about the mac addresses.
            } else {
                Log.d(TAG,"start scanFromSavedPrefs, looking for the following mac addresses, left:"+savedLeftMacAddress+" right:"+savedRightMacAddress);
                // I should add a check if the saved mac address is a real one, or maybe an empty string.
                MyApplication.getBleManager().scanFromSavedPrefs(savedLeftMacAddress,savedRightMacAddress,this);
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuLauncherResetDB:
                resetDataBase();
                return true;
            case R.id.menuLauncherHistory:
                Intent intent = new Intent(this, SavedActivitiesBrowserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetDataBase(){

        //getApplicationContext().deleteDatabase()
        SessionDBHelper myDBHelper = new SessionDBHelper(getApplicationContext());

        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        db.execSQL(SessionContract.SessionTable.SQL_DELETE_TABLE);//it is dangerous, after deleting table, it seems it cannot create it again.
        db.execSQL(SessionContract.SessionTable.SQL_CREATE_TABLE);
        //db.delete(SessionContract.SessionTable.TABLE_NAME,null,null);
        db.close();
        myDBHelper.close();

        Toast.makeText(this,"All database records deleted",Toast.LENGTH_LONG).show();

        myDBHelper = new SessionDBHelper(getApplicationContext());

        db = myDBHelper.getWritableDatabase();

        //now we should add the 2 fake data
        MockDataProcessor mDataProcessor = new MockDataProcessor();
        SessionData fakeSessionData1 = mDataProcessor.getFakeSession1Data();
        SessionData fakeSessionData2 = mDataProcessor.getFakeSession2Data();

        ContentValues values = new ContentValues();
//        values.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_CROUCHING,"17");
//        values.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_WALKING,"18");
//        values.put(SessionContract.SessionTable.COLUMN_NAME_DURATION_VIBRATION,"20");
        populateContentValuesWithSessionData(values,fakeSessionData1);

        long newRowId = db.insert(SessionContract.SessionTable.TABLE_NAME, null, values);
        Log.d(TAG,"after inserting a new first fake row, here is its id:"+newRowId);

        values = new ContentValues();
        populateContentValuesWithSessionData(values,fakeSessionData2);

        newRowId = db.insert(SessionContract.SessionTable.TABLE_NAME, null, values);
        Log.d(TAG,"after inserting a 2nd fake new row, here is its id:"+newRowId);

        db.close();

        myDBHelper.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_launcher, menu);
            //menu.findItem(R.id.menuLauncherResetMacAddresses).setVisible(false);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }
}
