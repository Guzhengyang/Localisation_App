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

    Antenna(int trxNumber, int antennaId) {
        this.trxNumber = trxNumber;
        this.antennaId = antennaId;
        this.bleChannel = Antenna.BLEChannel.UNKNOWN;
        this.isAntennaActive = new AtomicBoolean(true);
        this.hasReceivedRssi = new AtomicBoolean(false);
    }

    /**
     * Compare a new check with the last one, if they are equals the trx antenna is inactive
     *
     * @return true if the trx antenna is active (checker are different), false otherwise (checker are equals)
     */
    boolean isActive() {
        isAntennaActive.set(hasReceivedRssi.get());
        hasReceivedRssi.set(false);
        return isAntennaActive.get();
    }

    /**
     * Save the received rssi in the antenna historic
     *
     * @param rssi           the rssi of the packet received
     * @param isRssiReceived true if the rssi has been received, false otherwise
     * @param bleChannel     ble channel of the packet received
     */
    synchronized void saveRssi(int rssi, boolean isRssiReceived, Antenna.BLEChannel bleChannel) {
        if (!hasBeenInitialized) {
            this.currentOriginalRssi = rssi;
            this.bleChannel = bleChannel;
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
        this.bleChannel = bleChannel;
        hasReceivedRssi.set(isRssiReceived);
    }

    /**
     * Get antenna id
     *
     * @return the antenna id
     */
    int getAntennaId() {
        return antennaId;
    }

    /**
     * Get the current original rssi
     *
     * @return the current original rssi value.
     */
    int getCurrentOriginalRssi() {
        return currentOriginalRssi;
    }

    /**
     * Get the current ble channel
     *
     * @return the current ble channel
     */
    Antenna.BLEChannel getCurrentBLEChannel() {
        return bleChannel;
    }

    @Override
    public String toString() {
        return "Antenna{" +
                "antennaId=" + antennaId +
                ", trxNumber=" + trxNumber +
                ", isAntennaActive=" + isAntennaActive +
                ", currentOriginalRssi=" + currentOriginalRssi +
                ", bleChannel=" + bleChannel +
                '}';
    }

    /**
     * The antenna advertising channel
     */
    public enum BLEChannel {
        BLE_CHANNEL_37, BLE_CHANNEL_38, BLE_CHANNEL_39, UNKNOWN
    }
}
