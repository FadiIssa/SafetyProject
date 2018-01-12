package com.example.fadi.testingrx.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.fadi.testingrx.LauncherActivity;
import com.example.fadi.testingrx.MyApplication;
import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionDBHelper;
import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.f.StatsCalculator;
import com.example.fadi.testingrx.f.ble.Insoles;
import com.example.fadi.testingrx.f.posture.CommunicationCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.example.fadi.testingrx.ui.uvex.SessionStatsActivity;
import com.jakewharton.rxbinding2.view.RxView;
import com.polidea.rxandroidble.RxBleConnection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class NormalModeActivityNew extends AppCompatActivity implements StatsCalculaterCallback,CommunicationCallback{

    private static final int MINIMUM_ACTIVITY_TIME=45;//this value is determined in the firmware, I just have to update it here to reflect it, usually it should be 5 minutes, in order not to save huge data fro the whole day.

    // UI items
    //for connection status
    ImageView imageViewLeftConnectionStatus;
    Drawable drawableLeftConnecting;
    Drawable drawableLeftConnected;

    ImageView imageViewRightConnectionStatus;
    Drawable drawableRightConnecting;
    Drawable drawableRightConnected;

    Drawable drawableConnectionStopped;

    TextView textViewLeftMac;
    TextView textViewRightMac;

    // safety normal activity starting and stopping, by normal I mean the one that does not need real time connection.
    Button buttonStartNormalActivity;
    Button buttonStopNormalActivity;

    // timer
    TextView textViewTimer;
    ImageView imageViewTimer;

    Drawable drawableTimer0;
    Drawable drawableTimer1;
    Drawable drawableTimer2;
    Drawable drawableTimer3;
    Drawable drawableTimer4;

    String TAG="NMode";

    boolean isLeftInsoleConnected;
    boolean isRightInsoleConnected;

    boolean leftStartActivityCommandSentSuccessfully;
    boolean rightStartActivityCommandSentSuccessfully;

    boolean leftStopActivityCommandSentSuccessfully;
    boolean rightStopActivityCommandSentSuccessfully;

    boolean leftIndicationDataWellReceived;
    boolean rightIndicationDataWellReceived;

    boolean isActivityStarted;//this will be used to enable/disable the startActivity button.

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
        setContentView(R.layout.activity_normal_new);

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
            Log.d(TAG, "successful write operation of start activity to left insole");
            leftStartActivityCommandSentSuccessfully=true;
        } else {
            Log.d(TAG, "successful write operation of start activity to right insole");
            rightStartActivityCommandSentSuccessfully=true;
        }

        if (leftStartActivityCommandSentSuccessfully&&rightStartActivityCommandSentSuccessfully){
            leftStopActivityCommandSentSuccessfully=false;
            rightStopActivityCommandSentSuccessfully=false;
            startTimer();
            isActivityStarted=true;
            runOnUiThread(() -> {
                buttonStartNormalActivity.setEnabled(false);
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

        Log.d(TAG,"now checking if stop activity command was sent successfully to both insoles");
        if (leftStopActivityCommandSentSuccessfully&&rightStopActivityCommandSentSuccessfully){
            leftStartActivityCommandSentSuccessfully=false;
            rightStartActivityCommandSentSuccessfully=false;
            // here I am assuming that if the write command was sent successfully, it means the indication data was received successfully, but this is not the case.

        }
        else {
            Log.d(TAG,"it seems the stop command was not sent successfully to both insoles yet");
        }
    }

    private void onWriteError(Throwable e){
        Log.d(TAG, "error while writing characteristic: "+e.toString());
    }

    @Override
    public void updatePositionCallBack(int i, int currentPosCounter, int crouchingCounter, int kneelingCounter, int tiptoesCounter) {
        //Log.d(TAG, "received position in MainActivity call back is:"+i);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int crouchingPositionCounter=crouchingCounter/2;
                totalCrouchingTime=crouchingPositionCounter;
                //Log.d(TAG,"total crouching time:"+totalCrouchingTime);

                int kneelingPositionCounter=kneelingCounter/2;
                totalKneelingTime= kneelingPositionCounter;
                //Log.d(TAG,"total kneeling time:"+totalKneelingTime);

                int tiptoesPositionCounter=tiptoesCounter/2;
                totalTiptoesTime=tiptoesPositionCounter;
                //Log.d(TAG,"total tiptoes time:"+totalTiptoesTime);

            }
        });
    }

    @Override
    public void notifyLeftConnectionDisconnected() {
        runOnUiThread(() -> {
            imageViewLeftConnectionStatus.setImageDrawable(drawableConnectionStopped);//I am using the same as connecting, maybe this should be changed.
        });
            isLeftInsoleConnected=false;
    }

    @Override
    public void notifyRightConnectionDisconnected() {
        runOnUiThread(() -> {
            imageViewRightConnectionStatus.setImageDrawable(drawableConnectionStopped);
        });
        isRightInsoleConnected=false;
    }

    @Override
    public void notifyLeftConnectionIsConnecting() {
        runOnUiThread(() -> {
            imageViewLeftConnectionStatus.setImageDrawable(drawableLeftConnecting);
        });
        isLeftInsoleConnected=false;
    }

    @Override
    public void notifyRightConnectionIsConnecting() {
        runOnUiThread(() -> {
            imageViewRightConnectionStatus.setImageDrawable(drawableRightConnecting);
        });
        isRightInsoleConnected=false;
    }

    @Override
    public void notifyLeftConnectionConnected() {
        runOnUiThread(() -> {
            imageViewLeftConnectionStatus.setImageDrawable(drawableLeftConnected);
            });
        isLeftInsoleConnected=true;
        processEnablingStartSafetyActivityButton();
    }

    @Override
    public void notifyRightConnectionConnected() {
        runOnUiThread(() -> {
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

    private boolean areBothInsolesConnected(){
        return (isLeftInsoleConnected()&&isRightInsoleConnected());
    }

    private void resetTimer(){
        //if timer is running, then stop it
        if (timerSubscription!=null) {
            if (!timerSubscription.isUnsubscribed()) {
                timerSubscription.unsubscribe();
                runOnUiThread(() -> {textViewTimer.setText("0:0:0");});
            }
        }
        else {
            Log.d(TAG," timer is already unsubscibed,and it is null");
        }
    }

    //it should be called before sending the received data to new activity,
    // so once the user comes back, he can start receiving new indicationo data
    // for new activities
    private void resetIndicationFlags(){
       leftIndicationDataWellReceived=false;
       rightIndicationDataWellReceived=false;
    }

    private void stopActivityOnLeftInsole(){
        Log.d(TAG,"starting stopping activity on left insole, it will mean subscribe to indication, and write a stop command on the relevant characteristic.");
        byte[] stopCommandArray= {0x02};
        //send command to stop the activity
        if (isLeftInsoleConnected()){
            if (leftInsoleIndicationSubscription!=null){
                if (!leftInsoleIndicationSubscription.isUnsubscribed())
                    Log.d(TAG,"leftInsoleIndicationSubscription was alive, I will unsubscribe it");
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
                                    Log.d(TAG,"received indication data from first part of first chunk");
                                    mStatsCalculator.processFirstHalfLeft(bytes);

                                }
                                if ( ((bytes[0]&0xFF)==0) && ((bytes[1]&0xFF)==128) ) {
                                    Log.d(TAG,"received left indication data from second part of first chunk");

                                    leftIndicationDataWellReceived=true;//we consider that if we receive the first half of the first chunk, then we are sure we will receive the rest, so we consider that left data is well received.
                                    if (leftIndicationDataWellReceived&&rightIndicationDataWellReceived) {
                                        isActivityStarted = false;
                                        runOnUiThread(() -> {
                                            resetTimer();
                                            buttonStopNormalActivity.setEnabled(false);
                                            buttonStartNormalActivity.setEnabled(true);
                                        });
                                    }
                                    else {
                                        Log.d(TAG,"even though both insoles successfully received stop command, the indication data is not successfully received on both insoles yet");
                                    }

                                    mStatsCalculator.processSecondHalfLeft(bytes);
                                }
                            },
                            throwable -> {Log.d(TAG,"LeftInsoleIndicationObserver reader onError "+throwable.toString());});

            Log.d(TAG, "writing a stop command characteristic to left insole");

            leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),stopCommandArray))
                    .subscribe(bytes -> onStopActivityWriteSuccess(true),(e)->onWriteError(e));


        }
        else{
            Log.d(TAG,"could not stop activity on left insole, check your connection.");
        }
    }

    private void stopActivityOnRightInsole(){
        byte[] stopCommandArray= {0x02};
        if (isRightInsoleConnected()){
            if (rightInsoleIndicationSubscription!=null){
                if (!rightInsoleIndicationSubscription.isUnsubscribed()) {
                    Log.d(TAG,"rightInsoleIndicationSubscription was alive, I will unsubscribe it");
                    rightInsoleIndicationSubscription.unsubscribe();
                }
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
                                    Log.d(TAG,"received right indication data from second part of first chunk");
                                    rightIndicationDataWellReceived=true;

                                    if (leftIndicationDataWellReceived&&rightIndicationDataWellReceived) {
                                        isActivityStarted = false;
                                        runOnUiThread(() -> {
                                            buttonStopNormalActivity.setEnabled(false);
                                            buttonStartNormalActivity.setEnabled(true);
                                            resetTimer();
                                        });
                                    }
                                    else {
                                        Log.d(TAG,"even though both insoles successfully received stop command, the indication data is not successfully received on both insoles yet");
                                    }

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
            Log.d(TAG,"could not stop activity on right insole, check your connection.");
        }
    }

    private void stopSafetyActivity(){

        //check that both insoles are connected now
        if (!areBothInsolesConnected()) {
            Toast.makeText(this,"ensure both insoles are connected",Toast.LENGTH_LONG).show();
            return;
        }

        //check if the activity is not already started
        // to do..

        if (!leftStopActivityCommandSentSuccessfully) {
            stopActivityOnLeftInsole();
        }

        if (!rightStopActivityCommandSentSuccessfully) {
            stopActivityOnRightInsole();
        }

        if (leftStopActivityCommandSentSuccessfully&&rightStopActivityCommandSentSuccessfully){
            //actually, before resetting the timer, we should check if we received both indication data from both insoles
            resetTimer();
        }
    }

    private void initUI(){

        //setting the mac addresses received from previous step
        textViewLeftMac = (TextView) findViewById(R.id.textViewLeftMac);
        textViewLeftMac.setText(getIntent().getStringExtra(LauncherActivity.KEY_LEFT_INSOLE_MAC));

        textViewRightMac = (TextView) findViewById(R.id.textViewRightMac);
        textViewRightMac.setText(getIntent().getStringExtra(LauncherActivity.KEY_RIGHT_INSOLE_MAC));

        isActivityStarted=false;

        drawableLeftConnected = getDrawable(R.drawable.elten_connection_left_connected);
        drawableLeftConnecting = getDrawable(R.drawable.elten_connection_left_searching);

        drawableRightConnected = getDrawable(R.drawable.elten_connection_right_connected);
        drawableRightConnecting = getDrawable(R.drawable.elten_connection_right_searching);

        drawableTimer0 = getDrawable(R.drawable.ic_timer0);
        drawableTimer1 = getDrawable(R.drawable.ic_timer1);
        drawableTimer2 = getDrawable(R.drawable.ic_timer2);
        drawableTimer3 = getDrawable(R.drawable.ic_timer3);
        drawableTimer4 = getDrawable(R.drawable.ic_timer4);

        drawableConnectionStopped = getDrawable(R.drawable.elten_connection_stopped);

        imageViewLeftConnectionStatus = (ImageView) findViewById(R.id.imageViewLeftConnectionStatus);
        imageViewLeftConnectionStatus.setImageDrawable(drawableLeftConnecting);

        imageViewRightConnectionStatus = (ImageView) findViewById(R.id.imageViewRightConnectionStatus);
        imageViewRightConnectionStatus.setImageDrawable(drawableRightConnecting);


        textViewTimer = (TextView) findViewById(R.id.textViewTimerUvex);
        imageViewTimer = (ImageView) findViewById(R.id.imageViewTimer);

        imageViewTimer.setImageDrawable(drawableTimer0);

        buttonStartNormalActivity = (Button) findViewById(R.id.buttonStartActivityUvexNormal);
        buttonStartNormalActivity.setEnabled(false);

        RxView.clicks(buttonStartNormalActivity)
                .subscribe(a-> {
                    startSafetyActivity();
                });

        buttonStopNormalActivity = (Button) findViewById(R.id.buttonStopActivityUvexNormal);
        disableStopActivityButton();

        RxView.clicks(buttonStopNormalActivity)
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
                        buttonStopNormalActivity.setEnabled(true);
                    }
                    else {
                        buttonStopNormalActivity.setEnabled(false);
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
        // do nothing, it is here to meet interface implementation, but in normal mode this method will play no role, another method will paly the required role
    }

    private void processEnablingStartSafetyActivityButton(){
        Log.d(TAG,"processEnablingStartButton, left connection is:"+isLeftInsoleConnected+" right connection is:"+isRightInsoleConnected);
        if (isLeftInsoleConnected && isRightInsoleConnected && (!isActivityStarted)){
            runOnUiThread(() -> {
                buttonStartNormalActivity.setEnabled(true);
            });
        }
    }

    private void disableStopActivityButton(){
        buttonStopNormalActivity.setEnabled(false);
    }

    private void enableStopActivityButton(){
        buttonStartNormalActivity.setEnabled(true);
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
    public void updateStatsOnUIValues(int standingTime, int stairs, int steps, int walkingTime, int vibrationTime, int leftAngle, int rightAngle, int distanceMeters, int calories, int vibrationIntensity, int slip) {

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
        intent.putExtra(DataProcessing.SLIP, slip);

        //save the same data that got received to database.
        SessionDBHelper myDBHelper = new SessionDBHelper(getApplicationContext());

        SQLiteDatabase db = myDBHelper.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(System.currentTimeMillis());

        SessionData sd= new SessionData.Builder()
                .setNumSteps(steps)
                .setNumStairs(stairs)
                //.setNumStairs(0)//remove this line and uncomment the above line to get save the real number of stairs in the database.
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
                .setSlip(slip)
                .createSessionData();


        ContentValues values = new ContentValues();

        populateContentValuesWithSessionData(values,sd);

        long newRowId = db.insert(SessionContract.SessionTable.TABLE_NAME, null, values);
        Log.d(TAG,"after inserting the real activity row, here is its id:"+newRowId);

        db.close();
        myDBHelper.close();

        //reset current flags in this activity
        leftIndicationDataWellReceived=false;
        rightIndicationDataWellReceived=false;
        leftStopActivityCommandSentSuccessfully=false;
        rightStopActivityCommandSentSuccessfully=false;
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

        cv.put(SessionContract.SessionTable.COLUMN_NAME_SLIP,String.valueOf(sd.getSlip()));
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
