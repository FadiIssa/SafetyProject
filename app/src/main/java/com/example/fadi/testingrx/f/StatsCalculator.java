package com.example.fadi.testingrx.f;

import android.util.Log;

import com.example.fadi.testingrx.NormalModeActivity;

/**
 * Created by fadi on 14/09/2017.
 */

public class StatsCalculator {

    String TAG="Stats";

    boolean sessionStarted;

    boolean firstHalfLeftReceived;
    boolean firstHalfRightReceived;
    boolean secondHalfLeftReceived;
    boolean secondHalfRightReceived;

    int numberOfStepsLeft;
    int numberOfStepsRight;

    int numberOfStairsLeft;
    int numberOfStairsRight;

    int angleLeft;//pronation supination
    int angleRight;

    int activityTimeLeft;//it is in seconds, optimally, it should be the same as from the other insole.
    int activityTimeRight;

    int totalWalkingTimeLeft;
    int totalWalkingTimeRight;

    int standingTimeLeft;
    int standingTimeRight;

    byte firstByteVibrationLeft;
    byte firstByteVibrationRight;

    byte secondByteVibrationLeft;
    byte secondByteVibrationRight;

    NormalModeActivity caller;

    // optimally the parameter should be an interface, not a concrete object. to allow flexibility in who can construct this class.
    public StatsCalculator(NormalModeActivity n){
        caller = n;
    }
    public void reset(){
        firstHalfLeftReceived=false;
        firstHalfRightReceived=false;

        secondHalfLeftReceived=false;
        secondHalfRightReceived=false;

        sessionStarted=false;
    }


    public void startSession(){
        sessionStarted = true;
        reset();
    }

    public void processFirstHalfLeft(byte[] bytes){

        // calculating number of steps
        numberOfStepsLeft = (bytes[3] & 0xFF) | ((bytes[4]) << 8);
        Log.d(TAG, "number of steps left is:" + numberOfStepsLeft);

        // calculating number of stairs
        numberOfStairsLeft = (bytes[5] & 0xFF) | ((bytes[6]) << 8);
        Log.d(TAG, "number of stairs left is:" + numberOfStairsLeft);

        // calculating activity time
        activityTimeLeft = (bytes[7] & 0xFF) | ((bytes[8]) << 8);
        Log.d(TAG, " activity time of left is:" + activityTimeLeft);

        // calculating pronation supination angle of left insole
        angleLeft = (bytes[9] & 0xFF) | ((bytes[10]) << 8);
        Log.d(TAG, " angle of left is:" + angleLeft);

        // calculating total walking time of left insole
        totalWalkingTimeLeft = (bytes[11] & 0xFF) | ((bytes[12]) << 8);
        Log.d(TAG, " walking time of left is:" + totalWalkingTimeLeft);

        // calculating pronation supination angle of left insole
        standingTimeLeft = (bytes[13] & 0xFF) | ((bytes[14]) << 8);
        Log.d(TAG, " standing (static) time of left is:" + angleLeft);

        // storing first byte of vibration data of left insole
        firstByteVibrationLeft = bytes[15];

        firstHalfLeftReceived = true;
    }

    public void processSecondHalfLeft(byte[] bytes){
        secondByteVibrationLeft = bytes [3];

        secondHalfLeftReceived = true;
        notifyCallerIfReady();
    }

    public void processFirstHalfRight(byte[] bytes){
        // calculating number of steps
        numberOfStepsRight = (bytes[3] & 0xFF) | ((bytes[4]) << 8);
        Log.d(TAG, "number of steps right is:" + numberOfStepsRight);

        // calculating number of stairs
        numberOfStairsRight = (bytes[5] & 0xFF) | ((bytes[6]) << 8);
        Log.d(TAG, "number of stairs Right is:" + numberOfStairsRight);

        // calculating activity time
        activityTimeRight = (bytes[7] & 0xFF) | ((bytes[8]) << 8);
        Log.d(TAG, " activity time of Right is:" + activityTimeRight);

        // calculating pronation supination angle of Right insole
        angleRight = (bytes[9] & 0xFF) | ((bytes[10]) << 8);
        Log.d(TAG, " angle of Right is:" + angleRight);

        // calculating total walking time of Right insole
        totalWalkingTimeRight = (bytes[11] & 0xFF) | ((bytes[12]) << 8);
        Log.d(TAG, " walking time of Right is:" + totalWalkingTimeRight);

        // calculating pronation supination angle of left insole
        standingTimeRight = (bytes[13] & 0xFF) | ((bytes[14]) << 8);
        Log.d(TAG, " standing (static) time of right is:" + angleRight);

        // storing first byte of vibration data of right insole
        firstByteVibrationRight = bytes[15];

        firstHalfRightReceived = true;
    }

    public void processSecondHalfRight(byte[] bytes){

        secondByteVibrationRight = bytes [3];
        secondHalfRightReceived = true;

        notifyCallerIfReady();

    }

    private void notifyCallerIfReady(){
        if (secondHalfLeftReceived && secondHalfRightReceived) {
            // notify caller with all the necessary statistics data
            caller.updateStatsOnUI(String.valueOf(numberOfStepsLeft+numberOfStepsRight));
        }
    }

}
