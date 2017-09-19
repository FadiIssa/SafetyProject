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
        rtButton = (Button) findViewById(R.id.buttonRT);
        rtButton.setEnabled(false);

        buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setEnabled(false);//because when the activity first starts, it starts scanning automatically.

        textViewScanStatusLeft = (TextView) findViewById(R.id.textViewScanStatusLeft);
        textViewScanStatusLeft.setText("");
        textViewScanStatusRight = (TextView) findViewById(R.id.textViewScanStatusRight);
        textViewScanStatusRight.setText("");

        imageViewLogo = (ImageView) findViewById(R.id.imageViewLauncherLogo);
        if (MyApplication.EltenMode) {
            imageViewLogo.setImageDrawable(getDrawable(R.drawable.elten_logo));
        } else {// it is Uvex
            imageViewLogo.setImageDrawable(getDrawable(R.drawable.uvex_logo));
        }

        normalModeButton = (Button) findViewById(R.id.buttonNormalMode);
        normalModeButton.setEnabled(false);

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

        RxView.clicks(buttonScan)
                .subscribe(a->{
                    scan();
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
    public void scanSatusFinished(String leftInsoleMac, String rightInsoleMac) {
        SharedPreferences prefs = getSharedPreferences("PairedDevices", Context.MODE_PRIVATE);
        prefs.edit().putString(LEFT_INSOLE_MAC_KEY,leftInsoleMac).putString(RIGHT_INSOLE_MAC_KEY,rightInsoleMac).commit();
        runOnUiThread(() -> {
            buttonScan.setEnabled(true);
            textViewScanStatusLeft.setText("insole saved");// this is wrong, we dont know yet if it was saved or not.
            textViewScanStatusRight.setText("insole saved");
        });
    }
    // this function is called when the activity first launches, and also, if a user decides to make another scan (in case the first scan did not find the insoles due to them being disconnected for example).
    private void scan(){
        Log.d(TAG,"launching scan from LauncherActivity");// scan here means to make a completely new scan, to check for


            // we need to read from shared preferences, to decide which scan versin to call
            SharedPreferences prefs = getApplication().getSharedPreferences(
                    "PairedDevices", Context.MODE_PRIVATE);

            String savedLeftMacAddress=prefs.getString("leftInsoleMac",null);
            String savedRightMacAddress=prefs.getString("rightInsoleMac",null);

            if (savedLeftMacAddress==null||savedRightMacAddress==null){
                Log.d(TAG,"start scan for discovery at:"+ System.currentTimeMillis());
                MyApplication.getBleManager().scanForDiscovery(this);
                Log.d(TAG,"scan for discovery ended at:"+ System.currentTimeMillis());// there is no point of this line, because the previous line is asynchronous.
            }
            else {
                MyApplication.getBleManager().scanAndPair(() -> {
                    rtButton.setEnabled(true);
                    normalModeButton.setEnabled(true);
                });
            }

    }
}
