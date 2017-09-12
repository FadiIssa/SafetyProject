package com.example.fadi.testingrx.f.ble;

import android.util.Log;

import com.example.fadi.testingrx.MyApplication;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by fadi on 06/09/2017.
 * this class should handle the scannin for nearest ZTSafety insoles, and connect to them,
 * also, when eolved, it should deal with saving mac addresses of the insoles, to make
 * connection to them faster than always having to wait several seconds to define the closest insoles.
 */

public class ScanManager {

    RxBleClient rxBleClient;// it will be passed to it, ideally, it should ask for it locally from the Application.
    // should be called from the bleManager, try to pass an inner class that implements a functional interface, in order to notify the ble manager about the finishing of the scanning.

    // observer
    Observer<ScanResult> myScanObserver;//scan observer, no need for 2 objects, one is enough to find all ble devices.

    // subscriptions
    Subscription scanSubscription;// this is for scthere will be several subscriptions, for each functionality

    // to know when to stop scanning
    boolean isLeftInsoleDetectedNearby;
    boolean isRightInsoleDetectedNearby;

    public ScanManager(){
        this.rxBleClient = MyApplication.getRxBleClient();
    }

    public void scanAndPair(ScanFinishedCallBack callBack){
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

                if (scanResult.getBleDevice().getName().equals("ZTSafetyR")){
                    isRightInsoleDetectedNearby=true;
                }

                if (scanResult.getBleDevice().getName().equals("ZTSafetyL")){
                    isLeftInsoleDetectedNearby=true;
                }

                if (isLeftInsoleDetectedNearby&&isRightInsoleDetectedNearby){
                    scanSubscription.unsubscribe();
                    //should save the mad addresses to sharedPreferences.

                    callBack.notifyScanFinished();

                }
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
                .filter(scanResult -> {
                    String deviceName= scanResult.getBleDevice().getName();
                    if (deviceName==null)
                        return false;
                    else
                        return (deviceName.equals("ZTSafetyR") || deviceName.equals("ZTSafetyL"));
                })
                .subscribe(myScanObserver);
    }
}
