package com.valeo.bleranging.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.EventListener;

/**
 * @author Francis Delaunay
 * @version 1.5
 */
public interface BluetoothManagementListener extends EventListener {
    void onPassiveEntryTry(BluetoothDevice device, int rssi, ScanResponse scanResponse);
}
