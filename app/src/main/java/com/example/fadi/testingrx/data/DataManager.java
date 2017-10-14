package com.example.fadi.testingrx.data;

/**
 * Created by fadi on 04/10/2017.
 */

public class DataManager {

    private static final int DATA_SAVED_SUCCESSFULLY=1;
    private static final int ERROR_SAVING_DATA=2;

    DataProcessing mDataProcessor;//it is responsible for saving to database and retrieving searched information

    //change this to the other data processor based on sql lite once it is finished.
    public DataManager(){
        mDataProcessor = new MockDataProcessor();
    }

//    public SessionData getSessionData(int day, int month, int year){
//        return mDataProcessor.getSessionData(day,month,year);
//    }

    public int saveSessionData(SessionData s){
        return mDataProcessor.saveSessionData(s);
    }

    public int saveSessionData(SessionData s){
        return mDataProcessor.saveSessionData(s);
    }
}
