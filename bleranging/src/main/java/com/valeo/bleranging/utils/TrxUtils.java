package com.valeo.bleranging.utils;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by l-avaratha on 08/06/2016
 */
public class TrxUtils {
    private static final File logFile = new File(SdkPreferencesHelper.getInstance().getLogFileName());

    /**
     * Calculate the threshold to use for lock
     *
     * @param mode                 the mode of average
     * @param smartphoneIsInPocket true if the smartphone is in the pocket, false otherwise
     * @param smartphoneComIsActivated  true if the smartphone is near ears, false otherwise
     * @return the threshold with an offset if the smartphone is in a pocket
     */
    public static int getCurrentThreshold(int mode, boolean smartphoneIsInPocket, boolean smartphoneComIsActivated) {
        int threshold;
        String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        switch (mode) {
            case Antenna.AVERAGE_START:
                threshold = SdkPreferencesHelper.getInstance().getStartThreshold(connectedCarType);
                if (smartphoneComIsActivated && smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetEarForStart(connectedCarType);
                } else if (smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForStart(connectedCarType);
                }
                break;
            case Antenna.AVERAGE_LOCK:
                threshold = SdkPreferencesHelper.getInstance().getLockThreshold(connectedCarType);
                if (smartphoneComIsActivated && smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetEarForLock(connectedCarType);
                } else if (smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForLock(connectedCarType);
                }
                break;
            case Antenna.AVERAGE_UNLOCK:
                threshold = SdkPreferencesHelper.getInstance().getUnlockThreshold(connectedCarType);
                if (smartphoneComIsActivated && smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetEarForUnlock(connectedCarType);
                } else if (smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock(connectedCarType);
                }
                break;
            case Antenna.AVERAGE_WELCOME:
                threshold = SdkPreferencesHelper.getInstance().getWelcomeThreshold(connectedCarType);
                if (smartphoneComIsActivated && smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetEarForLock(connectedCarType);
                } else if (smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForLock(connectedCarType);
                }
                break;
            case Antenna.AVERAGE_DELTA_LOCK:
                threshold = SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(connectedCarType);
                if (smartphoneComIsActivated && smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetEarForLock(connectedCarType);
                } else if (smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForLock(connectedCarType);
                }
                break;
            case Antenna.AVERAGE_DELTA_UNLOCK:
                threshold = SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(connectedCarType);
                if (smartphoneComIsActivated && smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetEarForUnlock(connectedCarType);
                } else if (smartphoneIsInPocket) {
                    threshold += SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock(connectedCarType);
                }
                break;
            default:
                threshold = SdkPreferencesHelper.getInstance().getLockThreshold(connectedCarType);
                break;
        }
        return threshold;
    }

    /**
     * Compare value with threshold
     *
     * @param value     the value to compare
     * @param threshold the threshold to compare to
     * @param isGreater true if compare sign is greater than, false if it is lower than
     * @return true if isGreater is true and value greater than threshold, false otherwise, inverse if isGreater is false
     */
    public static boolean compareWithThreshold(int value, int threshold, boolean isGreater) {
        if (isGreater) {
            return value > threshold;
        } else {
            return value < threshold;
        }
    }

    private static String booleanToString(boolean toConvert) {
        if (toConvert) {
            return "1";
        } else {
            return "0";
        }
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
     * @param smartphoneIsMovingSlowly true if the smartphone is moving, false otherwise
     * @param isLockStatusChangedTimerExpired true if the lock timeout is expired, false otherwise
     * @param blockStart true if the lock can be done, false otherwise
     * @param forcedStart true if the lock can be done, false otherwise
     * @param smartphoneIsFrozen true if the lock can be done, false otherwise
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
    public static void appendRssiLogs(int rssiLeft, int rssiMiddle1, int rssiMiddle2, int rssiRight, int rssiTrunk,
                                      int rssiFrontLeft, int rssiFrontRight, int rssiRearLeft, int rssiRearRight, int rssiBack,
                                      float z, float x, float y,
                                      boolean isSmartphoneInPocket, boolean smartphoneIsMovingSlowly, boolean isLockStatusChangedTimerExpired,
                                      boolean blockStart, boolean forcedStart, boolean smartphoneIsFrozen,
                                      boolean rearmLock, boolean rearmUnlock, boolean rearmWelcome, boolean lockStatus,
                                      byte welcomeByte, byte lockByte, byte startByte,
                                      byte leftAreaByte, byte rightAreaByte, byte backAreaByte,
                                      byte walkAwayByte, byte approachByte,
                                      byte leftTurnByte, byte fullTurnByte, byte rightTurnByte, byte recordByte, int rangingPredictionInt,
                                      boolean lockFromTrx, boolean lockToSend, boolean startAllowed, boolean isThatcham) {
        final String comma = ";";
        String log = String.valueOf(rssiLeft) + comma + rssiMiddle1 + comma + rssiMiddle2 + comma + rssiRight + comma + rssiTrunk + comma +
                rssiFrontLeft + comma + rssiFrontRight + comma + rssiRearLeft + comma + rssiRearRight + comma + rssiBack + comma +
                z + comma + x + comma + y + comma +
                booleanToString(isSmartphoneInPocket) + comma + booleanToString(smartphoneIsMovingSlowly) + comma + booleanToString(isLockStatusChangedTimerExpired) + comma +
                booleanToString(blockStart) + comma + booleanToString(forcedStart) + comma + booleanToString(smartphoneIsFrozen) + comma;
        if (lockStatus) {
            log += "5" + comma;
        } else {
            log += "4" + comma;
        }
        if (rearmLock) {
            log += "7" + comma;
        } else {
            log += "6" + comma;
        }
        if (rearmUnlock) {
            log += "9" + comma;
        } else {
            log += "8" + comma;
        }
        log += booleanToString(rearmWelcome) + comma + welcomeByte + comma;
        if (lockByte == 1) {
            log += "3" + comma;
        } else {
            log += "2" + comma;
        }
        log += startByte + comma;
        if (leftAreaByte == 1) {
            log += "11" + comma;
        } else {
            log += "10" + comma;
        }
        if (rightAreaByte == 1) {
            log += "12" + comma;
        } else {
            log += "10" + comma;
        }
        if (backAreaByte == 1) {
            log += "13" + comma;
        } else {
            log += "10" + comma;
        }
        if (walkAwayByte == 1) {
            log += "15" + comma;
        } else {
            log += "14" + comma;
        }
        if (approachByte == 1) {
            log += "16" + comma;
        } else {
            log += "14" + comma;
        }
        log += leftTurnByte + comma + fullTurnByte + comma + rightTurnByte + comma +
                recordByte + comma + rangingPredictionInt + comma +
                booleanToString(lockFromTrx) + comma + booleanToString(lockToSend) + comma
                + booleanToString(startAllowed) + comma + booleanToString(isThatcham) + comma;
        appendRssiLog(log);
    }

    /**
     * Function used to debug and write logs into a file.
     */
    private static void appendRssiLog(String text) {
        PSALogs.d("log", text);
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
