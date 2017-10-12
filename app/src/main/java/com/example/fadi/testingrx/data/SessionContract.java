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
        public static final String COLUMN_NAME_DURATION_WALKING = "d_walking";
        public static final String COLUMN_NAME_DURATION_KNEELING = "d_kneeling";
        public static final String COLUMN_NAME_DURATION_TIPTOES = "d_tiptoes";
        public static final String COLUMN_NAME_DURATION_VIBRATION = "d_vibration";
        public static final String COLUMN_NAME_VIBRATION_INTENSITY = "vibration_intensity";
        public static final String COLUMN_NAME_ANGLE_LEFT = "angle_left";
        public static final String COLUMN_NAME_ANGLE_RIGHT = "angle_right";
        public static final String COLUMN_NAME_NUM_STEPS = "n_steps";
        public static final String COLUMN_NAME_NUM_STAIRS = "n_stairs";
        public static final String COLUMN_NAME_DISTANCE_METERS = "distance";
        public static final String COLUMN_NAME_CALORIES = "calories";
        public static final String COLUMN_NAME_DURATION_CROUCHING = "d_crouching";


        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + SessionTable.TABLE_NAME + " (" +
                        SessionTable._ID + " INTEGER PRIMARY KEY," +
                        SessionTable.COLUMN_NAME_DURATION_STATIC + " INTEGER," +
                        SessionTable.COLUMN_NAME_DURATION_WALKING + " INTEGER," +
                        SessionTable.COLUMN_NAME_DURATION_KNEELING + " INTEGER," +
                        SessionTable.COLUMN_NAME_DURATION_TIPTOES + " INTEGER," +
                        SessionTable.COLUMN_NAME_DURATION_VIBRATION + " INTEGER," +
                        SessionTable.COLUMN_NAME_ANGLE_LEFT + " INTEGER," +
                        SessionTable.COLUMN_NAME_ANGLE_RIGHT + " INTEGER," +
                        SessionTable.COLUMN_NAME_VIBRATION_INTENSITY + " INTEGER," +
                        SessionTable.COLUMN_NAME_NUM_STEPS + " INTEGER," +
                        SessionTable.COLUMN_NAME_DISTANCE_METERS + " INTEGER," +
                        SessionTable.COLUMN_NAME_CALORIES + " INTEGER," +
                        SessionTable.COLUMN_NAME_NUM_STAIRS + " INTEGER," +
                        SessionTable.COLUMN_NAME_DURATION_CROUCHING + " INTEGER)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + SessionTable.TABLE_NAME;

    }
}
