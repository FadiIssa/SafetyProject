package com.example.fadi.testingrx;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fadi.testingrx.data.DataProcessing;


public class FragmentHealthTips extends Fragment {

    public FragmentHealthTips() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView= (ViewGroup) inflater.inflate(R.layout.fragment_health_tips, container, false);

        Bundle mBundle=getArguments();

        return rootView;
    }

}
