package com.example.fadi.testingrx;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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


import org.w3c.dom.Text;

import java.util.UUID;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    RxBleClient rxBleClient;// one instance in the whole app lifecycle
    Subscription scanSubscription;// this is for scthere will be several subscriptions, for each functionality
    Subscription connectionSubscription;
    Observer<ScanResult> myScanObserver;

    Observer<RxBleConnection> myLeftConnectionObserver;
    Observer<RxBleConnection> myRightConnectionObserver;


    Observer<RxBleDeviceServices> myServicesDiscoveryObserver;

    Observer<byte[]> myLeftBatteryReadObserver;
    Observer<byte[]> myRightBatteryReadObserver;

    Observer<Observable<byte[]>> myLeftAccelometerNotifyObserver;
    Observer<Observable<byte[]>> myRightAccelometerNotifyObserver;

    int latestLX;// latest left accelerometer X
    int latestLY;
    int latestLZ;
    int latestRX;
    int latestRY;
    int latestRZ;

    PostureTracker mPostureTracker;

    ImageView currentPostureImageView;
    ImageView tiptoesImageView;
    ImageView kneelingImageView;
    ImageView crouchingImageView;

    TextView counterCurrentPostureTextView;
    TextView counterCrouchingTextView;
    TextView counterKneelingTextView;
    TextView counterTiptoesTextView;


    Drawable drawableTipToesFull;
    Drawable drawableCrouchingFull;
    Drawable drawableKneelingFull;
    Drawable drawableTipToesBorder;
    Drawable drawableCrouchingBorder;
    Drawable drawableKneelingBorder;
    Drawable drawableUnknown;

    String leftInsoleMacAddress;
    String serviceUUID;
    RxBleDevice leftInsoleDevice;
    RxBleDevice rightInsoleDevice;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPostureTracker = new PostureTracker(this);

        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";

        drawableTipToesFull = getDrawable(R.drawable.tipoesfull);
        drawableTipToesBorder = getDrawable(R.drawable.tiptoesborder);
        drawableCrouchingFull = getDrawable(R.drawable.crouchingfull);
        drawableCrouchingBorder = getDrawable(R.drawable.crouchingborder);
        drawableKneelingFull = getDrawable(R.drawable.kneelingfull);
        drawableKneelingBorder = getDrawable(R.drawable.kneelingborder);
        drawableUnknown = getDrawable(R.drawable.unknownposition);

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

        rxBleClient = RxBleClient.create(getApplicationContext());
        //scanAndPairing();
        connect();
        //readBattery();
        //registerForSteps();


    }

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
                //scanSubscription.unsubscribe();

            }
        };

        scanSubscription = rxBleClient.scanBleDevices(
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
                .subscribe(myScanObserver);
    }

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


        myLeftConnectionObserver= new Observer<RxBleConnection>() {
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

        myRightConnectionObserver= new Observer<RxBleConnection>() {
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
        leftInsoleDevice = rxBleClient.getBleDevice(Insoles.LeftInsoleMacAddress);
        Log.d("RXTesting"," device name is:"+leftInsoleDevice.getName());

        rightInsoleDevice = rxBleClient.getBleDevice(Insoles.RightInsoleMacAddress);
        Log.d("RXTesting"," device name is:"+rightInsoleDevice.getName());


        leftInsoleDevice.establishConnection(false).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myLeftConnectionObserver);

        rightInsoleDevice.establishConnection(false).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myRightConnectionObserver);

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
                int currentPositionCounterMinutes= currentPositionCounter/60;
                int currentPositionCounterSeconds= currentPositionCounter%60;
                counterCurrentPostureTextView.setText(String.valueOf(currentPositionCounterMinutes)+":"+String.valueOf(currentPositionCounterSeconds));

                int crouchingPositionCounter=crouchingCounter/2;
                int crouchingPostureCounterMinutes=crouchingPositionCounter/60;
                int crouchingPostureCounterSeconds=crouchingPositionCounter%60;
                counterCrouchingTextView.setText(String.valueOf(crouchingPostureCounterMinutes)+":"+String.valueOf(crouchingPostureCounterSeconds));

                int kneelingPositionCounter=kneelingCounter/2;
                int kneelingPostureCounterMinutes=kneelingPositionCounter/60;
                int kneelingPostureCounterSeconds=kneelingPositionCounter%60;
                counterKneelingTextView.setText(String.valueOf(kneelingPostureCounterMinutes)+":"+String.valueOf(kneelingPostureCounterSeconds));

                int tiptoesPositionCounter=tiptoesCounter/2;
                int tiptoesPostureCounterMinutes=tiptoesPositionCounter/60;
                int tiptoesPostureCounterSeconds=tiptoesPositionCounter%60;
                counterTiptoesTextView.setText(String.valueOf(tiptoesPostureCounterMinutes)+":"+String.valueOf(tiptoesPostureCounterSeconds));

            }
        });


    }

}



