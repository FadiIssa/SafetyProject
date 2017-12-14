package com.example.fadi.testingrx.f.ai;

/**
 * Created by fadi on 13/12/2017.
 */

public class TrainingDataManager {

    static TrainingDataManager trainingDataManagerInstance;

    private TrainingDataManager(){

    }

    public static TrainingDataManager getInstance(){
        return trainingDataManagerInstance==null ? new TrainingDataManager():trainingDataManagerInstance;
    }

    void addPostureSample(PostureTrainingSample pts){

    }
}
