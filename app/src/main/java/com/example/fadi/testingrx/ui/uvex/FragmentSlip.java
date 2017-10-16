package com.example.fadi.testingrx.ui.uvex;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.ui.view.GaugeView;

/**
 * Created by fadi on 16/10/2017.
 */

public class FragmentSlip  extends Fragment {


    public FragmentSlip() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_slip, container, false);

        Bundle mBundle = getArguments();

        try {
            ((GaugeView)rootView.findViewById(R.id.gauge_view_cushioning_intensity_level)).setProgress(90);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        ((TextView)rootView.findViewById(R.id.tv_cushioning_value)).setText("90%");

        ((TextView)rootView.findViewById(R.id.tv_slip_time_value)).setText(mBundle.getInt(DataProcessing.SLIP,0)+ " times");

        return rootView;
    }
}
