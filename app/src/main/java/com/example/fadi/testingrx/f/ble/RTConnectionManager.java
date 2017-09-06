package com.example.fadi.testingrx.f.ble;

import android.util.Log;

import com.example.fadi.testingrx.MyApplication;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;

import java.util.UUID;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by fadi on 06/09/2017.
 */

public class RTConnectionManager {

    RxBleClient rxBleClient;

    Observer<RxBleDeviceServices> myServicesDiscoveryObserver;

    Observer<byte[]> myLeftBatteryReadObserver;
    Observer<byte[]> myRightBatteryReadObserver;

    Observer<Observable<byte[]>> myLeftAccelometerNotifyObserver;
    Observer<Observable<byte[]>> myRightAccelometerNotifyObserver;

    // connection observers, one for each insole
    Observer<RxBleConnection> leftInsoleConnectionObserver;
    Observer<RxBleConnection> rightInsoleConnectionObserver;

    RxBleDevice leftInsoleDevice;
    RxBleDevice rightInsoleDevice;

    // observables
    Observable<RxBleConnection> leftInsoleConnectionObservable;//this observable will be used any time we want to interact with a characteristic, no need to establish new connection for every operation.
    Observable<RxBleConnection> rightInsoleConnectionObservable;

    PostureTracker mPostureTracker;

    public RTConnectionManager(RxBleClient c, PostureTracker p){
        this.rxBleClient = c;
        this.mPostureTracker = p;
    }

    public void connect(){
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
}
