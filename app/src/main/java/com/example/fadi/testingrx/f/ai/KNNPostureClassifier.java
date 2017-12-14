package com.example.fadi.testingrx.f.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fadi on 13/12/2017.
 */

public class KNNPostureClassifier implements PostureClassifier {


    List<PostureTrainingSample> trainingSamplesList;

    public KNNPostureClassifier(List<PostureTrainingSample> samplesList){
        trainingSamplesList=samplesList;
    }

    public void setTrainingData(List<PostureTrainingSample> samplesList){
        trainingSamplesList=samplesList;
    }

    @Override
    public String getPostureName(SensorsReading sr) {
        HashMap<String,Double> distanceSumMap = new HashMap<>();
        HashMap<String,Integer> postureTypeCounterMap = new HashMap<>();
        HashMap<String, Double> distanceFromEachSetMap = new HashMap<>();

        // building the hashmaps
        for (PostureTrainingSample p:trainingSamplesList){
            if (distanceSumMap.containsKey(p.postureName)){
                double currentSumDistance = distanceSumMap.get(p.postureName);
                distanceSumMap.put(p.postureName,currentSumDistance+getDistance(sr,p.sensorsReading));
            }
            else {
                distanceSumMap.put(p.postureName,getDistance(sr,p.sensorsReading));
            }

            if (postureTypeCounterMap.containsKey(p.postureName)){
                int currentCount = postureTypeCounterMap.get(p.postureName);
                postureTypeCounterMap.put(p.postureName,currentCount+1);
            }
            else {
                postureTypeCounterMap.put(p.postureName,1);
            }
        }

        for (Map.Entry<String, Integer> pair:postureTypeCounterMap.entrySet()){
            distanceFromEachSetMap.put(pair.getKey(),distanceSumMap.get(pair.getKey())/pair.getValue());
        }

        String nameOfClosestPostureType="nan";
        double minimumDistance=100000;// a very big number to start with
        for (Map.Entry<String,Double> pair:distanceFromEachSetMap.entrySet()){
            if (pair.getValue()<minimumDistance){
                nameOfClosestPostureType=pair.getKey();
                minimumDistance=pair.getValue();
            }
        }

        return nameOfClosestPostureType;
    }

    double getDistance(SensorsReading sr1, SensorsReading sr2){
        return Math.sqrt(
                        (sr1.leftAccX - sr2.leftAccX)*(sr1.leftAccX - sr2.leftAccX) +
                        (sr1.leftAccY - sr2.leftAccY)*(sr1.leftAccY - sr2.leftAccY)+
                        (sr1.leftAccZ - sr2.leftAccZ)*(sr1.leftAccZ - sr2.leftAccZ)+
                        (sr1.rightAccX - sr2.rightAccX)*(sr1.rightAccX - sr2.rightAccX)+
                        (sr1.rightAccY - sr2.rightAccY)*(sr1.rightAccY - sr2.rightAccY)+
                        (sr1.rightAccZ - sr2.rightAccZ)*(sr1.rightAccZ - sr2.rightAccZ)
        );
    }
}
