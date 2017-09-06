package com.example.fadi.testingrx.f.ble;

import android.util.Log;

import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.polidea.rxandroidble.RxBleClient;

/**
 * Created by fadi on 06/09/2017.
 */

public class BleManager {

    RxBleClient rxBleClient;
    ScanManager scanManager;
    RTConnectionManager rtConnectionManager;

    public BleManager(RxBleClient c){
        this.rxBleClient=c;
        scanManager = new ScanManager(c);

    }

    // it should check if there are saved insoles in sharedPreferences (saved mac address), if so, it brings them and use them in the scan as filters, if not, it searches for the nearest safety insoles for both left and right.
    public void scanAndPair(ScanFinishedCallBack c){
        scanManager.scanAndPair(c);
    }

    // this method can be called after the call back from scanAndRepair is finished.
    public void connectRealTime(PostureTracker p){
        rtConnectionManager = new RTConnectionManager(this.rxBleClient,p);
        rtConnectionManager.connect();
    }
}
