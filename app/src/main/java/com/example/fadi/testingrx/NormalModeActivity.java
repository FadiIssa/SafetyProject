package com.example.fadi.testingrx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.f.StatsCalculator;
import com.example.fadi.testingrx.f.ble.Insoles;
import com.example.fadi.testingrx.f.posture.PostureResultCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.jakewharton.rxbinding2.view.RxView;
import com.polidea.rxandroidble.RxBleConnection;

import org.w3c.dom.Text;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class NormalModeActivity extends AppCompatActivity implements PostureResultCallback{

    String TAG="NormAct";

    //UI items
    Button buttonStartActivity;
    Button buttonStopActivity;

    boolean isLeftInsoleConnected;
    boolean isRightInsoleConnected;

    boolean leftStartActivityCommandSentSuccessfully;
    boolean rightStartActivityCommandSentSuccessfully;

    boolean leftStopActivityCommandSentSuccessfully;
    boolean rightStopActivityCommandSentSuccessfully;

    ImageView imageViewCrouching;
    ImageView imageViewKneeling;
    ImageView imageViewTiptoes;
    ImageView imageViewStanding;

    ImageView imageViewStairs;
    ImageView imageViewSteps;
    ImageView imageViewVibration;
    ImageView imageViewWalking;

    ImageView imageViewLogo;

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

    TextView textViewTimer;

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
        setContentView(R.layout.activity_normal_mode);

        mStatsCalculator = new StatsCalculator(this);

        isLeftInsoleConnected = false;
        isRightInsoleConnected = false;

        initUI();

        // prepare the object that will process posture detection
        mPostureTracker = new PostureTracker(this);
//        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";

        MyApplication.getBleManager().connectRealTime(mPostureTracker);
    }


    private void startSafetyActivity(){

        //reset posture counters
        mPostureTracker.reset();

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

    private void stopSafetyActivity(){

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

        textViewSteps = (TextView) findViewById(R.id.textViewSteps);
        textViewStairs = (TextView) findViewById(R.id.textViewStairs);
        textViewWalking = (TextView) findViewById(R.id.textViewWalking);
        textViewVibration = (TextView) findViewById(R.id.textViewVibration);

        textViewCrouching = (TextView) findViewById(R.id.textViewCrouching);
        textViewKneeling = (TextView) findViewById(R.id.textViewKneeling);
        textViewTiptoes = (TextView) findViewById(R.id.textViewTiptoes);
        textViewStanding = (TextView) findViewById(R.id.textViewStanding);

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


        imageViewPronSup = (ImageView) findViewById(R.id.imageViewPronSup);
        imageViewPronSup.setImageDrawable(getDrawable(R.drawable.elten_angles));

        imageViewWalking = (ImageView) findViewById(R.id.imageViewWalkingTime);
        imageViewWalking.setImageDrawable(getDrawable(R.drawable.walk1));


        imageViewVibration = (ImageView) findViewById(R.id.imageViewVibrationTime);
        imageViewVibration.setImageDrawable(getDrawable(R.drawable.vibra1));

        imageViewLogo = (ImageView) findViewById(R.id.imageViewLogo);
        imageViewLogo.setImageDrawable(getDrawable(R.drawable.unknownposition));

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
                    startTimer();
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
                    if (value>60){
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
        timerSubscription=timerObservable
                .subscribe(timerObserver);// creating the observer locally, to listen for the timer ticking, and updating the screen accordingly.
    }

    public void updateStatsOnUI(final String standigTime, final String nStairs, final String nSteps, final String walkingTime, final String vibrationTime, final String leftAngle, final String rightAngle ){
        runOnUiThread(() -> {
            textViewStanding.setText(standigTime);
            textViewStairs.setText(nStairs);
            textViewSteps.setText(nSteps);
            textViewWalking.setText(walkingTime);
            textViewVibration.setText(vibrationTime);
            textViewLeftAngle.setText(leftAngle);
            textViewRightAngle.setText(rightAngle);

            //now set the posture times
            Log.d(TAG,"total crouching time:"+totalCrouchingTime);
            int crouchingPostureCounterHours=totalCrouchingTime/3600;
            int crouchingPostureCounterMinutes=(totalCrouchingTime%3600)/60;
            int crouchingPostureCounterSeconds=(totalCrouchingTime%3600)%60;
            textViewCrouching.setText(String.valueOf(crouchingPostureCounterHours)+":"+String.valueOf(crouchingPostureCounterMinutes)+":"+String.valueOf(crouchingPostureCounterSeconds));

            int kneelingPostureCounterHours=totalKneelingTime/3600;
            int kneelingPostureCounterMinutes=(totalKneelingTime%3600)/60;
            int kneelingPostureCounterSeconds=(totalKneelingTime%3600)%60;
            textViewKneeling.setText(String.valueOf(kneelingPostureCounterHours)+":"+String.valueOf(kneelingPostureCounterMinutes)+":"+String.valueOf(kneelingPostureCounterSeconds));

            int tiptoesPostureCounterHours=totalTiptoesTime/3600;
            int tiptoesPostureCounterMinutes=(totalTiptoesTime%3600)/60;
            int tiptoesPostureCounterSeconds=(totalTiptoesTime%3600)%60;
            textViewTiptoes.setText(String.valueOf(tiptoesPostureCounterHours)+":"+String.valueOf(tiptoesPostureCounterMinutes)+":"+String.valueOf(tiptoesPostureCounterSeconds));

        });
    }

    private void processEnablingStartSafetyActivityButton(){
        Log.d(TAG,"processEnablingStartButton, left connection is:"+isLeftInsoleConnected+" right connection is:"+isRightInsoleConnected);
        if (isLeftInsoleConnected&&isRightInsoleConnected){
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
}
