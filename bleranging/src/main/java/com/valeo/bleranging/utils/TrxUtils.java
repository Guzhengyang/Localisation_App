package com.valeo.bleranging.utils;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
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
     * @param modeLong the long mode of average calculation
     * @param modeShort the short mode of average calculation
     * @param trxLeft the left trx
     * @param trxMiddle the middle trx
     * @param trxRight the right trx
     * @param trxBack the back trx
     * @return the delta between the average(L&S) of trx s average
     */
    public static int getAverageLSDelta(int modeLong, int modeShort, Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack) {
        int numberAntennaActive = 0;
        int averageLong = 0;
        int averageShort = 0;
        if(trxLeft.isActive()) {
            averageLong += trxLeft.getTrxRssiAverage(modeLong);
            averageShort += trxLeft.getTrxRssiAverage(modeShort);
            numberAntennaActive++;
        }
        if(trxMiddle.isActive()) {
            averageLong += trxMiddle.getTrxRssiAverage(modeLong);
            averageShort += trxMiddle.getTrxRssiAverage(modeShort);
            numberAntennaActive++;
        }
        if(trxRight.isActive()) {
            averageLong += trxRight.getTrxRssiAverage(modeLong);
            averageShort += trxRight.getTrxRssiAverage(modeShort);
            numberAntennaActive++;
        }
        if(trxBack.isActive()) {
            averageLong += trxBack.getTrxRssiAverage(modeLong);
            averageShort += trxBack.getTrxRssiAverage(modeShort);
            numberAntennaActive++;
        }
        if(numberAntennaActive > 0) {
            averageLong /= numberAntennaActive;
            averageShort /= numberAntennaActive;
        }
        return (averageLong - averageShort);
    }

    /**
     * Calculate all TRX delta of (L&S) average and compare with thresold
     * @param trxLeft the first trx
     * @param trxMiddle the second trx
     * @param trxRight the third trx
     * @param trxBack the fourth trx
     * @param threshold the threshold to compare with
     * @return true if the difference of the two average rssi is greater than the threshold, false otherwise
     */
    public static boolean getAverageLSDeltaGreaterThanThreshold(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, int threshold) {
        return getAverageLSDelta(Antenna.AVERAGE_LONG, Antenna.AVERAGE_SHORT, trxLeft, trxMiddle, trxRight, trxBack) > threshold;
    }

    /**
     * Calculate all TRX delta of (L&S) average and compare with thresold
     * @param trxLeft the first trx
     * @param trxMiddle the second trx
     * @param trxRight the third trx
     * @param trxBack the fourth trx
     * @param threshold the threshold to compare with
     * @return true if the difference of the two average rssi is lower than the threshold, false otherwise
     */
    public static boolean getAverageLSDeltaLowerThanThreshold(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, int threshold) {
        return getAverageLSDelta(Antenna.AVERAGE_LONG, Antenna.AVERAGE_SHORT, trxLeft, trxMiddle, trxRight, trxBack) < threshold;
    }
//    public static boolean getEcartTypeLowerThanThreshold(Trx trx, int threshold) {
//        boolean antenna1 = (trx.getAntenna1().getEcartType() < threshold);
//        boolean antenna2 = (trx.getAntenna2().getEcartType() < threshold);
//        return antenna1 && antenna2;
//    }

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
    private static boolean numberOfTrxValid(int mode, boolean trxL, boolean trxM, boolean trxR, boolean trxB) {
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
     * @return true if the strategy is verified, false otherwise
     */
    public static boolean startStrategy(boolean newLockStatus, Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, boolean smartphoneIsInPocket) {
        boolean isInStartArea = isInStartArea(trxLeft, trxMiddle, trxRight, trxBack, getCurrentStartThreshold(SdkPreferencesHelper.getInstance().getStartThreshold(), smartphoneIsInPocket));
        return (isInStartArea
//                && (getEcartTypeLowerThanThreshold(trxMiddle, SdkPreferencesHelper.getInstance().getEcartTypeThreshold()))
                && (!newLockStatus)
                && (getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, trxLeft, trxRight, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_START, trxLeft, trxRight, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold()))
                && (getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, trxMiddle, trxLeft, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax())
                || getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, trxMiddle, trxRight, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax()))
                && (getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, trxMiddle, trxLeft, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin())
                && getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, trxMiddle, trxRight, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin()))
        );
    }

    /**
     * Check if we are in start area
     * @return true if we are in start area, false otherwise
     */
    public static boolean isInStartArea(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, int threshold) {
        boolean trxL = false;
        boolean trxM = false;
        boolean trxR = false;
        boolean trxB = false;
        if(trxLeft.isActive()) {
            trxL = trxLeft.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        }
        if(trxMiddle.isActive()) {
            trxM = trxMiddle.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        }
        if(trxRight.isActive()) {
            trxR = trxRight.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        }
        if(trxBack.isActive()) {
            trxB = trxBack.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        }
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getStartMode(), trxL, trxM, trxR, trxB);
    }

    /**
     * Condition to enable unlock action
     * @return true if the strategy is verified, false otherwise
     */
    public static int unlockStrategy(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, boolean smartphoneIsInPocket) {
        boolean isInUnlockArea = isInUnlockArea(trxLeft, trxMiddle, trxRight, trxBack, getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getUnlockThreshold(), smartphoneIsInPocket));
        boolean isNextToDoorLRMax = getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxRight, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLRMin = getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxRight, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLBMax = getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxBack, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorLBMin = getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxBack, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isNextToDoorRBMax = getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, trxRight, trxBack, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorRBMin = getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, trxRight, trxBack, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
//        boolean isApproaching = isApproaching(trxLeft, trxMiddle, trxRight, trxBack);
        boolean isApproaching = getAverageLSDeltaLowerThanThreshold(trxLeft, trxMiddle, trxRight, trxBack, getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(), smartphoneIsInPocket));
        if(trxLeft.isActive() && trxRight.isActive()) {
            if(trxBack.isActive()) {
                if(isInUnlockArea && (isNextToDoorLRMax && isNextToDoorLBMax) && isApproaching) {
                    return Trx.NUMBER_TRX_LEFT;
                } else if(isInUnlockArea && (isNextToDoorLRMin && isNextToDoorRBMax) && isApproaching) {
                    return Trx.NUMBER_TRX_RIGHT;
                } else if(isInUnlockArea && (isNextToDoorLBMin && isNextToDoorRBMin) && isApproaching) {
                    return Trx.NUMBER_TRX_BACK;
                }
            } else {
                if(isInUnlockArea && isNextToDoorLRMax && isApproaching) {
                    return Trx.NUMBER_TRX_LEFT;
                } else if(isInUnlockArea && isNextToDoorLRMin && isApproaching) {
                    return Trx.NUMBER_TRX_RIGHT;
                }
            }
        }
        return 0;
    }

    /**
     * Check if we are in unlock area
     * @return true if we are in unlock area, false otherwise
     */
    public static boolean isInUnlockArea(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, int threshold) {
        boolean trxL = false;
        boolean trxM = false;
        boolean trxR = false;
        boolean trxB = false;
        if(trxLeft.isActive()) {
            trxL = trxLeft.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        }
        if(trxMiddle.isActive()) {
            trxM = trxMiddle.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        }
        if(trxRight.isActive()) {
            trxR = trxRight.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        }
        if(trxBack.isActive()) {
            trxB = trxBack.trxGreaterThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        }
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getUnlockMode(), trxL, trxM, trxR, trxB);
    }

    /**
     * Condition to enable lock action
     * @return true if the strategy is verified, false otherwise
     */
    public static boolean lockStrategy(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, boolean smartphoneIsInPocket) {
        boolean isInLockArea = isInLockArea(trxLeft, trxMiddle, trxRight, trxBack, getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getLockThreshold(), smartphoneIsInPocket));
        boolean isLeaving = getAverageLSDeltaGreaterThanThreshold(trxLeft, trxMiddle, trxRight, trxBack, getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(), smartphoneIsInPocket));
//        boolean isLeaving = isLeaving(trxLeft, trxMiddle, trxRight, trxBack);
        return (isInLockArea && isLeaving);
//        return ((!newLockStatus && isInLockArea)
//                || (isLeaving && isInLockArea && !newLockStatus));
    }

    /**
     * Check if we are in lock area
     * @return true if we are in lock area, false otherwise
     */
    public static boolean isInLockArea(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack, int threshold) {
        boolean trxL = false;
        boolean trxM = false;
        boolean trxR = false;
        boolean trxB = false;
        if(trxLeft.isActive()) {
            trxL = trxLeft.trxLowerThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        }
        if(trxMiddle.isActive()) {
            trxM = trxMiddle.trxLowerThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        }
        if(trxRight.isActive()) {
            trxR = trxRight.trxLowerThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        }
        if(trxBack.isActive()) {
            trxB = trxBack.trxLowerThanThreshold(Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        }
        return (numberOfTrxValid(SdkPreferencesHelper.getInstance().getLockMode(), trxL, trxM, trxR, trxB)
                || (trxM && (getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_LOCK, trxLeft, trxRight, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_LOCK, trxLeft, trxRight, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())) ));
    }

    /**
     * Condition to enable welcome action
     * @param totalAverage the total average of all antenna rssi
     * @return true if the strategy is verified, false otherwise
     */
    public static boolean welcomeStrategy(boolean newlockStatus, int totalAverage, boolean smartphoneIsInPocket) {
        int threshold = getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getWelcomeThreshold(), smartphoneIsInPocket);
        return (totalAverage >= threshold) && newlockStatus;
    }

//    public static boolean isApproaching(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack) {
//        int approachingThreshold = 7;
//        int averageApproachingPenteAllTrx = (trxLeft.getPenteApproximationValue()
//                + trxMiddle.getPenteApproximationValue() + trxRight.getPenteApproximationValue()
//                + trxBack.getPenteApproximationValue()) / 4;
//        return averageApproachingPenteAllTrx > approachingThreshold;
//    }
//
//    public static boolean isLeaving(Trx trxLeft, Trx trxMiddle, Trx trxRight, Trx trxBack) {
//        int leavingThreshold = -7;
//        int averageLeavingPenteAllTrx = (trxLeft.getPenteApproximationValue()
//                + trxMiddle.getPenteApproximationValue() + trxRight.getPenteApproximationValue()
//                + trxBack.getPenteApproximationValue()) / 4;
//        return averageLeavingPenteAllTrx < leavingThreshold;
//    }

    /**
     * Create the string to write in the log file and add it
     */
    public static void appendRssiLogs(int rssiLeft, int rssiMiddle1, int rssiMiddle2, int rssiRight, int rssiBack,
                                      boolean isSmartphoneInPocket, boolean isSmartphoneLaid, boolean isPassiveEntryAction, boolean isLockStatusChangedTimerExpired,
                                      boolean rearmLock, boolean rearmUnlock, boolean rearmWelcome, boolean lockStatus,
                                      byte welcomeByte, byte lockByte, byte startByte,
                                      byte leftAreaByte, byte rightAreaByte, byte backAreaByte,
                                      byte walkAwayByte, byte steadyByte, byte approachByte,
                                      byte leftTurnByte, byte fullTurnByte, byte rightTurnByte, byte recordByte,
                                      boolean lockFromTrx, boolean lockToSend, boolean startAllowed) {
        StringBuilder log = new StringBuilder();
        log.append(rssiLeft).append(";").append(rssiMiddle1).append(";").append(rssiMiddle2).append(";").append(rssiRight).append(";").append(rssiBack).append(";");
        log.append(isSmartphoneInPocket).append(";").append(isSmartphoneLaid).append(";").append(isPassiveEntryAction).append(";").append(isLockStatusChangedTimerExpired).append(";");
        log.append(rearmLock).append(";").append(rearmUnlock).append(";").append(rearmWelcome).append(";").append(lockStatus).append(";");
        log.append(welcomeByte).append(";").append(lockByte).append(";").append(startByte).append(";");
        log.append(leftAreaByte).append(";").append(rightAreaByte).append(";").append(backAreaByte).append(";");
        log.append(walkAwayByte).append(";").append(steadyByte).append(";").append(approachByte).append(";");
        log.append(leftTurnByte).append(";").append(fullTurnByte).append(";").append(rightTurnByte).append(";");
        log.append(recordByte).append(";");
        log.append(lockFromTrx).append(";").append(lockToSend).append(";").append(startAllowed).append(";");
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
                String ColNames = "TIMESTAMP;RSSI LEFT;RSSI MIDDLE1;RSSI MIDDLE2;RSSI RIGHT;RSSI BACK;IN POCKET;IS LAID;IS PEPS;IS LOCK STATUS CHANGED TIMER;REARM LOCK;REARM UNLOCK;REARM WELCOME;IS LOCK;WELCOME FLAG;LOCK FLAG;START FLAG;LEFT AREA FLAG; RIGHT AREA FLAG; BACK AREA FLAG;WALK AWAY FLAG;STEADY FLAG;APPROACH FLAG; LEFT TURN FLAG; FULL TURN FLAG; RIGHT TURN FLAG;RECORD FLAG;LOCK FROM TRX;LOCK TO SEND;START ALLOWED;";
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
