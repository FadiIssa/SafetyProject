package com.example.fadi.testingrx.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.MockDataProcessor;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionDBHelper;

import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.ui.uvex.SessionStatsActivity;


/**
 * Created by fadi on 13/10/2017.
 */

public class SavedActivitiesBrowserActivity extends AppCompatActivity {

    ListView mListView;

    static final String TAG="savedAct";

    SimpleCursorAdapter mSimpleCursorAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_saved_activities_browser);

        initUI();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.saved_activities_toolbar);
        myToolbar.setOverflowIcon(getDrawable(R.drawable.icon_settings));
        //myToolbar.setLogo(getDrawable(R.drawable.uvex_logo_launcher_bar));
        myToolbar.setNavigationIcon(getDrawable(R.drawable.menu_icon));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.action_bar_title,null));

        readFromDBAndPopulateViews();
    }

    private void initUI(){
        mListView = (ListView) findViewById(R.id.list_view_saved_activities);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //((TextView)view.findViewById(R.id.item_static_duration)).setText("fadi");
                Log.d(TAG,"i:"+i+",  l:"+l);
                //guerying the database for the specific row id
                SessionDBHelper myDBHelper = new SessionDBHelper(getApplicationContext());

                SQLiteDatabase db = myDBHelper.getReadableDatabase();
                // so again, I have to write these sql statements, just to ensure the database is working fine.

                String selection = SessionContract.SessionTable._ID + " = ?";
                String[] selectionArgs = { String.valueOf(l) };

                Cursor myCursor = db.query(
                        SessionContract.SessionTable.TABLE_NAME,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null,
                        null
                );

                //Log.d(TAG," duration static is:"+myCursor.getInt(myCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_STATIC)));

                //myDBHelper.close();
                //then getting results, making intent, then start an activity that shows the retrieved results.
                Intent intent = new Intent(getApplicationContext(), SessionStatsActivity.class);
                populateIntentWithSessionDataFromCursor(intent,myCursor);
                startActivity(intent);
            }
        });
    }

    private void populateIntentWithSessionDataFromCursor(Intent intent, Cursor mCursor){
        if (mCursor.moveToNext()){

            int numSteps=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_NUM_STEPS));
            Log.d(TAG,"populate: nSteps="+numSteps);
            intent.putExtra(DataProcessing.NUM_STEPS,numSteps);

            int numStairs=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_NUM_STAIRS));
            Log.d(TAG,"populate: nStairs="+numStairs);
            intent.putExtra(DataProcessing.NUM_STAIRS,numStairs);

            int durationCrouching=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_CROUCHING));
            Log.d(TAG,"populate: dCrouching="+durationCrouching);
            intent.putExtra(DataProcessing.DURATION_CROUCHING,durationCrouching);

            int durationKneeling=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_KNEELING));
            Log.d(TAG,"populate: dKneeling="+durationKneeling);
            intent.putExtra(DataProcessing.DURATION_KNEELING,durationKneeling);

            int durationTiptoes=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_TIPTOES));
            Log.d(TAG,"populate: dTiptpes="+durationTiptoes);
            intent.putExtra(DataProcessing.DURATION_TIPTOES,durationTiptoes);

            int durationWalking=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_WALKING));
            Log.d(TAG,"populate: dWalking="+durationWalking);
            intent.putExtra(DataProcessing.DURATION_WALKING,durationWalking);

            int durationStatic=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_STATIC));
            Log.d(TAG,"populate: dStatic="+durationStatic);
            intent.putExtra(DataProcessing.DURATION_STATIC,durationStatic);

            int calories=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_CALORIES));
            Log.d(TAG,"populate: calories="+calories);
            intent.putExtra(DataProcessing.CALORIES,calories);

            int distanceMeters=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DISTANCE_METERS));
            Log.d(TAG,"populate: distance="+distanceMeters);
            intent.putExtra(DataProcessing.DISTANCE_METERS,distanceMeters);

            int angleLeft=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_ANGLE_LEFT));
            Log.d(TAG,"populate: angleLeft="+angleLeft);
            intent.putExtra(DataProcessing.ANGLE_LEFT,angleLeft);

            int angleRight=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_ANGLE_RIGHT));
            Log.d(TAG,"populate: angleRight="+angleRight);
            intent.putExtra(DataProcessing.ANGLE_RIGHT,angleRight);

            int fatigue= mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_FATUGUE_LEVEL));
            Log.d(TAG,"populate: fatigue="+fatigue);
            intent.putExtra(DataProcessing.FATIGUE,fatigue);

            int durationVibration=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_VIBRATION));
            Log.d(TAG,"populate: durationVibration="+durationVibration);
            intent.putExtra(DataProcessing.VIBRATION_DURATION,durationVibration);

            int vibrationIntensity=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_VIBRATION_INTENSITY));
            Log.d(TAG,"populate: vibrationIntensity="+vibrationIntensity);
            intent.putExtra(DataProcessing.VIBRATION_INTENSITY,vibrationIntensity);

            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_saved_activities_browser, menu);
            return true;
        } catch (Exception e) {
            return super.onCreateOptionsMenu(menu);
        }
    }

    private void readFromDBAndPopulateViews(){
        SessionDBHelper myDBHelper = new SessionDBHelper(getApplicationContext());

        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        // so again, I have to write these sql statements, just to ensure the database is working fine.
        String[] projection = {
                SessionContract.SessionTable._ID,
                SessionContract.SessionTable.COLUMN_NAME_DURATION_STATIC,
                SessionContract.SessionTable.COLUMN_NAME_DURATION_CROUCHING
        };

        Cursor myCursor = db.query(
                SessionContract.SessionTable.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        String fromColumns[]={SessionContract.SessionTable.COLUMN_NAME_DATETIME};
        int toViews[]={R.id.item_date_time};
        mSimpleCursorAdapter = new SimpleCursorAdapter(this,R.layout.layout_saved_ctivity_item,myCursor,fromColumns,toViews,0);

        mListView.setAdapter(mSimpleCursorAdapter);
        db.close();
        myDBHelper.close();
    }

}
