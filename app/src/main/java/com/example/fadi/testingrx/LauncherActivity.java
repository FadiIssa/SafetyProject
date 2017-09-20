package com.example.fadi.testingrx;


import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fadi.testingrx.f.ble.ScanStatusCallback;
import com.jakewharton.rxbinding2.view.RxView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LauncherActivity extends AppCompatActivity implements ScanStatusCallback{
    static final String LEFT_INSOLE_MAC_KEY="leftInsoleMac";
    static final String RIGHT_INSOLE_MAC_KEY="rightInsoleMac";
    public static final String SHARED_PREF_ENTRY="PairedDevices";
    String TAG="1Act";

    TextView textViewScanStatusLeft;
    TextView textViewScanStatusRight;

    ImageView imageViewLogo;
    Button rtButton;
    Button normalModeButton;//this mode is the one Karim suggested. to hide what is in real time and what is sent after an activity.
    Button buttonScan;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        getSupportActionBar().hide();
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

        normalModeButton = (Button) findViewById(R.id.buttonNormalMode);
        normalModeButton.setEnabled(false);

        rtButton = (Button) findViewById(R.id.buttonRT);
        rtButton.setEnabled(false);

        imageViewLogo = (ImageView) findViewById(R.id.imageViewLauncherLogo);

        if (MyApplication.EltenMode) {
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.elten_logo));
            imageViewLogo.setImageDrawable(getDrawable(R.drawable.elten100));
            rtButton.setBackground(getDrawable(R.drawable.realtime_elten));
            normalModeButton.setBackground(getDrawable(R.drawable.normal_mode_elten));

        } else {// it is Uvex
            imageViewLogo.setImageDrawable(getDrawable(R.drawable.uvex_logo));
            rtButton.setBackground(getDrawable(R.drawable.realtime_icon0));
            normalModeButton.setBackground(getDrawable(R.drawable.normalmode_icon));
        }

        RxView.clicks(rtButton)
                .subscribe(a-> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });

        RxView.clicks(normalModeButton)
                .subscribe(a->{
                    Intent intent = new Intent(this, NormalModeActivity.class);
                    startActivity(intent);
                    finish();
                });

        // the user can make a scan if the previous scan timed out without finding any insoles for example.
        RxView.clicks(buttonScan)
                .subscribe(a->{
                    scan();
                    rtButton.setEnabled(false);
                    normalModeButton.setEnabled(false);
                    buttonScan.setEnabled(false);
                });
    }

    @Override
    public void scanStatusLeftStopped() {
        runOnUiThread(() -> {
            textViewScanStatusLeft.setText("stopped");
        });

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
}
