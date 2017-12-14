package com.example.fadi.testingrx.f.ai;

/**
 * Created by fadi on 12/12/2017.
 */

public class SensorsReading {

    int leftAccX;
    int leftAccY;
    int leftAccZ;

    int rightAccX;
    int rightAccY;
    int rightAccZ;

    public SensorsReading(int lx, int ly, int lz, int rx, int ry, int rz){
        leftAccX=lx;
        leftAccY=ly;
        leftAccZ=lz;

        rightAccX=rx;
        rightAccY=ry;
        rightAccZ=rz;
    }

}
