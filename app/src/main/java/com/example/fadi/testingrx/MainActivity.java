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


    // subscriptions
    Subscription scanSubscription;// this is for scthere will be several subscriptions, for each functionality
    Subscription connectionSubscription; // one purpose for these subscriptions is to handle their life cycle according to the activity life cycle.

    // observers
    Observer<ScanResult> myScanObserver;//scan observer, no need for 2 objects, one is enough to find all ble devices.

    // connection observers, one for each insole
    Observer<RxBleConnection> leftInsoleConnectionObserver;
    Observer<RxBleConnection> rightInsoleConnectionObserver;

    // observables
    Observable<RxBleConnection> leftInsoleConnectionObservable;//this observable will be used any time we want to interact with a characteristic, no need to establish new connection for every operation.
    Observable<RxBleConnection> rightInsoleConnectionObservable;

    Observer<RxBleDeviceServices> myServicesDiscoveryObserver;

    Observer<byte[]> myLeftBatteryReadObserver;
    Observer<byte[]> myRightBatteryReadObserver;

    Observer<Observable<byte[]>> myLeftAccelometerNotifyObserver;
    Observer<Observable<byte[]>> myRightAccelometerNotifyObserver;

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

    String serviceUUID;

    RxBleDevice leftInsoleDevice;
    RxBleDevice rightInsoleDevice;

    // to know when to stop scanning
    boolean isLeftInsoleDetectedNearby;
    boolean isRightInsoleDetectedNearby;


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

        isLeftInsoleDetectedNearby=false;
        isRightInsoleDetectedNearby=false;

        //scanAndPairing();
        MyApplication.getBleManager().scanAndPair(()->{
            MyApplication.getBleManager().connectRealTime(mPostureTracker);
        });

        //connect();// it is not called from here any more, instead, it is called from whithin the scan observer when he finds both left and right insoles nearby.
        //readBattery();
        //registerForSteps();
    }

    private void startSafetyActivity(){

            //check if the activity is not already started

            byte[] startCommandArray= {0x01};
            //send command to stop the activity
            if (leftInsoleDevice.getConnectionState()== RxBleConnection.RxBleConnectionState.CONNECTED){
                leftInsoleConnectionObservable
                        .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),startCommandArray))
                .subscribe(bytes -> onWriteSuccess(),(e)->onWriteError(e));

                Log.d("RxTesting", "activity started, writing to command characteristic of left insole");
            }
            else{
                Log.d("RXtesting","could not start activity, no connection with left insole.");
            }
    }

    private void stopSafetyActivity(){

        //check if the activity is not already started

        byte[] startCommandArray= {0x02};
        //send command to stop the activity
        if (leftInsoleDevice.getConnectionState()== RxBleConnection.RxBleConnectionState.CONNECTED){
            leftInsoleConnectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_COMMAND),startCommandArray))
                    .subscribe(bytes -> onWriteSuccess(),(e)->onWriteError(e));

            Log.d("RxTesting", "activity stopped, writing to command characteristic of left insole");
        }
        else{
            Log.d("RXtesting","could not stop activity, no connection with left insole.");
        }

    }

    private boolean isLeftInsoleConnected(){
        if (leftInsoleDevice!=null){
            return (leftInsoleDevice.getConnectionState()== RxBleConnection.RxBleConnectionState.CONNECTED);
        }
        else {
            Log.d("RXTesting","you should not see this, try to prevent this operation if the bleDevice is null");
            return false;
        }
    }

    private void onWriteSuccess(){
        Log.d("WriteCh", "successful write operation");
    }

    private void onWriteError(Throwable e){
        Log.d("WriteCh", "error while writing characteristic: "+e.toString());
    }

    //this method will be called when the observer receives a notification from the insole that it successfully started the activity.
    private void notifyRInsoleStartedActivity(){

    }

    private void notifyLInsoleStartedActivity(){

    }

    /*

    private void scanAndPairing(){
        myScanObserver=new Observer<ScanResult>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","scan onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","scan onError "+e.toString());
            }

            @Override
            public void onNext(ScanResult scanResult) {
                Log.d("RXTesting","scan onNext "+scanResult.toString());
                //deviceMacTextView.setText(scanResult.getBleDevice().getMacAddress());
                //deviceNameTextView.setText(scanResult.getBleDevice().getName());

                if (scanResult.getBleDevice().getName().equals("ZTSafetyR")){
                    isRightInsoleDetectedNearby=true;
                }

                if (scanResult.getBleDevice().getName().equals("ZTSafetyL")){
                    isLeftInsoleDetectedNearby=true;
                }

                if (isLeftInsoleDetectedNearby&&isRightInsoleDetectedNearby){
                    scanSubscription.unsubscribe();
                    connect();
                }
            }
        };

        scanSubscription = MyApplication.getRxBleClient().scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        // add custom filters if needed
                        .build()

        )
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(scanResult -> { String deviceName= scanResult.getBleDevice().getName();
                    if (deviceName==null)
                        return false;
                    else
                        return (deviceName.equals("ZTSafetyR") || deviceName.equals("ZTSafetyL"));
                })
                .subscribe(myScanObserver);
    }

    */



    private void connect(){

        myServicesDiscoveryObserver= new Observer<RxBleDeviceServices>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","ServicesDiscoveryObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","ServicesDiscoveryObserver onError "+e.toString());
            }


            @Override
            public void onNext(RxBleDeviceServices rxBleDeviceServices) {
                Log.d("RXTesting","ServicesDiscoveryObserver onNext "+rxBleDeviceServices.toString());

                /*
                for (BluetoothGattService b:rxBleDeviceServices.getBluetoothGattServices()){
                    Log.d("RXTesting", "service "+b.getUuid());

                }

                for (BluetoothGattCharacteristic c:rxBleDeviceServices.getBluetoothGattServices().get(2).getCharacteristics()){
                    Log.d("RXTesting", c.getUuid().toString());
                }*/



                //.getService(UUID.fromString(serviceUUID));

            }


            };

        myLeftBatteryReadObserver = new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","LeftBatteryReadObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","LeftBatteryReadObserver onError "+e.toString());

            }

            @Override
            public void onNext(byte[] bytes) {
                int leftBatteryValue=bytes[0]&0xFF;
                Log.d("RXTesting","LeftBatteryReadObserver onNext "+ leftBatteryValue);
                //updateLeftBattery(leftBatteryValue);

            }
        };

        myRightBatteryReadObserver = new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","RightBatteryReadObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","RightBatteryReadObserver onError "+e.toString());

            }

            @Override
            public void onNext(byte[] bytes) {
                int rightBatteryValue=bytes[0]&0xFF;
                Log.d("RXTesting","RightBatteryReadObserver onNext "+ rightBatteryValue);
                //updateRightBattery(rightBatteryValue);

            }
        };

        myLeftAccelometerNotifyObserver= new Observer<Observable<byte[]>>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","LeftAccelometerNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","LeftAccelometerNotifyObserver onError "+e.toString());

            }

            @Override
            public void onNext(Observable<byte[]> observable) {
                observable.subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        Log.d("RXTesting","LeftAccelometerNotifyObserver reader onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("RXTesting","LeftAccelometerNotifyObserver reader "+e.toString());

                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        Log.d("RXTesting","LeftAccelometerNotifyObserver reader onNext "+ bytes.toString());
                        for (int i=0;i<bytes.length;i++){
                            Log.d("RXTesting","byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                        }

                        //int totalSteps= bytes[0]&0xFF+((bytes[1]&0xFF)<<8);
                        //Log.d("RXTesting","totalSteps:"+totalSteps);

                        //int accX= bytes[4]&0xFF+((bytes[5]&0xFF)<<8);
                        int accX= (bytes[4]&0xFf)|((bytes[5])<<8);
                        Log.d("RXTesting","Left accX:"+accX);

                        //int accY= bytes[6]&0xFF+((bytes[7]&0xFF)<<8);
                        int accY= (bytes[6]&0xFF)|((bytes[7])<<8);
                        Log.d("RXTesting","Left accY:"+accY);

                        //int accZ= bytes[8]&0xFF+((bytes[9]&0xFF)<<8);
                        int accZ= (bytes[8]&0xFF)|((bytes[9])<<8);
                        Log.d("RXTesting","Left accZ:"+accZ);

                        //updateLeftAccelometer(accX,accY,accZ);
                        mPostureTracker.updateLeftAccelometer(accX,accY,accZ);
                    }
                });
            }
        };

        myRightAccelometerNotifyObserver= new Observer<Observable<byte[]>>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","RightAccelometerNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","RightAccelometerNotifyObserver onError "+e.toString());

            }

            @Override
            public void onNext(Observable<byte[]> observable) {
                observable.subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        Log.d("RXTesting","RightAccelometerNotifyObserver reader onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("RXTesting","RightAccelometerNotifyObserver reader "+e.toString());

                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        Log.d("RXTesting","RightAccelometerNotifyObserver reader onNext "+ bytes.toString());


                        /*for (int i=0;i<bytes.length;i++){
                            Log.d("RXTesting","byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                        }*/

                        //int accX= bytes[4]&0xFF+((bytes[5]&0xFF)<<8);
                        int accX= (bytes[4]&0xFF)|((bytes[5])<<8);
                        Log.d("RXTesting","Right accX:"+accX);

                        //int accY= bytes[6]&0xFF+((bytes[7]&0xFF)<<8);
                        int accY= (bytes[6]&0xFF)|((bytes[7])<<8);
                        Log.d("RXTesting","Right accY:"+accY);

                        //int accZ= bytes[8]&0xFF+((bytes[9]&0xFF)<<8);
                        int accZ= (bytes[8]&0xFF)|((bytes[9])<<8);
                        Log.d("RXTesting","Right accZ:"+accZ);

                        //updateRightAccelometer(accX,accY,accZ);
                        mPostureTracker.updateRightAccelometer(accX,accY,accZ);
                    }
                });
            }
        };


        leftInsoleConnectionObserver= new Observer<RxBleConnection>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","LeftconnectionObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","LeftconnectionObserver onError "+e.toString());

            }

            @Override
            public void onNext(RxBleConnection rxBleConnection) {
                Log.d("RXTesting","LeftconnectionObserver onNext "+rxBleConnection.toString());

                rxBleConnection.discoverServices().subscribe(myServicesDiscoveryObserver);

                rxBleConnection.readCharacteristic(UUID.fromString("99dd0016-a80c-4f94-be5d-c66b9fba40cf"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myLeftBatteryReadObserver);

                rxBleConnection.setupNotification(UUID.fromString("99dd0108-a80c-4f94-be5d-c66b9fba40cf"))
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myLeftAccelometerNotifyObserver);
            }
        };

        rightInsoleConnectionObserver= new Observer<RxBleConnection>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","RightconnectionObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","RightconnectionObserver onError "+e.toString());

            }

            @Override
            public void onNext(RxBleConnection rxBleConnection) {
                Log.d("RXTesting","RightconnectionObserver onNext "+rxBleConnection.toString());

                rxBleConnection.discoverServices().subscribe(myServicesDiscoveryObserver);

                rxBleConnection.readCharacteristic(UUID.fromString("99dd0016-a80c-4f94-be5d-c66b9fba40cf"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myRightBatteryReadObserver);

                rxBleConnection.setupNotification(UUID.fromString("99dd0108-a80c-4f94-be5d-c66b9fba40cf"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myRightAccelometerNotifyObserver);
            }
        };

        //leftInsoleDevice = rxBleClient.getBleDevice(leftInsoleMacAddress);
        leftInsoleDevice = MyApplication.getRxBleClient().getBleDevice(Insoles.LeftInsoleMacAddress);
        Log.d("RXTesting"," device name is:"+leftInsoleDevice.getName());

        rightInsoleDevice = MyApplication.getRxBleClient().getBleDevice(Insoles.RightInsoleMacAddress);
        Log.d("RXTesting"," device name is:"+rightInsoleDevice.getName());


        leftInsoleConnectionObservable=leftInsoleDevice.establishConnection(false);
        leftInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(leftInsoleConnectionObserver);

        rightInsoleDevice.establishConnection(false).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rightInsoleConnectionObserver);

    }

    //this will be called from the posture detection class, to let MainActivity updates the views it has to reflect the real postures.
    public void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter){

        Log.d("RXTesting", "received position in MainActivity call back is:"+i);

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
                .map(a->buttonStartActivity.getText().toString().equals("start"))
                .subscribe(a-> {
                    if (a) {
                        buttonStartActivity.setText("stop");
                        startSafetyActivity();
                    } else{
                        buttonStartActivity.setText("start");
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
    }
}