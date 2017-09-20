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
import rx.schedulers.Schedulers;

/**
 * Created by fadi on 06/09/2017.
 */

public class RTConnectionManager {

    RxBleClient rxBleClient;

    Observer<RxBleDeviceServices> myLeftServicesDiscoveryObserver;
    Observer<RxBleDeviceServices> myRightServicesDiscoveryObserver;

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

        myLeftServicesDiscoveryObserver= ObserverPool.getNewLeftInsoleServiceDiscoveryObserver(mPostureTracker);
        myRightServicesDiscoveryObserver= ObserverPool.getNewRightInsoleServiceDiscoveryObserver(mPostureTracker);

        myLeftBatteryReadObserver = ObserverPool.getNewLeftBatteryReaderObserver();
        myRightBatteryReadObserver = ObserverPool.getNewRightBatteryReaderObserver();

        myLeftAccelometerNotifyObserver= ObserverPool.getNewLeftAccelerometerNotificationObserver(mPostureTracker);
        myRightAccelometerNotifyObserver= ObserverPool.getNewRightAccelerometerNotificationObserver(mPostureTracker);

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

                rxBleConnection.discoverServices().subscribe(myLeftServicesDiscoveryObserver);

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

                rxBleConnection.discoverServices().subscribe(myRightServicesDiscoveryObserver);

                rxBleConnection.readCharacteristic(UUID.fromString("99dd0016-a80c-4f94-be5d-c66b9fba40cf"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myRightBatteryReadObserver);

                rxBleConnection.setupNotification(UUID.fromString("99dd0108-a80c-4f94-be5d-c66b9fba40cf"))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myRightAccelometerNotifyObserver);
            }
        };

        //leftInsoleDevice = MyApplication.getRxBleClient().getBleDevice(Insoles.LeftInsoleMacAddress);
        leftInsoleDevice = bleManager.getLeftInsoleDevice();
        //bleManager.notifyBleManagerOfLeftInsoleDevice(leftInsoleDevice);
        Log.d(TAG," device name is:"+leftInsoleDevice.getName());

        //rightInsoleDevice = MyApplication.getRxBleClient().getBleDevice(Insoles.RightInsoleMacAddress);
        rightInsoleDevice = bleManager.getRightInsoleDevice();
        //bleManager.notifyBleManagerOfRightInsoleDevice(rightInsoleDevice);//this step was necessary before, but now, the bleManager is responsible of creating and establishing the bleDevice objects.
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
        leftInsoleConnectionSubscription=leftInsoleConnectionObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(leftInsoleConnectionObserver);

        //rightInsoleConnectionObservable=rightInsoleDevice.establishConnection(false);
        rightInsoleConnectionObservable= prepareRightConnectionObservable();
        rightInsoleConnectionSubscription=rightInsoleConnectionObservable.subscribeOn(Schedulers.newThread())
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
