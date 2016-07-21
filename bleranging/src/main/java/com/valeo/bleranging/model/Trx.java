package com.valeo.bleranging.model;

/**
 * Created by l-avaratha on 08/06/2016.
 */
public class Trx {
    public static final int ANTENNA_OR = 1;
    public static final int ANTENNA_AND = 2;
    public static final int NUMBER_TRX_LEFT = 1;
    public static final int NUMBER_TRX_MIDDLE = 2;
    public static final int NUMBER_TRX_RIGHT = 3;
    public static final int NUMBER_TRX_BACK = 4;
    public final static int ANTENNA_ID_0 = 0;
    public final static int ANTENNA_ID_1 = 4;
    public final static int ANTENNA_ID_2 = 8;
    private final Antenna antenna1;
    private final Antenna antenna2;

    public Trx(int numberTrx, int historicDefaultValue) {
        this.antenna1 = new Antenna(numberTrx, ANTENNA_ID_1, historicDefaultValue);
        this.antenna2 = new Antenna(numberTrx, ANTENNA_ID_2, historicDefaultValue);
    }

    public Antenna getAntenna1() {
        return antenna1;
    }

    public Antenna getAntenna2() {
        return antenna2;
    }

    /**
     * Calculate the TRX average RSSI
     * @return the average of both antenna's rssi
     */
    public int getTrxRssiAverage(int mode) {
        return (int) -(Math.sqrt(antenna1.getAntennaRssiAverage(mode) * antenna2.getAntennaRssiAverage(mode)));
    }

    /**
     * Check if the trx is active
     * @return true if the trx is active, false otherwise
     */
    public boolean isActive() {
        return (antenna1.isAntennaActive() && antenna2.isAntennaActive());
    }

    /**
     * Calculate the TRX RSSI and compare with thresold
     * @param mode the mode to use to compare with threshold (&& or ||)
     * @param threshold the threshold to compare with
     * @return true if the rssi of each antenna is greater than the threshold, false otherwise
     */
    public boolean trxGreaterThanThreshold(int mode, int averageMode, int threshold) {
        if(isActive()) {
            switch (mode) {
                case ANTENNA_OR:
                    return ((antenna1.getAntennaRssiAverage(averageMode) > threshold) || (antenna2.getAntennaRssiAverage(averageMode) > threshold));
                case ANTENNA_AND:
                    return ((antenna1.getAntennaRssiAverage(averageMode) > threshold) && (antenna2.getAntennaRssiAverage(averageMode) > threshold));
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Calculate the TRX RSSI and compare with thresold
     * @param mode the mode to use to compare with threshold (&& or ||)
     * @param threshold the threshold to compare with
     * @return true if the rssi of each antenna is lower than the threshold, false otherwise
     */
    public boolean trxLowerThanThreshold(int mode, int averageMode, int threshold) {
        if(isActive()) {
            switch (mode) {
                case ANTENNA_OR:
                    return ((antenna1.getAntennaRssiAverage(averageMode) < threshold) || (antenna2.getAntennaRssiAverage(averageMode) < threshold));
                case ANTENNA_AND:
                    return ((antenna1.getAntennaRssiAverage(averageMode) < threshold) && (antenna2.getAntennaRssiAverage(averageMode) < threshold));
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

//    /**
//     * Calculate the TRX pente of RSSI and compare with thresold
//     * @param mode the mode to use to compare with threshold (&& or ||)
//     * @param threshold the threshold to compare with
//     * @return true if the pente of rssi of each antenna is greater than the threshold, false otherwise
//     */
//    public boolean trxPenteGreaterThanThreshold(int mode, int threshold) {
//        if(isActive()) {
//            switch (mode) {
//                case ANTENNA_OR:
//                    return ((antenna1.getPenteApproximationValue() > threshold) || (antenna2.getPenteApproximationValue() > threshold));
//                case ANTENNA_AND:
//                    return ((antenna1.getPenteApproximationValue() > threshold) && (antenna2.getPenteApproximationValue() > threshold));
//                default:
//                    return false;
//            }
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Calculate the TRX pente of RSSI and compare with thresold
//     * @param mode the mode to use to compare with threshold (&& or ||)
//     * @param threshold the threshold to compare with
//     * @return true if the pente of rssi of each antenna is greater than the threshold, false otherwise
//     */
//    public boolean trxPenteLowerThanThreshold(int mode, int threshold) {
//        if(isActive()) {
//            switch (mode) {
//                case ANTENNA_OR:
//                    return ((antenna1.getPenteApproximationValue() < threshold) || (antenna2.getPenteApproximationValue() < threshold));
//                case ANTENNA_AND:
//                    return ((antenna1.getPenteApproximationValue() < threshold) && (antenna2.getPenteApproximationValue() < threshold));
//                default:
//                    return false;
//            }
//        } else {
//            return false;
//        }
//    }

//    /**
//     * Calculate the offsetBleChannel37 and offsetBleChannel39 comparing to channel38 of each antenna
//     */
//    public void calculateChannelOffsets() {
//        antenna1.calculateChannelOffsets();
//        antenna2.calculateChannelOffsets();
//    }

    /**
     * Compare checker then set isAntennaActive and newChecker
     */
    public void compareCheckerAndSetAntennaActive() {
        antenna1.compareCheckerAndSetAntennaActive();
        antenna2.compareCheckerAndSetAntennaActive();
    }

    public int getPenteApproximationValue() {
        return (antenna1.getPenteApproximationValue() + antenna2.getPenteApproximationValue()) / 2;
    }

    /**
     * Reset Hysteresis in every antenna
     */
    public void resetWithHysteresis(int defaultValue) {
        antenna1.resetWithHysteresis(defaultValue);
        antenna2.resetWithHysteresis(defaultValue);
    }

}
