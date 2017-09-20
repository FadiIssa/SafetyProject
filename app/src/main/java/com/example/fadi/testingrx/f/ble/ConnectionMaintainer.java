package com.example.fadi.testingrx.f.ble;

/**
 * Created by fadi on 20/09/2017.
 * the purpose of this class is to maintain rxBleConnection observable,
 * which means that if a connection with insole is lost, this class should
 * notify the callbacks about it, and then keep trying to connect to the
 * lost insole, and one it is connected again, it informs the callback of
 * a successful connection again.
 */

public class ConnectionMaintainer {

}
