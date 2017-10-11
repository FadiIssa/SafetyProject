package com.example.fadi.testingrx.data;

/**
 * Created by fadi on 04/10/2017.
 */

public class MockDataProcessor implements DataProcessing {
    @Override
    public SessionData getSessionData(int day, int month, int year) {
        SessionData result= new SessionData.Builder()
                .setNumSteps(150)
                .setNumStairs(20)
                .setDurationCrouching(10)
                .setDurationKneeling(11)
                .setDurationTiptoes(12)
                .setDurationStatic(13)
                .setDurationWalking(14)
                .setCalories(30)
                .setDistanceMeters(40)
                .setAngleLeft(-4)
                .setAngleRight(6)
                .setFatigue(8)
                .setVibrationDuration(33)
                .setVibrationIntensity(44)
                .createSessionData();

        return result;
    }
}
