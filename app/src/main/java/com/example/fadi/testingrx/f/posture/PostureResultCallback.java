package com.example.fadi.testingrx.f.posture;

/**
 * Created by fadi on 13/09/2017.
 * the purpose of this functional interface is to handle the results
 * received from PostureTracker, the interface should be implemented by any
 * activity that wants to deal with posture data, it is up to the activity
 * to decide whether to show these data in real time, using images, or texts, etc..
 */

// this call back will also play the role of notifying if a connection is lost. to be shown to the user in both the Mainactivity and the normalActivity classes.
public interface PostureResultCallback {
    public void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter);

    void notifyLeftConnectionDisconnected();
    void notifyRightConnectionDisconnected();

    void notifyLeftConnectionIsConnecting();
    void notifyRightConnectionIsConnecting();

    void notifyLeftConnectionConnected();
    void notifyRightConnectionConnected();
}


