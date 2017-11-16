package com.example.fadi.testingrx.ui;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fadi.testingrx.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.*;


public class ManDownActivity extends AppCompatActivity {

    String TAG="ManDown";
    Disposable timerDisposable;

    TextView textViewMandownCounter;

    ConstraintLayout constraintLayoutSOSNow;
    ConstraintLayout constraintLayoutIAmOk;

    Toolbar toolbarManDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_man_down);

        initUI();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_mandown_activity);
        toolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        toolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarMandown);
        //progressBar.setMin(0);
        progressBar.setMax(30);
        progressBar.setProgress(0);



        //fitChart.setValue(80f);


        timerDisposable=Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .take(30)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                            Log.d(TAG,"one second passed, it is:"+aLong);
                            //fitChart.setValue(aLong);
                            //values.add(new FitChartValue(aLong,R.color.colorPrimaryDark));
                            //fitChart.setValues(values);
                    progressBar.incrementProgressBy(1);
                    textViewMandownCounter.setText(String.valueOf(30-aLong));

                        },
                        t -> {
                            Log.e(TAG, "error from TimerObserver:" + t.toString());
                        },
                        () -> {
                            Log.e(TAG, " scan timer Observer received onCompleted()");
                            sendSOSAction();
                        });
    }

    private void initUI(){
        textViewMandownCounter = (TextView) findViewById(R.id.tv_mandown_counter);

        constraintLayoutSOSNow = (ConstraintLayout) findViewById(R.id.constraintLayoutNeedHelpNow);
        constraintLayoutSOSNow.setOnClickListener(v -> {
            Log.d(TAG,"sos was pressed");
            sendSOSAction();
        });

        constraintLayoutIAmOk = (ConstraintLayout) findViewById(R.id.constraintLayoutIAMOK);
        constraintLayoutIAmOk.setOnClickListener(v->{
            Log.d(TAG,"I am OK");
            IAmOk();
        });
    }

    private void sendSOSAction(){
        if (!timerDisposable.isDisposed()){
            timerDisposable.dispose();        }
        textViewMandownCounter.setText("SOS sent");
        constraintLayoutIAmOk.setClickable(false);
    }

    private void IAmOk(){
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_launcher, menu);
            //menu.findItem(R.id.menuLauncherResetMacAddresses).setVisible(false);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }



}
