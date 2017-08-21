package com.example.fadi.testingrx;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.polidea.rxandroidble.NotificationSetupMode;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;


import java.util.UUID;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    RxBleClient rxBleClient;
    Subscription scanSubscription;
    Subscription connectionSubscription;
    Observer<ScanResult> myScanObserver;
    Observer<RxBleConnection> myConnectionObserver;
    Observer<RxBleDeviceServices> myServicesDiscoveryObserver;
    Observer<byte[]> myBatteryReadObserver;
    Observer<Observable<byte[]>> myStepsNotifyObserver;
    TextView deviceNameTextView;
    TextView deviceMacTextView;
    String leftInsoleMacAddress;
    String serviceUUID;
    RxBleDevice leftInsoleDevice;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        counter=0;
        String deviceAdress="C1:56:ED:7A:AB:07";
        leftInsoleMacAddress="C1:56:ED:7A:AB:07";
        serviceUUID="99ddcda5-a80c-4f94-be5d-c66b9fba40cf";
        deviceMacTextView= (TextView) findViewById(R.id.textView2);
        deviceNameTextView= (TextView) findViewById(R.id.textView);

        rxBleClient = RxBleClient.create(getApplicationContext());
        scanAndPairing();
        connect();
        //readBattery();
        //registerForSteps();


    }

    private void scanAndPairing(){
        myScanObserver=new Observer<ScanResult>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","onCompleted");

            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","onError "+e.toString());
            }

            @Override
            public void onNext(ScanResult scanResult) {
                Log.d("RXTesting","onNext "+scanResult.toString());
                deviceMacTextView.setText(scanResult.getBleDevice().getMacAddress());
                deviceNameTextView.setText(scanResult.getBleDevice().getName());
                scanSubscription.unsubscribe();

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
                .subscribeOn(Schedulers.io())
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

        myBatteryReadObserver = new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","BatteryReadObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","BatteryReadObserver onError "+e.toString());

            }

            @Override
            public void onNext(byte[] bytes) {
                Log.d("RXTesting","BatteryReadObserver onNext "+ bytes.toString());

            }
        };

        myStepsNotifyObserver= new Observer<Observable<byte[]>>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","StepsNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","StepsNotifyObserver onError "+e.toString());

            }

            @Override
            public void onNext(Observable<byte[]> observable) {
                observable.subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        Log.d("RXTesting","StepsNotifyObserver reader onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("RXTesting","StepsNotifyObserver reader "+e.toString());

                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        Log.d("RXTesting","StepsNotifyObserver reader onNext "+ bytes.toString());
                        for (int i=0;i<bytes.length;i++){
                            Log.d("RXTesting","byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                        }

                        int totalSteps= bytes[0]&0xFF+((bytes[1]&0xFF)<<8);
                        Log.d("RXTesting","totalSteps:"+totalSteps);
                    }
                });

            }
        };



        myConnectionObserver= new Observer<RxBleConnection>() {
            @Override
            public void onCompleted() {
                Log.d("RXTesting","connectionObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("RXTesting","connectionObserver onError "+e.toString());

            }

            @Override
            public void onNext(RxBleConnection rxBleConnection) {
                Log.d("RXTesting","connectionObserver onNext "+rxBleConnection.toString());

                rxBleConnection.discoverServices().subscribe(myServicesDiscoveryObserver);

                rxBleConnection.readCharacteristic(UUID.fromString("99dd0016-a80c-4f94-be5d-c66b9fba40cf"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myBatteryReadObserver);

                rxBleConnection.setupNotification(UUID.fromString("99dd0106-a80c-4f94-be5d-c66b9fba40cf"))
                        .subscribe(myStepsNotifyObserver);
            }
        };

        leftInsoleDevice = rxBleClient.getBleDevice(leftInsoleMacAddress);
        String s=leftInsoleDevice.getName();
        Log.d("RXTesting"," device name is:"+s);
        leftInsoleDevice.establishConnection(false).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myConnectionObserver);
        /*connectionSubscription=rxBleClient.getBleDevice(leftInsoleMacAddress)
                .establishConnection(false)
                .*/
    }

}



