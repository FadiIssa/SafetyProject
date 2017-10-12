package com.example.fadi.testingrx.ui.uvex;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.ui.view.GaugeView;

import org.w3c.dom.Text;

import java.util.Random;

/**
 * Created by fadi on 05/10/2017.
 */

public class FragmentVibration extends Fragment {

    Random myRandom;
    public FragmentVibration() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_vibration, container, false);

        Bundle mBundle=getArguments();


        int vibrationIntensityValue = mBundle.getInt(DataProcessing.VIBRATION_INTENSITY,0);

        int vibrationDurationValue = mBundle.getInt(DataProcessing.VIBRATION_DURATION,0);
        String vibrationDurationText=convertSecond(vibrationDurationValue);

        ((TextView)rootView.findViewById(R.id.tv_vibration_time_value)).setText(vibrationDurationText);

        if (vibrationDurationValue>=10){
            try {
                ((GaugeView)rootView.findViewById(R.id.gauge_view_vibration_intensity_level)).setProgress(vibrationIntensityValue);
                ((TextView)rootView.findViewById(R.id.tv_intensity_value)).setText(vibrationIntensityValue > 33? "Level 2" : "Level 1");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else if (vibrationDurationValue>=2){
            try {
                ((GaugeView)rootView.findViewById(R.id.gauge_view_vibration_intensity_level)).setProgress(vibrationIntensityValue);
                ((TextView)rootView.findViewById(R.id.tv_intensity_value)).setText("Level 1");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            try {
                ((GaugeView)rootView.findViewById(R.id.gauge_view_vibration_intensity_level)).setProgress(vibrationIntensityValue);
                ((TextView)rootView.findViewById(R.id.tv_intensity_value)).setText("Not enough Vibration Detected");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return rootView;
    }

    private String convertSecond(int seconds){
        int timeInHours=seconds/3600;
        int timeInMinutes=(seconds%3600)/60;
        int timeInSeconds=(seconds%3600)%60;
        return (String.valueOf(timeInHours)+":"+String.valueOf(timeInMinutes)+":"+String.valueOf(timeInSeconds));
    }

}

