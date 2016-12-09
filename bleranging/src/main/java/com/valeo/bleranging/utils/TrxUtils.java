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

    private static int selectedThreshold(int thresholdIndoor, int thresholdOutside, boolean isIndoor,
                                         boolean smartphoneIsInPocket, boolean smartphoneComIsActivated,
                                         int offsetEar, int offsetPocket) {
        int result;
        if (isIndoor) {
            result = thresholdIndoor;
        } else {
            result = thresholdOutside;
        }
        if (smartphoneComIsActivated && smartphoneIsInPocket) {
            result += offsetEar;
        } else if (smartphoneIsInPocket) {
            result += offsetPocket;
        }
        return result;
    }

    /**
     * Calculate the threshold to use for lock
     *
     * @param mode                 the mode of average
     * @param smartphoneIsInPocket true if the smartphone is in the pocket, false otherwise
     * @param smartphoneComIsActivated  true if the smartphone is near ears, false otherwise
     * @return the threshold with an offset if the smartphone is in a pocket
     */
    public static int getCurrentThreshold(int mode, boolean isIndoor, boolean smartphoneIsInPocket,
                                          boolean smartphoneComIsActivated) {
        int threshold;
        String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        switch (mode) {
            case Antenna.AVERAGE_START:
                threshold = selectedThreshold(
                        SdkPreferencesHelper.getInstance().getIndoorStartThreshold(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOutsideStartThreshold(connectedCarType),
                        isIndoor, smartphoneIsInPocket, smartphoneComIsActivated,
                        SdkPreferencesHelper.getInstance().getOffsetEarForStart(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOffsetPocketForStart(connectedCarType));
                break;
            case Antenna.AVERAGE_LOCK:
                threshold = selectedThreshold(
                        SdkPreferencesHelper.getInstance().getIndoorLockThreshold(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOutsideLockThreshold(connectedCarType),
                        isIndoor, smartphoneIsInPocket, smartphoneComIsActivated,
                        SdkPreferencesHelper.getInstance().getOffsetEarForLock(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOffsetPocketForLock(connectedCarType));
                break;
            case Antenna.AVERAGE_UNLOCK:
                threshold = selectedThreshold(
                        SdkPreferencesHelper.getInstance().getIndoorUnlockThreshold(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOutsideUnlockThreshold(connectedCarType),
                        isIndoor, smartphoneIsInPocket, smartphoneComIsActivated,
                        SdkPreferencesHelper.getInstance().getOffsetEarForUnlock(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock(connectedCarType));
                break;
            case Antenna.AVERAGE_WELCOME:
                threshold = selectedThreshold(
                        SdkPreferencesHelper.getInstance().getIndoorWelcomeThreshold(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOutsideWelcomeThreshold(connectedCarType),
                        isIndoor, smartphoneIsInPocket, smartphoneComIsActivated,
                        SdkPreferencesHelper.getInstance().getOffsetEarForLock(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOffsetPocketForLock(connectedCarType));
                break;
            case Antenna.AVERAGE_DELTA_LOCK:
                threshold = selectedThreshold(
                        SdkPreferencesHelper.getInstance().getIndoorAverageDeltaLockThreshold(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOutsideAverageDeltaLockThreshold(connectedCarType),
                        isIndoor, smartphoneIsInPocket, smartphoneComIsActivated,
                        SdkPreferencesHelper.getInstance().getOffsetEarForLock(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOffsetPocketForLock(connectedCarType));
                break;
            case Antenna.AVERAGE_DELTA_UNLOCK:
                threshold = selectedThreshold(
                        SdkPreferencesHelper.getInstance().getIndoorAverageDeltaUnlockThreshold(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOutsideAverageDeltaUnlockThreshold(connectedCarType),
                        isIndoor, smartphoneIsInPocket, smartphoneComIsActivated,
                        SdkPreferencesHelper.getInstance().getOffsetEarForUnlock(connectedCarType),
                        SdkPreferencesHelper.getInstance().getOffsetPocketForUnlock(connectedCarType));
                break;
            default:
                threshold = SdkPreferencesHelper.getInstance().getOutsideLockThreshold(connectedCarType);
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
     * @param rssiMiddle the middle trx antenna 1 rssi
     * @param rssiRight the right trx rssi
     * @param rssiBack the back trx rssi
     * @param rssiFrontLeft the front left trx rssi
     * @param rssiFrontRight the front right trx rssi
     * @param rssiRearLeft the rear left trx rssi
     * @param rssiRearRight the rear right trx rssi
     * @param rssiLeftOriginal the original left trx rssi
     * @param rssiMiddleOriginal the original middle trx rssi
     * @param rssiRightOriginal the original right trx rssi
     * @param rssiBackOriginal the original back trx rssi
     * @param rssiFrontLeftOriginal the original front left trx rssi
     * @param rssiFrontRightOriginal the original front right trx rssi
     * @param rssiRearLeftOriginal the original rear left trx rssi
     * @param rssiRearRightOriginal the original rear right trx rssi
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
     * @param rightTurnByte equals 1 if right turn is activated, 0 otherwise
     * @param approachSideByte equals 1 if right side, 0 if left side
     * @param approachRoadByte equals [1-6] for the road taken during test
     * @param recordByte equals 1 if record is activated, 0 otherwise
     * @param rangingPrediction prediction from random forest algorithm
     * @param lockFromTrx lock status from trx
     * @param lockToSend lock status to send
     * @param startAllowed true if start is allowed, false otherwise
     * @param isThatcham true if thatcham, false otherwise
     * @param channelLeft left trx ble channel
     * @param channelMiddle middle trx ble channel
     * @param channelRight right trx ble channel
     * @param channelTrunk trunk trx ble channel
     * @param channelFrontLeft front left trx ble channel
     * @param channelFrontRight front right trx ble channel
     * @param channelRearLeft rear left trx ble channel
     * @param channelRearRight rear right trx ble channel
     * @param channelBack back trx ble channel
     */
    public static void appendRssiLogs(int rssiLeft, int rssiMiddle, int rssiRight, int rssiTrunk,
                                      int rssiFrontLeft, int rssiFrontRight,
                                      int rssiRearLeft, int rssiRearRight, int rssiBack,
                                      int rssiLeftOriginal, int rssiMiddleOriginal, int rssiRightOriginal, int rssiTrunkOriginal,
                                      int rssiFrontLeftOriginal, int rssiFrontRightOriginal,
                                      int rssiRearLeftOriginal, int rssiRearRightOriginal, int rssiBackOriginal,
                                      int offset38Left, int offset38Middle, int offset38Right, int offset38Trunk,
                                      int offset38FrontLeft, int offset38FrontRight,
                                      int offset38RearLeft, int offset38RearRight, int offset38Back,
                                      int offset39Left, int offset39Middle, int offset39Right, int offset39Trunk,
                                      int offset39FrontLeft, int offset39FrontRight,
                                      int offset39RearLeft, int offset39RearRight, int offset39Back,
                                      float z, float x, float y,
                                      boolean isSmartphoneInPocket, boolean smartphoneIsMovingSlowly, boolean isLockStatusChangedTimerExpired,
                                      boolean blockStart, boolean forcedStart,
                                      boolean blockLock, boolean forcedLock,
                                      boolean blockUnlock, boolean forcedUnlock, boolean smartphoneIsFrozen,
                                      boolean rearmLock, boolean rearmUnlock, boolean rearmWelcome, boolean lockStatus,
                                      byte welcomeByte, byte lockByte, byte startByte,
                                      byte leftAreaByte, byte rightAreaByte, byte backAreaByte,
                                      byte walkAwayByte, byte approachByte, byte leftTurnByte,
                                      byte rightTurnByte, byte approachSideByte, byte approachRoadByte,
                                      byte recordByte, String rangingPrediction,
                                      boolean lockFromTrx, boolean lockToSend, boolean startAllowed, boolean isThatcham,
                                      String channelLeft, String channelMiddle, String channelRight, String channelTrunk,
                                      String channelFrontLeft, String channelFrontRight,
                                      String channelRearLeft, String channelRearRight,
                                      String channelBack, int beepInt) {
        final String comma = ";";
        String log = rssiLeft + comma + rssiMiddle + comma + rssiRight + comma + rssiTrunk + comma +
                rssiFrontLeft + comma + rssiFrontRight + comma + rssiRearLeft + comma + rssiRearRight + comma + rssiBack + comma +
                rssiLeftOriginal + comma + rssiMiddleOriginal + comma + rssiRightOriginal + comma + rssiTrunkOriginal + comma +
                rssiFrontLeftOriginal + comma + rssiFrontRightOriginal + comma + rssiRearLeftOriginal + comma + rssiRearRightOriginal + comma + rssiBackOriginal + comma +
                offset38Left + comma + offset38Middle + comma + offset38Right + comma + offset38Trunk + comma +
                offset38FrontLeft + comma + offset38FrontRight + comma +
                offset38RearLeft + comma + offset38RearRight + comma + offset38Back + comma +
                offset39Left + comma + offset39Middle + comma + offset39Right + comma + offset39Trunk + comma +
                offset39FrontLeft + comma + offset39FrontRight + comma +
                offset39RearLeft + comma + offset39RearRight + comma + offset39Back + comma +
                z + comma + x + comma + y + comma +
                booleanToString(isSmartphoneInPocket) + comma + booleanToString(smartphoneIsMovingSlowly) + comma + booleanToString(isLockStatusChangedTimerExpired) + comma +
                booleanToString(blockStart) + comma + booleanToString(forcedStart) + comma +
                booleanToString(blockLock) + comma + booleanToString(forcedLock) + comma +
                booleanToString(blockUnlock) + comma + booleanToString(forcedUnlock) + comma + booleanToString(smartphoneIsFrozen) + comma;
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
        log += leftTurnByte + comma + rightTurnByte + comma
                + approachSideByte + comma + approachRoadByte + comma
                + recordByte + comma + rangingPrediction + comma
                + booleanToString(lockFromTrx) + comma + booleanToString(lockToSend) + comma
                + booleanToString(startAllowed) + comma + booleanToString(isThatcham) + comma
                + channelLeft + comma + channelMiddle + comma + channelRight + comma
                + channelTrunk + comma + channelFrontLeft + comma + channelFrontRight + comma
                + channelRearLeft + comma + channelRearRight + comma
                + channelBack + comma + beepInt + comma;
        appendRssiLog(log);
    }

    public static void appendSettingLogs(String carType, String carBase, String algoType, boolean isIndoor,
                                         String addressConnectable, String addressConnectableRemote, String addressConnectablePC,
                                         String addressFrontLeft, String addressFrontRight,
                                         String addressLeft, String addressMiddle, String addressRight, String addressTrunk,
                                         String addressRearLeft, String addressBack, String addressRearRight,
                                         int logNumber, int rollAvElement, int startNumElement, int lockNumElement,
                                         int unlockNumElement, int welcomeNumElement, int LongNumElement, int shortNumElement,
                                         float thatchamTimeout, float preAuthTimeout, float actionTimeout, int sizeAcc,
                                         float correctionAcc, float frozenThr, float wantedSpeed, int stepSize,
                                         int indoorRatioCloseToCar, int outsideRatioCloseToCar,
                                         int offsetEarStart, int offsetEarLock, int offsetEarUnlock,
                                         int offsetPocketStart, int offsetPocketLock, int offsetPocketUnlock,
                                         int indoorStartThr, int indoorUnlockThr, int indoorLockThr,
                                         int indoorWelcomeThr, int indoorNearDoorRatioThr, int indoorBackDoorRatioMinThr, int indoorBackDoorRatioMaxThr,
                                         int indoorNearDoorRatioMB, int indoorNearDoorRatioMLMRMaxThr,
                                         int indoorNearDoorRatioMLMRMinThr, int indoorNearDoorRatioTLTRMaxThr, int indoorNearDoorRatioTLTRMinThr,
                                         int indoorAverageDeltaLockThr, int indoorAverageDeltaUnlockThr,
                                         int outsideStartThr, int outsideUnlockThr, int outsideLockThr,
                                         int outsideWelcomeThr, int outsideNearDoorRatioThr, int outsideBackDoorRatioMinThr, int outsideBackDoorRatioMaxThr,
                                         int outsideNearDoorRatioMB, int outsideNearDoorRatioMLMRMaxThr,
                                         int outsideNearDoorRatioMLMRMinThr, int outsideNearDoorRatioTLTRMaxThr, int outsideNearDoorRatioTLTRMinThr,
                                         int outsideAverageDeltaLockThr, int outsideAverageDeltaUnlockThr,
                                         int unlockValidNumber,
                                         int unlockMode, int lockMode, int startMode, float ecretage100,
                                         float ecretage70, float ecretage50, float ecretage30,
                                         int equaLeft, int equaMiddle, int equaRight, int equaTrunk,
                                         int equaBack, int equaFrontLeft, int equaRearLeft, int equaFrontRight, int equaRearRight) {
        final String comma = ";";
        String log = carType + comma + carBase + comma + algoType + comma + String.valueOf(isIndoor) + comma
                + addressConnectable + comma + addressConnectableRemote + comma
                + addressConnectablePC + comma + addressFrontLeft + comma + addressFrontRight + comma
                + addressLeft + comma + addressMiddle + comma + addressRight + comma + addressTrunk + comma
                + addressRearLeft + comma + addressBack + comma + addressRearRight + comma
                + String.valueOf(logNumber) + comma + String.valueOf(rollAvElement) + comma
                + String.valueOf(startNumElement) + comma + String.valueOf(lockNumElement) + comma
                + String.valueOf(unlockNumElement) + comma + String.valueOf(welcomeNumElement) + comma
                + String.valueOf(LongNumElement) + comma + String.valueOf(shortNumElement) + comma
                + String.valueOf(thatchamTimeout) + comma + String.valueOf(preAuthTimeout) + comma
                + String.valueOf(actionTimeout) + comma + String.valueOf(sizeAcc) + comma
                + String.valueOf(correctionAcc) + comma + String.valueOf(frozenThr) + comma
                + String.valueOf(wantedSpeed) + comma + String.valueOf(stepSize) + comma
                + String.valueOf(indoorRatioCloseToCar) + comma + String.valueOf(outsideRatioCloseToCar) + comma
                + String.valueOf(offsetEarStart) + comma + String.valueOf(offsetEarLock) + comma
                + String.valueOf(offsetEarUnlock) + comma + String.valueOf(offsetPocketStart) + comma
                + String.valueOf(offsetPocketLock) + comma + String.valueOf(offsetPocketUnlock) + comma
                + String.valueOf(indoorStartThr) + comma + String.valueOf(indoorUnlockThr) + comma
                + String.valueOf(indoorLockThr) + comma + String.valueOf(indoorWelcomeThr) + comma
                + String.valueOf(indoorNearDoorRatioThr) + comma + String.valueOf(indoorBackDoorRatioMinThr) + comma
                + String.valueOf(indoorBackDoorRatioMaxThr) + comma + String.valueOf(indoorNearDoorRatioMB) + comma
                + String.valueOf(indoorNearDoorRatioMLMRMaxThr) + comma
                + String.valueOf(indoorNearDoorRatioMLMRMinThr) + comma
                + String.valueOf(indoorNearDoorRatioTLTRMaxThr) + comma
                + String.valueOf(indoorNearDoorRatioTLTRMinThr) + comma
                + String.valueOf(indoorAverageDeltaLockThr) + comma
                + String.valueOf(indoorAverageDeltaUnlockThr) + comma
                + String.valueOf(outsideStartThr) + comma + String.valueOf(outsideUnlockThr) + comma
                + String.valueOf(outsideLockThr) + comma + String.valueOf(outsideWelcomeThr) + comma
                + String.valueOf(outsideNearDoorRatioThr) + comma + String.valueOf(outsideBackDoorRatioMinThr) + comma
                + String.valueOf(outsideBackDoorRatioMaxThr) + comma + String.valueOf(outsideNearDoorRatioMB) + comma
                + String.valueOf(outsideNearDoorRatioMLMRMaxThr) + comma
                + String.valueOf(outsideNearDoorRatioMLMRMinThr) + comma
                + String.valueOf(outsideNearDoorRatioTLTRMaxThr) + comma
                + String.valueOf(outsideNearDoorRatioTLTRMinThr) + comma
                + String.valueOf(outsideAverageDeltaLockThr) + comma
                + String.valueOf(outsideAverageDeltaUnlockThr) + comma
                + String.valueOf(unlockValidNumber) + comma + String.valueOf(unlockMode) + comma
                + String.valueOf(lockMode) + comma + String.valueOf(startMode) + comma
                + String.valueOf(ecretage100) + comma + String.valueOf(ecretage70) + comma
                + String.valueOf(ecretage50) + comma + String.valueOf(ecretage30) + comma
                + String.valueOf(equaLeft) + comma + String.valueOf(equaMiddle) + comma
                + String.valueOf(equaRight) + comma + String.valueOf(equaTrunk) + comma + String.valueOf(equaBack) + comma
                + String.valueOf(equaFrontLeft) + comma + String.valueOf(equaRearLeft) + comma
                + String.valueOf(equaFrontRight) + comma + String.valueOf(equaRearRight);
        appendRssiLog(log);
    }

    /**
     * Function used to debug and write logs into a file.
     */
    private static void appendRssiLog(String text) {
//        PSALogs.d("log", text);
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

    public static boolean createLogFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk", Locale.FRANCE);
        String timestampLog = sdf.format(new Date());
        SdkPreferencesHelper.getInstance().setLogFileName("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        PSALogs.d("LogFileName", SdkPreferencesHelper.getInstance().getLogFileName());
        File logFile = new File("sdcard/InBlueRssi/allRssi_" + SdkPreferencesHelper.getInstance().getRssiLogNumber() + "_" + timestampLog + ".csv");
        if (!logFile.exists()) {
            try {
                //Create file
                if (logFile.createNewFile()) {
                    PSALogs.d("make", "file Success");
                    return true;
                } else {
                    PSALogs.d("make", "file Failed");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    public static void writeFirstColumnSettings() {
        //Write 1st row with column names
        //BufferedWriter for performance, true to set append to file flag
        String ColNames = "TIMESTAMP;carType;carBase;algoSelected;isIndoor;addressConnectable;addressConnectableRemote;addressConnectablePC;addressFrontLeft;addressFrontRight;"
                + "addressLeft;addressMiddle;addressRight;addressTrunk;addressRearLeft;addressBack;addressRearRight;"
                + "logNumber;rollAvElement;startNumElement;lockNumelement;unlockNumElement;welcomeNumElement;LongNumElement;shortNumElement;"
                + "thatchamTimeout;preAuthTimeout;actionTimeout;sizeAcc;correctionAcc;frozenThr;wantedSpeed;stepSize;"
                + "indoorRatioCloseToCar;outsideRatioCloseToCar;offsetEarStart;offsetEarLock;offsetEarUnlock;offsetPocketStart;offsetPocketLock;offsetPocketUnlock;"
                + "indoorStartThr;indoorUnlockThr;indoorLockThr;indoorWelcomeThr;indoorNearDoorRatioThr;indoorBackDoorRatioMinThr;indoorBackDoorRatioMaxThr;"
                + "indoorNearDoorRatioMB;indoorNearDoorRatioMLMRMaxThr;indoorNearDoorRatioMLMRMinThr;indoorNearDoorRatioTLTRMaxThr;indoorNearDoorRatioTLTRMinThr;"
                + "indoorAverageDeltaLockThr;indoorAverageDeltaUnlockThr;"
                + "outsideStartThr;outsideUnlockThr;outsideLockThr;outsideWelcomeThr;outsideNearDoorRatioThr;outsideBackDoorRatioMinThr;outsideBackDoorRatioMaxThr;"
                + "outsideNearDoorRatioMB;outsideNearDoorRatioMLMRMaxThr;outsideNearDoorRatioMLMRMinThr;outsideNearDoorRatioTLTRMaxThr;outsideNearDoorRatioTLTRMinThr;"
                + "outsideAverageDeltaLockThr;outsideAverageDeltaUnlockThr;"
                + "unlockValidNumber;"
                + "unlockMode;lockMode;startMode;ecretage100;ecretage70;ecretage50;ecretage30;"
                + "equaLeft;equaMiddle;equaRight;equaTrunk;equaBack;equaFrontLeft;equaRearLeft;equaFrontRight;equaRearRight;";
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(ColNames);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFirstColumnLogs() {
        //Write 1st row with column names
        //BufferedWriter for performance, true to set append to file flag
        String ColNames = "TIMESTAMP;RSSI LEFT;RSSI MIDDLE;RSSI RIGHT;"
                + "RSSI TRUNK;RSSI FRONTLEFT;RSSI FRONTRIGHT;RSSI REARLEFT;RSSI REARRIGHT;RSSI BACK;"
                + "RSSI LEFT_ORIGIN;RSSI MIDDLE_ORIGIN;RSSI RIGHT_ORIGIN;"
                + "RSSI TRUNK_ORIGIN;RSSI FRONTLEFT_ORIGIN;RSSI FRONTRIGHT_ORIGIN;"
                + "RSSI REARLEFT_ORIGIN;RSSI REARRIGHT_ORIGIN;RSSI BACK_ORIGIN;"
                + "LEFT_OFFSET38;MIDDLE_OFFSET38;RIGHT_OFFSET38;"
                + "TRUNK_OFFSET38;FRONTLEFT_OFFSET38;FRONTRIGHT_OFFSET38;"
                + "REARLEFT_OFFSET38;REARRIGHT_OFFSET38;BACK_OFFSET38;"
                + "LEFT_OFFSET39;MIDDLE_OFFSET_39;RIGHT_OFFSET39;"
                + "TRUNK_OFFSET39;FRONTLEFT_OFFSET39;FRONTRIGHT_OFFSET39;"
                + "REARLEFT_OFFSET39;REARRIGHT_OFFSET39;BACK_OFFSET39;"
                + "Z AZIMUTH;X PITCH;Y ROLL;IN POCKET;IS LAID;ARE LOCK ACTIONS AVAILABLE;"
                + "IS START BLOCKED;IS START FORCED;IS LOCK BLOCKED;IS LOCK FORCED;"
                + "IS UNLOCK BLOCKED;IS UNLOCK FORCED;IS FROZEN;IS LOCK;REARM LOCK;REARM UNLOCK;"
                + "REARM WELCOME;WELCOME FLAG;LOCK FLAG;START FLAG;LEFT AREA FLAG;RIGHT AREA FLAG;"
                + "BACK AREA FLAG;WALK AWAY FLAG;APPROACH FLAG;LEFT TURN FLAG;"
                + "RIGHT TURN FLAG;APPROACHSIDE FLAG;APPROACHROAD FLAG;RECORD FLAG;"
                + "PREDICTION;LOCK FROM TRX;LOCK TO SEND;START ALLOWED;IS THATCHAM;"
                + "BLE CHANNEL LEFT;BLE CHANNEL MIDDLE;BLE CHANNEL RIGHT;BLE CHANNEL TRUNK;"
                + "BLE CHANNEL FRONTLEFT;BLE CHANNEL FRONTRIGHT;"
                + "BLE CHANNEL REARLEFT;BLE CHANNEL REARRIGHT;BLE CHANNEL BACK;BEEPINT;";
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(ColNames);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
