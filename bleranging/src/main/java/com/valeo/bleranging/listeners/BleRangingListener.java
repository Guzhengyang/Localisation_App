package com.valeo.bleranging.listeners;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface BleRangingListener {
    void showSnackBar(String message);
    void updateBLEStatus();
    void askBleOn();
    void doWelcome();
}