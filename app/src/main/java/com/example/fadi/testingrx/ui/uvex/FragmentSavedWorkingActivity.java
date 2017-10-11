package com.example.fadi.testingrx.ui.uvex;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;

/**
 * Created by fadi on 03/10/2017.
 */

public class FragmentSavedWorkingActivity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_saved_working_activity,container,false);
        Bundle mBundle=getArguments();

        //String walkingText=String.format("%d",mBundle.getInt(DataProcessing.DURATION_WALKING,0));
        String walkingText=convertSecond(mBundle.getInt(DataProcessing.DURATION_WALKING,0));
        ((TextView)rootView.findViewById(R.id.tv_walking_value)).setText(walkingText);

        //String staticText = String.format("%d",mBundle.getInt(DataProcessing.DURATION_STATIC,0));
        String staticText = convertSecond(mBundle.getInt(DataProcessing.DURATION_STATIC,0));
        ((TextView)rootView.findViewById(R.id.tv_static_value)).setText(staticText);

        String stairsText = String.format("nr: %d",mBundle.getInt(DataProcessing.NUM_STAIRS),0);
        ((TextView) rootView.findViewById(R.id.tv_stairs_value)).setText(stairsText);

        String distanceText = String.format("%d meters",mBundle.getInt(DataProcessing.DISTANCE_METERS),0);
        ((TextView)rootView.findViewById(R.id.tv_total_distance_value)).setText(distanceText);

        String stepsText = String.format("%d steps",mBundle.getInt(DataProcessing.NUM_STEPS),0);
        ((TextView)rootView.findViewById(R.id.tv_total_steps_value)).setText(stepsText);

        String caloriesText = String.format("%d",mBundle.getInt(DataProcessing.CALORIES),0);
        ((TextView)rootView.findViewById(R.id.tv_calories_burned_value)).setText(caloriesText);

        return rootView;
    }

    private String convertSecond(int seconds){
        int timeInHours=seconds/3600;
        int timeInMinutes=(seconds%3600)/60;
        int timeInSeconds=(seconds%3600)%60;
        return (String.valueOf(timeInHours)+":"+String.valueOf(timeInMinutes)+":"+String.valueOf(timeInSeconds));
    }
}
