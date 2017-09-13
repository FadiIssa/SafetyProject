package com.example.fadi.testingrx.f.posture;

/**
 * Created by fadi on 13/09/2017.
 * the purpose of this functional interface is to handle the results
 * received from PostureTracker, the interface should be implemented by any
 * activity that wants to deal with posture data, it is up to the activity
 * to decide whether to show these data in real time, using images, or texts, etc..
 */

public interface PostureResultCallback {
    public void updatePositionCallBack(final int i, final int currentPosCounter, final int crouchingCounter, final int kneelingCounter, final int tiptoesCounter);
}
