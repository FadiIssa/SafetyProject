package com.example.fadi.testingrx;


import android.content.Context;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LauncherActivity extends AppCompatActivity {
    Button rtButton;
    Button normalModeButton;//this mode is the one Karim suggested. to hide what is in real time and what is sent after an activity.
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initUI();

        //scanAndPairing();
        MyApplication.getBleManager().scanAndPair(()->{
            rtButton.setEnabled(true);
            normalModeButton.setEnabled(true);
        });
    }

    private void initUI(){
        rtButton = (Button) findViewById(R.id.buttonRT);
        rtButton.setEnabled(false);

        normalModeButton = (Button) findViewById(R.id.buttonNormalMode);
        normalModeButton.setEnabled(false);

        RxView.clicks(rtButton)
                .subscribe(a-> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                });

        RxView.clicks(normalModeButton)
                .subscribe(a->{
                    Intent intent = new Intent(this, NormalModeActivity.class);
                    startActivity(intent);
                });

    }
}
