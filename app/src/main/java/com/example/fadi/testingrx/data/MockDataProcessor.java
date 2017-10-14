package com.example.fadi.testingrx.data;

import java.text.SimpleDateFormat;
import java.util.Date;

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
                .setVibrationIntensity(44)// be careful, this method does not set datetime object
                .createSessionData();

        return result;
    }

    @Override
    public int saveSessionData(SessionData s) {
        return 0;
    }

    public SessionData getFakeSession1Data() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(System.currentTimeMillis());
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
                .setDateTime(sdf.format(resultdate))
                .createSessionData();

        return result;
    }

    public SessionData getFakeSession2Data() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(System.currentTimeMillis());

        SessionData result= new SessionData.Builder()
                .setNumSteps(250)
                .setNumStairs(30)
                .setDurationCrouching(20)
                .setDurationKneeling(21)
                .setDurationTiptoes(22)
                .setDurationStatic(23)
                .setDurationWalking(24)
                .setCalories(40)
                .setDistanceMeters(50)
                .setAngleLeft(-5)
                .setAngleRight(7)
                .setFatigue(9)
                .setVibrationDuration(43)
                .setVibrationIntensity(34)
                .setDateTime(sdf.format(resultdate))
                .createSessionData();

        return result;
    }


}
