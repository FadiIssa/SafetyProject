package com.example.fadi.testingrx.f.ble;

import com.polidea.rxandroidble.RxBleClient;

/**
 * Created by fadi on 06/09/2017.
 */

public class BleManager {

    RxBleClient rxBleClient;
    ScanManager scanManager;

    public BleManager(RxBleClient c){
        this.rxBleClient=c;
    }

    // it should check if there are saved insoles in sharedPreferences (saved mac address), if so, it brings them and use them in the scan as filters, if not, it searches for the nearest safety insoles for both left and right.
    public void scanAndPair(){

    }
}
