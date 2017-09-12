package com.example.fadi.testingrx;

import android.app.Application;
import android.util.Log;

import com.example.fadi.testingrx.f.ble.BleManager;
import com.polidea.rxandroidble.RxBleClient;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by fadi on 01/09/2017.
 */

public class MyApplication extends Application{

    static BleManager bleManager;// one bleManager for the whole application (it is not strict by the library, but it makes sense and it is logical).

    // bluetooth client
    // one instance in the whole app lifecycle// maybe this variable should be in a service.
    static RxBleClient rxBleClient;// maybe I should experiment with making it static.

    String TAG = "MyApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() is called");

        Log.d(TAG,"setting the Font for the whole application");
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/HELR45W.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // make the unique bleclient on the whole application level.
        Log.d(TAG,"creating the unique instance of RxBleClient");
        rxBleClient = RxBleClient.create(getApplicationContext());

        //initialize ble manager
        Log.d(TAG,"creating the BleManager instance, to be used throughout the whole application");
        bleManager = new BleManager();
    }

    public static BleManager getBleManager(){
        return bleManager;
    }

    public static RxBleClient getRxBleClient(){
        return rxBleClient;
    }

}
