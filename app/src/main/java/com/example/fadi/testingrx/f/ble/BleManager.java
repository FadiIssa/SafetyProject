package com.example.fadi.testingrx.f.ble;

import android.util.Log;

import com.example.fadi.testingrx.MainActivity;
import com.example.fadi.testingrx.MyApplication;
import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;

import rx.Observable;

/**
 * Created by fadi on 06/09/2017.
 * purpose of this class is to handle the connectivity part, it should deal with subscribers that implement an interface, in order to notify them about connectivity events, such as when an insole is diconnected, or when an error happens.
 * ideally.. this class should be referenced from a service, in order to allow it to serve multiple activities, right now, since it we have a 1 activity model, then we have to use fragments, to share teh same object for different functionalities, like for real time posture, standing and walking time.. etc.. each group of funcionalities will be in a different fragment.
 * this manager will reference several managers, one for scanning, one for real time readings (for postures), and one for normal reading of characteristics.
 */

// this class will be static referenced from the MyApplication class, to make it available for all the activities.

public class BleManager {

    RxBleClient rxBleClient;
    ScanManager scanManager;
    RTConnectionManager rtConnectionManager;

    RxBleDevice leftInsoleDevice;
    RxBleDevice rightInsoleDevice;

    public BleManager(){
        this.rxBleClient = MyApplication.getRxBleClient();
        scanManager = new ScanManager();
    }

    // it should check if there are saved insoles in sharedPreferences (saved mac address), if so, it brings them and use them in the scan as filters, if not, it searches for the nearest safety insoles for both left and right.
    public void scanAndPair(ScanFinishedCallBack c){
        scanManager.scanAndPair(c);
    }

    // this method can be called after the call back from scanAndRepair is finished.
    public void connectRealTime(PostureTracker p){
        rtConnectionManager = new RTConnectionManager(this.rxBleClient,p, this);
        rtConnectionManager.connect();
    }

    public void subscribeRTPostureActivity(MainActivity a){

    }

    public void notifyBleManagerOfLeftInsoleDevice(RxBleDevice d){
        leftInsoleDevice=d;
    }

    public void notifyBleManagerOfRightInsoleDevice(RxBleDevice d){
        rightInsoleDevice=d;
    }

    public RxBleDevice getLeftInsoleDevice(){
        return leftInsoleDevice;
    }

    public RxBleDevice getRightInsoleDevice(){
        return rightInsoleDevice;
    }

    public Observable<RxBleConnection> getLeftInsoleConnectionObservable(){
        return rtConnectionManager.getLeftInsoleConnectionObservable();
    }

    public Observable<RxBleConnection> getRightInsoleConnectionObservable(){
        return rtConnectionManager.getRightInsoleConnectionObservable();
    }

    public boolean areDevicesAlreadyScanned(){
        return (leftInsoleDevice!=null && rightInsoleDevice!=null);
    }
}
