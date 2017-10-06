package com.example.fadi.testingrx.ui.uvex;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;

/**
 * Created by fadi on 05/10/2017.
 */

public class FragmentVibration extends Fragment {


    public FragmentVibration() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_vibration, container, false);

        Bundle mBundle=getArguments();

        String vibrationDurationText=String.format("%d",mBundle.getInt(DataProcessing.VIBRATION_DURATION,-1));
        ((TextView)rootView.findViewById(R.id.tv_vibration_time_value)).setText(vibrationDurationText);

        return rootView;
    }

}

