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


    private SessionData(int nSteps, int nStairs, int dWalking, int dStatic, int dCrouching, int dKneeling, int dTiptoes, int nCalories, int distance){
        this.numSteps=nSteps;
        this.numStairs=nStairs;
        this.durationWalking=dWalking;
        this.durationStatic=dStatic;
        this.durationCrouching=dCrouching;
        this.durationKneeling=dKneeling;
        this.durationTiptoes=dTiptoes;
        this.calories=nCalories;
        this.distanceMeters=distance;
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
            return this;}

        public Builder setDistanceMeters(int n){
            this.distanceMeters = n;
            return this;}

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
                    this.distanceMeters
            );
        }
    }





}
