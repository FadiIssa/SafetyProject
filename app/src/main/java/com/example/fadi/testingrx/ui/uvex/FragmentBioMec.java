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

        int leftAngle= mBundle.getInt(DataProcessing.ANGLE_LEFT,0);

        String leftAngleText=String.format("%d",leftAngle);
        if (leftAngle<-2){
            ((TextView) rootView.findViewById(R.id.textViewLeftAngleType)).setText("Supination");
            //leftAngleText = leftAngleText + " supination";
        }
        else if (leftAngle>4){
            //leftAngleText = leftAngleText + " pronation";
            ((TextView) rootView.findViewById(R.id.textViewLeftAngleType)).setText("Pronation");
        } else {
            ((TextView) rootView.findViewById(R.id.textViewLeftAngleType)).setText("Universal");
        }

        ((TextView)rootView.findViewById(R.id.textViewAngleLeft)).setText(leftAngleText);


        int rightAngle = mBundle.getInt(DataProcessing.ANGLE_RIGHT,0);
        String rightAngleText=String.format("%d",rightAngle);

        if (rightAngle<-2){
            //rightAngleText = rightAngleText + " supination";
            ((TextView) rootView.findViewById(R.id.textViewRightAngleType)).setText("Supination");
        } else if (rightAngle>4){
            //rightAngleText= rightAngleText + " pronation";
            ((TextView) rootView.findViewById(R.id.textViewRightAngleType)).setText("Pronation");
        } else {
            ((TextView) rootView.findViewById(R.id.textViewRightAngleType)).setText("Universal");
        }

        ((TextView)rootView.findViewById(R.id.textViewAngleRight)).setText(rightAngleText);

        return rootView;
    }
}
