package com.example.fadi.testingrx;

import android.app.Application;
import android.util.Log;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by fadi on 01/09/2017.
 */

public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApp", "onCreate() is called");

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/pacifico.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
