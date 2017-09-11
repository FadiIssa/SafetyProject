package com.example.fadi.testingrx;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fadi.testingrx.f.ble.Insoles;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.example.fadi.testingrx.f.posture.Postures;
import com.polidea.rxandroidble.NotificationSetupMode;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import com.jakewharton.rxbinding2.view.RxView;


import org.w3c.dom.Text;

import java.util.UUID;
import java.util.function.Predicate;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;



public class MainActivity extends AppCompatActivity {

    // observables
    Observable<RxBleConnection> leftInsoleConnectionObservable;//this observable will be used any time we want to interact with a characteristic, no need to establish new connection for every operation.
    Observable<RxBleConnection> rightInsoleConnectionObservable;

    // for reading the indication characteristic
    Observer<Observable<byte[]>> myLeftInsoleIndicationObserver;
    Observer<Observable<byte[]>> myRightInsoleIndicationObserver;

    PostureTracker mPostureTracker;

    ImageView currentPostureImageView;
    ImageView tiptoesImageView;
    ImageView kneelingImageView;
    ImageView crouchingImageView;

    TextView counterCurrentPostureTextView;
    TextView counterCrouchingTextView;
    TextView counterKneelingTextView;
    TextView counterTiptoesTextView;

    Button buttonStartActivity;// this is when I have to show that I am doing some advancement.

    Drawable drawableTipToesFull; // still silly by all measures.
    Drawable drawableCrouchingFull;
    Drawable drawableKneelingFull;
    Drawable drawableTipToesBorder;
    Drawable drawableCrouchingBorder;
    Drawable drawableKneelingBorder;
    Drawable drawableUnknown;


    String TAG="RxTesting";

    // this is to ensure font changes happen in this activity.
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        // prepare the object that will process posture detection
        mPostureTracker = new PostureTracker(this);

//        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";

        //scanAndPairing();
        MyApplication.getBleManager().scanAndPair(()->{
            MyApplication.getBleManager().connectRealTime(mPostureTracker);
        });

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

    private void stopSafetyActivity(){

        //check if the activity is not already started

        byte[] stopCommandArray= {0x02};
        //send command to stop the activity
        if (isLeftInsoleConnected()){
            leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),stopCommandArray))
                    .subscribe(bytes -> onWriteSuccess(),(e)->onWriteError(e));

            Log.d(TAG, "activity stopped, writing to command characteristic of left insole");

            //here I should start the indication thig.

            leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupIndication(UUID.fromString(Insoles.CHARACTERISTIC_CHUNK)))
                    .subscribe(myLeftInsoleIndicationObserver);

        }
        else{
            Log.d(TAG,"could not stop activity, no connection with left insole.");
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

    private void onWriteSuccess(){
        Log.d(TAG, "successful write operation");
        // from here I should start the indication subscription, to read the whole data after the activity finishes.
    }

    private void onStopActivityWriteSuccess(){
        Log.d(TAG, "successful write operation to stop activity");
        // from here I should start the indication subscription, to read the whole data after the activity finishes.
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
                } else if (i== Postures.UNKNOWN){
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

    private void initUI(){

        // prepare the drawables that will represent the different postures.
        drawableTipToesFull = getDrawable(R.drawable.tipoesfull);
        drawableTipToesBorder = getDrawable(R.drawable.tiptoesborder);
        drawableCrouchingFull = getDrawable(R.drawable.crouchingfull);
        drawableCrouchingBorder = getDrawable(R.drawable.crouchingborder);
        drawableKneelingFull = getDrawable(R.drawable.kneelingfull);
        drawableKneelingBorder = getDrawable(R.drawable.kneelingborder);
        drawableUnknown = getDrawable(R.drawable.unknownposition);

        buttonStartActivity = (Button) findViewById(R.id.buttonStartActivity);

        RxView.clicks(buttonStartActivity)
                .map(a->buttonStartActivity.getText().toString().equals("Start"))
                .subscribe(a-> {
                    if (a) {
                        buttonStartActivity.setText("Stop");
                        startSafetyActivity();
                    } else{
                        buttonStartActivity.setText("Start");
                        stopSafetyActivity();
                    }
                });

        currentPostureImageView= (ImageView) findViewById(R.id.currentPostureImageView);
        currentPostureImageView.setImageDrawable(getDrawable(R.drawable.unknownposition));

        kneelingImageView = (ImageView) findViewById(R.id.kneelingImageView);
        kneelingImageView.setImageDrawable(getDrawable(R.drawable.kneelingfull));

        crouchingImageView = (ImageView) findViewById(R.id.crouchingImageView);
        crouchingImageView.setImageDrawable(getDrawable(R.drawable.crouchingfull));

        counterCurrentPostureTextView = (TextView) findViewById(R.id.currentPostureCounterTextView);
        counterCrouchingTextView = (TextView) findViewById(R.id.crouchingCounterTextView);
        counterKneelingTextView = (TextView) findViewById(R.id.kneelingCounterTextView);
        counterTiptoesTextView = (TextView) findViewById(R.id.tiptoesCounterTextView);

        tiptoesImageView = (ImageView) findViewById(R.id.tiptoesImageView);
        tiptoesImageView.setImageDrawable(getDrawable(R.drawable.tipoesfull));

        //init the indication observer
        myLeftInsoleIndicationObserver = new Observer<Observable<byte[]>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"LeftInsoleIndicationObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"LeftInsoleIndicationObserver onError "+e.toString());
            }

            @Override
            public void onNext(Observable<byte[]> observable) {
                observable.subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG,"LeftInsoleIndicationObserver reader onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"LeftInsoleIndicationObserver reader onError "+e.toString());
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        Log.d(TAG,"LeftInsoleIndicationObserver reader onNext "+ bytes.toString());
                        for (int i=0;i<bytes.length;i++){
                            Log.d(TAG,"indicastion byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                        }
                    }
                });
            }
        };
    }
}