package com.example.fadi.testingrx.f.ble;

import android.util.Log;

import com.example.fadi.testingrx.f.posture.PostureTracker;
import com.polidea.rxandroidble.RxBleDeviceServices;

import rx.Observable;
import rx.Observer;

import static android.content.ContentValues.TAG;

/**
 * Created by fadi on 20/09/2017.
 * this class has the purpose to provide Observers, to be used to subscribe to observables.
 */

public class ObserverPool {

    static String TAG="ObservPool";

    public static Observer<RxBleDeviceServices> getNewLeftInsoleServiceDiscoveryObserver(PostureTracker p){
        return new Observer<RxBleDeviceServices>() {
            @Override
            public void onCompleted() {
                Log.d(TAG," left ServicesDiscoveryObserver onCompleted");
                p.getCaller().notifyLeftServiceDiscoveryCompleted();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"left ServicesDiscoveryObserver onError "+e.toString());
            }

            @Override
            public void onNext(RxBleDeviceServices rxBleDeviceServices) {
                Log.d(TAG," left ServicesDiscoveryObserver onNext "+rxBleDeviceServices.toString());
            }
        };
    }

    public static Observer<RxBleDeviceServices> getNewRightInsoleServiceDiscoveryObserver(PostureTracker p){
        return new Observer<RxBleDeviceServices>() {
            @Override
            public void onCompleted() {
                Log.d(TAG," right ServicesDiscoveryObserver onCompleted");
                p.getCaller().notifyRightServiceDiscoveryCompleted();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"right ServicesDiscoveryObserver onError "+e.toString());
            }

            @Override
            public void onNext(RxBleDeviceServices rxBleDeviceServices) {
                Log.d(TAG,"right ServicesDiscoveryObserver onNext "+rxBleDeviceServices.toString());
            }
        };
    }

    public static Observer<byte[]> getNewLeftBatteryReaderObserver(PostureTracker p){
        return new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"LeftBatteryReadObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"LeftBatteryReadObserver onError "+e.toString());
            }

            @Override
            public void onNext(byte[] bytes) {
                int leftBatteryValue=bytes[0]&0xFF;
                Log.d(TAG,"LeftBatteryReadObserver onNext "+ leftBatteryValue);
                p.updateLeftBattery(leftBatteryValue);
                //updateLeftBattery(leftBatteryValue);
            }
        };
    }

    public static Observer<byte[]> getNewRightBatteryReaderObserver(PostureTracker p){
        return new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"RightBatteryReadObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"RightBatteryReadObserver onError "+e.toString());
            }

            @Override
            public void onNext(byte[] bytes) {
                int rightBatteryValue=bytes[0]&0xFF;
                Log.d(TAG,"RightBatteryReadObserver onNext "+ rightBatteryValue);
                p.updateRightBattery(rightBatteryValue);
                //updateRightBattery(rightBatteryValue);
            }
        };
    }

    public static Observer<byte[]> getLeftFWReaderObserver(PostureTracker p){
        return new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"lefetFWReaderObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"lefetFWReaderObserver onError "+e.toString());
            }

            @Override
            public void onNext(byte[] bytes) {
                int leftFWBuildValue=bytes[4]&0xFF;
                Log.d(TAG,"lefetFWReaderObserver onNext "+ leftFWBuildValue);
                p.updateLeftFW(leftFWBuildValue);
            }
        };
    }

    public static Observer<byte[]> getRightFWReaderObserver(PostureTracker p){
        return new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"rightFWReaderObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"rightFWReaderObserver onError "+e.toString());
            }

            @Override
            public void onNext(byte[] bytes) {
                int rightFWBuildValue=bytes[4]&0xFF;
                Log.d(TAG,"rightFWReaderObserver onNext "+ rightFWBuildValue);
                p.updateRightFW(rightFWBuildValue);
            }
        };
    }

    public static Observer<byte[]> getNewLeftBatteryNotifyObserver(PostureTracker p){
        return new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"LeftBatteryNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"LefttBatteryNotifyObserver onError "+e.toString());
            }

            @Override
            public void onNext(byte[] bytes) {
                int leftBatteryValue=bytes[0]&0xFF;
                Log.d(TAG,"LeftBatteryNotifyObserver onNext "+ leftBatteryValue);
                p.updateLeftBattery(leftBatteryValue);
            }
        };
    }

    public static Observer<byte[]> getNewRightBatteryNotifyObserver(PostureTracker p){
        return new Observer<byte[]>() {
            @Override
            public void onCompleted() {
                Log.d(TAG,"RightBatteryNotifyObserver onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"RightBatteryNotifyObserver onError "+e.toString());
            }

            @Override
            public void onNext(byte[] bytes) {
                int rightBatteryValue=bytes[0]&0xFF;
                Log.d(TAG,String.format("RightBatteryNotify observer onNext is %1$d",rightBatteryValue));
                p.updateLeftBattery(rightBatteryValue);
            }
        };
    }


    public static Observer<Observable<byte[]>> getNewLeftAccelerometerNotificationObserver(PostureTracker p){
        return new Observer<Observable<byte[]>>() {
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
                        p.updateLeftAccelometer(accX,accY,accZ);
                    }
                });
            }
        };
    }

    public static Observer<Observable<byte[]>> getNewRightAccelerometerNotificationObserver(PostureTracker postureTracker){
        return new Observer<Observable<byte[]>>() {
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
                        postureTracker.updateRightAccelometer(accX,accY,accZ);
                    }
                });
            }
        };
    }

}
