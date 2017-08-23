package com.example.fadi.testingrx;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.fadi.testingrx.f.ble.Insoles;
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

    TextView deviceNameTextView;
    TextView deviceMacTextView;

    TextView laX;
    TextView laY;
    TextView laZ;

    TextView raX;
    TextView raY;
    TextView raZ;

    TextView positionValue;

    int latestLX;
    int latestLY;
    int latestLZ;
    int latestRX;
    int latestRY;
    int latestRZ;


    String leftInsoleMacAddress;
    String serviceUUID;
    RxBleDevice leftInsoleDevice;
    RxBleDevice rightInsoleDevice;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //String RPDeviceAdress="C1:56:ED:7A:AB:07";
        //leftInsoleMacAddress="C1:56:ED:7A:AB:07";
        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";
        deviceMacTextView= (TextView) findViewById(R.id.textView2);
        deviceNameTextView= (TextView) findViewById(R.id.textView);

        laX=(TextView) findViewById(R.id.leftX);
        laY=(TextView) findViewById(R.id.leftY);
        laZ=(TextView) findViewById(R.id.leftZ);

        raX=(TextView) findViewById(R.id.rightX);
        raY=(TextView) findViewById(R.id.rightY);
        raZ=(TextView) findViewById(R.id.rightZ);

        positionValue= (TextView) findViewById(R.id.positionTextView);

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
                deviceMacTextView.setText(scanResult.getBleDevice().getMacAddress());
                deviceNameTextView.setText(scanResult.getBleDevice().getName());
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
                updateLeftBattery(leftBatteryValue);

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
                updateRightBattery(rightBatteryValue);

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

                        updateLeftAccelometer(accX,accY,accZ);
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

                        updateRightAccelometer(accX,accY,accZ);
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

    private void updateRightBattery(int i){
        TextView rbv=(TextView) findViewById(R.id.rightBatteryValueTextView);
        rbv.setText(String.valueOf(i));

    }

    private void updateLeftBattery(int i){
        TextView lbv=(TextView) findViewById(R.id.leftBatteryValueTextView);
        lbv.setText(String.valueOf(i));
    }

    private void updateLeftAccelometer(final int x, final int y, final int z){
        latestLX=x;
        latestLY=-1*y;
        latestLZ=z;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                laX.setText(String.valueOf(x));
                laY.setText(String.valueOf(-y));
                laZ.setText(String.valueOf(z));
                updatePosition();
            }
        });


    }

    private void updateRightAccelometer(final int x, final int y, final int z){
        latestRX=x;
        latestRY=y;
        latestRZ=z;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                raX.setText(String.valueOf(x));
                raY.setText(String.valueOf(y));
                raZ.setText(String.valueOf(z));
                updatePosition();
            }
        });
    }

    private void updatePosition(){
        String position="unknown";
        if (latestRZ<300 && latestLZ>800 && latestLZ<1100 && latestRY>700 && latestLY<300 && latestLY>-300){
            position="crouching";
            Log.d("RXTesting","position:"+position);
        }

        if (latestRZ<300 && latestLZ<300 && latestRY>600 && latestLY>600){
            position="kneeling";
            Log.d("RXTesting","position:"+position);
        }
        else {
            if (latestRZ>400 && latestRZ<700 && latestLZ>400 && latestLZ<700 && latestRY>600 && latestLY>600)
            {
                position="tip toes";
                Log.d("RXTesting","position:"+position);
            }
        }
        Log.d("RXTesting","final position:"+position);

        positionValue.setText(position);
    }




}



