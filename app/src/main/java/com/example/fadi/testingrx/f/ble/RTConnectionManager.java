package com.example.fadi.testingrx.f.ble;

import android.util.Log;

import com.example.fadi.testingrx.MyApplication;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import java.time.LocalDate;
import java.util.UUID;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
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

    Subscription leftInsoleConnectionSubscription;
    Subscription rightInsoleConnectionSubscription;

    BleManager bleManager;

    PostureTracker mPostureTracker;

    String TAG="RTConn";

    public RTConnectionManager(RxBleClient c, PostureTracker p, BleManager caller){
        this.rxBleClient = c;
        this.mPostureTracker = p;
        bleManager = caller;
    }

    public void connect(){
        myServicesDiscoveryObserver= new Observer<RxBleDeviceServices>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"ServicesDiscoveryObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"ServicesDiscoveryObserver onError "+e.toString());
            }


            @Override
            public void onNext(RxBleDeviceServices rxBleDeviceServices) {
                Log.d(TAG,"ServicesDiscoveryObserver onNext "+rxBleDeviceServices.toString());

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
                Log.d(TAG,"LeftBatteryReadObserver onError "+e.toString());

            }

            @Override
            public void onNext(byte[] bytes) {
                int leftBatteryValue=bytes[0]&0xFF;
                Log.d(TAG,"LeftBatteryReadObserver onNext "+ leftBatteryValue);
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
                Log.d(TAG,"RightBatteryReadObserver onError "+e.toString());

            }

            @Override
            public void onNext(byte[] bytes) {
                int rightBatteryValue=bytes[0]&0xFF;
                Log.d(TAG,"RightBatteryReadObserver onNext "+ rightBatteryValue);
                //updateRightBattery(rightBatteryValue);

            }
        };

        myLeftAccelometerNotifyObserver= new Observer<Observable<byte[]>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"LeftAccelometerNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"LeftAccelometerNotifyObserver onError "+e.toString());

            }

            @Override
            public void onNext(Observable<byte[]> observable) {
                observable.subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG,"LeftAccelometerNotifyObserver reader onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"LeftAccelometerNotifyObserver reader "+e.toString());

                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        //Log.d(TAG,"LeftAccelometerNotifyObserver reader onNext "+ bytes.toString());
                        for (int i=0;i<bytes.length;i++){
                            //Log.d(TAG,"byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                        }

                        //int accX= bytes[4]&0xFF+((bytes[5]&0xFF)<<8);
                        int accX= (bytes[4]&0xFF)|((bytes[5])<<8);
                        //Log.d(TAG,"Left accX:"+accX);

                        //int accY= bytes[6]&0xFF+((bytes[7]&0xFF)<<8);
                        int accY= (bytes[6]&0xFF)|((bytes[7])<<8);
                        //Log.d(TAG,"Left accY:"+accY);

                        //int accZ= bytes[8]&0xFF+((bytes[9]&0xFF)<<8);
                        int accZ= (bytes[8]&0xFF)|((bytes[9])<<8);
                        //Log.d(TAG,"Left accZ:"+accZ);

                        //updateLeftAccelometer(accX,accY,accZ);
                        mPostureTracker.updateLeftAccelometer(accX,accY,accZ);
                    }
                });
            }
        };

        myRightAccelometerNotifyObserver= new Observer<Observable<byte[]>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"RightAccelometerNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"RightAccelometerNotifyObserver onError "+e.toString());

            }

            @Override
            public void onNext(Observable<byte[]> observable) {
                observable.subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG,"RightAccelometerNotifyObserver reader onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"RightAccelometerNotifyObserver reader "+e.toString());

                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        //Log.d(TAG,"RightAccelometerNotifyObserver reader onNext "+ bytes.toString());


                        /*for (int i=0;i<bytes.length;i++){
                            Log.d(TAG,"byte nr:"+ i+" is:"+(bytes[i]&0xFF));
                        }*/

                        //int accX= bytes[4]&0xFF+((bytes[5]&0xFF)<<8);
                        int accX= (bytes[4]&0xFF)|((bytes[5])<<8);
                        //Log.d(TAG,"Right accX:"+accX);

                        //int accY= bytes[6]&0xFF+((bytes[7]&0xFF)<<8);
                        int accY= (bytes[6]&0xFF)|((bytes[7])<<8);
                        //Log.d(TAG,"Right accY:"+accY);

                        //int accZ= bytes[8]&0xFF+((bytes[9]&0xFF)<<8);
                        int accZ= (bytes[8]&0xFF)|((bytes[9])<<8);
                        //Log.d(TAG,"Right accZ:"+accZ);

                        //updateRightAccelometer(accX,accY,accZ);
                        mPostureTracker.updateRightAccelometer(accX,accY,accZ);
                    }
                });
            }
        };

        leftInsoleConnectionObserver= new Observer<RxBleConnection>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"LeftconnectionObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"LeftconnectionObserver onError "+e.toString());
                //mPostureTracker.getCaller().notifyLeftConnectionLost();
                //retryLeftConnection();

            }

            @Override
            public void onNext(RxBleConnection rxBleConnection) {
                Log.d(TAG,"LeftconnectionObserver onNext "+rxBleConnection.toString());

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
                Log.d(TAG,"RightconnectionObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"RightconnectionObserver onError "+e.toString());
                //mPostureTracker.getCaller().notifyRightConnectionLost();
                //retryRightConnection();

            }

            @Override
            public void onNext(RxBleConnection rxBleConnection) {
                Log.d(TAG,"RightconnectionObserver onNext "+rxBleConnection.toString());

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
        bleManager.notifyBleManagerOfLeftInsoleDevice(leftInsoleDevice);
        Log.d(TAG," device name is:"+leftInsoleDevice.getName());

        rightInsoleDevice = MyApplication.getRxBleClient().getBleDevice(Insoles.RightInsoleMacAddress);
        bleManager.notifyBleManagerOfRightInsoleDevice(rightInsoleDevice);
        Log.d(TAG," device name is:"+rightInsoleDevice.getName());

        leftInsoleDevice.observeConnectionStateChanges()
                .subscribe(rxBleConnectionState -> {
                    Log.d(TAG," left connectionStateChange:"+rxBleConnectionState.toString());

                    if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)){
                        mPostureTracker.getCaller().notifyLeftConnectionDisconnected();
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTING)){
                        mPostureTracker.getCaller().notifyLeftConnectionIsConnecting();
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
                        mPostureTracker.getCaller().notifyLeftConnectionConnected();
                    } else {
                        mPostureTracker.getCaller().notifyLeftConnectionDisconnected();
                    }
                },throwable -> {
                    Log.d(TAG,"left connectionStateChange error:"+throwable.toString());
                });

        rightInsoleDevice.observeConnectionStateChanges()
                .subscribe(rxBleConnectionState -> {
                    Log.d(TAG," right connectionStateChange:"+rxBleConnectionState.toString());

                    if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)){
                        mPostureTracker.getCaller().notifyRightConnectionDisconnected();
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTING)){
                        mPostureTracker.getCaller().notifyRightConnectionIsConnecting();
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
                        mPostureTracker.getCaller().notifyRightConnectionConnected();
                    } else {
                        mPostureTracker.getCaller().notifyRightConnectionDisconnected();
                    }
                },throwable -> {
                    Log.d(TAG,"right connectionStateChange error:"+throwable.toString());
                });



        //leftInsoleConnectionObservable=leftInsoleDevice.establishConnection(false);
        leftInsoleConnectionObservable = prepareLeftConnectionObservable();
        leftInsoleConnectionSubscription=leftInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(leftInsoleConnectionObserver);

        //rightInsoleConnectionObservable=rightInsoleDevice.establishConnection(false);
        rightInsoleConnectionObservable= prepareRightConnectionObservable();
        rightInsoleConnectionSubscription=rightInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rightInsoleConnectionObserver);
    }

    public Observable<RxBleConnection> getLeftInsoleConnectionObservable(){
        return leftInsoleConnectionObservable;
    }

    public Observable<RxBleConnection> getRightInsoleConnectionObservable(){
        return rightInsoleConnectionObservable;
    }

    private Observable<RxBleConnection> prepareLeftConnectionObservable() {
        return leftInsoleDevice
                .establishConnection(true)
                .compose(new ConnectionSharingAdapter());
    }

    private Observable<RxBleConnection> prepareRightConnectionObservable() {
        return rightInsoleDevice
                .establishConnection(true)
                .compose(new ConnectionSharingAdapter());
    }

    private void retryLeftConnection(){
        Log.d(TAG,"retryLeftConnection is called");
        leftInsoleConnectionSubscription.unsubscribe();
        leftInsoleConnectionSubscription=leftInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(leftInsoleConnectionObserver);
    }

    private void retryRightConnection(){
        Log.d(TAG,"retryRightConnection is called");
        rightInsoleConnectionSubscription.unsubscribe();
        rightInsoleConnectionSubscription = rightInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rightInsoleConnectionObserver);
    }
}
