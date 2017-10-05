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
 * A simple {@link Fragment} subclass.
 */

public class FragmentBioMec extends Fragment {


    public FragmentBioMec() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView=(ViewGroup) inflater.inflate(R.layout.fragment_fragment_bio_mec, container, false);

        Bundle mBundle=getArguments();

        String leftAngleText=String.format("%d",mBundle.getInt(DataProcessing.ANGLE_LEFT,0));
        ((TextView)rootView.findViewById(R.id.textViewAngleLeft)).setText(leftAngleText);

        String rightAngleText=String.format("%d",mBundle.getInt(DataProcessing.ANGLE_RIGHT,0));
        ((TextView)rootView.findViewById(R.id.textViewAngleRight)).setText(rightAngleText);

        return rootView;
    }
}
