package com.example.fadi.testingrx.f.ble;

/**
 * Created by fadi on 22/08/2017.
 */
/* the purpose of this class is to store static properties related to our safety insoles, things like characteristic IDs, service IDs, InsoleName, etc..

 */
public class Insoles {
    //public static final String LeftInsoleMacAddress="F3:A0:56:E9:2C:AF";
    //public static final String RightInsoleMacAddress="C4:A0:47:C0:73:4E";

    public static final String CHARACTERISTIC_COMMAND="99dd0014-a80c-4f94-be5d-c66b9fba40cf";

    public static final String CHARACTERISTIC_CHUNK="99dd0004-a80c-4f94-be5d-c66b9fba40cf";

    public static final String CHARACTERISTIC_BATTERY="99dd0016-a80c-4f94-be5d-c66b9fba40cf";

    public static final String CHARACTERISTIC_ACCELEROMETER="99dd0108-a80c-4f94-be5d-c66b9fba40cf";

    public static final String CHARACTERISTIC_FIRMWARE="99dd001e-a80c-4f94-be5d-c66b9fba40cf";
}
