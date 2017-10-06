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

public class FragmentPostures extends Fragment {


    public FragmentPostures() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView= (ViewGroup) inflater.inflate(R.layout.fragment_postures, container, false);

        Bundle mBundle=getArguments();

        String crouchingText=String.format("%d",mBundle.getInt(DataProcessing.DURATION_CROUCHING,-1));
        ((TextView)rootView.findViewById(R.id.tv_crounching_value)).setText(crouchingText);

        String kneelingText=String.format("%d",mBundle.getInt(DataProcessing.DURATION_KNEELING,-1));
        ((TextView)rootView.findViewById(R.id.tv_kneeling_value)).setText(kneelingText);

        String standingText=String.format("%d",mBundle.getInt(DataProcessing.DURATION_STATIC,-1));
        ((TextView)rootView.findViewById(R.id.tv_standing_value)).setText(standingText);

        String tiptoesText=String.format("%d",mBundle.getInt(DataProcessing.DURATION_TIPTOES,-1));
        ((TextView)rootView.findViewById(R.id.tv_tip_toes_value)).setText(tiptoesText);

        return rootView;
    }

}