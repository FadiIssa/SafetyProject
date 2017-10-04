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

        String walkingText=String.format("steps are:%d",mBundle.getInt(DataProcessing.NUM_STEPS,2));

        ((TextView)rootView.findViewById(R.id.tv_walking_value)).setText(walkingText);

        return rootView;
    }
}
