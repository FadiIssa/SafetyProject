package com.example.fadi.testingrx;


import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.f.ble.ScanStatusCallback;
import com.example.fadi.testingrx.ui.uvex.NormalModeUvex;
import com.example.fadi.testingrx.ui.uvex.SessionStatsActivity;
import com.jakewharton.rxbinding2.view.RxView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LauncherActivity extends AppCompatActivity implements ScanStatusCallback{
    static final String LEFT_INSOLE_MAC_KEY="leftInsoleMac";
    static final String RIGHT_INSOLE_MAC_KEY="rightInsoleMac";
    public static final String SHARED_PREF_ENTRY="PairedDevices";

    private static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    String TAG="1Act";

    TextView textViewScanStatusLeft;
    TextView textViewScanStatusRight;

    ImageView imageViewLogo;
    Button rtButton;
    Button normalModeButton;//this mode is the one Karim suggested. to hide what is in real time and what is sent after an activity.
    Button buttonScan;

    //Button buttonSavedActivity;
    //Button buttonUvexNormalMode;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_launcher);

//        if (getSupportActionBar()!=null) {// at first I had to hide it myself, but when changed styles and added the UI from the original safety app, this started to return null object.
//            getSupportActionBar().hide();
//        }


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

        //ActionBar myActionBar=getSupportActionBar();
        //myActionBar.setDisplayShowHomeEnabled(true);
        //myActionBar.setDisplayShowTitleEnabled(false);
        //myActionBar.setIcon(R.drawable.uvex_logo_launcher_bar);
        //myActionBar.
        //myActionBar.setLogo(R.drawable.uvex_logo_launcher_bar);

        //myActionBar.setDisplayShowHomeEnabled(true);

        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setEnabled(false);//because when the activity first starts, it starts scanning automatically.

        //buttonSavedActivity = (Button) findViewById(R.id.buttonSavedActivity);

        textViewScanStatusLeft = (TextView) findViewById(R.id.textViewScanStatusLeft);
        textViewScanStatusLeft.setText("");
        textViewScanStatusRight = (TextView) findViewById(R.id.textViewScanStatusRight);
        textViewScanStatusRight.setText("");

        normalModeButton = (Button) findViewById(R.id.buttonNormalMode);
        normalModeButton.setEnabled(false);

        //buttonUvexNormalMode = (Button) findViewById(R.id.buttonUvexNormal);
//        RxView.clicks(buttonUvexNormalMode)
//                .subscribe(a->{
//                    Intent intent=new Intent(this, NormalModeUvex.class);
//                    startActivity(intent);
//                    finish();
//                });

        rtButton = (Button) findViewById(R.id.buttonRT);
        rtButton.setEnabled(false);

        //imageViewLogo = (ImageView) findViewById(R.id.imageViewLauncherLogo);

        if (MyApplication.EltenMode) {
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.elten_logo));
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.elten_logo_red));
            //Drawable rtDrawable=getDrawable(R.drawable.elten_real_time);
            //rtDrawable.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
            rtButton.setBackground(getDrawable(R.drawable.elten_real_time));
            normalModeButton.setBackground(getDrawable(R.drawable.elten_normal_mode));


        } else {// it is Uvex
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.uvex_logo));
            rtButton.setBackground(getDrawable(R.drawable.realtime_icon0));
            normalModeButton.setBackground(getDrawable(R.drawable.normalmode_icon));
        }

        RxView.clicks(rtButton)
                .subscribe(a-> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();//for demo mode
                });

        RxView.clicks(normalModeButton)
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
                    rtButton.setEnabled(false);
                    normalModeButton.setEnabled(false);
                    buttonScan.setEnabled(false);
                });

//        RxView.clicks(buttonSavedActivity)
//                .subscribe(a->{
//                    Intent intent = new Intent(this, SessionStatsActivity.class);
//                    populateIntentWithSessionData(intent,MyApplication.getDataManager().getSessionData(1,1,1));
//                    startActivity(intent);
//                });
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
        prefs.edit().putString(LEFT_INSOLE_MAC_KEY,leftInsoleMac).putString(RIGHT_INSOLE_MAC_KEY,rightInsoleMac).commit();
        MyApplication.getBleManager().prepareBleDevices();
        runOnUiThread(() -> {
            buttonScan.setEnabled(true);
            textViewScanStatusLeft.setText("detected successfully");// this is wrong, we dont know yet if it was saved or not.
            textViewScanStatusRight.setText("detected successfully");
            normalModeButton.setEnabled(true);
            rtButton.setEnabled(true);
        });
    }

    @Override
    public void scanStatusFinishedUnsuccessfully() {

        runOnUiThread(() -> {
            buttonScan.setEnabled(true);
            normalModeButton.setEnabled(false);
            rtButton.setEnabled(false);
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
            case R.id.menuLauncherResetMacAddresses:
                Toast.makeText(this,"insoles addresses deleted from phone",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
