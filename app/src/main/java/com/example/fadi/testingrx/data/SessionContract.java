package com.example.fadi.testingrx.data;

import android.provider.BaseColumns;

/**
 * Created by fadi on 06/10/2017.
 */

public final class SessionContract {

    private SessionContract(){
        ;//do nothing, this is a private constructor, to avoid accidently instantiating this class.
    }

    public static final class SessionTable implements BaseColumns {
        public static final String TABLE_NAME = "session";
        public static final String COLUMN_NAME_DURATION_STATIC = "d_static";
        public static final String COLUMN_NAME_DURATION_CROUCHING = "d_crouching";


        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + SessionTable.TABLE_NAME + " (" +
                        SessionTable._ID + " INTEGER PRIMARY KEY," +
                        SessionTable.COLUMN_NAME_DURATION_STATIC + " INTEGER," +
                        SessionTable.COLUMN_NAME_DURATION_CROUCHING + " INTEGER)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + SessionTable.TABLE_NAME;

    }
}
