package com.example.fadi.testingrx.data;

/**
 * Created by fadi on 04/10/2017.
 */

public class SessionData {

    int numSteps;
    int numStairs;
    int durationWalking;
    int durationStatic;
    int durationCrouching;
    int durationKneeling;
    int durationTiptoes;
    int calories;
    int distanceMeters;

    int angleLeft;
    int angleRight;
    int fatigueLevel;

    int vibrationDuration;
    int vibrationIntensity;

    String currentDateTime;

    int slip;

    private SessionData(int nSteps, int nStairs, int dWalking, int dStatic, int dCrouching, int dKneeling, int dTiptoes, int nCalories, int distance, int angleLeft, int angleRight, int fatigue, int vibrationTime, int vibrationIntensity, String dateTime, int slip){
        this.numSteps=nSteps;
        this.numStairs=nStairs;
        this.durationWalking=dWalking;
        this.durationStatic=dStatic;
        this.durationCrouching=dCrouching;
        this.durationKneeling=dKneeling;
        this.durationTiptoes=dTiptoes;
        this.calories=nCalories;
        this.distanceMeters=distance;
        this.angleLeft=angleLeft;
        this.angleRight= angleRight;
        this.fatigueLevel=fatigue;
        this.vibrationDuration=vibrationTime;
        this.vibrationIntensity=vibrationIntensity;
        this.currentDateTime=dateTime;
        this.slip=slip;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public int getNumStairs() {
        return numStairs;
    }

    public int getDurationWalking() {
        return durationWalking;
    }

    public int getDurationStatic() {
        return durationStatic;
    }

    public int getDurationCrouching() {
        return durationCrouching;
    }

    public int getDurationKneeling() {
        return durationKneeling;
    }

    public int getDurationTiptoes() {
        return durationTiptoes;
    }

    public int getCalories() {
        return calories;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public int getAngleLeft() {
        return angleLeft;
    }

    public int getAngleRight() {
        return angleRight;
    }

    public int getFatigueLevel() {
        return fatigueLevel;
    }

    public int getVibrationDuration() {
        return vibrationDuration;
    }

    public int getVibrationIntensity(){
        return vibrationIntensity;
    }

    public String getCurrentDateTime(){
        return currentDateTime;
    }

    public int getSlip() {return slip;}


    public static class Builder {
        int numSteps;
        int numStairs;
        int durationWalking;
        int durationStatic;
        int durationCrouching;
        int durationKneeling;
        int durationTiptoes;
        int calories;
        int distanceMeters;
        int angleLeft;
        int angleRight;
        int fatigueLevel;
        int vibrationDuration;
        int vibrationIntensity;
        String currentDateTime;
        int slip;

        public Builder setNumSteps(int n){
            this.numSteps = n;
            return this;}

        public Builder setNumStairs(int n){
            this.numStairs = n;
            return this;}

        public Builder setDurationWalking(int n){
            this.durationWalking = n;
            return this;}

        public Builder setDurationStatic(int n){
            this.durationStatic = n;
            return this;}

        public Builder setDurationCrouching(int n){
            this.durationCrouching = n;
            return this;}

        public Builder setDurationKneeling(int n){
            this.durationKneeling = n;
            return this;}

        public Builder setDurationTiptoes(int n){
            this.durationTiptoes = n;
            return this;}

        public Builder setCalories(int n){
            this.calories = n;
            return this;
        }

        public Builder setDistanceMeters(int n){
            this.distanceMeters = n;
            return this;
        }

        public Builder setAngleLeft(int l){
            this.angleLeft=l;
            return this;
        }

        public Builder setAngleRight(int r){
            this.angleRight=r;
            return this;
        }

        public Builder setFatigue(int f){
            this.fatigueLevel=f;
            return this;
        }

        public Builder setVibrationDuration(int d){
            this.vibrationDuration=d;
            return this;
        }

        public Builder setVibrationIntensity(int d){
            this.vibrationIntensity=d;
            return this;
        }

        public Builder setDateTime(String d){
            this.currentDateTime=d;
            return this;
        }

        public Builder setSlip(int s){
            this.slip=s;
            return this;
        }

        public SessionData createSessionData(){
            return new SessionData(
                    this.numSteps,
                    this.numStairs,
                    this.durationWalking,
                    this.durationStatic,
                    this.durationCrouching,
                    this.durationKneeling,
                    this.durationTiptoes,
                    this.calories,
                    this.distanceMeters,
                    this.angleLeft,
                    this.angleRight,
                    this.fatigueLevel,
                    this.vibrationDuration,
                    this.vibrationIntensity,
                    this.currentDateTime,
                    this.slip
            );
        }
    }
}
