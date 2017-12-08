package com.example.fadi.testingrx;


import android.content.Context;
import android.content.Intent;
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

import com.example.fadi.testingrx.f.ble.Insoles;
import com.example.fadi.testingrx.f.posture.CommunicationCallback;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.example.fadi.testingrx.f.posture.Postures;
import com.example.fadi.testingrx.ui.ManDownActivity;
import com.example.fadi.testingrx.ui.SavedActivitiesBrowserActivity;
import com.polidea.rxandroidble.RxBleConnection;

import com.jakewharton.rxbinding2.view.RxView;


import java.util.UUID;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/*
this activity will show real time postures, based on real time notifications received from both insoles.
it should pause the posture tracking in case the connection with one of both insoles is lost.
* */

public class MainActivity extends AppCompatActivity implements CommunicationCallback {

    // observables
    Observable<RxBleConnection> leftInsoleConnectionObservable;//this observable will be used any time we want to interact with a characteristic, no need to establish new connection for every operation.
    Observable<RxBleConnection> rightInsoleConnectionObservable;

    PostureTracker mPostureTracker;

    ImageView currentPostureImageView;
    ImageView tiptoesImageView;
    ImageView kneelingImageView;
    ImageView crouchingImageView;


    boolean manDownStarted;

    TextView counterCurrentPostureTextView;
    TextView counterCrouchingTextView;
    TextView counterKneelingTextView;
    TextView counterTiptoesTextView;

    TextView textViewLeftConnectionStatus;
    TextView textViewRightConnectionStatus;

    TextView textViewLeftBatteryValue;
    TextView textViewRightBatteryValue;

    //Button buttonStartActivity;// this is when I have to show that I am doing some advancement.

    Drawable drawableTipToesFull; // still silly by all measures.
    Drawable drawableCrouchingFull;
    Drawable drawableKneelingFull;
    Drawable drawableTipToesBorder;
    Drawable drawableCrouchingBorder;
    Drawable drawableKneelingBorder;
    Drawable drawableFallDown;
    Drawable drawableUnknown;

    //Subscription leftInsoleIndicationSubscription;
    String TAG="mainAct";

    // this is to ensure font changes happen in this activity.
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        manDownStarted=false;
        Toolbar myToolbar = (Toolbar) findViewById(R.id.mainActivity_toolbar);
        myToolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        //myToolbar.setLogo(getDrawable(R.drawable.uvex_logo_launcher_bar));
        myToolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));

        Log.d(TAG,"MainActivity onCreate is called");

        initUI();

        // prepare the object that will process posture detection
        mPostureTracker = new PostureTracker(this);

//        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";

        MyApplication.getBleManager().connectRealTime(mPostureTracker);

        //connect();// it is not called from here any more, instead, it is called from whithin the scan observer when he finds both left and right insoles nearby.
        //readBattery();
        //registerForSteps();
    }

    private void startSafetyActivity(){

        if (leftInsoleConnectionObservable==null){
            leftInsoleConnectionObservable=MyApplication.getBleManager().getLeftInsoleConnectionObservable();
        }

        if (rightInsoleConnectionObservable==null){
            leftInsoleConnectionObservable=MyApplication.getBleManager().getLeftInsoleConnectionObservable();
        }

            //check if the activity is not already started

            byte[] startCommandArray= {0x01};
            //send command to stop the activity
            if (isLeftInsoleConnected()){
                leftInsoleConnectionObservable
                        .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),startCommandArray))
                .subscribe(bytes -> onWriteSuccess(),(e)->onWriteError(e));

                Log.d(TAG, "activity started, writing to command characteristic of left insole");
            }
            else{
                Log.d(TAG,"could not start activity, no connection with left insole.");
            }
    }

    /*private void stopSafetyActivity(){

        //check if the activity is not already started

        byte[] stopCommandArray= {0x02};
        //send command to stop the activity
        if (isLeftInsoleConnected()){
            if (leftInsoleIndicationSubscription!=null){
                if (!leftInsoleIndicationSubscription.isUnsubscribed())
                leftInsoleIndicationSubscription.unsubscribe();
            }
            // from here I should start the indication subscription, to read the whole data after the activity finishes.
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
                                    Log.d(TAG,"indication byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                                }
                            },
                            throwable -> {Log.d(TAG,"LeftInsoleIndicationObserver reader onError "+throwable.toString());});

            leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),stopCommandArray))
                    .subscribe(bytes -> onStopActivityWriteSuccess(),(e)->onWriteError(e));

            Log.d(TAG, "activity stopped, writing to command characteristic of left insole");
        }
        else{
            Log.d(TAG,"could not stop activity, no connection with left insole.");
        }
    }*/

    private boolean isLeftInsoleConnected(){
        if (MyApplication.getBleManager().getLeftInsoleDevice()!=null){
            return (MyApplication.getBleManager().getLeftInsoleDevice().getConnectionState()== RxBleConnection.RxBleConnectionState.CONNECTED);
        }
        else {
            Log.d(TAG,"you should not see this, try to prevent this operation if the bleDevice is null");
            return false;
        }
    }

    private void onWriteSuccess(){
        Log.d(TAG, "successful write operation");

    }

    private void onStopActivityWriteSuccess(){
        Log.d(TAG, "successful write operation to stop activity");
    }

    private void onWriteError(Throwable e){
        Log.d(TAG, "error while writing characteristic: "+e.toString());
    }

    //this method will be called when the observer receives a notification from the insole that it successfully started the activity.
    private void notifyRInsoleStartedActivity(){

    }

    private void notifyLInsoleStartedActivity(){

    }

    //this will be called from the posture detection class, to let MainActivity updates the views it has to reflect the real postures.
    @Override
    public void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter){

        Log.d(TAG, "received position in MainActivity call back is:"+i);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPositionCounter=currentPosCounter/2;
                if (i== Postures.TIPTOES){
                    currentPostureImageView.setImageDrawable(currentPositionCounter>=4? drawableTipToesFull:drawableTipToesBorder);
                } else if (i== Postures.CROUCHING){
                    currentPostureImageView.setImageDrawable(currentPositionCounter>=4? drawableCrouchingFull:drawableCrouchingBorder);
                } else if (i== Postures.KNEELING){
                    currentPostureImageView.setImageDrawable(currentPositionCounter>=4? drawableKneelingFull:drawableKneelingBorder);
                } else if (i==Postures.FALLDOWN){
                    currentPostureImageView.setImageDrawable(drawableFallDown);
                    if (!manDownStarted) {
                        Intent intent = new Intent(getApplicationContext(), ManDownActivity.class);
                        startActivity(intent);
                        manDownStarted=true;
                    }
                }
                else if (i== Postures.UNKNOWN){
                    currentPostureImageView.setImageDrawable(drawableUnknown);
                }

                //setting the counter of current posture
                int currentPositionCounterHours= currentPositionCounter/3600;
                int currentPositionCounterMinutes= (currentPositionCounter%3600)/60;
                int currentPositionCounterSeconds= (currentPositionCounter%3600)%60;

                counterCurrentPostureTextView.setText(String.valueOf(currentPositionCounterHours)+":"+String.valueOf(currentPositionCounterMinutes)+":"+String.valueOf(currentPositionCounterSeconds));

                int crouchingPositionCounter=crouchingCounter/2;
                int crouchingPostureCounterHours=crouchingPositionCounter/3600;
                int crouchingPostureCounterMinutes=(crouchingPositionCounter%3600)/60;
                int crouchingPostureCounterSeconds=(crouchingPositionCounter%3600)%60;
                counterCrouchingTextView.setText(String.valueOf(crouchingPostureCounterHours)+":"+String.valueOf(crouchingPostureCounterMinutes)+":"+String.valueOf(crouchingPostureCounterSeconds));

                int kneelingPositionCounter=kneelingCounter/2;
                int kneelingPostureCounterHours=kneelingPositionCounter/3600;
                int kneelingPostureCounterMinutes=(kneelingPositionCounter%3600)/60;
                int kneelingPostureCounterSeconds=(kneelingPositionCounter%3600)%60;
                counterKneelingTextView.setText(String.valueOf(kneelingPostureCounterHours)+":"+String.valueOf(kneelingPostureCounterMinutes)+":"+String.valueOf(kneelingPostureCounterSeconds));

                int tiptoesPositionCounter=tiptoesCounter/2;
                int tiptoesPostureCounterHours=tiptoesPositionCounter/3600;
                int tiptoesPostureCounterMinutes=(tiptoesPositionCounter%3600)/60;
                int tiptoesPostureCounterSeconds=(tiptoesPositionCounter%3600)%60;
                counterTiptoesTextView.setText(String.valueOf(tiptoesPostureCounterHours)+":"+String.valueOf(tiptoesPostureCounterMinutes)+":"+String.valueOf(tiptoesPostureCounterSeconds));
            }
        });
    }

    @Override
    public void notifyLeftConnectionDisconnected() {
        runOnUiThread(() -> {textViewLeftConnectionStatus.setText("Disconnected");});
    }

    @Override
    public void notifyRightConnectionDisconnected() {
        runOnUiThread(() -> {textViewRightConnectionStatus.setText("Disconnected");});
    }

    @Override
    public void notifyLeftConnectionIsConnecting() {
        runOnUiThread(() -> {textViewLeftConnectionStatus.setText("Connecting..");
                            textViewLeftBatteryValue.setText("?");});
    }

    @Override
    public void notifyRightConnectionIsConnecting() {
        runOnUiThread(() -> {textViewRightConnectionStatus.setText("Connecting..");
                                textViewRightBatteryValue.setText("?");});
    }

    @Override
    public void notifyLeftConnectionConnected() {
        runOnUiThread(() -> {textViewLeftConnectionStatus.setText("Connected");});
    }

    @Override
    public void notifyRightConnectionConnected() {
        runOnUiThread(() -> {textViewRightConnectionStatus.setText("Connected");});
    }

    @Override
    public void notifyLeftServiceDiscoveryCompleted() {

    }

    @Override
    public void notifyRightServiceDiscoveryCompleted() {

    }

    @Override
    public void notifyLeftBattery(int value) {
        runOnUiThread(() -> {textViewLeftBatteryValue.setText(String.valueOf(value)+"%");});
    }

    @Override
    public void notifyRightBattery(int value) {
        runOnUiThread(() -> {textViewRightBatteryValue.setText(String.valueOf(value)+"%");});
    }


    private void initUI() {

        // prepare the drawables that will represent the different postures.

        if (MyApplication.EltenMode) {
            drawableTipToesFull = getDrawable(R.drawable.elten_tiptoes_black_filled);
            drawableTipToesBorder = getDrawable(R.drawable.elten_tiptoes_black_border);
            drawableCrouchingFull = getDrawable(R.drawable.elten_crouching_black_filled);
            drawableCrouchingBorder = getDrawable(R.drawable.elten_crouching_black_border);
            drawableKneelingFull = getDrawable(R.drawable.elten_kneeling_black_filled);
            drawableKneelingBorder = getDrawable(R.drawable.elten_kneeling_black_border);
            drawableFallDown = getDrawable(R.drawable.ic_fall_border);
        } else {
            drawableTipToesFull = getDrawable(R.drawable.tipoesfull);
            drawableTipToesBorder = getDrawable(R.drawable.tiptoesborder);
            drawableCrouchingFull = getDrawable(R.drawable.crouchingfull);
            drawableCrouchingBorder = getDrawable(R.drawable.crouchingborder);
            drawableKneelingFull = getDrawable(R.drawable.kneelingfull);
            drawableKneelingBorder = getDrawable(R.drawable.kneelingborder);
            drawableFallDown = getDrawable(R.drawable.ic_fall_border);
        }

        if (MyApplication.EltenMode) {
            drawableUnknown = getDrawable(R.drawable.zh_logo_big_black);
        }else {
            drawableUnknown = getDrawable(R.drawable.unknownposition);
        }

        textViewLeftConnectionStatus = (TextView) findViewById(R.id.textViewLeftConnectionStatus);
        textViewLeftConnectionStatus.setText("Communicating");
        textViewRightConnectionStatus = (TextView) findViewById(R.id.textViewRightConnectionStatus);
        textViewRightConnectionStatus.setText("Communicating");

        textViewLeftBatteryValue = (TextView) findViewById(R.id.textViewLeftBatteryValue);
        textViewRightBatteryValue = (TextView) findViewById(R.id.textViewRightBatteryValue);

        currentPostureImageView= (ImageView) findViewById(R.id.currentPostureImageView);
        if (MyApplication.EltenMode) {
            currentPostureImageView.setImageDrawable(getDrawable(R.drawable.zh_logo_big_black));
        } else {
            currentPostureImageView.setImageDrawable(getDrawable(R.drawable.unknownposition));
        }

        kneelingImageView = (ImageView) findViewById(R.id.kneelingImageView);
        crouchingImageView = (ImageView) findViewById(R.id.crouchingImageView);
        tiptoesImageView = (ImageView) findViewById(R.id.tiptoesImageView);


        // checking if elten mode, to use the relevant icons.
        if (MyApplication.EltenMode){
            //kneelingImageView.setImageDrawable(getDrawable(R.drawable.elten_kneeling));
            kneelingImageView.setImageDrawable(getDrawable(R.drawable.elten_kneeling_red_circle));
            //crouchingImageView.setImageDrawable(getDrawable(R.drawable.elten_crouching));
            crouchingImageView.setImageDrawable(getDrawable(R.drawable.elten_crouching_red_circle));
            tiptoesImageView.setImageDrawable(getDrawable(R.drawable.elten_tiptoes_red_circle));
        } else {
            kneelingImageView.setImageDrawable(getDrawable(R.drawable.kneelingfull));
            crouchingImageView.setImageDrawable(getDrawable(R.drawable.crouchingfull));
            tiptoesImageView.setImageDrawable(getDrawable(R.drawable.tipoesfull));
        }

        counterCurrentPostureTextView = (TextView) findViewById(R.id.currentPostureCounterTextView);
        counterCrouchingTextView = (TextView) findViewById(R.id.crouchingCounterTextView);
        counterKneelingTextView = (TextView) findViewById(R.id.kneelingCounterTextView);
        counterTiptoesTextView = (TextView) findViewById(R.id.tiptoesCounterTextView);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_live__history:
                Intent intent = new Intent(this, SavedActivitiesBrowserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop is called for the MainActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        manDownStarted=false;
    }
}