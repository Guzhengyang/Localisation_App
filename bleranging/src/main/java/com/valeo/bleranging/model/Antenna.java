package com.valeo.bleranging.model;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Antenna {
    private final int antennaId;
    private final int trxNumber;
    private final AtomicBoolean isAntennaActive;
    private final AtomicBoolean hasReceivedRssi;
    private int currentOriginalRssi;
    private Antenna.BLEChannel bleChannel;
    private boolean hasBeenInitialized = false;

    public Antenna(int trxNumber, int antennaId) {
        this.trxNumber = trxNumber;
        this.antennaId = antennaId;
        this.bleChannel = Antenna.BLEChannel.UNKNOWN;
        this.isAntennaActive = new AtomicBoolean(true);
        this.hasReceivedRssi = new AtomicBoolean(false);
    }

    public void init(int historicDefaultValue) {
        currentOriginalRssi = historicDefaultValue;
    }

    /**
     * Compare a new check with the last one, if they are equals the trx antenna is inactive
     *
     * @return true if the trx antenna is active (checker are different), false otherwise (checker are equals)
     */
    public boolean isActive() {
        isAntennaActive.set(hasReceivedRssi.get());
        hasReceivedRssi.set(false);
        return isAntennaActive.get();
    }

    /**
     * Save the received rssi in the antenna historic
     *
     * @param rssi           the rssi of the packet received
     * @param isRssiReceived true if the rssi has been received, false otherwise
     */
    public synchronized void saveRssi(int rssi, boolean isRssiReceived) {
        if (!hasBeenInitialized) {
            this.currentOriginalRssi = rssi;
            this.hasBeenInitialized = true;
        }
        if (rssi == 0) {
            return;
        } else if (rssi < -100) {
            rssi = -101;
        } else if (rssi > -20) {
            rssi = -21;
        }
        currentOriginalRssi = rssi;
        hasReceivedRssi.set(isRssiReceived);
    }

    public int getAntennaId() {
        return antennaId;
    }

    /**
     * Get the current original rssi
     *
     * @return the current original rssi value.
     */
    public int getCurrentOriginalRssi() {
        return currentOriginalRssi;
    }

    public Antenna.BLEChannel getCurrentBLEChannel() {
        return bleChannel;
    }

    public void saveBleChannel(Antenna.BLEChannel bleChannel) {
        this.bleChannel = bleChannel;
    }

    public enum BLEChannel {
        BLE_CHANNEL_37, BLE_CHANNEL_38, BLE_CHANNEL_39, UNKNOWN
    }
}
