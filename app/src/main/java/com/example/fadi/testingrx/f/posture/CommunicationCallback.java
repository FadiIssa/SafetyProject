package com.example.fadi.testingrx.f.posture;

import android.util.Log;

/**
 * Created by fadi on 13/09/2017.
 * the purpose of this functional interface is to handle the results
 * received from PostureTracker, the interface should be implemented by any
 * activity that wants to deal with posture data, it is up to the activity
 * to decide whether to show these data in real time, using images, or texts, etc..
 */

// this call back will also play the role of notifying if a connection is lost. to be shown to the user in both the Mainactivity and the normalActivity classes.
public interface CommunicationCallback {
    void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter);

    void notifyLeftConnectionDisconnected();
    void notifyRightConnectionDisconnected();

    void notifyLeftConnectionIsConnecting();
    void notifyRightConnectionIsConnecting();

    void notifyLeftConnectionConnected();
    void notifyRightConnectionConnected();

    void notifyLeftServiceDiscoveryCompleted();
    void notifyRightServiceDiscoveryCompleted();

    void notifyLeftBattery(int value);
    void notifyRightBattery(int value);

    //it is meant to be called in normal mode activity, no somewhere else
    default void notifyLeftFW(int value){
        Log.e("error"," this is a default method, it is better that your class provide its own implementation.");
    }

    default void notifyRightFW(int value){
        Log.e("error"," this is a default method, it is better that your class provide its own implementation.");
    }

    default void notifyLatestSensorReadings(int lx,int ly, int lz, int rx, int ry, int rz){
        Log.e("error"," this is a default method, it is better that your class provide its own implementation.");
    };
}


