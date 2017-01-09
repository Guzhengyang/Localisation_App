package com.valeo.bleranging.model;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Antenna {
    public static final int AVERAGE_DEFAULT = 0;
    public static final int AVERAGE_START = 1;
    public static final int AVERAGE_LOCK = 2;
    public static final int AVERAGE_UNLOCK = 3;
    public static final int AVERAGE_WELCOME = 4;
    public static final int AVERAGE_LONG = 5;
    public static final int AVERAGE_SHORT = 6;
    public static final int AVERAGE_DELTA_LOCK = 7;   // use for threshold calculation
    public static final int AVERAGE_DELTA_UNLOCK = 8; // use for threshold calculation
    private final AtomicBoolean isAntennaActive;
    private final AtomicBoolean hasReceivedRssi;
    private final ArrayList<Integer> rssiHistoric;
    private int rssiIncrease = 0;
    private int rssiDecrease = 0;
    private BLEChannel bleChannel;
    private int lastOriginalRssi;
    private int currentOriginalRssi;
    private BLEChannel lastBleChannel;
    private boolean lastIsSmartphoneMovingSlowly;
    private int antennaRssiAverage;
    private int antennaRssiAverageStart;
    private int antennaRssiAverageLock;
    private int antennaRssiAverageUnlock;
    private int antennaRssiAverageWelcome;
    private int antennaRssiAverageLong;
    private int antennaRssiAverageShort;
    private boolean hasBeenInitialized = false;

    Antenna() {
        this.bleChannel = BLEChannel.UNKNOWN;
        this.isAntennaActive = new AtomicBoolean(true);
        this.hasReceivedRssi = new AtomicBoolean(false);
        this.rssiHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getRollingAvElement());
    }

    public void init(int historicDefaultValue) {
        this.antennaRssiAverage = historicDefaultValue;
        this.antennaRssiAverageStart = historicDefaultValue;
        this.antennaRssiAverageLock = historicDefaultValue;
        this.antennaRssiAverageUnlock = historicDefaultValue;
        this.antennaRssiAverageWelcome = historicDefaultValue;
        this.antennaRssiAverageLong = historicDefaultValue;
        this.antennaRssiAverageShort = historicDefaultValue;
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

    /**
     * Reset with Hysteresis over all saved rssi
     */
    synchronized void resetWithHysteresis(int defaultValue) {
        for (int i = 0; i < SdkPreferencesHelper.getInstance().getRollingAvElement(); i++) {
            rssiHistoric.set(i, defaultValue);
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
    public synchronized void saveRssi(int rssi, boolean isSmartphoneMovingSlowly, boolean isRssiReceived) {
        if (!hasBeenInitialized) {
            this.lastOriginalRssi = rssi;
            this.currentOriginalRssi = rssi;
            this.lastBleChannel = bleChannel;
            this.lastIsSmartphoneMovingSlowly = isSmartphoneMovingSlowly;
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
        this.lastBleChannel = bleChannel;
        if (lastIsSmartphoneMovingSlowly != isSmartphoneMovingSlowly) {
            lastIsSmartphoneMovingSlowly = isSmartphoneMovingSlowly;
        }
        hasReceivedRssi.set(isRssiReceived);
    }

    public void resetRssiIncrease() {
        rssiIncrease = 0;
    }

    public void resetRssiDecrease() {
        rssiDecrease = 0;
    }

    public int getRssiIncrease() {
        if (currentOriginalRssi > lastOriginalRssi) {
            rssiIncrease++;
        } else {
            rssiIncrease = 0;
        }
        return rssiIncrease;
    }

    public int getRssiDecrease() {
        if (currentOriginalRssi < lastOriginalRssi) {
            rssiDecrease++;
        } else {
            rssiDecrease = 0;
        }
        return rssiDecrease;
    }

    /**
     * Compare a new check with the last one, if they are equals the antenna is inactive
     * @return true if the antenna is active (checker are different), false otherwise (checker are equals)
     */
    public boolean isAntennaActive() {
        return isAntennaActive.get();
    }

    /**
     * Compare checker then set isAntennaActive and newChecker
     */
    public void compareCheckerAndSetAntennaActive() {
        // antenna active if antenna has received an rssi during the last 2,5 seconds
        isAntennaActive.set(hasReceivedRssi.get());
        hasReceivedRssi.set(false);
    }

    /**
     * Get the antenna rssi average for a mode
     * @param mode the mode of average
     * @return the average rssi for a mode
     */
    public int getAntennaRssiAverage(int mode) {
        switch (mode) {
            case AVERAGE_DEFAULT:
                return antennaRssiAverage;
            case AVERAGE_START:
                return antennaRssiAverageStart;
            case AVERAGE_LOCK:
                return antennaRssiAverageLock;
            case AVERAGE_UNLOCK:
                return antennaRssiAverageUnlock;
            case AVERAGE_WELCOME:
                return antennaRssiAverageWelcome;
            case AVERAGE_LONG:
                return antennaRssiAverageLong;
            case AVERAGE_SHORT:
                return antennaRssiAverageShort;
            default:
                return antennaRssiAverage;
        }
    }

    public int getCurrentOriginalRssi() {
        return currentOriginalRssi;
    }

    public enum BLEChannel {
        BLE_CHANNEL_37, BLE_CHANNEL_38, BLE_CHANNEL_39, UNKNOWN
    }

}
