package com.example.fadi.testingrx.f.posture;

import android.util.Log;

import com.example.fadi.testingrx.MainActivity;

/**
 * Created by fadi on 24/08/2017.
 */

public class PostureTracker {

    int latestLX;
    int latestLY;
    int latestLZ;
    int latestRX;
    int latestRY;
    int latestRZ;

    int counterCrouching;// the counter is of how many half seconds, (we consider each notification of a posture as half a second, because the interval os the bluetooth is 1 second, and since we have 2 insoles, it means if we receive 2 notifications per second, then each notification is better to be considered as half a second.
    int counterTiptoes;
    int counterKneeling;

    int counterCurrentPosition;

    int lastPosture;//this will be used to know when a specific counter should be incremented or reset.
    int currentPosture;//this will also be used to decide what counters to increase.

    CommunicationCallback caller;

    boolean isPaused;

    public PostureTracker(CommunicationCallback father){
        caller=father;

        latestLX=0;
        latestLY=0;
        latestLZ=0;
        latestRX=0;
        latestRY=0;
        latestRZ=0;

        counterCrouching=0;
        counterCurrentPosition=0;
        counterKneeling=0;
        counterTiptoes=0;

        isPaused=true;//at first, the timer should be considered as 0 , so no counting
    }

    public void processLatestAccelometerReadings(){

        int position;

        if (isPaused)
        {
            position=Postures.UNKNOWN;
            currentPosture=Postures.UNKNOWN;
            processCounters();
            Log.d("RXTesting"," postureTracker is paused");
            caller.updatePositionCallBack(position, counterCurrentPosition, counterCrouching, counterKneeling, counterTiptoes);
            return;
        }


        Log.d("RXTesting","processLatestAccelometerReading(): latest LX:"+latestLX+" LY:"+latestLY+" LZ:"+latestLZ+" RX:"+latestRX+" RY:"+latestRY+" RZ:"+latestRZ);
        if (
                (latestRZ<300 && latestLZ>800 && latestLZ<1100 && latestRY>700 && latestLY<300 && latestLY>-300)
            ||  (latestLZ<300 && latestRZ>800 && latestRZ<1100 && latestLY>700 && latestRY<300 && latestRY>-300)
                ){
            position=Postures.CROUCHING;
            currentPosture=Postures.CROUCHING;
            Log.d("RXTesting","position:"+position);
        } else if (latestRZ<300 && latestLZ<300 && latestRY>600 && latestLY>600){
            position=Postures.KNEELING;
            currentPosture=Postures.KNEELING;
            Log.d("RXTesting","position:"+position);
        }
        else //if (latestRZ>400 && latestRZ<700 && latestLZ>400 && latestLZ<700 && latestRY>600 && latestLY>600) {
            if (latestRZ>350 && latestRZ<900 && latestLZ>350 && latestLZ<900 && latestRY>400 && latestLY>400) {
                 position=Postures.TIPTOES;
                currentPosture=Postures.TIPTOES;
                Log.d("RXTesting","position:"+position);
        } else {
            position=Postures.UNKNOWN;
            currentPosture=Postures.UNKNOWN;
        }

        processCounters();

        Log.d("RXTesting","final position:"+position);

        caller.updatePositionCallBack(position, counterCurrentPosition, counterCrouching, counterKneeling, counterTiptoes);
    }

    //this method will be called after each detection of a posture, its purpose is to maintain a reliable readings of the counter varialbes at any time, these values will be sent to update the UI whenever there is a change from this side.
    private void processCounters(){
        if (isPaused){
            return;
        }

        if (currentPosture!=lastPosture){
            counterCurrentPosition=1;//we start by assigning value 1 (represents half a second) to the current posture counter.
            lastPosture=currentPosture;
        }
        else {
            counterCurrentPosition++;
        }

        if (counterCurrentPosition>8){//it passed the legal 4 seconds limit.

            if (currentPosture == Postures.CROUCHING) {
                counterCrouching++;
            }
            if (currentPosture == Postures.KNEELING) {
                counterKneeling++;
            }
            if (currentPosture == Postures.TIPTOES) {
                counterTiptoes++;
            }
        } else if (counterCurrentPosition==8){
            if (currentPosture == Postures.CROUCHING) {
                counterCrouching+=8;
            }
            if (currentPosture == Postures.KNEELING) {
                counterKneeling+=8;
            }
            if (currentPosture == Postures.TIPTOES) {
                counterTiptoes+=8;
            }
        }
    }

    public void updateRightAccelometer(int x, int y, int z) {
        latestRX = x;
        latestRY = y;
        latestRZ = z;

        processLatestAccelometerReadings();
    }

    public void updateLeftAccelometer(int x, int y, int z) {
        latestLX = x;
        latestLY = y;// the old way I was inverting the sign of Y myself, right now it comes inverted from the firmware.
        latestLZ = z;

        processLatestAccelometerReadings();
    }

    public CommunicationCallback getCaller(){
        return caller;
    }

    // this could be in case the connection with one insole is lost.
    public void pauseCounting(){
        isPaused=true;
    }

    public void resumeCounting(){
        isPaused=false;
    }

    // this will be called from the normal activity mode, to allow starting and stopping several times, without counters accumulating.
    public void reset(){
        counterCrouching=0;
        counterCurrentPosition=0;
        counterKneeling=0;
        counterTiptoes=0;
    }

    public void updateLeftBattery(int value){
        caller.notifyLeftBattery(value);
    }

    public void updateRightBattery(int value){
        caller.notifyRightBattery(value);
    }
}
