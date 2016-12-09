package com.valeo.bleranging.model;

import com.valeo.bleranging.utils.TrxUtils;

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
     * Calculate the TRX average RSSI
     *
     * @param mode      the trx average mode
     * @return the average of both antenna's rssi
     */
    public int getAntennaRssiAverage(int mode) {
        return antenna.getAntennaRssiAverage(mode);
    }

    /**
     * Get the current original rssi
     *
     * @return the current original rssi value.
     */
    public int getCurrentOriginalRssi() {
        return antenna.getCurrentOriginalRssi();
    }

    /**
     * Get the current modified rssi
     *
     * @return the current modified rssi value.
     */
    public int getCurrentModifiedRssi() {
        return antenna.getCurrentModifiedRssi();
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
     * Calculate the TRX RSSI and compare with thresold
     * @param averageMode the mode of average
     * @param threshold the threshold to compare with
     * @param isGreater true if compare sign is >, false if it is <
     * @return true if the rssi of each antenna is greater than the threshold, false otherwise
     */
    public boolean compareTrxWithThreshold(int averageMode, int threshold, boolean isGreater) {
        return isEnabled() && TrxUtils.compareWithThreshold(
                antenna.getAntennaRssiAverage(averageMode), threshold, isGreater);
    }

    /**
     * Compare checker then set isAntennaActive and newChecker
     */
    public void compareCheckerAndSetAntennaActive() {
        antenna.compareCheckerAndSetAntennaActive();
    }

    /**
     * Reset Hysteresis in every antenna
     */
    public synchronized void resetWithHysteresis(int defaultValue) {
        antenna.resetWithHysteresis(defaultValue);
    }

    /**
     * Save the received rssi
     *
     * @param rssi                     the rssi value
     * @param smartphoneIsMovingSlowly the boolean which determines if the smartphone is moving or not
     */
    public void saveRssi(int rssi, boolean smartphoneIsMovingSlowly,
                         boolean hasReceivedRssi) {
        antenna.saveRssi(rssi, smartphoneIsMovingSlowly, hasReceivedRssi);
    }

    /**
     * Get the offset for channel 38
     *
     * @return the offset for channel 38
     */
    public int getOffset38() {
        return antenna.getOffsetBleChannel38();
    }

    /**
     * Get the offset for channel 39
     *
     * @return the offset for channel 39
     */
    public int getOffset39() {
        return antenna.getOffsetBleChannel39();
    }

    /**
     * Get antenna ratio max min
     *
     * @return the ratio max min over 50 last rssi received
     */
    public int getRatioMaxMin() {
        return antenna.getRatioMaxMin();
    }

    public int getMin() {
        return antenna.getMin();
    }

    public int getMax() {
        return antenna.getMax();
    }

    public int getRssiIncrease() {
        return antenna.getRssiIncrease();
    }

    public void resetRssiIncrease() {
        antenna.resetRssiIncrease();
    }

    public void resetRssiDecrease() {
        antenna.resetRssiDecrease();
    }

    public int getRssiDecrease() {
        return antenna.getRssiDecrease();
    }

    public void saveBleChannel(Antenna.BLEChannel bleChannel) {
        antenna.saveBleChannel(bleChannel);
    }
}
