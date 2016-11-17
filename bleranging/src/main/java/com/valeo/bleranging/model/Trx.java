package com.valeo.bleranging.model;

import com.valeo.bleranging.utils.TrxUtils;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class Trx {
    public static final int ANTENNA_AND = 2;
    public final static int ANTENNA_ID_0 = 0;
    public final static int ANTENNA_ID_1 = 4;
    public final static int ANTENNA_ID_2 = 8;
    private static final int ANTENNA_OR = 1;
    private final int trxNumber;
    private final String trxName;
    private final Antenna antenna1;
    private final Antenna antenna2;
    private boolean isEnabled = false;

    public Trx(int trxNumber, String trxName) {
        this.trxNumber = trxNumber;
        this.trxName = trxName;
        this.antenna1 = new Antenna(trxNumber, ANTENNA_ID_1);
        this.antenna2 = new Antenna(trxNumber, ANTENNA_ID_2);
    }

    public void init(int historicDefaultValue) {
        antenna1.init(historicDefaultValue);
        antenna2.init(historicDefaultValue);
    }

    public int getTrxNumber() {
        return trxNumber;
    }

    public String getTrxName() {
        return trxName;
    }

    /**
     * Calculate the TRX average RSSI
     * @param mode the trx average mode
     * @return the average of both antenna's rssi
     */
    public int getTrxRssiAverage(int mode) {
        return (int) -(Math.sqrt(antenna1.getAntennaRssiAverage(mode) * antenna2.getAntennaRssiAverage(mode)));
    }

    /**
     * Calculate the TRX average RSSI
     *
     * @param antennaId the antenna id
     * @param mode      the trx average mode
     * @return the average of both antenna's rssi
     */
    public int getAntennaRssiAverage(int antennaId, int mode) {
        switch (antennaId) {
            case ANTENNA_ID_0:
                return getTrxRssiAverage(mode);
            case ANTENNA_ID_1:
                return antenna1.getAntennaRssiAverage(mode);
            case ANTENNA_ID_2:
                return antenna2.getAntennaRssiAverage(mode);
            default:
                return getTrxRssiAverage(mode);
        }
    }

    /**
     * Get the current original rssi
     *
     * @param antennaId the antenna id that received the rssi
     * @return the current original rssi value for id 1 or 2. Antenna id 0 will return antenna 1 current rssi
     */
    public int getCurrentOriginalRssi(int antennaId) {
        switch (antennaId) {
            case ANTENNA_ID_0:
                return antenna1.getCurrentOriginalRssi();
            case ANTENNA_ID_1:
                return antenna1.getCurrentOriginalRssi();
            case ANTENNA_ID_2:
                return antenna2.getCurrentOriginalRssi();
            default:
                return antenna1.getCurrentOriginalRssi();
        }
    }

    /**
     * Get the current modified rssi
     *
     * @param antennaId the antenna id that received the rssi
     * @return the current modified rssi value for id 1 or 2. Antenna id 0 will return antenna 1 current rssi
     */
    public int getCurrentModifiedRssi(int antennaId) {
        switch (antennaId) {
            case ANTENNA_ID_0:
                return antenna1.getCurrentModifiedRssi();
            case ANTENNA_ID_1:
                return antenna1.getCurrentModifiedRssi();
            case ANTENNA_ID_2:
                return antenna2.getCurrentModifiedRssi();
            default:
                return antenna1.getCurrentModifiedRssi();
        }
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
        return isEnabled && (antenna1.isAntennaActive() && antenna2.isAntennaActive());
    }

    /**
     * Calculate the TRX RSSI and compare with thresold
     * @param mode the mode to use to compare with threshold (&& or ||)
     * @param averageMode the mode of average
     * @param threshold the threshold to compare with
     * @param isGreater true if compare sign is >, false if it is <
     * @return true if the rssi of each antenna is greater than the threshold, false otherwise
     */
    public boolean compareTrxWithThreshold(int mode, int averageMode, int threshold, boolean isGreater) {
        if(isActive()) {
            switch (mode) {
                case ANTENNA_OR:
                    return TrxUtils.compareWithThreshold(antenna1.getAntennaRssiAverage(averageMode), threshold, isGreater) ||
                            TrxUtils.compareWithThreshold(antenna2.getAntennaRssiAverage(averageMode), threshold, isGreater);
                case ANTENNA_AND:
                    return TrxUtils.compareWithThreshold(antenna1.getAntennaRssiAverage(averageMode), threshold, isGreater) &&
                            TrxUtils.compareWithThreshold(antenna2.getAntennaRssiAverage(averageMode), threshold, isGreater);
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Compare checker then set isAntennaActive and newChecker
     */
    public void compareCheckerAndSetAntennaActive() {
        antenna1.compareCheckerAndSetAntennaActive();
        antenna2.compareCheckerAndSetAntennaActive();
    }

    /**
     * Reset Hysteresis in every antenna
     */
    public synchronized void resetWithHysteresis(int defaultValue) {
        antenna1.resetWithHysteresis(defaultValue);
        antenna2.resetWithHysteresis(defaultValue);
    }

    /**
     * Save the received rssi
     *
     * @param antennaId                the antenna id that sent the rssi
     * @param rssi                     the rssi value
     * @param bleChannel               the ble channel use to send signal
     * @param smartphoneIsMovingSlowly the boolean which determines if the smartphone is moving or not
     */
    public void saveRssi(int antennaId, int rssi, Antenna.BLEChannel bleChannel, boolean smartphoneIsMovingSlowly) {
        switch (antennaId) {
            case ANTENNA_ID_0:
                antenna1.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                antenna2.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                break;
            case ANTENNA_ID_1:
                antenna1.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                if (!antenna2.isAntennaActive()) {
                    antenna2.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                }
                break;
            case ANTENNA_ID_2:
                antenna2.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                if (!antenna1.isAntennaActive()) {
                    antenna1.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                }
                break;
            default:
                antenna1.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                antenna2.saveRssi(rssi, bleChannel, smartphoneIsMovingSlowly);
                break;
        }
    }

    /**
     * Get the offset for channel 38
     *
     * @param antennaId the antenna id
     * @return the offset for channel 38
     */
    public int getOffset38(int antennaId) {
        if (antennaId == ANTENNA_ID_1) {
            return antenna1.getOffsetBleChannel38();
        } else if (antennaId == ANTENNA_ID_2) {
            return antenna2.getOffsetBleChannel38();
        } else {
            return 0;
        }
    }

    /**
     * Get antenna ratio max min
     *
     * @return the ratio max min over 50 last rssi received
     */
    public int getRatioMaxMin() {
        return antenna1.getRatioMaxMin();
    }

    public int getMin() {
        return antenna1.getMin();
    }

    public int getMax() {
        return antenna1.getMax();
    }
}
