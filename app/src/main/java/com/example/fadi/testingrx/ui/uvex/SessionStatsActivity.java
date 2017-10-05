package com.example.fadi.testingrx.ui.uvex;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.WindowManager;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;

/**
 * Created by fadi on 04/10/2017.
 * this activity is supposed to show the saved safety activity session details,
 * the activity will receive the stats in the intent that will start it,
 * otherwise there is nothing to show.then this activity will pass the different stats
 * to its different fragments, managed by the viewpager and fragmentAdapter.
 */

public class SessionStatsActivity extends AppCompatActivity {

    ViewPager mViewPager;

    PagerAdapter mPagerAdapter;

    TabLayout mTabLayout;

    int numSteps;
    int numStairs;
    int durationWalking;
    int durationCrouching;
    int durationStatic;
    int durationKneeling;
    int durationTiptoes;
    int calories;
    int ditanceInMeters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_normal_mode_uvex);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.savedStats_toolbar);
        myToolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        myToolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));

        Intent receivedIntent=getIntent();

        this.numSteps= receivedIntent.getIntExtra(DataProcessing.NUM_STEPS,-1);
        this.numStairs= receivedIntent.getIntExtra(DataProcessing.NUM_STAIRS,-1);
        this.durationCrouching=receivedIntent.getIntExtra(DataProcessing.DURATION_CROUCHING,-1);
        this.durationKneeling=receivedIntent.getIntExtra(DataProcessing.DURATION_KNEELING,-1);
        this.durationTiptoes=receivedIntent.getIntExtra(DataProcessing.DURATION_TIPTOES,-1);
        this.durationStatic=receivedIntent.getIntExtra(DataProcessing.DURATION_STATIC,-1);
        this.durationWalking=receivedIntent.getIntExtra(DataProcessing.DURATION_WALKING,-1);
        this.calories=receivedIntent.getIntExtra(DataProcessing.CALORIES,-1);
        this.ditanceInMeters=receivedIntent.getIntExtra(DataProcessing.DISTANCE_METERS,-1);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        mViewPager = (ViewPager) findViewById(R.id.viewPagerSavedActivity);

        mTabLayout.setupWithViewPager(mViewPager);

        mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);

        // setup the tabs icons
        TabLayout.Tab tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(0);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_activity);
        }

        tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(1);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_biomec);
        }

        tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(2);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_vibration);
        }

        tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(3);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_fatigue);
        }

        tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(4);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_posture);
        }
    }


    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        // so now again, I have to write the purpose of this function
        // which is to give some impressions
        public MyFragmentPagerAdapter(FragmentManager fm){
            super(fm);
        }

        // so now I have to create the viewpager adapter, which should populate the fragments.
        // note that to pass the data passed with the intent, we create bundles for each child fragment, and pass the relevant data
        // then in the onCreateView method of the fragment, we set the individual values to each view.

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Fragment result1 = new FragmentSavedWorkingActivity();
                    Bundle bundle = new Bundle();
                    bundle.putInt(DataProcessing.NUM_STEPS,numSteps);
                    bundle.putInt(DataProcessing.NUM_STAIRS,numStairs);
                    result1.setArguments(bundle);
                    return result1;
                case 1:
                    Fragment result2 = new FragmentBioMec();
                    return result2;
                case 2:
                    Fragment result3 = new FragmentVibration();
                    return result3;
                default:
                    return new Fragment2();

            }

        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_launcher, menu);
            //menu.findItem(R.id.menuLauncherResetMacAddresses).setVisible(false);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }

    }
}

