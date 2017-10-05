package com.example.fadi.testingrx.ui.uvex;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fadi.testingrx.R;

/**
 * Created by fadi on 05/10/2017.
 */

public class FragmentFatigue extends Fragment {


    public FragmentFatigue() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup viewGroup= (ViewGroup) inflater.inflate(R.layout.fragment_fatigue, container, false);

        return viewGroup;
    }

}

