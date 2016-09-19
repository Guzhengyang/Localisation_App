package com.valeo.bleranging.utils;

import android.util.Log;

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
    private static final File logFile = new File(SdkPreferencesHelper.getInstance().getLogFileName());

    /**
     * Calculate Next to Door delta rssi
     * @param mode the average mode of calculation
     * @param trx1 the first trx
     * @param trx2 the second trx
     * @return the delta between both trx average rssi
     */
    public static int getRatioNearDoor(int mode, Trx trx1, Trx trx2) {
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
    public static boolean getRatioNearDoorGreaterThanThreshold(int mode, Trx trx1, Trx trx2, int threshold) {
        if(trx1.isActive() && trx2.isActive()) {
            int ratioValue = getRatioNearDoor(mode, trx1, trx2);
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
    public static boolean getRatioNearDoorLowerThanThreshold(int mode, Trx trx1, Trx trx2, int threshold) {
        if(trx1.isActive() && trx2.isActive()) {
            int ratioValue = getRatioNearDoor(mode, trx1, trx2);
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
            threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForLock(SdkPreferencesHelper.getInstance().getConnectedCarType());
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
            threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock(SdkPreferencesHelper.getInstance().getConnectedCarType());
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
            threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForStart(SdkPreferencesHelper.getInstance().getConnectedCarType());
        }
        return threshold;
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
     * @param rangingPredictionInt prediction from random forest algorithm
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
                                      byte leftTurnByte, byte fullTurnByte, byte rightTurnByte, byte recordByte, int rangingPredictionInt,
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
        log.append(recordByte).append(comma).append(rangingPredictionInt).append(comma);
        log.append(lockFromTrx).append(comma).append(lockToSend).append(comma).append(startAllowed).append(comma);
        appendRssiLog(log.toString());
    }

    /**
     * Function used to debug and write logs into a file.
     */
    private static void appendRssiLog(String text) {
        Log.d("log", text);
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
