package com.valeo.bleranging.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.bluetooth.scanresponse.CentralScanResponse;

import java.util.EventListener;

/**
 * @author Francis Delaunay
 * @version 1.5
 */
interface BluetoothManagementListener extends EventListener {
    void onCentralScanResponseCatch(BluetoothDevice device, CentralScanResponse centralScanResponse);

    void onBeaconScanResponseCatch(BluetoothDevice device, int rssi, BeaconScanResponse beaconScanResponse, byte[] advertisedData);
}
