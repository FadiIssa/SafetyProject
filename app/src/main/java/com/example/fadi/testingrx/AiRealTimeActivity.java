package com.example.fadi.testingrx;

import android.content.Context;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.audio.Speaker;
import com.example.fadi.testingrx.f.ai.AiPostureManager;

import com.example.fadi.testingrx.f.ai.SensorsReading;
import com.example.fadi.testingrx.f.posture.CommunicationCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;

import com.example.fadi.testingrx.ui.AddAIPostureActivity;
import com.polidea.rxandroidble.RxBleConnection;


import java.util.HashMap;
import java.util.Random;
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
    TextView textViewCurrentPostureLabel;
    ProgressBar progressBarAnalyzing;

    ImageView imageViewCurrentPosture;

    Speaker mSpeaker;

    boolean leftInsoleConnected;
    boolean rightInsoleConnected;

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

    HashMap<String,Integer> postureIcons = new HashMap<>();

    Drawable drawable_posture_1;
    Drawable drawable_posture_2;
    Drawable drawable_posture_3;
    Drawable drawable_posture_4;
    Drawable drawable_posture_5;
    Drawable drawable_posture_6;

    Random mRandom;

    boolean postureChangedFromLastTime;//it will be used to determine whether to speak about the new posture in speaker or not.
    String latestPostureName;//it will hold the name of the laters posture, to be used to determine whether to speak the posture name or not.
    int lastPostureDuration;
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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.ai_activity_toolbar);
        myToolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        myToolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));

        Log.d(TAG,"ai real time activity onCreate is called");

        initUI();

        // prepare the object that will process posture detection
        mPostureTracker = new PostureTracker(this);

        MyApplication.getBleManager().connectRealTime(mPostureTracker);

        aiPostureManager = new AiPostureManager();
        mSpeaker = Speaker.getInstance(getApplicationContext());
        mSpeaker.speak("Welcome to S cube A I system");

        leftInsoleConnected=false;
        rightInsoleConnected=false;

        postureChangedFromLastTime=false;
        lastPostureDuration=0;
        latestPostureName="fake";//just any name to be different from default posture name

        mRandom = new Random(3);


    }

    //this will be called from the posture detection class, to let MainActivity updates the views it has to reflect the real postures.
    @Override
    public void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter){

        Log.d(TAG, "received position in MainActivity call back is:"+i);
        // in the ai real time activity, we will not need the actual positions, instead, we need the raw sensor data.
    }

    @Override
    public void notifyLeftConnectionDisconnected() {
        runOnUiThread(() -> {
            leftInsoleConnected=false;
            textViewAILeftConnectionStatus.setText("Disconnected");
            buttonAddSample.setVisibility(View.GONE);
        });
    }

    @Override
    public void notifyRightConnectionDisconnected() {
        runOnUiThread(() -> {
            rightInsoleConnected=false;
            textViewAIRightConnectionStatus.setText("Disconnected");});
    }

    @Override
    public void notifyLeftConnectionIsConnecting() {
        leftInsoleConnected=false;
        runOnUiThread(() -> {textViewAILeftConnectionStatus.setText("Connecting..");
            textViewAILeftBatteryValue.setText("?");});
    }

    @Override
    public void notifyRightConnectionIsConnecting() {
        rightInsoleConnected=false;
        runOnUiThread(() -> {
            textViewAIRightConnectionStatus.setText("Connecting..");
            textViewAIRightBatteryValue.setText("?");
        });
    }

    @Override
    public void notifyLeftConnectionConnected() {
        leftInsoleConnected=true;

        runOnUiThread(() -> {
            textViewAILeftConnectionStatus.setText("Connected");
            if (rightInsoleConnected && leftInsoleConnected){
                buttonAddSample.setVisibility(View.VISIBLE);
                mSpeaker.speak("I am ready to learn.");
            }
        });
    }

    @Override
    public void notifyRightConnectionConnected() {
        rightInsoleConnected=true;

        runOnUiThread(() -> {
            textViewAIRightConnectionStatus.setText("Connected");
            if (rightInsoleConnected && leftInsoleConnected){
                buttonAddSample.setVisibility(View.VISIBLE);
                mSpeaker.speak("I am ready to learn.");
            }
        });
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

        progressBarAnalyzing = findViewById(R.id.progressBarAnalyzing);

        textViewCurrentPostureLabel = findViewById(R.id.textViewCurrentPostureLabel);

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
        imageViewBrain.setImageDrawable(getResources().getDrawable(R.drawable.ic_learning_status));
        imageViewBrain.setVisibility(View.GONE);

        imageViewCurrentPosture = findViewById(R.id.imageViewCurrentPosture);

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
            getMenuInflater().inflate(R.menu.menu_ai_mode, menu);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop is called for the aiRT");
        mSpeaker.muteSpeaker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSpeaker!=null){
            mSpeaker.unmuteSpeaker();
        }
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
        if (currentPostureName.equalsIgnoreCase("no posture available") || (buttonAddSample.getVisibility()==View.GONE)){
            ;//ignoe
        }
        else {
            if (currentPostureName.equalsIgnoreCase(latestPostureName)){
                postureChangedFromLastTime=false;//do nothing
                lastPostureDuration++;
                if (lastPostureDuration==4){
                    speakPosture(currentPostureName);
                }
            } else {
                postureChangedFromLastTime=true;
                lastPostureDuration=0;
                latestPostureName=currentPostureName;
                //speakPosture(currentPostureName);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewCurrentPosture.setText(currentPostureName);

                if (postureIcons.get(currentPostureName)==null)
                    return;

                int iconOrder=postureIcons.get(currentPostureName);
                switch (iconOrder){
                    case 1:
                        if (drawable_posture_1==null)
                        {
                            drawable_posture_1 = getDrawable(R.drawable.ic_posture_1);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_1);
                        break;
                    case 2:
                        if (drawable_posture_2==null)
                        {
                            drawable_posture_2 = getDrawable(R.drawable.ic_posture_2);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_2);
                        break;
                    case 3:
                        if (drawable_posture_3==null)
                        {
                            drawable_posture_3 = getDrawable(R.drawable.ic_posture_3);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_3);
                        break;
                    case 4:
                        if (drawable_posture_4==null)
                        {
                            drawable_posture_4 = getDrawable(R.drawable.ic_posture_4);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_4);
                        break;
                    case 5:
                        if (drawable_posture_5==null)
                        {
                            drawable_posture_5 = getDrawable(R.drawable.ic_posture_5);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_5);
                        break;
                    case 6:
                        if (drawable_posture_6==null)
                        {
                            drawable_posture_6 = getDrawable(R.drawable.ic_posture_6);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_6);
                        break;
                    default:
                        if (drawable_posture_6==null)
                        {
                            drawable_posture_6 = getDrawable(R.drawable.ic_posture_6);
                        }
                        imageViewCurrentPosture.setImageDrawable(drawable_posture_6);
                        Log.e(TAG,"there should be no posture icon that is not between 1 and 6");
                        break;//maybe the break is not needed here
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==1){
            textViewCurrentPosture.setVisibility(View.GONE);
            String postureName=data.getStringExtra("posture_name");
            addTrainingSample(postureName);
            postureIcons.put(postureName,data.getIntExtra("posture_icon",6));
        }

        if (resultCode==0){
            Log.d(TAG,"result code is 0, means user choose cancel");
        }
    }

    private void addTrainingSample(String pName){
        //String postureName= editTextPostureName.getText().toString();

        startLearningAnimation();
        aiPostureManager.addPostureSample(latestSensorsReading,pName);
        Toast.makeText(getApplicationContext(),"a new sample added to training data",Toast.LENGTH_LONG).show();

        imageViewBrain.setVisibility(View.VISIBLE);
        textViewTrainingLabel.setVisibility(View.VISIBLE);

        timerDisposable= io.reactivex.Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .take(4)
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(aLong -> {
                            Log.d(TAG,"one second passed, it is:"+aLong);
                        },
                        t -> {
                            Log.e(TAG, "error from TimerObserver:" + t.toString());
                        },
                        () -> {
                            Log.e(TAG, " scan timer Observer received onCompleted()");
                            imageViewBrain.setVisibility(View.GONE);
                            textViewTrainingLabel.setVisibility(View.GONE);
                            //textViewCurrentPosture.setVisibility(View.VISIBLE);
                            finishLearningAnimation();
                        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_ai_reset:
                resetPostures();
                imageViewCurrentPosture.setImageDrawable(getDrawable(R.drawable.ic_posture_6));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetPostures(){
        aiPostureManager.resetPostures();
        latestPostureName="fake";
        postureChangedFromLastTime=false;
        lastPostureDuration=0;
    }

    private void startLearningAnimation(){
        buttonAddSample.setVisibility(View.GONE);
        imageViewCurrentPosture.setVisibility(View.GONE);
        textViewCurrentPosture.setVisibility(View.GONE);
        textViewCurrentPostureLabel.setVisibility(View.GONE);
        progressBarAnalyzing.setVisibility(View.GONE);
        mSpeaker.speak("I am learning");
    }

    private void finishLearningAnimation(){
        buttonAddSample.setVisibility(View.VISIBLE);
        imageViewCurrentPosture.setVisibility(View.VISIBLE);
        textViewCurrentPosture.setVisibility(View.VISIBLE);
        textViewCurrentPostureLabel.setVisibility(View.VISIBLE);
        progressBarAnalyzing.setVisibility(View.VISIBLE);
        mSpeaker.speak("your posture is now saved. Now you can add another posture.");
    }

    private void speakPosture(String postureName){
        int i = mRandom.nextInt(3);

        switch (i){
            case 0:
                mSpeaker.speakPosture("you are in " + postureName + " posture.");
                break;
            case 1:
                mSpeaker.speakPosture("your posture is "+ postureName);
                break;
            case 2:
                mSpeaker.speakPosture(postureName);
                break;
            default:
                mSpeaker.speakPosture(postureName + " posture");
                break;
        }
    }



}