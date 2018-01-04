package com.example.fadi.testingrx.f.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fadi on 12/12/2017.
 */

// this is the class that will be the connection point with the app
public class AiPostureManager {

    TrainingDataManager trainingDataManager;

    List<PostureTrainingSample> postureSamplesList;

    PostureClassifier postureClassifier;

    public  AiPostureManager(){
        trainingDataManager = TrainingDataManager.getInstance();
        postureSamplesList= new ArrayList<>();
    }

    public void addPostureSample(SensorsReading sr, String postureName){
        postureSamplesList.add(new PostureTrainingSample(sr,postureName));
        if (postureClassifier==null){//once we have the first training sample, then the postclassifier can start doing its job.
            postureClassifier = new KNNPostureClassifier(postureSamplesList);
        }
    }

    public String getPostureName(SensorsReading sr){
        if (postureClassifier==null){// alternatively , we can initiate the postureClassifer from the constructor
            return "No posture available";
        }
        return postureClassifier.getPostureName(sr);
    }

    public void resetPostures(){
        postureSamplesList.clear();
        postureClassifier = null;
    }


}
