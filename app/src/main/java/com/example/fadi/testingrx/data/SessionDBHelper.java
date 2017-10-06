package com.example.fadi.testingrx.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by fadi on 06/10/2017.
 */

public class SessionDBHelper extends SQLiteOpenHelper {

    String TAG="SDBH";

    public SessionDBHelper(Context context){
        super(context, "safety_db", null, 1);
        Log.d(TAG," constructor of SessionDBHelper is called");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate is called");
        sqLiteDatabase.execSQL(SessionContract.SessionTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "onUpgrate is called");
        sqLiteDatabase.execSQL(SessionContract.SessionTable.SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.d(TAG, "onOpen is called");
    }
}
