package com.valeo.bleranging.model;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Trx {
    private final int trxNumber;
    private final String trxName;
    private final Antenna antenna;
    private boolean isEnabled = false;

    public Trx(int trxNumber, String trxName) {
        this.trxNumber = trxNumber;
        this.trxName = trxName;
        this.antenna = new Antenna(trxNumber);
    }

    public void init(int historicDefaultValue) {
        antenna.init(historicDefaultValue);
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
        return antenna.getCurrentOriginalRssi();
    }

    public Antenna.BLEChannel getCurrentBLEChannel() {
        return antenna.getCurrentBLEChannel();
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
        return isEnabled && antenna.isAntennaActive();
    }

    /**
     * Save the received rssi
     *
     * @param rssi                     the rssi value
     */
    public void saveRssi(int rssi, boolean hasReceivedRssi) {
        antenna.saveRssi(rssi, hasReceivedRssi);
    }

    public void saveBleChannel(Antenna.BLEChannel bleChannel) {
        antenna.saveBleChannel(bleChannel);
    }
}
