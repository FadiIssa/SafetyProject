package com.example.fadi.testingrx.ui.uvex;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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

import com.example.fadi.testingrx.MyApplication;
import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.MockDataProcessor;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionDBHelper;
import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.f.StatsCalculator;
import com.example.fadi.testingrx.f.ble.Insoles;
import com.example.fadi.testingrx.f.posture.CommunicationCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.example.fadi.testingrx.ui.SavedActivitiesBrowserActivity;
import com.example.fadi.testingrx.ui.StatsCalculaterCallback;
import com.jakewharton.rxbinding2.view.RxView;
import com.polidea.rxandroidble.RxBleConnection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class NormalModeUvex extends AppCompatActivity implements StatsCalculaterCallback,CommunicationCallback{

    private static final int MINIMUM_ACTIVITY_TIME=40;//this value is determined in the firmware, I just have to update it here to reflect it, usually it should be 5 minutes, in order not to save huge data fro the whole day.

    String TAG="UvexN";

    boolean isLeftInsoleConnected;
    boolean isRightInsoleConnected;

    boolean leftStartActivityCommandSentSuccessfully;
    boolean rightStartActivityCommandSentSuccessfully;

    boolean leftStopActivityCommandSentSuccessfully;
    boolean rightStopActivityCommandSentSuccessfully;

    boolean isActivityStarted;//this will be used to enable/disable the startActivity button.

    //TextView textViewLeftConnectionStatus;
    ImageView imageViewLeftConnectionStatus;
    Drawable drawableLeftConnecting;
    Drawable drawableLeftConnected;

    //TextView textViewRightConnectionStatus;
    ImageView imageViewRightConnectionStatus;
    Drawable drawableRightConnecting;
    Drawable drawableRightConnected;

    Drawable drawableConnectionStopped;

    Button buttonStartActivityUvex;
    Button buttonStopActivityUvex;

    TextView textViewTimer;
    ImageView imageViewTimer;

    Drawable drawableTimer0;
    Drawable drawableTimer1;
    Drawable drawableTimer2;
    Drawable drawableTimer3;
    Drawable drawableTimer4;

    Subscription leftInsoleIndicationSubscription;
    Subscription rightInsoleIndicationSubscription;

    int totalCrouchingTime;// in seconds
    int totalTiptoesTime;
    int totalKneelingTime;

    StatsCalculator mStatsCalculator;
    PostureTracker mPostureTracker;

    // observables
    Observable<RxBleConnection> leftInsoleConnectionObservable;//this observable will be used any time we want to interact with a characteristic, no need to establish new connection for every operation.
    Observable<RxBleConnection> rightInsoleConnectionObservable;

    Observable<Long> timerObservable;
    Subscription timerSubscription;
    Observer<Long> timerObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_uvex_normal);

        mStatsCalculator = new StatsCalculator(this);

        isLeftInsoleConnected = false;
        isRightInsoleConnected = false;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.uvex_normal_toolbar);
        myToolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        //myToolbar.setLogo(getDrawable(R.drawable.uvex_logo_launcher_bar));
        myToolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));

        initUI();

        // prepare the object that will process posture detection
        mPostureTracker = new PostureTracker(this);
//        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";

        MyApplication.getBleManager().connectRealTime(mPostureTracker);
    }

    private void resetOnScreenStats(){
        runOnUiThread(() -> {

        });
    }

    private void startSafetyActivity(){


        //reset global variable states
        leftStartActivityCommandSentSuccessfully=false;
        rightStartActivityCommandSentSuccessfully=false;

        //reset posture counters
        mPostureTracker.reset();
        resetOnScreenStats();

        //reset statistics
        mStatsCalculator.startSession();

        if (leftInsoleConnectionObservable==null){
            leftInsoleConnectionObservable=MyApplication.getBleManager().getLeftInsoleConnectionObservable();
        }

        if (rightInsoleConnectionObservable==null){
            rightInsoleConnectionObservable=MyApplication.getBleManager().getRightInsoleConnectionObservable();
        }

        //check if the activity is not already started

        byte[] startCommandArray= {0x01};

        if (isLeftInsoleConnected()&&isRightInsoleConnected()){
            //both insoles are connected, now sending commands to each one alone.
            Log.d(TAG,"startSafety Activity, both insoles are connected now, so sending the write command to them one by one.");

            if (isLeftInsoleConnected()){
                leftInsoleConnectionObservable
                        .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),startCommandArray))
                        .subscribe(bytes -> onWriteSuccess(true),(e)->onWriteError(e));
                Log.d(TAG, "activity started, writing to command characteristic of left insole");
            }
            else{
                Log.d(TAG,"could not start activity, no connection with left insole.");
                Toast.makeText(this, "could not start, left insole disconnected", Toast.LENGTH_SHORT).show();
            }

            if (isRightInsoleConnected()){
                rightInsoleConnectionObservable
                        .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),startCommandArray))
                        .subscribe(bytes -> onWriteSuccess(false),(e)->onWriteError(e));
                Log.d(TAG, "activity started, writing to command characteristic of right insole");
            }
            else{
                Log.d(TAG,"could not start activity, no connection with right insole.");
                Toast.makeText(this, "could not start, right insole disconnected", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "ensure both insoles are connected then try again",Toast.LENGTH_LONG).show();
        }
    }

    private boolean isLeftInsoleConnected(){
        if (MyApplication.getBleManager().getLeftInsoleDevice()!=null){
            return (MyApplication.getBleManager().getLeftInsoleDevice().getConnectionState()== RxBleConnection.RxBleConnectionState.CONNECTED);
        }
        else {
            Log.d(TAG,"you should not see this, try to prevent this operation if the bleDevice is null");
            return false;
        }
    }

    private boolean isRightInsoleConnected(){
        if (MyApplication.getBleManager().getRightInsoleDevice()!=null){
            return (MyApplication.getBleManager().getRightInsoleDevice().getConnectionState()== RxBleConnection.RxBleConnectionState.CONNECTED);
        }
        else {
            Log.d(TAG,"you should not see this, try to prevent this operation if the bleDevice is null");
            return false;
        }
    }

    private void onWriteSuccess( boolean isLeftInsole){
        if (isLeftInsole) {
            Log.d(TAG, "successful write operation to left insole");
            leftStartActivityCommandSentSuccessfully=true;
        } else {
            Log.d(TAG, "successful write operation to right insole");
            rightStartActivityCommandSentSuccessfully=true;
        }

        if (leftStartActivityCommandSentSuccessfully&&rightStartActivityCommandSentSuccessfully){
            leftStopActivityCommandSentSuccessfully=false;
            rightStopActivityCommandSentSuccessfully=false;
            startTimer();
            isActivityStarted=true;
            runOnUiThread(() -> {
                buttonStartActivityUvex.setEnabled(false);
            });
        }
    }

    private void onStopActivityWriteSuccess(boolean isleftInsole){
        if (isleftInsole) {
            Log.d(TAG, "successful left write operation to stop activity");
            leftStopActivityCommandSentSuccessfully=true;
        } else {
            Log.d(TAG, "successful right write operation to stop activity");
            rightStopActivityCommandSentSuccessfully=true;
        }

        if (leftStopActivityCommandSentSuccessfully&&rightStopActivityCommandSentSuccessfully){
            leftStartActivityCommandSentSuccessfully=false;
            rightStartActivityCommandSentSuccessfully=false;
            isActivityStarted=false;
            runOnUiThread(() -> {
                buttonStopActivityUvex.setEnabled(false);
                buttonStartActivityUvex.setEnabled(true);
            });
        }
    }

    private void onWriteError(Throwable e){
        Log.d(TAG, "error while writing characteristic: "+e.toString());
    }

    @Override
    public void updatePositionCallBack(int i, int currentPosCounter, int crouchingCounter, int kneelingCounter, int tiptoesCounter) {
        Log.d(TAG, "received position in MainActivity call back is:"+i);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPositionCounter=currentPosCounter/2;


                //setting the counter of current posture
                int currentPositionCounterHours= currentPositionCounter/3600;
                int currentPositionCounterMinutes= (currentPositionCounter%3600)/60;
                int currentPositionCounterSeconds= (currentPositionCounter%3600)%60;


                //counterCurrentPostureTextView.setText(String.valueOf(currentPositionCounterHours)+":"+String.valueOf(currentPositionCounterMinutes)+":"+String.valueOf(currentPositionCounterSeconds));

                int crouchingPositionCounter=crouchingCounter/2;
                totalCrouchingTime=crouchingPositionCounter;
                Log.d(TAG,"total crouching time:"+totalCrouchingTime);
                int crouchingPostureCounterHours=crouchingPositionCounter/3600;
                int crouchingPostureCounterMinutes=(crouchingPositionCounter%3600)/60;
                int crouchingPostureCounterSeconds=(crouchingPositionCounter%3600)%60;
                //counterCrouchingTextView.setText(String.valueOf(crouchingPostureCounterHours)+":"+String.valueOf(crouchingPostureCounterMinutes)+":"+String.valueOf(crouchingPostureCounterSeconds));

                int kneelingPositionCounter=kneelingCounter/2;
                totalKneelingTime= kneelingPositionCounter;
                Log.d(TAG,"total kneeling time:"+totalKneelingTime);
                int kneelingPostureCounterHours=kneelingPositionCounter/3600;
                int kneelingPostureCounterMinutes=(kneelingPositionCounter%3600)/60;
                int kneelingPostureCounterSeconds=(kneelingPositionCounter%3600)%60;
                //counterKneelingTextView.setText(String.valueOf(kneelingPostureCounterHours)+":"+String.valueOf(kneelingPostureCounterMinutes)+":"+String.valueOf(kneelingPostureCounterSeconds));

                int tiptoesPositionCounter=tiptoesCounter/2;
                totalTiptoesTime=tiptoesPositionCounter;
                Log.d(TAG,"total tiptoes time:"+totalTiptoesTime);
                int tiptoesPostureCounterHours=tiptoesPositionCounter/3600;
                int tiptoesPostureCounterMinutes=(tiptoesPositionCounter%3600)/60;
                int tiptoesPostureCounterSeconds=(tiptoesPositionCounter%3600)%60;
                //counterTiptoesTextView.setText(String.valueOf(tiptoesPostureCounterHours)+":"+String.valueOf(tiptoesPostureCounterMinutes)+":"+String.valueOf(tiptoesPostureCounterSeconds));
            }
        });
    }

    @Override
    public void notifyLeftConnectionDisconnected() {
        runOnUiThread(() -> {
            //textViewLeftConnectionStatus.setText("Disconnected");
            imageViewLeftConnectionStatus.setImageDrawable(drawableConnectionStopped);//I am using the same as connecting, maybe this should be changed.
        });

            isLeftInsoleConnected=false;
    }

    @Override
    public void notifyRightConnectionDisconnected() {
        runOnUiThread(() -> {
            //textViewRightConnectionStatus.setText("Disconnected");
            imageViewRightConnectionStatus.setImageDrawable(drawableConnectionStopped);
        });
        isRightInsoleConnected=false;
    }

    @Override
    public void notifyLeftConnectionIsConnecting() {
        runOnUiThread(() -> {
            //textViewLeftConnectionStatus.setText("Connecting..");
            imageViewLeftConnectionStatus.setImageDrawable(drawableLeftConnecting);
        });
        isLeftInsoleConnected=false;
    }

    @Override
    public void notifyRightConnectionIsConnecting() {
        runOnUiThread(() -> {
            //textViewRightConnectionStatus.setText("Connecting..");
            imageViewRightConnectionStatus.setImageDrawable(drawableRightConnecting);
        });
        isRightInsoleConnected=false;
    }

    @Override
    public void notifyLeftConnectionConnected() {
        runOnUiThread(() -> {
            //textViewLeftConnectionStatus.setText("Connected");
            imageViewLeftConnectionStatus.setImageDrawable(drawableLeftConnected);
            });
        isLeftInsoleConnected=true;
        processEnablingStartSafetyActivityButton();
    }

    @Override
    public void notifyRightConnectionConnected() {
        runOnUiThread(() -> {
        //    textViewRightConnectionStatus.setText("Connected");
            imageViewRightConnectionStatus.setImageDrawable(drawableRightConnected);
        });
        isRightInsoleConnected = true;
        processEnablingStartSafetyActivityButton();
    }

    @Override
    public void notifyLeftServiceDiscoveryCompleted() {

    }

    @Override
    public void notifyRightServiceDiscoveryCompleted() {
    }

    @Override
    public void notifyLeftBattery(int value) {

    }

    @Override
    public void notifyRightBattery(int value) {

    }

    private void stopSafetyActivity(){



        //check that both insoles are connected now
        if (!(isLeftInsoleConnected()&&isRightInsoleConnected())) {
            Toast.makeText(this,"ensure both insoles are connected",Toast.LENGTH_LONG).show();
            return;
        }

        if (timerSubscription!=null) {
            if (!timerSubscription.isUnsubscribed()) {
                timerSubscription.unsubscribe();
                runOnUiThread(() -> {textViewTimer.setText("0:0:0");});
            }
        }

        //check if the activity is not already started
        byte[] stopCommandArray= {0x02};
        //send command to stop the activity
        if (isLeftInsoleConnected()){
            if (leftInsoleIndicationSubscription!=null){
                if (!leftInsoleIndicationSubscription.isUnsubscribed())
                    leftInsoleIndicationSubscription.unsubscribe();
            }
            // from here I should start the indication subscription, to read the whole data after the activity finishes.
            Log.d(TAG," subscribing to indication for left insole");
            leftInsoleIndicationSubscription=leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupIndication(UUID.fromString(Insoles.CHARACTERISTIC_CHUNK)))
                    //.doOnNext(indicationObservable -> {Log.d(TAG,"indication observable is set");})
                    //.subscribe(myLeftInsoleIndicationObserver);

                    .flatMap(notificationObservable -> notificationObservable)
                    //  .distinct()
                    //       .first()
                    //   .doOnCompleted(()->{leftInsoleIndicationSubscription.unsubscribe();})
                    .subscribe(bytes -> {
                                for (int i=0;i<bytes.length;i++){
                                    Log.d(TAG,"indication left byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                                }

                                if ( ((bytes[0]&0xFF)==0) && ((bytes[1]&0xFF)==0) ) {
                                    mStatsCalculator.processFirstHalfLeft(bytes);
                                }
                                if ( ((bytes[0]&0xFF)==0) && ((bytes[1]&0xFF)==128) ) {
                                    mStatsCalculator.processSecondHalfLeft(bytes);
                                }
                            },
                            throwable -> {Log.d(TAG,"LeftInsoleIndicationObserver reader onError "+throwable.toString());});

            leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),stopCommandArray))
                    .subscribe(bytes -> onStopActivityWriteSuccess(true),(e)->onWriteError(e));

            Log.d(TAG, "activity stopped, writing to command characteristic of left insole");

            //here I should start the indication thig.

//            leftInsoleConnectionObservable
//                    .flatMap(rxBleConnection -> rxBleConnection.setupIndication(UUID.fromString(Insoles.CHARACTERISTIC_CHUNK)))
//                    .subscribe(myLeftInsoleIndicationObserver);

        }
        else{
            Log.d(TAG,"could not stop activity, no connection with left insole.");
        }

        if (isRightInsoleConnected()){
            if (rightInsoleIndicationSubscription!=null){
                if (!rightInsoleIndicationSubscription.isUnsubscribed())
                    rightInsoleIndicationSubscription.unsubscribe();
            }
            // from here I should start the indication subscription, to read the whole data after the activity finishes.
            Log.d(TAG," subscribing to indication for right insole");
            rightInsoleIndicationSubscription=rightInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupIndication(UUID.fromString(Insoles.CHARACTERISTIC_CHUNK)))
                    //.doOnNext(indicationObservable -> {Log.d(TAG,"indication observable is set");})
                    //.subscribe(myLeftInsoleIndicationObserver);

                    .flatMap(notificationObservable -> notificationObservable)
                    //  .distinct()
                    //       .first()
                    //   .doOnCompleted(()->{leftInsoleIndicationSubscription.unsubscribe();})
                    .subscribe(bytes -> {
                                for (int i=0;i<bytes.length;i++){
                                    Log.d(TAG,"indication right byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                                }
                                if ( ((bytes[0]&0xFF)==0) && ((bytes[1]&0xFF)==0) ) {
                                    mStatsCalculator.processFirstHalfRight(bytes);
                                }
                                if ( ((bytes[0]&0xFF)==0) && ((bytes[1]&0xFF)==128) ) {
                                    mStatsCalculator.processSecondHalfRight(bytes);
                                }
                            },
                            throwable -> {Log.d(TAG,"RightInsoleIndicationObserver reader onError "+throwable.toString());});

            rightInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),stopCommandArray))
                    .subscribe(bytes -> onStopActivityWriteSuccess(false),(e)->onWriteError(e));

            Log.d(TAG, "activity stopped, writing to command characteristic of right insole");
        }
        else{
            Log.d(TAG,"could not stop activity, no connection with right insole.");
        }
    }

    private void initUI(){

        isActivityStarted=false;

        drawableLeftConnected = getDrawable(R.drawable.ic_connected_left);
        drawableLeftConnecting = getDrawable(R.drawable.ic_connecting_left);

        drawableRightConnected = getDrawable(R.drawable.ic_connected_right);
        drawableRightConnecting = getDrawable(R.drawable.ic_connecting_right);

        drawableTimer0 = getDrawable(R.drawable.ic_timer0);
        drawableTimer1 = getDrawable(R.drawable.uvex_timer_1);
        drawableTimer2 = getDrawable(R.drawable.uvex_timer_2);
        drawableTimer3 = getDrawable(R.drawable.uvex_timer_3);
        drawableTimer4 = getDrawable(R.drawable.uvex_timer_4);

        drawableConnectionStopped = getDrawable(R.drawable.elten_connection_stopped);

        imageViewLeftConnectionStatus = (ImageView) findViewById(R.id.imageViewLeftConnectionStatus);
        imageViewLeftConnectionStatus.setImageDrawable(drawableLeftConnecting);

        imageViewRightConnectionStatus = (ImageView) findViewById(R.id.imageViewRightConnectionStatus);
        imageViewRightConnectionStatus.setImageDrawable(drawableRightConnecting);


        textViewTimer = (TextView) findViewById(R.id.textViewTimerUvex);
        imageViewTimer = (ImageView) findViewById(R.id.imageViewTimer);

        imageViewTimer.setImageDrawable(drawableTimer0);

        buttonStartActivityUvex = (Button) findViewById(R.id.buttonStartActivityUvexNormal);
        buttonStartActivityUvex.setEnabled(false);

        RxView.clicks(buttonStartActivityUvex)
                // .map(a->buttonStartActivity.getText().toString().equals("Start"))
                .subscribe(a-> {
                    startSafetyActivity();
                    //startTimer();// the timer should only be started if the commands were sent successfuly.
                });

        buttonStopActivityUvex = (Button) findViewById(R.id.buttonStopActivityUvexNormal);
        disableStopActivityButton();

        RxView.clicks(buttonStopActivityUvex)
                .subscribe(a->{
                    stopSafetyActivity();
                });

        // init observables and observers
        timerObservable=Observable.interval(1, TimeUnit.SECONDS);
        timerObserver= new Observer<Long>() {

            @Override
            public void onNext(Long value) {
                runOnUiThread(() -> {
                    Long timeInHours=value/3600;
                    Long timeInMinutes=(value%3600)/60;
                    Long timeInSeconds=(value%3600)%60;
                    String finalText= (String.valueOf(timeInHours)+":"+String.valueOf(timeInMinutes)+":"+String.valueOf(timeInSeconds));
                    textViewTimer.setText(finalText);

                    switch (value.intValue()%4){
                        case 1:
                            imageViewTimer.setImageDrawable(drawableTimer1);
                            break;
                        case 2:
                            imageViewTimer.setImageDrawable(drawableTimer2);
                            break;
                        case 3:
                            imageViewTimer.setImageDrawable(drawableTimer3);
                            break;
                        case 0:
                            imageViewTimer.setImageDrawable(drawableTimer4);
                            break;
                        default:
                            imageViewTimer.setImageDrawable(drawableTimer0);
                            break;
                    }
                    if (value>MINIMUM_ACTIVITY_TIME){
                        buttonStopActivityUvex.setEnabled(true);
                    }
                    else {
                        buttonStopActivityUvex.setEnabled(false);
                    }

                });
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"error in time observer:"+e.toString());
            }

            @Override
            public void onCompleted() {
            }
        };

    }

    private void startTimer(){
        // we have first to check if previous subscriptions still exist, to unsubscribe them.
        if (timerSubscription!=null){
            if (!timerSubscription.isUnsubscribed()){
                timerSubscription.unsubscribe();
            }
        }
        timerSubscription=timerObservable
                .subscribe(timerObserver);// creating the observer locally, to listen for the timer ticking, and updating the screen accordingly.
    }

    @Override
    public void updateStatsOnUIString(final String standigTime, final String nStairs, final String nSteps, final String walkingTime, final String vibrationTime, final String leftAngle, final String rightAngle, final String distance, final String calories ){
        runOnUiThread(() -> {

            //now set the posture times
            Log.d(TAG,"total crouching time:"+totalCrouchingTime);
            //totalCrouchingTime=0;//remove this line after the demo, this is only for stability reasons, since the connectivity is not finalized yet.
            int crouchingPostureCounterHours=totalCrouchingTime/3600;
            int crouchingPostureCounterMinutes=(totalCrouchingTime%3600)/60;
            int crouchingPostureCounterSeconds=(totalCrouchingTime%3600)%60;
            //textViewCrouching.setText(String.valueOf(crouchingPostureCounterHours)+":"+String.valueOf(crouchingPostureCounterMinutes)+":"+String.valueOf(crouchingPostureCounterSeconds));

            //totalKneelingTime=0;//remove this line urgently after the demo.
            int kneelingPostureCounterHours=totalKneelingTime/3600;
            int kneelingPostureCounterMinutes=(totalKneelingTime%3600)/60;
            int kneelingPostureCounterSeconds=(totalKneelingTime%3600)%60;
            //textViewKneeling.setText(String.valueOf(kneelingPostureCounterHours)+":"+String.valueOf(kneelingPostureCounterMinutes)+":"+String.valueOf(kneelingPostureCounterSeconds));

            //totalTiptoesTime=0;//remove this line urgently after the demo.
            int tiptoesPostureCounterHours=totalTiptoesTime/3600;
            int tiptoesPostureCounterMinutes=(totalTiptoesTime%3600)/60;
            int tiptoesPostureCounterSeconds=(totalTiptoesTime%3600)%60;
            //textViewTiptoes.setText(String.valueOf(tiptoesPostureCounterHours)+":"+String.valueOf(tiptoesPostureCounterMinutes)+":"+String.valueOf(tiptoesPostureCounterSeconds));

        });
    }

    private void processEnablingStartSafetyActivityButton(){
        Log.d(TAG,"processEnablingStartButton, left connection is:"+isLeftInsoleConnected+" right connection is:"+isRightInsoleConnected);
        if (isLeftInsoleConnected && isRightInsoleConnected && (!isActivityStarted)){
            runOnUiThread(() -> {
                buttonStartActivityUvex.setEnabled(true);
            });
        }
    }

    private void disableStopActivityButton(){
        buttonStopActivityUvex.setEnabled(false);
    }

    private void enableStopActivityButton(){
        buttonStartActivityUvex.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_normal_mode, menu);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public void updateStatsOnUIValues(int standingTime, int stairs, int steps, int walkingTime, int vibrationTime, int leftAngle, int rightAngle, int distanceMeters, int calories, int vibrationIntensity) {

        //release any connection, to allow reconnecting when restarting the app for example.
        //MyApplication.getBleManager().closeAllConnections();//maybe I should not cancel all subscriptions, since going back to previous activities will not enable reconnecting automatically, the retryConnection will be disabled, to solve this, handle the reconnection and unsubscribing in onStop, onResume callbacks of the activity lifecycle.

        //generate an intent, and save its data to sql, and send the intent to statsActivityBrowser.
        Intent intent=new Intent(this,SessionStatsActivity.class);

        intent.putExtra(DataProcessing.NUM_STEPS,steps);
        intent.putExtra(DataProcessing.NUM_STAIRS,stairs);
        //intent.putExtra(DataProcessing.NUM_STAIRS,0);// remove this line and uncomment the above line to get the real count of the stairs.
        intent.putExtra(DataProcessing.DURATION_CROUCHING,totalCrouchingTime);
        intent.putExtra(DataProcessing.DURATION_KNEELING,totalKneelingTime);
        intent.putExtra(DataProcessing.DURATION_TIPTOES,totalTiptoesTime);
        intent.putExtra(DataProcessing.DURATION_WALKING,walkingTime);
        intent.putExtra(DataProcessing.DURATION_STATIC,standingTime);
        intent.putExtra(DataProcessing.CALORIES,calories);
        intent.putExtra(DataProcessing.DISTANCE_METERS,distanceMeters);
        intent.putExtra(DataProcessing.ANGLE_LEFT,leftAngle);
        intent.putExtra(DataProcessing.ANGLE_RIGHT,rightAngle);
        intent.putExtra(DataProcessing.FATIGUE,0);//needs to be calculated
        intent.putExtra(DataProcessing.VIBRATION_DURATION,vibrationTime);
        intent.putExtra(DataProcessing.VIBRATION_INTENSITY,vibrationIntensity);

        //save the same data that got received to database.
        SessionDBHelper myDBHelper = new SessionDBHelper(getApplicationContext());

        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(System.currentTimeMillis());

        SessionData sd= new SessionData.Builder()
                .setNumSteps(steps)
                //.setNumStairs(stairs)
                .setNumStairs(0)//remove this line and uncomment the above line to get the real number of stairs.
                .setDurationCrouching(totalCrouchingTime)
                .setDurationKneeling(totalKneelingTime)
                .setDurationTiptoes(totalTiptoesTime)
                .setDurationStatic(standingTime)
                .setDurationWalking(walkingTime)
                .setCalories(calories)
                .setDistanceMeters(distanceMeters)
                .setAngleLeft(leftAngle)
                .setAngleRight(rightAngle)
                .setFatigue(0)
                .setVibrationDuration(vibrationTime)
                .setVibrationIntensity(vibrationIntensity)
                .setDateTime(sdf.format(resultdate))
                .createSessionData();


        ContentValues values = new ContentValues();

        populateContentValuesWithSessionData(values,sd);

        long newRowId = db.insert(SessionContract.SessionTable.TABLE_NAME, null, values);
        Log.d(TAG,"after inserting the real activity row, here is its id:"+newRowId);

        db.close();
        myDBHelper.close();

        startActivity(intent);
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
    protected void onDestroy() {
        super.onDestroy();
        //MyApplication.getBleManager().closeAllConnections();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //MyApplication.getBleManager().closeAllConnections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MyApplication.getBleManager().resumeAllConnections();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_normal_mode_history:
                Intent intent = new Intent(this, SavedActivitiesBrowserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
