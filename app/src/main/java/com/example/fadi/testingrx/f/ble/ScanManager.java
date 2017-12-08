package com.example.fadi.testingrx.f.ble;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.fadi.testingrx.MyApplication;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
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
    final int MAXIMUM_SCAN_TIME=9;

    RxBleClient rxBleClient;// it will be passed to it, ideally, it should ask for it locally from the Application.
    // should be called from the bleManager, try to pass an inner class that implements a functional interface, in order to notify the ble manager about the finishing of the scanning.

    // observers
    Observer<ScanResult> myScanObserver;//scan observer, no need for 2 objects, one is enough to find all ble devices.

    Observer<Long> scanTimerObserver;// it will be used to cancel an ongoing scan operation after a time out.

    // subscriptions
    Subscription scanSubscription;// this is for scthere will be several subscriptions, for each functionality

    // to know when to stop scanning
    boolean isLeftInsoleDetectedNearby;
    boolean isRightInsoleDetectedNearby;

    int bestLeftInsoleRSSI;
    int bestRightInsoleRSSI;

    String leftInsoleMacAddress;
    String rightInsoleMacAddress;

    String leftInsoleMacToSearchFor;
    String rightInsoleMacToSearchFor;

    public ScanManager(){
        this.rxBleClient = MyApplication.getRxBleClient();
    }

    private void reset(){
        //here we should reset parameters, and unsubscribe any still running subscription.
        isLeftInsoleDetectedNearby=false;
        isRightInsoleDetectedNearby=false;

        leftInsoleMacAddress="";
        rightInsoleMacAddress="";

        bestLeftInsoleRSSI=-100;
        bestRightInsoleRSSI=-100;

        if (scanSubscription!=null){
            if (!scanSubscription.isUnsubscribed()) {
                scanSubscription.unsubscribe();
            }
        }
    }

    public void scanForDiscovery(ScanStatusCallback callback){
        Log.d(TAG,"scanForDiscovery is called");

        // first we reset the values related to scanning
        reset();

        Log.d(TAG,"notify callback that status isScanning for both insoles");
        callback.scanStatusLeftIsScanning();
        callback.scanStatusRightIsScanning();

        myScanObserver=new Observer<ScanResult>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"scan for discovery onCompleted");// when there is a timer, this should never be called.
                // I should check if both types of insoles were found or not.
                /*if (isLeftInsoleDetectedNearby && isRightInsoleDetectedNearby) {
                    callback.scanStatusFinished(leftInsoleMacAddress,rightInsoleMacAddress);//notify caller that scan finished, so it can deal with the saved mac addresses. (to save them to shared preferences maybe).
                } else {
                    callback.scanStatusLeftStopped();
                    callback.scanStatusRightStopped();
                }*/
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

                if (scanResult.getBleDevice().getName().equals("ZTSafetyL")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    int currentDetectedLeftRSSI=scanResult.getRssi();

                    if (currentDetectedLeftRSSI>bestLeftInsoleRSSI){
                        leftInsoleMacAddress=scanResult.getBleDevice().getMacAddress();;
                        bestLeftInsoleRSSI=currentDetectedLeftRSSI;
                    }

                    isLeftInsoleDetectedNearby=true;
                    callback.scanStatusLeftFound();
                }

                if (scanResult.getBleDevice().getName().equals("ZTSafetyR")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    int currentDetectedRightRSSI=scanResult.getRssi();

                    if (currentDetectedRightRSSI>bestRightInsoleRSSI){
                        rightInsoleMacAddress=scanResult.getBleDevice().getMacAddress();;
                        bestRightInsoleRSSI=currentDetectedRightRSSI;
                    }
                    isRightInsoleDetectedNearby=true;
                    callback.scanStatusRightFound();
                }

                if (isLeftInsoleDetectedNearby&&isRightInsoleDetectedNearby){
                    //scanSubscription.unsubscribe();//right now, when we find both devices, we unsubscribe, remove this line if you want the scan to finish only based on timer off, like for detecting the closest insoles in a range.
                    //should save the mac addresses to sharedPreferences. from the caller activity.
                    //callback.scanStatusFinished(leftInsoleMacAddress,rightInsoleMacAddress);
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
                .doOnSubscribe(()->{startScanTimer();})
                .doOnUnsubscribe(()->{
                    // I should check if both types of insoles were found or not.
                    if (isLeftInsoleDetectedNearby && isRightInsoleDetectedNearby) {
                        callback.scanStatusFinished(leftInsoleMacAddress,rightInsoleMacAddress);//notify caller that scan finished, so it can deal with the saved mac addresses. (to save them to shared preferences maybe).
                    } else {
                        callback.scanStatusLeftStopped();
                        callback.scanStatusRightStopped();
                        callback.scanStatusFinishedUnsuccessfully();
                    }
                })
                //.take(4)
                .subscribe(myScanObserver);
    }

    //////////////////////////////////////////

    public void scanFromSavedPrefs(String leftMac, String rightMac, ScanStatusCallback callback){
        Log.d(TAG,"scanFromSavedPrefs is called");
        // first we reset the values related to scanning
        reset();

        leftInsoleMacToSearchFor=leftMac;
        rightInsoleMacToSearchFor=rightMac;

        Log.d(TAG,"notify callback that status isScanning for both insoles");
        callback.scanStatusLeftIsScanning();
        callback.scanStatusRightIsScanning();

        myScanObserver=new Observer<ScanResult>() {
            @Override
            public void onCompleted() {// it seems this will never be called, because we unsubscribe when both insoles are found.
                Log.d(TAG,"scanObserver for scanFromSavedPrefs called onCompleted");
                // this will only be called if both insoles were found from the observable.
                //notify caller that scan finished, so it can deal with the saved mac addresses.
                // (to save them to shared preferences maybe).
                /*leftInsoleMacAddress=leftInsoleMacToSearchFor;
                rightInsoleMacAddress= rightInsoleMacToSearchFor;
                callback.scanStatusFinished(leftInsoleMacAddress,rightInsoleMacAddress);*/

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"scanObserver for scanFromSavedPrefs called onError: "+e.toString());
                callback.scanStatusLeftStopped();
                callback.scanStatusRightStopped();
            }

            @Override
            public void onNext(ScanResult scanResult) {
                Log.d(TAG,"scan onNext "+scanResult.toString());

                if (scanResult.getBleDevice().getName().equals("ZTSafetyL")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    if (scanResult.getBleDevice().getMacAddress().equals(leftInsoleMacToSearchFor)) {
                        isLeftInsoleDetectedNearby = true;
                        callback.scanStatusLeftFound();
                    }
                }

                if (scanResult.getBleDevice().getName().equals("ZTSafetyR")){
                    Log.d(TAG,"signal strength is:"+scanResult.getRssi());
                    if (scanResult.getBleDevice().getMacAddress().equals(rightInsoleMacToSearchFor)) {
                        isRightInsoleDetectedNearby = true;
                        callback.scanStatusRightFound();
                    }

                }

                if (isLeftInsoleDetectedNearby&&isRightInsoleDetectedNearby){
                    scanSubscription.unsubscribe();
                    leftInsoleMacAddress=leftInsoleMacToSearchFor;
                    rightInsoleMacAddress= rightInsoleMacToSearchFor;
                    callback.scanStatusFinished(leftInsoleMacToSearchFor,rightInsoleMacToSearchFor);
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
                .subscribe(myScanObserver);
    }



    public String getLeftInsoleMacAddress(){
        return leftInsoleMacAddress;
    }

    public String getRightInsoleMacAddress(){
        return rightInsoleMacAddress;
    }

    //the purpose is to unsubscribe the scanning (to close scanning) after a timer is finished (in case the scanning was not finished already for finding the desired insoles).
    private void startScanTimer() {
        Observable.interval(1, TimeUnit.SECONDS)
                .take(MAXIMUM_SCAN_TIME)
                .subscribe(aLong -> {
                            Log.d(TAG,"one second of scanning passed, it is:"+aLong);
                        },
                        t -> {
                            Log.e(TAG, "error from scanTimerObserver:" + t.toString());
                        },
                        () -> {
                            Log.e(TAG, " scan timer Observer received onCompleted()");
                            if (scanSubscription != null) {
                                Log.d(TAG,"scanSubscription is not Null");
                                if (!scanSubscription.isUnsubscribed()) {
                                    Log.d(TAG,"scanSubscription is not unsubscribed, so we call unsubscribe");
                                    scanSubscription.unsubscribe();
                                }
                                else {
                                    Log.d(TAG,"scanSubscription is already unsubscribed");
                                }
                            }
                        });
    }
}
