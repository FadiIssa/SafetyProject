package com.example.fadi.testingrx.ui;

/**
 * Created by fadi on 06/10/2017.
 */

public interface StatsCalculaterCallback {

    void updateStatsOnUIString(String standingString,
                         String stairsString,
                         String stepsString,
                         String walkingString,
                         String vibrationString,
                         String leftAngleString,
                         String rightAngleString,
                         String distanceString,
                         String caloriesString
            );

    void updateStatsOnUIValues(int standingTime,//in seconds
                               int stairs,
                               int steps,
                               int walkingTime,
                               int vibrationTime,
                               int leftAngle,
                               int rightAngle,
                               int distanceMeters,
                               int calories
    );
}
