package com.example.fadi.testingrx;

import android.content.Context;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;

import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.f.ai.AiPostureManager;

import com.example.fadi.testingrx.f.ai.SensorsReading;
import com.example.fadi.testingrx.f.posture.CommunicationCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;

import com.example.fadi.testingrx.ui.AddAIPostureActivity;
import com.polidea.rxandroidble.RxBleConnection;


import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import rx.Observable;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by fadi on 06/12/2017.
 */

public class AiRealTimeActivity  extends AppCompatActivity implements CommunicationCallback {

    // observables
    Observable<RxBleConnection> leftInsoleConnectionObservable;//this observable will be used any time we want to interact with a characteristic, no need to establish new connection for every operation.
    Observable<RxBleConnection> rightInsoleConnectionObservable;

    PostureTracker mPostureTracker;

    TextView textViewAILeftConnectionStatus;
    TextView textViewAIRightConnectionStatus;

    TextView textViewAILeftBatteryValue;
    TextView textViewAIRightBatteryValue;

    TextView textViewCurrentPosture;
    TextView textViewTrainingLabel;

    public static final int REQUEST_CODE_GET_POSTURE_NAME_AND_ICON=12;

    String TAG="AiRT";

    AiPostureManager aiPostureManager;

    Button buttonAddSample;

    EditText editTextPostureName;

    int latestLX;
    int latestLY;
    int latestLZ;
    int latestRX;
    int latestRY;
    int latestRZ;
    SensorsReading latestSensorsReading;

    ImageView imageViewBrain;

    Disposable timerDisposable;

    TextView textViewLeftMac;
    TextView textViewRightMac;


    // this is to ensure font changes happen in this activity.
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_ai_real_time);


        Log.d(TAG,"ai real time activity onCreate is called");

        initUI();

        // prepare the object that will process posture detection
        mPostureTracker = new PostureTracker(this);

        MyApplication.getBleManager().connectRealTime(mPostureTracker);

        aiPostureManager = new AiPostureManager();
    }



    //this will be called from the posture detection class, to let MainActivity updates the views it has to reflect the real postures.
    @Override
    public void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter){

        Log.d(TAG, "received position in MainActivity call back is:"+i);
        // in the ai real time activity, we will not need the actual positions, instead, we need the raw sensor data.
    }

    @Override
    public void notifyLeftConnectionDisconnected() {
        runOnUiThread(() -> {textViewAILeftConnectionStatus.setText("Disconnected");});
    }

    @Override
    public void notifyRightConnectionDisconnected() {
        runOnUiThread(() -> {textViewAIRightConnectionStatus.setText("Disconnected");});
    }

    @Override
    public void notifyLeftConnectionIsConnecting() {
        runOnUiThread(() -> {textViewAILeftConnectionStatus.setText("Connecting..");
            textViewAILeftBatteryValue.setText("?");});
    }

    @Override
    public void notifyRightConnectionIsConnecting() {
        runOnUiThread(() -> {textViewAIRightConnectionStatus.setText("Connecting..");
            textViewAIRightBatteryValue.setText("?");});
    }

    @Override
    public void notifyLeftConnectionConnected() {
        runOnUiThread(() -> {textViewAILeftConnectionStatus.setText("Connected");});
    }

    @Override
    public void notifyRightConnectionConnected() {
        runOnUiThread(() -> {textViewAIRightConnectionStatus.setText("Connected");});
    }

    @Override
    public void notifyLeftServiceDiscoveryCompleted() {

    }

    @Override
    public void notifyRightServiceDiscoveryCompleted() {

    }

    @Override
    public void notifyLeftBattery(int value) {
        runOnUiThread(() -> {textViewAILeftBatteryValue.setText(String.valueOf(value)+"%");});
    }

    @Override
    public void notifyRightBattery(int value) {
        runOnUiThread(() -> {textViewAIRightBatteryValue.setText(String.valueOf(value)+"%");});
    }


    private void initUI() {

        textViewLeftMac = (TextView) findViewById(R.id.textViewAILeftMac);
        textViewLeftMac.setText(getIntent().getStringExtra(LauncherActivity.KEY_LEFT_INSOLE_MAC));

        textViewRightMac = (TextView) findViewById(R.id.textViewAIRightMac);
        textViewRightMac.setText(getIntent().getStringExtra(LauncherActivity.KEY_RIGHT_INSOLE_MAC));

        textViewAILeftConnectionStatus = (TextView) findViewById(R.id.textViewAILeftConnectionStatus);
        textViewAILeftConnectionStatus.setText("Communicating");
        textViewAIRightConnectionStatus = (TextView) findViewById(R.id.textViewAIRightConnectionStatus);
        textViewAIRightConnectionStatus.setText("Communicating");

        textViewAILeftBatteryValue = (TextView) findViewById(R.id.textViewAILeftBatteryValue);
        textViewAIRightBatteryValue = (TextView) findViewById(R.id.textViewAIRightBatteryValue);

        textViewTrainingLabel = (TextView) findViewById(R.id.textViewTrainingLabel);
        textViewTrainingLabel.setVisibility(View.GONE);

        buttonAddSample= (Button) findViewById(R.id.buttonAddSample);

        buttonAddSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latestSensorsReading = new SensorsReading(latestLX,latestLY,latestLZ,latestRX,latestRY,latestRZ);

                //now start another activity to get the name of posture
                Intent getPostureNameIntent = new Intent (getApplication().getApplicationContext(), AddAIPostureActivity.class);

                startActivityForResult(getPostureNameIntent,REQUEST_CODE_GET_POSTURE_NAME_AND_ICON);//once the result comes, it will be handled in onActivityResult method
            }
        });

        editTextPostureName = (EditText) findViewById(R.id.editTextPostureName);

        textViewCurrentPosture = (TextView) findViewById(R.id.textViewCurrentPosture);

        imageViewBrain = (ImageView) findViewById(R.id.imageViewBrain);
        imageViewBrain.setImageDrawable(getResources().getDrawable(R.drawable.brain));
        imageViewBrain.setVisibility(View.GONE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getBleManager().closeAllConnections();
        Log.d(TAG,"MainActivity onDestroy is called");
        finish();//demo mode
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_live_mode, menu);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop is called for the aiRT");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onStop is called for the aiRT");
    }

    @Override
    public void notifyLatestSensorReadings(int lx,int ly, int lz, int rx, int ry, int rz) {
        Log.d(TAG,String.format("latestSensorReadings are: lx:%d ly:%d lz:%d rx:%d ry:%d rz:%d",lx,ly,lz,rx,ry,rz));

        latestLX=lx;
        latestLY=ly;
        latestLZ=lz;

        latestRX=rx;
        latestRY=ry;
        latestRZ=rz;

        String currentPostureName=aiPostureManager.getPostureName(new SensorsReading(latestLX,latestLY,latestLZ,latestRX,latestRY,latestRZ));
        Log.d(TAG,"current posture name is:"+currentPostureName);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewCurrentPosture.setText(currentPostureName);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==1){
            addTrainingSample(data.getStringExtra("posture_name"));
        }

        if (resultCode==0){
            Log.d(TAG,"result code is 0, means user choose cancel");
        }
    }

    private void addTrainingSample(String pName){
        //String postureName= editTextPostureName.getText().toString();

        aiPostureManager.addPostureSample(latestSensorsReading,pName);
        Toast.makeText(getApplicationContext(),"a new sample added to training data",Toast.LENGTH_LONG).show();

        timerDisposable= io.reactivex.Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .take(2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                            Log.d(TAG,"one second passed, it is:"+aLong);
                            imageViewBrain.setVisibility(View.VISIBLE);
                            textViewTrainingLabel.setVisibility(View.VISIBLE);


                        },
                        t -> {
                            Log.e(TAG, "error from TimerObserver:" + t.toString());
                        },
                        () -> {
                            Log.e(TAG, " scan timer Observer received onCompleted()");
                            imageViewBrain.setVisibility(View.GONE);
                            textViewTrainingLabel.setVisibility(View.GONE);
                        });
    }
}