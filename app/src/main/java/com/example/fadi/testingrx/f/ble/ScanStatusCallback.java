package com.example.fadi.testingrx.f.ble;

/**
 * Created by fadi on 18/09/2017.
 */

public interface ScanStatusCallback {
    void scanStatusLeftStopped();
    void scanStatusRightStopped();
    void scanStatusLeftIsScanning();
    void scanStatusRightIsScanning();
    void scanStatusLeftFound();
    void scanStatusRightFound();
    void scanStatusFinished(String leftInsoleMac, String rightInsoleMac);//it will be called when scanning for both devices has found devices.
    void scanStatusFinishedUnsuccessfully();
}
