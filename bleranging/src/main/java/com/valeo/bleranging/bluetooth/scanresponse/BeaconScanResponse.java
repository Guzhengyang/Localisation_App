package com.valeo.bleranging.bluetooth.scanresponse;

import com.valeo.bleranging.utils.TextUtils;

/**
 * Created by l-avaratha on 01/12/2016
 */

public class BeaconScanResponse {
    private final byte advertisingChannel;
    private final byte pcbaPnIndicator;
    private final byte[] softwareVersion;
    private final int dataType;
    private final byte[] data;
    private final byte batteryMeasurement;
    private final byte[] rollingCounter;
    private final byte beaconPosition;

    BeaconScanResponse(byte pcbaPnIndicator, byte[] softwareVersion, byte advertisingChannel,
                       int dataType, byte[] data, byte batteryMeasurement,
                       byte[] rollingCounter, byte beaconPosition) {
        this.pcbaPnIndicator = pcbaPnIndicator;
        this.softwareVersion = softwareVersion;
        this.advertisingChannel = advertisingChannel;
        this.dataType = dataType;
        this.data = data;
        this.batteryMeasurement = batteryMeasurement;
        this.rollingCounter = rollingCounter;
        this.beaconPosition = beaconPosition;
    }

    public byte getAdvertisingChannel() {
        return advertisingChannel;
    }

    @Override
    public String toString() {
        return "BeaconScanResponse{" +
                "pcbaPnIndicator=" + pcbaPnIndicator +
                ", softwareVersion=" + TextUtils.printBleBytes(softwareVersion) +
                ", advertisingChannel=" + advertisingChannel +
                ", dataType=" + dataType +
                ", data=" + TextUtils.printBleBytes(data) +
                ", batteryMeasurement=" + batteryMeasurement +
                ", rollingCounter=" + TextUtils.printBleBytes(rollingCounter) +
                ", beaconPosition=" + beaconPosition +
                '}';
    }
}
