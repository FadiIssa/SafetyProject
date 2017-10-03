package com.example.fadi.testingrx.ui.uvex;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.fadi.testingrx.R;

public class NormalMode extends AppCompatActivity {

    ViewPager mViewPager;

    PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_mode_uvex);

        Intent receivedIntent=getIntent();
        int walkingSteps= receivedIntent.getIntExtra("walking",0);

        mViewPager = (ViewPager) findViewById(R.id.viewPagerSavedActivity);

        mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);

    }


    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {


        public MyFragmentPagerAdapter(FragmentManager fm){
            super(fm);


        }

        // so now I have to create the viewpager adapter, which should populate the fragments.
        // note that to pass the data passed with the intent, we create bundles for each child fragment, and pass the relevant data
        // then in the onCreateView method of the fragment, we set the individual values to each view.

        @Override
        public Fragment getItem(int position) {
            if (position==0) {
                // maybe I have to prepare a list of fragments in the activity when it is created.
                Fragment result = new FragmentSavedWorkingActivity();
                Bundle bundle = new Bundle();
                bundle.putInt("walking",16);
                result.setArguments(bundle);
                return result;
            } else {
                return new Fragment2();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
