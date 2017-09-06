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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApp", "onCreate() is called");

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/pacifico.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // make the unique bleclient
        rxBleClient = RxBleClient.create(getApplicationContext());

        //initialize ble manager
        bleManager = new BleManager(rxBleClient);
    }

    public static BleManager getBleManager(){
        return bleManager;
    }

    public static RxBleClient getRxBleClient(){
        return rxBleClient;
    }

}
