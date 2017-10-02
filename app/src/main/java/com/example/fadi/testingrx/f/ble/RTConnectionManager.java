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

    Observer<byte[]> myLeftBatteryNotifyObserver;
    Observer<byte[]> myRightBatteryNotifyObserver;

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

    boolean leftInsoleIsConnecting;
    boolean rightInsoleIsConnecting;

    boolean leftInsoleReadyForTracking;
    boolean rightInsoleReadyForTracking;

    boolean isRetryConnectionEnabled;//it is usually true, but when the activity gets destroyed, it should be set to false, otherwise the connection will gets subscribed again and stay alive.

    public RTConnectionManager(RxBleClient c, PostureTracker p, BleManager caller){
        this.rxBleClient = c;
        this.mPostureTracker = p;
        bleManager = caller;
        isRetryConnectionEnabled=true;
    }

    public void connect(){

        isRetryConnectionEnabled=true;

        myLeftServicesDiscoveryObserver= ObserverPool.getNewLeftInsoleServiceDiscoveryObserver(mPostureTracker);
        myRightServicesDiscoveryObserver= ObserverPool.getNewRightInsoleServiceDiscoveryObserver(mPostureTracker);

        myLeftBatteryReadObserver = ObserverPool.getNewLeftBatteryReaderObserver(mPostureTracker);
        myRightBatteryReadObserver = ObserverPool.getNewRightBatteryReaderObserver(mPostureTracker);

        myLeftBatteryNotifyObserver = ObserverPool.getNewLeftBatteryNotifyObserver(mPostureTracker);
        myRightBatteryNotifyObserver = ObserverPool.getNewRightBatteryNotifyObserver(mPostureTracker);


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

                // discover services, right now I am not sure if I may need this,
                //rxBleConnection.discoverServices().subscribe(myLeftServicesDiscoveryObserver);

                rxBleConnection.readCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_BATTERY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myLeftBatteryReadObserver);

                rxBleConnection.setupNotification(UUID.fromString(Insoles.CHARACTERISTIC_BATTERY))
                        .flatMap(observable -> observable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myLeftBatteryNotifyObserver);

                rxBleConnection.setupNotification(UUID.fromString(Insoles.CHARACTERISTIC_ACCELEROMETER))
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

                //rxBleConnection.discoverServices().subscribe(myRightServicesDiscoveryObserver);// testing to see if services were not discovered, would this still not break the app.

                rxBleConnection.readCharacteristic(UUID.fromString(Insoles.CHARACTERISTIC_BATTERY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myRightBatteryReadObserver);

                rxBleConnection.setupNotification(UUID.fromString(Insoles.CHARACTERISTIC_BATTERY))
                        .flatMap(observable -> observable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(myRightBatteryNotifyObserver);

                rxBleConnection.setupNotification(UUID.fromString(Insoles.CHARACTERISTIC_ACCELEROMETER))
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
                        leftInsoleReadyForTracking=false;
                        informPostureTracker();
                        if (leftInsoleIsConnecting)
                        {
                            leftInsoleIsConnecting=false;
                            retryLeftConnection();
                        }
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTING)){
                        leftInsoleReadyForTracking=false;
                        mPostureTracker.getCaller().notifyLeftConnectionIsConnecting();
                        leftInsoleIsConnecting=true;
                        informPostureTracker();
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
                        leftInsoleReadyForTracking=true;
                        mPostureTracker.getCaller().notifyLeftConnectionConnected();
                        informPostureTracker();
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
                        rightInsoleReadyForTracking=false;
                        informPostureTracker();
                        if (rightInsoleIsConnecting)
                        {
                            rightInsoleIsConnecting=false;
                            retryRightConnection();
                        }
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTING)){
                        mPostureTracker.getCaller().notifyRightConnectionIsConnecting();
                        rightInsoleIsConnecting=true;
                        rightInsoleReadyForTracking=false;
                        informPostureTracker();
                    } else if (rxBleConnectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)) {
                        mPostureTracker.getCaller().notifyRightConnectionConnected();
                        rightInsoleReadyForTracking=true;
                        informPostureTracker();
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
                .establishConnection(false)
                .compose(new ConnectionSharingAdapter());
    }

    private Observable<RxBleConnection> prepareRightConnectionObservable() {
        return rightInsoleDevice
                .establishConnection(false)
                .compose(new ConnectionSharingAdapter());
    }

    private void retryLeftConnection(){
        Log.d(TAG,"retryLeftConnection is called");
        if (isRetryConnectionEnabled) {
            leftInsoleConnectionSubscription.unsubscribe();
            leftInsoleConnectionSubscription = leftInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(leftInsoleConnectionObserver);
        } else {
            Log.d(TAG,"retry left connection is disabled");
        }
    }

    private void retryRightConnection(){
        Log.d(TAG,"retryRightConnection is called");
        if (isRetryConnectionEnabled) {
            rightInsoleConnectionSubscription.unsubscribe();
            rightInsoleConnectionSubscription = rightInsoleConnectionObservable.subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rightInsoleConnectionObserver);
        } else {
            Log.d(TAG,"retry right connection is disabled");
        }
    }

    private void informPostureTracker(){
        if (leftInsoleReadyForTracking && rightInsoleReadyForTracking){
            mPostureTracker.resumeCounting();
        }
        else {
            mPostureTracker.pauseCounting();
        }
    }

    //this will be called for example when the activity is destroyed, to not leak any subscription.
    public void closeAllConnections(){
        isRetryConnectionEnabled=false;
        if (leftInsoleConnectionSubscription!=null){
            if (!leftInsoleConnectionSubscription.isUnsubscribed()){
                Log.d(TAG,"unsubscibr leftConnectionsubscription");
                leftInsoleConnectionSubscription.unsubscribe();

            }
        }

        if (rightInsoleConnectionSubscription!=null){
            if (!rightInsoleConnectionSubscription.isUnsubscribed()){
                Log.d(TAG,"unsubscibr leftConnectionsubscription");
                rightInsoleConnectionSubscription.unsubscribe();
            }
        }


    }
}
