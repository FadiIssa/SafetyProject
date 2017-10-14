package com.example.fadi.testingrx.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.fadi.testingrx.R;
import com.example.fadi.testingrx.data.DataProcessing;
import com.example.fadi.testingrx.data.SessionContract;
import com.example.fadi.testingrx.data.SessionDBHelper;
import com.example.fadi.testingrx.data.SessionData;
import com.example.fadi.testingrx.ui.uvex.SessionStatsActivity;

import java.util.Random;

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
        setContentView(R.layout.activity_saved_activities_browser);

        initUI();

        //drop old database for the sake of it


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

        //myCursor.moveToNext();

        //Log.d(TAG," duration static is:"+myCursor.getInt(myCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_STATIC)));


        String fromColumns[]={SessionContract.SessionTable.COLUMN_NAME_DURATION_STATIC,SessionContract.SessionTable.COLUMN_NAME_DURATION_CROUCHING};
        int toViews[]={R.id.item_static_duration,R.id.item_duration_crouching};
        mSimpleCursorAdapter = new SimpleCursorAdapter(this,R.layout.layout_saved_ctivity_item,myCursor,fromColumns,toViews,0);

        mListView.setAdapter(mSimpleCursorAdapter);
        myDBHelper.close();
    }

    private void initUI(){
        mListView = (ListView) findViewById(R.id.list_view_saved_activities);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)view.findViewById(R.id.item_static_duration)).setText("fadi");
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

//            int numSteps=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_NUM_STEPS));
//            Log.d(TAG,"populate: nSteps="+numSteps);
//            intent.putExtra(DataProcessing.NUM_STEPS,numSteps);

            //intent.putExtra(DataProcessing.NUM_STAIRS,sessionData.getNumStairs());
            int durationCrouching=mCursor.getInt(mCursor.getColumnIndex(SessionContract.SessionTable.COLUMN_NAME_DURATION_CROUCHING));
            intent.putExtra(DataProcessing.DURATION_CROUCHING,durationCrouching);

//            intent.putExtra(DataProcessing.DURATION_KNEELING,sessionData.getDurationKneeling());
//            intent.putExtra(DataProcessing.DURATION_TIPTOES,sessionData.getDurationTiptoes());
//            intent.putExtra(DataProcessing.DURATION_WALKING,sessionData.getDurationWalking());
//            intent.putExtra(DataProcessing.DURATION_STATIC,sessionData.getDurationStatic());
//            intent.putExtra(DataProcessing.CALORIES,sessionData.getCalories());
//            intent.putExtra(DataProcessing.DISTANCE_METERS,sessionData.getDistanceMeters());
//            intent.putExtra(DataProcessing.ANGLE_LEFT,sessionData.getAngleLeft());
//            intent.putExtra(DataProcessing.ANGLE_RIGHT,sessionData.getAngleRight());
//            intent.putExtra(DataProcessing.FATIGUE,sessionData.getFatigueLevel());
//            intent.putExtra(DataProcessing.VIBRATION_DURATION,sessionData.getVibrationDuration());
//            intent.putExtra(DataProcessing.VIBRATION_INTENSITY,randomVibrationIntensity);

            }
    }
}
