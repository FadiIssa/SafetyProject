package com.example.fadi.testingrx.data;

/**
 * Created by fadi on 04/10/2017.
 */

public interface DataProcessing {

    static final String NUM_STEPS="num_steps";
    static final String NUM_STAIRS="num_stairs";
    static final String DURATION_CROUCHING="dur_crouching";
    static final String DURATION_KNEELING="dur_kneeling";
    static final String DURATION_TIPTOES="dur_tiptpes";
    static final String DURATION_WALKING="dur_walking";
    static final String DURATION_STATIC="dur_static";
    static final String CALORIES="calories";
    static final String DISTANCE_METERS="distance_meters";

    SessionData getSessionData(int day, int month, int year);
}