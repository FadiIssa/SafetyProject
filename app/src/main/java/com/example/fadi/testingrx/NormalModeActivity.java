package com.example.fadi.testingrx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.f.StatsCalculator;
import com.example.fadi.testingrx.f.ble.Insoles;
import com.example.fadi.testingrx.f.posture.CommunicationCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.example.fadi.testingrx.ui.StatsCalculaterCallback;
import com.jakewharton.rxbinding2.view.RxView;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class NormalModeActivity extends AppCompatActivity implements CommunicationCallback, StatsCalculaterCallback {

    String TAG="NormAct";

    private static final int MINIMUM_ACTIVITY_TIME=30;//this value is determined in the firmware, I just have to update it here to reflect it, usually it should be 5 minutes, in order not to save huge data fro the whole day.

    //UI items
    Button buttonStartActivity;
    Button buttonStopActivity;

    ImageView imageViewCrouching;
    ImageView imageViewKneeling;
    ImageView imageViewTiptoes;
    ImageView imageViewStanding;

    ImageView imageViewStairs;
    ImageView imageViewSteps;
    ImageView imageViewVibration;
    ImageView imageViewWalking;

    ImageView imageViewcalories;
    ImageView imageViewDistance;

    ImageView imageViewPronSup;

    TextView textViewCrouching;
    TextView textViewKneeling;
    TextView textViewTiptoes;
    TextView textViewStanding;

    TextView textViewSteps;
    TextView textViewStairs;
    TextView textViewVibration;
    TextView textViewWalking;

    TextView textViewLeftAngle;
    TextView textViewRightAngle;

    TextView textViewLeftConnectionStatus;
    TextView textViewRightConnectionStatus;

    TextView textViewDistance;
    TextView textViewCalories;

    TextView textViewTimer;

    boolean isLeftInsoleConnected;
    boolean isRightInsoleConnected;

    boolean leftStartActivityCommandSentSuccessfully;
    boolean rightStartActivityCommandSentSuccessfully;

    boolean leftStopActivityCommandSentSuccessfully;
    boolean rightStopActivityCommandSentSuccessfully;

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
        setContentView(R.layout.activity_normal_mode_old);

        mStatsCalculator = new StatsCalculator(this);

        isLeftInsoleConnected = false;
        isRightInsoleConnected = false;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.normalModeActivity_toolbar);
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
            String timeZero="0:0:0";
            textViewStanding.setText(timeZero);
            textViewStairs.setText(timeZero);
            textViewSteps.setText(timeZero);
            textViewWalking.setText(timeZero);
            textViewVibration.setText(timeZero);
            textViewLeftAngle.setText("0");
            textViewRightAngle.setText("0");
            textViewCrouching.setText(timeZero);
            textViewKneeling.setText(timeZero);
            textViewTiptoes.setText(timeZero);
        });
    }
    private void startSafetyActivity(){

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
                buttonStartActivity.setEnabled(false);
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
                buttonStopActivity.setEnabled(false);
                buttonStartActivity.setEnabled(true);
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
        runOnUiThread(() -> {textViewLeftConnectionStatus.setText("Disconnected");});
        isLeftInsoleConnected=false;
    }

    @Override
    public void notifyRightConnectionDisconnected() {
        runOnUiThread(() -> {textViewRightConnectionStatus.setText("Disconnected");});
        isRightInsoleConnected=false;
    }

    @Override
    public void notifyLeftConnectionIsConnecting() {
        runOnUiThread(() -> {textViewLeftConnectionStatus.setText("Connecting..");});
        isLeftInsoleConnected=false;
    }

    @Override
    public void notifyRightConnectionIsConnecting() {
        runOnUiThread(() -> {textViewRightConnectionStatus.setText("Connecting..");});
        isRightInsoleConnected=false;
    }

    @Override
    public void notifyLeftConnectionConnected() {
        runOnUiThread(() -> {textViewLeftConnectionStatus.setText("Connected");});
        isLeftInsoleConnected=true;
        processEnablingStartSafetyActivityButton();
    }

    @Override
    public void notifyRightConnectionConnected() {
        runOnUiThread(() -> {textViewRightConnectionStatus.setText("Connected");});
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

        textViewSteps = (TextView) findViewById(R.id.textViewSteps);
        textViewStairs = (TextView) findViewById(R.id.textViewStairs);
        textViewWalking = (TextView) findViewById(R.id.textViewWalking);
        textViewVibration = (TextView) findViewById(R.id.textViewVibration);

        textViewCrouching = (TextView) findViewById(R.id.textViewCrouching);
        textViewKneeling = (TextView) findViewById(R.id.textViewKneeling);
        textViewTiptoes = (TextView) findViewById(R.id.textViewTiptoes);
        textViewStanding = (TextView) findViewById(R.id.textViewStanding);

        textViewDistance = (TextView) findViewById(R.id.textViewDistance);
        textViewCalories = (TextView) findViewById(R.id.textViewCalories);

        textViewLeftAngle = (TextView) findViewById(R.id.textViewAngleLeft);
        textViewRightAngle = (TextView) findViewById(R.id.textViewAngleRight);

        imageViewCrouching = (ImageView) findViewById(R.id.imageViewCrouching);
        imageViewCrouching.setImageDrawable(getDrawable(R.drawable.crouchingfull));

        imageViewKneeling = (ImageView) findViewById(R.id.imageViewKneeling);
        imageViewKneeling.setImageDrawable(getDrawable(R.drawable.kneelingfull));

        imageViewTiptoes = (ImageView) findViewById(R.id.imageViewTiptoes);
        imageViewTiptoes.setImageDrawable(getDrawable(R.drawable.tipoesfull));

        imageViewStanding = (ImageView) findViewById(R.id.imageViewStanding);
        imageViewStanding.setImageDrawable(getDrawable(R.drawable.stand1));

        imageViewStairs = (ImageView) findViewById(R.id.imageViewStairs);
        imageViewStairs.setImageDrawable(getDrawable(R.drawable.stairs1));

        imageViewSteps = (ImageView) findViewById(R.id.imageViewSteps);
        imageViewSteps.setImageDrawable(getDrawable(R.drawable.steps));

        imageViewcalories = (ImageView) findViewById(R.id.imageViewCalories);
        imageViewcalories.setImageDrawable(getDrawable(R.drawable.ic_calories));

        imageViewDistance = (ImageView) findViewById(R.id.imageViewDistance);
        imageViewDistance.setImageDrawable(getDrawable(R.drawable.ic_distance));

        imageViewPronSup = (ImageView) findViewById(R.id.imageViewPronSup);
        if (MyApplication.EltenMode) {
            imageViewPronSup.setImageDrawable(getDrawable(R.drawable.elten_angles));
        } else {
            imageViewPronSup.setImageDrawable(getDrawable(R.drawable.uvex_angles));
        }

        imageViewWalking = (ImageView) findViewById(R.id.imageViewWalkingTime);
        imageViewWalking.setImageDrawable(getDrawable(R.drawable.walk1));


        imageViewVibration = (ImageView) findViewById(R.id.imageViewVibrationTime);
        imageViewVibration.setImageDrawable(getDrawable(R.drawable.vibra1));

        //imageViewLogo = (ImageView) findViewById(R.id.imageViewLogo);
        if (MyApplication.EltenMode) {
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.elten_logo_red));
        } else {
            //imageViewLogo.setImageDrawable(getDrawable(R.drawable.unknownposition));
        }


        textViewLeftConnectionStatus = (TextView) findViewById(R.id.textViewLeftConnectionStatus);
        textViewLeftConnectionStatus.setText("Communicating");
        textViewRightConnectionStatus = (TextView) findViewById(R.id.textViewRightConnectionStatus);
        textViewRightConnectionStatus.setText("Communicating");

        textViewTimer = (TextView) findViewById(R.id.textViewTimer);

        buttonStartActivity = (Button) findViewById(R.id.buttonStartActivityNormal);
        buttonStartActivity.setEnabled(false);

        RxView.clicks(buttonStartActivity)
               // .map(a->buttonStartActivity.getText().toString().equals("Start"))
                .subscribe(a-> {
                        startSafetyActivity();
                    //startTimer();// the timer should only be started if the commands were sent successfuly.
                });

        buttonStopActivity = (Button) findViewById(R.id.buttonStopActivityNormal);
        disableStopActivityButton();

        RxView.clicks(buttonStopActivity)
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
                    if (value>MINIMUM_ACTIVITY_TIME){
                        buttonStopActivity.setEnabled(true);
                    }
                    else {
                        buttonStopActivity.setEnabled(false);
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
            textViewStanding.setText(standigTime);
            textViewStairs.setText(nStairs);
            //textViewStairs.setText("0");//this is temporar, I should revert back to the previous line once the firmware detects stairs correctly.
            textViewSteps.setText(nSteps);
            textViewWalking.setText(walkingTime);
            textViewVibration.setText(vibrationTime);
            textViewLeftAngle.setText(leftAngle);
            textViewRightAngle.setText(rightAngle);
            textViewDistance.setText(distance);
            textViewCalories.setText(calories);


            //now set the posture times
            Log.d(TAG,"total crouching time:"+totalCrouchingTime);
            //totalCrouchingTime=0;//remove this line after the demo, this is only for stability reasons, since the connectivity is not finalized yet.
            int crouchingPostureCounterHours=totalCrouchingTime/3600;
            int crouchingPostureCounterMinutes=(totalCrouchingTime%3600)/60;
            int crouchingPostureCounterSeconds=(totalCrouchingTime%3600)%60;
            textViewCrouching.setText(String.valueOf(crouchingPostureCounterHours)+":"+String.valueOf(crouchingPostureCounterMinutes)+":"+String.valueOf(crouchingPostureCounterSeconds));

            //totalKneelingTime=0;//remove this line urgently after the demo.
            int kneelingPostureCounterHours=totalKneelingTime/3600;
            int kneelingPostureCounterMinutes=(totalKneelingTime%3600)/60;
            int kneelingPostureCounterSeconds=(totalKneelingTime%3600)%60;
            textViewKneeling.setText(String.valueOf(kneelingPostureCounterHours)+":"+String.valueOf(kneelingPostureCounterMinutes)+":"+String.valueOf(kneelingPostureCounterSeconds));

            //totalTiptoesTime=0;//remove this line urgently after the demo.
            int tiptoesPostureCounterHours=totalTiptoesTime/3600;
            int tiptoesPostureCounterMinutes=(totalTiptoesTime%3600)/60;
            int tiptoesPostureCounterSeconds=(totalTiptoesTime%3600)%60;
            textViewTiptoes.setText(String.valueOf(tiptoesPostureCounterHours)+":"+String.valueOf(tiptoesPostureCounterMinutes)+":"+String.valueOf(tiptoesPostureCounterSeconds));

        });
    }

    private void processEnablingStartSafetyActivityButton(){
        Log.d(TAG,"processEnablingStartButton, left connection is:"+isLeftInsoleConnected+" right connection is:"+isRightInsoleConnected);
        if (isLeftInsoleConnected && isRightInsoleConnected && (!isActivityStarted)){
            runOnUiThread(() -> {
                buttonStartActivity.setEnabled(true);
            });
        }
    }

    private void disableStopActivityButton(){
        buttonStopActivity.setEnabled(false);
    }

    private void enableStopActivityButton(){
        buttonStartActivity.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_launcher, menu);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }

    }

    //here is it nor useful, but in the uvex normal activity , it will be used instead of the string one, to create intent and pass the data to the stats activity browser.
    @Override
    public void updateStatsOnUIValues(int standingTime, int stairs, int steps, int walkingTime, int vibrationTime, int leftAngle, int rightAngle, int distanceMeters, int calories, int vibIntensity, int slip) {


    }
}
