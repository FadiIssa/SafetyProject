package com.example.fadi.testingrx.f.ble;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.fadi.testingrx.MyApplication;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fadi on 06/09/2017.
 * this class should handle the scannin for nearest ZTSafety insoles, and connect to them,
 * also, when eolved, it should deal with saving mac addresses of the insoles, to make
 * connection to them faster than always having to wait several seconds to define the closest insoles.
 */

public class ScanManager {

    String TAG="ScanMgr";

    RxBleClient rxBleClient;// it will be passed to it, ideally, it should ask for it locally from the Application.
    // should be called from the bleManager, try to pass an inner class that implements a functional interface, in order to notify the ble manager about the finishing of the scanning.

    // observer
    Observer<ScanResult> myScanObserver;//scan observer, no need for 2 objects, one is enough to find all ble devices.

    // subscriptions
    Subscription scanSubscription;// this is for scthere will be several subscriptions, for each functionality

    // to know when to stop scanning
    boolean isLeftInsoleDetectedNearby;
    boolean isRightInsoleDetectedNearby;

    String leftInsoleMacAddress;
    String rightInsoleMacAddress;

    public ScanManager(){
        this.rxBleClient = MyApplication.getRxBleClient();
    }

    public void scanFromSavedPrefs(ScanFinishedCallBack callBack){
        Log.d(TAG,"scanFromSavedPrefs is called");
        myScanObserver=new Observer<ScanResult>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"scan onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"scan onError "+e.toString());
            }

            @Override
            public void onNext(ScanResult scanResult) {
                Log.d(TAG,"scan onNext "+scanResult.toString());
                //deviceMacTextView.setText(scanResult.getBleDevice().getMacAddress());
                //deviceNameTextView.setText(scanResult.getBleDevice().getName());

                if (scanResult.getBleDevice().getName().equals("ZTSafetyR")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    isRightInsoleDetectedNearby=true;
                }

                if (scanResult.getBleDevice().getName().equals("ZTSafetyL")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    isLeftInsoleDetectedNearby=true;
                }

                if (isLeftInsoleDetectedNearby&&isRightInsoleDetectedNearby){
                    //scanSubscription.unsubscribe();
                    //should save the mad addresses to sharedPreferences.

                    //callBack.notifyScanFinished();

                }
            }
        };

        //to deal with the case in case scanning was performed before. maybe for discovery purpose, and now we want to scan for specific device.
        if (scanSubscription!=null){
            if (!scanSubscription.isUnsubscribed()){
                scanSubscription.unsubscribe();
            }
        }

        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        // add custom filters if needed
                        .build()

        )

                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(scanResult -> {
                    String deviceName= scanResult.getBleDevice().getName();
                    if (deviceName==null)
                        return false;
                    else
                        return (deviceName.equals("ZTSafetyR") || deviceName.equals("ZTSafetyL"));
                })
                .take(4)
                .subscribe(myScanObserver);
    }

    public void scanForDiscovery(ScanStatusCallback callback){
        Log.d(TAG,"scanForDiscovery is called");
        // first we reset the values related to scanning
        reset();

        callback.scanStatusLeftIsScanning();
        callback.scanStatusRightIsScanning();
        myScanObserver=new Observer<ScanResult>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"scan for discovery onCompleted");
                // I should check if both types of insoles were found or not.
                if (isLeftInsoleDetectedNearby && isRightInsoleDetectedNearby) {
                    callback.scanSatusFinished(leftInsoleMacAddress,rightInsoleMacAddress);//notify caller that scan finished, so it can deal with the saved mac addresses. (to save them to shared preferences maybe).
                } else {
                    callback.scanStatusLeftStopped();
                    callback.scanStatusRightStopped();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"scan for discovery onError:"+e.toString());
                callback.scanStatusLeftStopped();
                callback.scanStatusRightStopped();
            }

            @Override
            public void onNext(ScanResult scanResult){
                Log.d(TAG,"scan for discovery onNext "+scanResult.toString());

                if (scanResult.getBleDevice().getName().equals("ZTSafetyR")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    leftInsoleMacAddress=scanResult.getBleDevice().getMacAddress();
                    isRightInsoleDetectedNearby=true;
                    callback.scanStatusRightFound();
                }

                if (scanResult.getBleDevice().getName().equals("ZTSafetyL")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    rightInsoleMacAddress=scanResult.getBleDevice().getMacAddress();
                    isLeftInsoleDetectedNearby=true;
                    callback.scanStatusLeftFound();
                }

                if (isLeftInsoleDetectedNearby&&isRightInsoleDetectedNearby){
                    scanSubscription.unsubscribe();
                    //should save the mad addresses to sharedPreferences.
                    callback.scanSatusFinished(leftInsoleMacAddress,rightInsoleMacAddress);
                    //callBack.notifyScanFinished();

                }
            }
        };

        //to deal with the case in case scanning was performed before. maybe for discovery purpose, and now we want to scan for specific device.
        if (scanSubscription!=null){
            if (!scanSubscription.isUnsubscribed()){
                scanSubscription.unsubscribe();
            }
        }

        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        // add custom filters if needed
                        .build()

        )

                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(scanResult -> {
                    String deviceName= scanResult.getBleDevice().getName();
                    if (deviceName==null)
                        return false;
                    else
                        return (deviceName.equals("ZTSafetyR") || deviceName.equals("ZTSafetyL"));
                })
                .take(4)
                .subscribe(myScanObserver);
    }

    private void reset(){
        //here we should reset parameters, and unsubscribe any still running subscription.
        isLeftInsoleDetectedNearby=false;
        isRightInsoleDetectedNearby=false;

        if (scanSubscription!=null){
            if (!scanSubscription.isUnsubscribed()) {
                scanSubscription.unsubscribe();
            }
        }
    }

    public String getLeftInsoleMacAddress(){
        return leftInsoleMacAddress;
    }

    public String getRightInsoleMacAddress(){
        return rightInsoleMacAddress;
    }
}
