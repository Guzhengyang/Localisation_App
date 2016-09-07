package com.valeo.bleranging.utils;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by l-avaratha on 08/06/2016.
 */
public class TrxUtils {

    private static final int MODE_ONE_OF_FOUR = 1;
    private static final int MODE_TWO_OF_FOUR = 2;
    private static final int MODE_ALL_FOUR = 3;
    private static final int MODE_ONE_OF_THREE_WITHOUT_BACK = 4;
    private static final int MODE_TWO_OF_THREE_WITHOUT_BACK = 5;
    private static final int MODE_ALL_OF_THREE_WITHOUT_BACK = 6;
    private static final int MODE_ONE_OF_THREE_WITHOUT_MIDDLE = 7;
    private static final int MODE_TWO_OF_THREE_WITHOUT_MIDDLE = 8;
    private static final int MODE_ALL_OF_THREE_WITHOUT_MIDDLE = 9;
    private static final int MODE_ONE_OF_TWO_WITHOUT_BACK_AND_MIDDLE = 10;
    private static final int MODE_ALL_OF_TWO_WITHOUT_BACK_AND_MIDDLE = 11;
    private static final int MODE_ONLY_MIDDLE = 12;
    private static final int MODE_LEFT_SIDE = 13;
    private static final int MODE_RIGHT_SIDE = 14;
    private static final int MODE_TRUNK_SIDE = 15;
    private static final int MODE_LEFT_BACK_ANGLE = 16;
    private static final int MODE_RIGHT_BACK_ANGLE = 17;

    /**
     * Calculate Next to Door delta rssi
     * @param mode the average mode of calculation
     * @param trx1 the first trx
     * @param trx2 the second trx
     * @return the delta between both trx average rssi
     */
    public static int getRatioNextToDoor(int mode, Trx trx1, Trx trx2) {
        int trxAverageRssi1 = trx1.getTrxRssiAverage(mode);
        int trxAverageRssi2 = trx2.getTrxRssiAverage(mode);
        return trxAverageRssi1 - trxAverageRssi2;
    }

    /**
     * Calculate two TRX average RSSI and compare their difference with thresold
     * @param mode the average mode of calculation
     * @param trx1 the first trx
     * @param trx2 the second trx
     * @param threshold the threshold to compare with
     * @return true if the difference of the two average rssi is greater than the threshold, false otherwise
     */
    public static boolean getRatioNextToDoorGreaterThanThreshold(int mode, Trx trx1, Trx trx2, int threshold) {
        if(trx1.isActive() && trx2.isActive()) {
            int ratioValue = getRatioNextToDoor(mode, trx1, trx2);
            return (ratioValue > threshold);
        } else {
            return true;
        }
    }

    /**
     * Calculate two TRX average RSSI and compare their difference with thresold
     * @param mode the average mode of calculation
     * @param trx1 the first trx
     * @param trx2 the second trx
     * @param threshold the threshold to compare with
     * @return true if the difference of the two average rssi is greater than the threshold, false otherwise
     */
    public static boolean getRatioNextToDoorLowerThanThreshold(int mode, Trx trx1, Trx trx2, int threshold) {
        if(trx1.isActive() && trx2.isActive()) {
            int ratioValue = getRatioNextToDoor(mode, trx1, trx2);
            return (ratioValue < threshold);
        } else {
            return true;
        }
    }

    /**
     * Calculate the delta between the average (Long and Short) of trx 's averages
     * @param connectedCar the connected car
     * @return the delta between the average(L&S) of trx s average
     */
    public static int getAverageLSDelta(ConnectedCar connectedCar) {
        int averageLong = connectedCar.getAllTrxAverage(Antenna.AVERAGE_LONG);
        int averageShort = connectedCar.getAllTrxAverage(Antenna.AVERAGE_SHORT);
        return (averageLong - averageShort);
    }

    /**
     * Calculate all TRX delta of (L&S) average and compare with thresold
     * @param connectedCar the connected car
     * @param threshold the threshold to compare with
     * @return true if the difference of the two average rssi is greater than the threshold, false otherwise
     */
    public static boolean getAverageLSDeltaGreaterThanThreshold(ConnectedCar connectedCar, int threshold) {
        return getAverageLSDelta(connectedCar) > threshold;
    }

    /**
     * Calculate all TRX delta of (L&S) average and compare with thresold
     * @param connectedCar the connected car
     * @param threshold the threshold to compare with
     * @return true if the difference of the two average rssi is lower than the threshold, false otherwise
     */
    public static boolean getAverageLSDeltaLowerThanThreshold(ConnectedCar connectedCar, int threshold) {
        return getAverageLSDelta(connectedCar) < threshold;
    }

    /**
     *
     * Select a mode of validity and check it
     * @param mode the mode of validity to check
     * @param trxL the left trx status
     * @param trxM the middle trx status
     * @param trxR the right trx status
     * @param trxB the back trx status
     * @return true if the trx check the condition of validity of the select mode
     */
    private static boolean numberOfTrxValid(int mode, boolean trxL, boolean trxM, boolean trxR, boolean trxB,
                                            boolean trxFL, boolean trxRL, boolean trxFR, boolean trxRR) {
        switch (mode) {
            case MODE_ONE_OF_FOUR:
                return (trxL || trxM || trxR || trxB);
            case MODE_TWO_OF_FOUR:
                return (trxL && trxM) || (trxL && trxR) || (trxL && trxB) || (trxM && trxR) || (trxM && trxB) || (trxR && trxB);
            case MODE_ALL_FOUR:
                return trxL && trxM && trxR && trxB;
            case MODE_ONE_OF_THREE_WITHOUT_BACK:
                return (trxL || trxM || trxR);
            case MODE_TWO_OF_THREE_WITHOUT_BACK:
                return (trxL && trxM) || (trxL && trxR) || (trxM && trxR);
            case MODE_ALL_OF_THREE_WITHOUT_BACK:
                return trxL && trxM && trxR;
            case MODE_ONE_OF_THREE_WITHOUT_MIDDLE:
                return (trxL || trxR || trxB);
            case MODE_TWO_OF_THREE_WITHOUT_MIDDLE:
                return (trxL && trxR) || (trxL && trxB)  || (trxR && trxB);
            case MODE_ALL_OF_THREE_WITHOUT_MIDDLE:
                return trxL  && trxR && trxB;
            case MODE_ONE_OF_TWO_WITHOUT_BACK_AND_MIDDLE:
                return (trxL || trxR);
            case MODE_ALL_OF_TWO_WITHOUT_BACK_AND_MIDDLE:
                return (trxL && trxR) ;
            case MODE_ONLY_MIDDLE:
                return trxM;
            case MODE_LEFT_SIDE:
                return trxL && trxFL && trxRL;
            case MODE_RIGHT_SIDE:
                return trxR && trxFR && trxRR;
            case MODE_TRUNK_SIDE:
                return trxB && trxRL && trxRR;
            case MODE_LEFT_BACK_ANGLE:
                return trxL && trxRL && trxB;
            case MODE_RIGHT_BACK_ANGLE:
                return trxR && trxRR && trxB;
            default:
                return false;
        }
    }

//    /**
//     * Calculate the threshold to use for lock
//     * @param mode the mode of average
//     * @param smartphoneIsInPocket true if the smartphone is in the pocket, false otherwise
//     * @return the threshold with an offset if the smartphone is in a pocket
//     */
//    public static int getCurrentThreshold(int mode, boolean smartphoneIsInPocket) {
//        int threshold = 0;
//        switch (mode) {
//            case Antenna.AVERAGE_START:
//                threshold = SdkPreferencesHelper.getInstance().getStartThreshold();
//                if(smartphoneIsInPocket) {
//                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForStart();
//                }
//                break;
//            case Antenna.AVERAGE_LOCK:
//                threshold = SdkPreferencesHelper.getInstance().getLockThreshold();
//                if(smartphoneIsInPocket) {
//                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForLock();
//                }
//                break;
//            case Antenna.AVERAGE_UNLOCK:
//                threshold = SdkPreferencesHelper.getInstance().getUnlockThreshold();
//                if(smartphoneIsInPocket) {
//                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock();
//                }
//                break;
//            case Antenna.AVERAGE_WELCOME:
//                threshold = SdkPreferencesHelper.getInstance().getWelcomeThreshold();
//                if(smartphoneIsInPocket) {
//                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForWelcome();
//                }
//                break;
//        }
//        return threshold;
//    }

    /**
     * Calculate the threshold to use for lock
     * @param threshold the threshold to analyze
     * @param smartphoneIsInPocket true if the smartphone is in the pocket, false otherwise
     * @return the threshold with an offset if the smartphone is in a pocket
     */
    public static int getCurrentLockThreshold(int threshold, boolean smartphoneIsInPocket) {
        if(smartphoneIsInPocket) {
            threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForLock();
        }
        return threshold;
    }

    /**
     * Calculate the threshold to use for unlock
     * @param threshold the threshold to analyze
     * @param smartphoneIsInPocket true if the smartphone is in the pocket, false otherwise
     * @return the threshold with an offset if the smartphone is in a pocket
     */
    public static int getCurrentUnlockThreshold(int threshold, boolean smartphoneIsInPocket) {
        if(smartphoneIsInPocket) {
            threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock();
        }
        return threshold;
    }

    /**
     * Calculate the threshold to use for start
     * @param threshold the threshold to analyze
     * @param smartphoneIsInPocket true if the smartphone is in the pocket, false otherwise
     * @return the threshold with an offset if the smartphone is in a pocket for start
     */
    public static int getCurrentStartThreshold(int threshold, boolean smartphoneIsInPocket) {
        if(smartphoneIsInPocket) {
            threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForStart();
        }
        return threshold;
    }

    /**
     * Condition to enable Start action
     * @param connectedCar the connected car
     * @param newLockStatus the car lock status
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public static boolean startStrategy(ConnectedCar connectedCar, boolean newLockStatus, boolean smartphoneIsInPocket) {
        boolean isInStartArea = isInStartArea(connectedCar, getCurrentStartThreshold(SdkPreferencesHelper.getInstance().getStartThreshold(), smartphoneIsInPocket));
        return (isInStartArea
                && (!newLockStatus)
                && (connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || connectedCar.isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold()))
                && (connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax())
                || connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax()))
                && (connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin())
                && connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin()))
        );
    }

    /**
     * Check if we are in start area
     * @return true if we are in start area, false otherwise
     */
    public static boolean isInStartArea(ConnectedCar connectedCar, int threshold) {
        boolean trxL = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxM = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxR = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxB = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxFL = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxRL = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxFR = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxRR = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getStartMode(), trxL, trxM, trxR, trxB, trxFL, trxRL, trxFR, trxRR);
    }

    /**
     * Condition to enable unlock action
     * @param connectedCar the connected car
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public static int unlockStrategy(ConnectedCar connectedCar, boolean smartphoneIsInPocket) {
        boolean isInUnlockArea = isInUnlockArea(connectedCar, getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getUnlockThreshold(), smartphoneIsInPocket));
        boolean isNextToDoorLRMax = connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLRMin = connectedCar.isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLBMax = connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorLBMin = connectedCar.isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isNextToDoorRBMax = connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorRBMin = connectedCar.isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isApproaching = getAverageLSDeltaLowerThanThreshold(connectedCar, getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(), smartphoneIsInPocket));
        if (isInUnlockArea && (isNextToDoorLRMax && isNextToDoorLBMax) && isApproaching) {
            return ConnectedCar.NUMBER_TRX_LEFT;
        } else if (isInUnlockArea && (isNextToDoorLRMin && isNextToDoorRBMax) && isApproaching) {
            return ConnectedCar.NUMBER_TRX_RIGHT;
        } else if (isInUnlockArea && (isNextToDoorLBMin && isNextToDoorRBMin) && isApproaching) {
            return ConnectedCar.NUMBER_TRX_BACK;
        }
        return 0;
    }

    /**
     * Check if we are in unlock area
     * @return true if we are in unlock area, false otherwise
     */
    public static boolean isInUnlockArea(ConnectedCar connectedCar, int threshold) {
        boolean trxL = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxM = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxR = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxB = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxFL = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRL = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxFR = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRR = connectedCar.isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getUnlockMode(), trxL, trxM, trxR, trxB, trxFL, trxRL, trxFR, trxRR);
    }

    /**
     * Condition to enable lock action
     * @param connectedCar the connected car
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public static boolean lockStrategy(ConnectedCar connectedCar, boolean smartphoneIsInPocket) {
        boolean isInLockArea = isInLockArea(connectedCar, getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getLockThreshold(), smartphoneIsInPocket));
        boolean isLeaving = getAverageLSDeltaGreaterThanThreshold(connectedCar, getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(), smartphoneIsInPocket));
        return (isInLockArea && isLeaving);
//        return ((!newLockStatus && isInLockArea)
//                || (isLeaving && isInLockArea && !newLockStatus));
    }

    /**
     * Check if we are in lock area
     * @return true if we are in lock area, false otherwise
     */
    public static boolean isInLockArea(ConnectedCar connectedCar, int threshold) {
        boolean trxL = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxM = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxR = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxB = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxFL = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRL = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxFR = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRR = connectedCar.isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getLockMode(), trxL, trxM, trxR, trxB, trxFL, trxRL, trxFR, trxRR)
                || (trxM && (connectedCar.isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_LOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || connectedCar.isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_LOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())));
    }

    /**
     * Condition to enable welcome action
     * @param totalAverage the total average of all antenna rssi
     * @param newlockStatus the lock status
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public static boolean welcomeStrategy(int totalAverage, boolean newlockStatus, boolean smartphoneIsInPocket) {
        int threshold = getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getWelcomeThreshold(), smartphoneIsInPocket);
        return (totalAverage >= threshold) && newlockStatus;
    }

    /**
     * Create the string to write in the log file and add it
     * @param rssiLeft the left trx rssi
     * @param rssiMiddle1 the middle trx antenna 1 rssi
     * @param rssiMiddle2 the middle trx antenna 2 rssi
     * @param rssiRight the right trx rssi
     * @param rssiBack the back trx rssi
     * @param rssiFrontLeft the front left trx rssi
     * @param rssiFrontRight the front right trx rssi
     * @param rssiRearLeft the rear left trx rssi
     * @param rssiRearRight the rear right trx rssi
     * @param z the device z position azimuth
     * @param x the device x position pitch
     * @param y the device y position roll
     * @param isSmartphoneInPocket true if the smartphone is in pocket, false otherwise
     * @param isSmartphoneLaid true if the smartphone is moving, false otherwise
     * @param isPassiveEntryAction true if it is a passive entry action, false otherwise
     * @param isLockStatusChangedTimerExpired true if the lock timeout is expired, false otherwise
     * @param rearmLock true if the lock can be done, false otherwise
     * @param rearmUnlock true if the unlock can be done, false otherwise
     * @param rearmWelcome true if the welcome can be done, false otherwise
     * @param lockStatus true if the car is locked, false otherwise
     * @param welcomeByte equals 1 if welcome is activated, 0 otherwise
     * @param lockByte equals 1 if lock is activated, 0 otherwise
     * @param startByte equals 1 if start is activated, 0 otherwise
     * @param leftAreaByte equals 1 if left area is activated, 0 otherwise
     * @param rightAreaByte equals 1 if right area is activated, 0 otherwise
     * @param backAreaByte equals 1 if back area is activated, 0 otherwise
     * @param walkAwayByte equals 1 if walk away is activated, 0 otherwise
     * @param steadyByte equals 1 if steady is activated, 0 otherwise
     * @param approachByte equals 1 if approach is activated, 0 otherwise
     * @param leftTurnByte equals 1 if left turn is activated, 0 otherwise
     * @param fullTurnByte equals 1 if full turn is activated, 0 otherwise
     * @param rightTurnByte equals 1 if right turn is activated, 0 otherwise
     * @param recordByte equals 1 if record is activated, 0 otherwise
     * @param lockFromTrx lock status from trx
     * @param lockToSend lock status to send
     * @param startAllowed true if start is allowed, false otherwise
     */
    public static void appendRssiLogs(int rssiLeft, int rssiMiddle1, int rssiMiddle2, int rssiRight, int rssiBack,
                                      int rssiFrontLeft, int rssiFrontRight, int rssiRearLeft, int rssiRearRight,
                                      float z, float x, float y,
                                      boolean isSmartphoneInPocket, boolean isSmartphoneLaid, boolean isPassiveEntryAction, boolean isLockStatusChangedTimerExpired,
                                      boolean rearmLock, boolean rearmUnlock, boolean rearmWelcome, boolean lockStatus,
                                      byte welcomeByte, byte lockByte, byte startByte,
                                      byte leftAreaByte, byte rightAreaByte, byte backAreaByte,
                                      byte walkAwayByte, byte steadyByte, byte approachByte,
                                      byte leftTurnByte, byte fullTurnByte, byte rightTurnByte, byte recordByte,
                                      boolean lockFromTrx, boolean lockToSend, boolean startAllowed) {
        final String comma = ";";
        StringBuilder log = new StringBuilder();
        log.append(rssiLeft).append(comma).append(rssiMiddle1).append(comma).append(rssiMiddle2).append(comma).append(rssiRight).append(comma).append(rssiBack).append(comma);
        log.append(rssiFrontLeft).append(comma).append(rssiFrontRight).append(comma).append(rssiRearLeft).append(comma).append(rssiRearRight).append(comma);
        log.append(z).append(comma).append(x).append(comma).append(y).append(comma);
        log.append(isSmartphoneInPocket).append(comma).append(isSmartphoneLaid).append(comma).append(isPassiveEntryAction).append(comma).append(isLockStatusChangedTimerExpired).append(comma);
        log.append(rearmLock).append(comma).append(rearmUnlock).append(comma).append(rearmWelcome).append(comma).append(lockStatus).append(comma);
        log.append(welcomeByte).append(comma).append(lockByte).append(comma).append(startByte).append(comma);
        log.append(leftAreaByte).append(comma).append(rightAreaByte).append(comma).append(backAreaByte).append(comma);
        log.append(walkAwayByte).append(comma).append(steadyByte).append(comma).append(approachByte).append(comma);
        log.append(leftTurnByte).append(comma).append(fullTurnByte).append(comma).append(rightTurnByte).append(comma);
        log.append(recordByte).append(comma);
        log.append(lockFromTrx).append(comma).append(lockToSend).append(comma).append(startAllowed).append(comma);
        appendRssiLog(log.toString());
    }

    /**
     * Function used to debug and write logs into a file.
     */
    private static void appendRssiLog(String text) {
        File dir = new File("sdcard/InBlueRssi/");
        //if the folder doesn't exist
        if (!dir.exists()) {
            dir.mkdir();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk", Locale.FRANCE);
        String timestampLog = sdf.format(new Date());
        File logFile = new File("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        if (!logFile.exists()) {
            try {
                //Create file
                logFile.createNewFile();
                //Write 1st row with column names
                //BufferedWriter for performance, true to set append to file flag
                String ColNames = "TIMESTAMP;RSSI LEFT;RSSI MIDDLE1;RSSI MIDDLE2;RSSI RIGHT;RSSI BACK;RSSI FRONTLEFT;RSSI FRONTRIGHT;RSSI REARLEFT;RSSI REARRIGHT;Z AZIMUTH;X PITCH;Y ROLL;IN POCKET;IS LAID;IS PEPS;IS LOCK STATUS CHANGED TIMER;REARM LOCK;REARM UNLOCK;REARM WELCOME;IS LOCK;WELCOME FLAG;LOCK FLAG;START FLAG;LEFT AREA FLAG; RIGHT AREA FLAG; BACK AREA FLAG;WALK AWAY FLAG;STEADY FLAG;APPROACH FLAG; LEFT TURN FLAG; FULL TURN FLAG; RIGHT TURN FLAG;RECORD FLAG;LOCK FROM TRX;LOCK TO SEND;START ALLOWED;";
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(ColNames);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss:SSS", Locale.FRANCE);
            String timestamp = s.format(new Date());
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(timestamp).append(";").append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
