package com.example.fadi.testingrx.f;

import android.os.SystemClock;
import android.util.Log;

import com.example.fadi.testingrx.NormalModeActivity;
import com.example.fadi.testingrx.ui.StatsCalculaterCallback;

import java.util.Random;

/**
 * Created by fadi on 14/09/2017.
 */

public class StatsCalculator {

    String TAG="StatsCalc";

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

    int vibrationTimeLeft;
    int vibrationTimeRight;

    int vibrationIntensity;

    byte firstByteVibrationLeft;
    byte firstByteVibrationRight;

    byte secondByteVibrationLeft;
    byte secondByteVibrationRight;

    //NormalModeActivity caller;
    StatsCalculaterCallback caller;

    // optimally the parameter should be an interface, not a concrete object. to allow flexibility in who can construct this class.
    public StatsCalculator(StatsCalculaterCallback n){
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
        angleLeft = angleLeft/10;
        Log.d(TAG, " angle of left is:" + angleLeft);

        // calculating total walking time of left insole
        totalWalkingTimeLeft = (bytes[11] & 0xFF) | ((bytes[12]) << 8);
        Log.d(TAG, " walking time of left is:" + totalWalkingTimeLeft);

        // calculating standing time angle of left insole
        standingTimeLeft = (bytes[13] & 0xFF) | ((bytes[14]) << 8);
        Log.d(TAG, " standing (static) time of left is:" + standingTimeLeft);

        // storing first byte of vibration data of left insole
        firstByteVibrationLeft = bytes[15];

        firstHalfLeftReceived = true;
        Log.d(TAG,"firstHalfLeft is received");
    }

    public void processSecondHalfLeft(byte[] bytes){
        secondByteVibrationLeft = bytes [3];

        vibrationTimeLeft = (firstByteVibrationLeft&0xFF)|((secondByteVibrationLeft) <<8);

        secondHalfLeftReceived = true;
        Log.d(TAG,"secondHalfLeft is received");
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
        angleRight= angleRight/10;
        Log.d(TAG, " angle of Right is:" + angleRight);

        // calculating total walking time of Right insole
        totalWalkingTimeRight = (bytes[11] & 0xFF) | ((bytes[12]) << 8);
        Log.d(TAG, " walking time of Right is:" + totalWalkingTimeRight);

        // calculating pronation supination angle of left insole
        standingTimeRight = (bytes[13] & 0xFF) | ((bytes[14]) << 8);
        Log.d(TAG, " standing (static) time of right is:" + standingTimeRight);

        // storing first byte of vibration data of right insole
        firstByteVibrationRight = bytes[15];

        firstHalfRightReceived = true;
        Log.d(TAG,"firstHalfRight is received");
    }

    public void processSecondHalfRight(byte[] bytes){

        secondByteVibrationRight = bytes [3];
        vibrationTimeRight = (firstByteVibrationRight&0xFF)|((secondByteVibrationRight) <<8);

        secondHalfRightReceived = true;
        Log.d(TAG,"secondHalfRigh is received");

        notifyCallerIfReady();
    }

    private void notifyCallerIfReady(){
        if (secondHalfLeftReceived && secondHalfRightReceived) {
            // notify caller with all the necessary statistics data
            String standingString= convertSecond((standingTimeLeft+standingTimeRight)/2);
            String stairsString=String.valueOf(numberOfStairsLeft+numberOfStairsRight);
            String stepsString=String.valueOf(numberOfStepsLeft+numberOfStepsRight);

            int totalWalkingTime= (totalWalkingTimeLeft+totalWalkingTimeRight)/2;
            String walkingString=convertSecond(totalWalkingTime);


            int vibrationDuration=(vibrationTimeLeft+vibrationTimeRight)/2;
            String vibrationString=convertSecond(vibrationDuration);

            //calculating vibration intensity (emulation, since it is nor ready yet in the firmware)
            Random myRandom= new Random(SystemClock.currentThreadTimeMillis());
            int randomVibrationIntensity;
            if (vibrationDuration>=10) {
                randomVibrationIntensity = myRandom.nextInt(40);
            } else if (vibrationDuration>=2){
                randomVibrationIntensity = myRandom.nextInt(20);
            } else {
                randomVibrationIntensity=1;
            }
            if (randomVibrationIntensity<0){
                randomVibrationIntensity*=-1;
            }

            if (randomVibrationIntensity!=1) {
                randomVibrationIntensity += 10;//we add 10, to 10 is the minimum value.
            }

            vibrationIntensity = randomVibrationIntensity;
            Log.d(TAG,"vibrationIntensity is:"+vibrationIntensity);

            String leftAngleString=String.valueOf(angleLeft);
            String rightAngleString=String.valueOf(angleRight);

            //calculating distance;
            int totalSteps= numberOfStepsLeft+numberOfStepsRight;
            double averageHeight = 1.7;
            int distance=(int)(totalSteps* averageHeight * 0.505);
            String distanceString = String.valueOf(distance) + " meters";

            double caloriesDouble= (((double)totalWalkingTime)/3600) * 213;
            Log.d(TAG,"caloriesDouble is:"+caloriesDouble);
            int caloriesInt = (int) caloriesDouble;
            Log.d(TAG,"caloriesInt is:"+caloriesInt);
            // 213 is the burned calories per hour for a man with weigth of 75 kilograms walking for one hour with a speed of 3 km/h

            Log.d(TAG,"total walking time is:"+totalWalkingTime+" and calories are:"+caloriesInt);

            caller.updateStatsOnUIString(standingString,
                    stairsString,
                    stepsString,
                    walkingString,
                    vibrationString,
                    leftAngleString,
                    rightAngleString,
                    distanceString,
                    String.valueOf(caloriesInt)+" Kcal"
                    );

            caller.updateStatsOnUIValues(
                    (standingTimeLeft+standingTimeRight)/2,
                    numberOfStairsLeft+numberOfStairsRight,
                    numberOfStepsLeft+numberOfStepsRight,
                    totalWalkingTime,
                    (vibrationTimeLeft+vibrationTimeRight)/2,
                    angleLeft,
                    angleRight,
                    distance,
                    caloriesInt,
                    vibrationIntensity
            );
        }
    }

    private String convertSecond(int seconds){
        int timeInHours=seconds/3600;
        int timeInMinutes=(seconds%3600)/60;
        int timeInSeconds=(seconds%3600)%60;
        return (String.valueOf(timeInHours)+":"+String.valueOf(timeInMinutes)+":"+String.valueOf(timeInSeconds));
    }
}
