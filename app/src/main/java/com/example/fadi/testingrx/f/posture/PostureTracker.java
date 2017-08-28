package com.example.fadi.testingrx.f.posture;

import android.util.Log;

import com.example.fadi.testingrx.MainActivity;

/**
 * Created by fadi on 24/08/2017.
 */

public class PostureTracker {

    int latestLX;
    int latestLY;
    int latestLZ;
    int latestRX;
    int latestRY;
    int latestRZ;

    MainActivity caller;

    public PostureTracker(MainActivity father){
        caller=father;

        latestLX=0;
        latestLY=0;
        latestLZ=0;
        latestRX=0;
        latestRY=0;
        latestRZ=0;
    }

    public void processLatestAccelometerReadings(){
        int position;
        Log.d("RXTesting","processLatestAccelometerReading(): latest LX:"+latestLX+" LY:"+latestLY+" LZ:"+latestLZ+" RX:"+latestRX+" RY:"+latestRY+" RZ:"+latestRZ);
        if (latestRZ<300 && latestLZ>800 && latestLZ<1100 && latestRY>700 && latestLY<300 && latestLY>-300){
            position=Postures.CROUCHING;
            Log.d("RXTesting","position:"+position);
        } else if (latestRZ<300 && latestLZ<300 && latestRY>600 && latestLY>600){
            position=Postures.KNEELING;
            Log.d("RXTesting","position:"+position);
        }
        else if (latestRZ>400 && latestRZ<700 && latestLZ>400 && latestLZ<700 && latestRY>600 && latestLY>600) {
                 position=Postures.TIPTOES;
                Log.d("RXTesting","position:"+position);
        } else {
            position=Postures.UNKNOWN;
        }

        Log.d("RXTesting","final position:"+position);

        caller.updatePositionCallBack(position);

    }

    public void updateRightAccelometer(int x, int y, int z) {
        latestRX = x;
        latestRY = y;
        latestRZ = z;

        processLatestAccelometerReadings();
    }

    public void updateLeftAccelometer(int x, int y, int z) {
        latestLX = x;
        latestLY = -1*y;
        latestLZ = z;

        processLatestAccelometerReadings();
    }
}
