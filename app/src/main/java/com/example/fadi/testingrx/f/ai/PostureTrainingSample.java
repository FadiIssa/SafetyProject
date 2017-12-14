package com.example.fadi.testingrx.f.ai;

/**
 * Created by fadi on 13/12/2017.
 */

public class PostureTrainingSample {

    SensorsReading sensorsReading;
    String postureName;

    public PostureTrainingSample(SensorsReading sr, String postureName){
        this.sensorsReading=sr;
        this.postureName=postureName;
    }
}
