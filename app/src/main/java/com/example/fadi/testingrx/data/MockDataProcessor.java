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
                .setNumSteps(2856)
                .setNumStairs(187)
                .setDurationCrouching(163)
                .setDurationKneeling(191)
                .setDurationTiptoes(52)
                .setDurationStatic(3857)
                .setDurationWalking(1772)
                .setCalories(93)
                .setDistanceMeters(2420)
                .setAngleLeft(-5)
                .setAngleRight(-3)
                .setFatigue(4)
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
                .setNumSteps(1765)
                .setNumStairs(140)
                .setDurationCrouching(72)
                .setDurationKneeling(47)
                .setDurationTiptoes(184)
                .setDurationStatic(2134)
                .setDurationWalking(1572)
                .setCalories(72)
                .setDistanceMeters(1402)
                .setAngleLeft(-2)
                .setAngleRight(-4)
                .setFatigue(3)
                .setVibrationDuration(74)
                .setVibrationIntensity(34)
                .setDateTime(sdf.format(resultdate))
                .createSessionData();

        return result;
    }


}
