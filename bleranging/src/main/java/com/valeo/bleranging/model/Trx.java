package com.valeo.bleranging.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Trx {
    private final int trxNumber;
    private final String trxName;
    private final AtomicBoolean isAntennaActive;
    private final AtomicBoolean hasReceivedRssi;
    private boolean isEnabled = false;
    private int currentOriginalRssi;
    private BLEChannel bleChannel;
    private boolean hasBeenInitialized = false;

    public Trx(int trxNumber, String trxName) {
        this.trxNumber = trxNumber;
        this.trxName = trxName;
        this.bleChannel = BLEChannel.UNKNOWN;
        this.isAntennaActive = new AtomicBoolean(true);
        this.hasReceivedRssi = new AtomicBoolean(false);
    }

    public void init(int historicDefaultValue) {
        currentOriginalRssi = historicDefaultValue;
    }

    public int getTrxNumber() {
        return trxNumber;
    }

    public String getTrxName() {
        return trxName;
    }

    /**
     * Get the current original rssi
     *
     * @return the current original rssi value.
     */
    public int getCurrentOriginalRssi() {
        return currentOriginalRssi;
    }

    public BLEChannel getCurrentBLEChannel() {
        return bleChannel;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    /**
     * Check if the trx is active
     * @return true if the trx is active, false otherwise
     */
    public boolean isActive() {
        return isEnabled && isAntennaActive();
    }

    /**
     * Save the received rssi in the antenna historic
     * @param rssi the rssi of the packet received
     * @param isRssiReceived true if the rssi has been received, false otherwise
     */
    public void saveRssi(int rssi, boolean isRssiReceived) {
        if (!hasBeenInitialized) {
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
        currentOriginalRssi = rssi;
        hasReceivedRssi.set(isRssiReceived);
    }

    /**
     * Compare a new check with the last one, if they are equals the antenna is inactive
     *
     * @return true if the antenna is active (checker are different), false otherwise (checker are equals)
     */
    private boolean isAntennaActive() {
        isAntennaActive.set(hasReceivedRssi.get());
        hasReceivedRssi.set(false);
        return isAntennaActive.get();
    }

    public void saveBleChannel(BLEChannel bleChannel) {
        this.bleChannel = bleChannel;
    }

    public enum BLEChannel {
        BLE_CHANNEL_37, BLE_CHANNEL_38, BLE_CHANNEL_39, UNKNOWN
    }
}
