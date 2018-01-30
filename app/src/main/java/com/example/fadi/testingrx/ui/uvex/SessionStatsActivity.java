package com.example.fadi.testingrx.ui.uvex;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.fadi.testingrx.FragmentHealthTips;
import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionDBHelper;
import com.example.fadi.testingrx.ui.SavedActivitiesBrowserActivity;

import java.util.Random;

import java.util.Random;

/**
 * Created by fadi on 04/10/2017.
 * this activity is supposed to show the saved safety activity session details,
 * the activity will receive the stats in the intent that will start it,
 * otherwise there is nothing to show.then this activity will pass the different stats
 * to its different fragments, managed by the viewpager and fragmentAdapter.
 */

public class SessionStatsActivity extends AppCompatActivity {

    String TAG = "SSAct";

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
    int angleLeft;
    int angleRight;
    int fatigue;
    int vibrationDuration;
    int vibrationIntensity;
    int slip;

    SessionDBHelper myDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stats_browser);

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
        this.angleLeft=receivedIntent.getIntExtra(DataProcessing.ANGLE_LEFT,-1);
        this.angleRight=receivedIntent.getIntExtra(DataProcessing.ANGLE_RIGHT,-1);
        this.fatigue=receivedIntent.getIntExtra(DataProcessing.FATIGUE,-1);
        this.vibrationDuration=receivedIntent.getIntExtra(DataProcessing.VIBRATION_DURATION,-1);

        this.vibrationIntensity=receivedIntent.getIntExtra(DataProcessing.VIBRATION_INTENSITY,-2);
        this.slip=receivedIntent.getIntExtra(DataProcessing.SLIP,0);

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

        tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(5);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_slip);
        }

        tab = ((TabLayout) findViewById(R.id.tab_layout)).getTabAt(6);
        if (tab != null) {
            tab.setIcon(R.drawable.icon_tab_healthtips);
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
                    Bundle bundle1 = new Bundle();
                    bundle1.putInt(DataProcessing.DURATION_WALKING,durationWalking);
                    bundle1.putInt(DataProcessing.DURATION_STATIC,durationStatic);
                    bundle1.putInt(DataProcessing.NUM_STAIRS,numStairs);
                    bundle1.putInt(DataProcessing.DISTANCE_METERS,ditanceInMeters);
                    bundle1.putInt(DataProcessing.NUM_STEPS,numSteps);
                    bundle1.putInt(DataProcessing.CALORIES,calories);
                    result1.setArguments(bundle1);
                    return result1;
                case 1:
                    Fragment result2 = new FragmentBioMec();
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt(DataProcessing.ANGLE_LEFT,angleLeft);
                    bundle2.putInt(DataProcessing.ANGLE_RIGHT,angleRight);
                    result2.setArguments(bundle2);
                    return result2;
                case 2:
                    Fragment result3 = new FragmentVibration();
                    Bundle bundle3 = new Bundle();
                    bundle3.putInt(DataProcessing.VIBRATION_DURATION,vibrationDuration);
                    bundle3.putInt(DataProcessing.VIBRATION_INTENSITY,vibrationIntensity);
                    result3.setArguments(bundle3);
                    return result3;
                case 3:
                    Fragment result4 = new FragmentFatigue();
                    Bundle bundle4 = new Bundle();
                    bundle4.putInt(DataProcessing.FATIGUE,fatigue);
                    result4.setArguments(bundle4);
                    return result4;
                case 4:
                    Fragment result5 = new FragmentPostures();
                    Bundle bundle5= new Bundle();
                    bundle5.putInt(DataProcessing.DURATION_CROUCHING,durationCrouching);
                    bundle5.putInt(DataProcessing.DURATION_KNEELING,durationKneeling);
                    bundle5.putInt(DataProcessing.DURATION_TIPTOES,durationTiptoes);
                    bundle5.putInt(DataProcessing.DURATION_STATIC,durationStatic);
                    result5.setArguments(bundle5);
                    return result5;
                case 5:
                    Fragment result6 = new FragmentSlip();
                    Bundle bundle6= new Bundle();
                    bundle6.putInt(DataProcessing.SLIP,slip);
                    result6.setArguments(bundle6);
                    return result6;
                case 6:
                    Fragment result7 = new FragmentHealthTips();
                    Bundle bundle7 = new Bundle();
                    bundle7.putInt(DataProcessing.DURATION_CROUCHING,durationCrouching);
                    bundle7.putInt(DataProcessing.DURATION_KNEELING,durationKneeling);
                    bundle7.putInt(DataProcessing.DURATION_TIPTOES,durationTiptoes);
                    bundle7.putInt(DataProcessing.DURATION_STATIC,durationStatic);
                    bundle7.putInt(DataProcessing.VIBRATION_DURATION,vibrationDuration);
                    result7.setArguments(bundle7);
                    return result7;
                default:
                    return new Fragment2();

            }

        }

        @Override
        public int getCount() {
            return 7;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_session_stats, menu);
            //menu.findItem(R.id.menuLauncherResetMacAddresses).setVisible(false);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //test how the db looks like
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy is called");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_session_stats_history:
                Intent intent = new Intent(this, SavedActivitiesBrowserActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

