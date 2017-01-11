package com.valeo.bleranging.model;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Antenna {
    private final AtomicBoolean isAntennaActive;
    private final AtomicBoolean hasReceivedRssi;
    private final ArrayList<Integer> rssiHistoric;
    private final int numberTrx;
    private BLEChannel bleChannel;
    private int lastOriginalRssi;
    private int currentOriginalRssi;
    private boolean hasBeenInitialized = false;

    Antenna(int numberTrx) {
        this.numberTrx = numberTrx;
        this.bleChannel = BLEChannel.UNKNOWN;
        this.isAntennaActive = new AtomicBoolean(true);
        this.hasReceivedRssi = new AtomicBoolean(false);
        this.rssiHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getRollingAvElement());
    }

    public void init(int historicDefaultValue) {
        initWithHysteresis(historicDefaultValue);
    }

    /**
     * Init with Hysteresis over all saved rssi
     */
    private synchronized void initWithHysteresis(int defaultValue) {
        for (int i = 0; i < SdkPreferencesHelper.getInstance().getRollingAvElement(); i++) {
            rssiHistoric.add(i, defaultValue);
        }
    }

    public void saveBleChannel(BLEChannel bleChannel) {
        this.bleChannel = bleChannel;
    }

    public BLEChannel getCurrentBLEChannel() {
        return bleChannel;
    }

    /**
     * Save the received rssi in the antenna historic
     * @param rssi the rssi of the packet received
     */
    public synchronized void saveRssi(int rssi, boolean isRssiReceived) {
        if (!hasBeenInitialized) {
            this.lastOriginalRssi = rssi;
            this.currentOriginalRssi = rssi;
            this.hasBeenInitialized = true;
        }
        if (rssi == 0) {
            return;
        } else if (rssi < -100) {
            rssi = -101;
        } else if (rssi > -30) {
            rssi = -29;
        }
        lastOriginalRssi = currentOriginalRssi;
        currentOriginalRssi = rssi;
        hasReceivedRssi.set(isRssiReceived);
    }

    /**
     * Compare a new check with the last one, if they are equals the antenna is inactive
     * @return true if the antenna is active (checker are different), false otherwise (checker are equals)
     */
    public boolean isAntennaActive() {
        isAntennaActive.set(hasReceivedRssi.get());
        hasReceivedRssi.set(false);
        return isAntennaActive.get();
    }

    public int getCurrentOriginalRssi() {
        return currentOriginalRssi;
    }

    public enum BLEChannel {
        BLE_CHANNEL_37, BLE_CHANNEL_38, BLE_CHANNEL_39, UNKNOWN
    }

}
