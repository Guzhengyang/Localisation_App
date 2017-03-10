package com.valeo.bleranging.utils;

import android.content.Context;

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
public class LogFileUtils {
    public final static String RSSI_DIR = "/InBlueRssi/";
    public final static String CONFIG_DIR = "/InBlueConfig/";
    private final static String FILENAME_TIMESTAMP_FORMAT = "yyyy-MM-dd_kk";
    private final static String RSSI_TIMESTAMP_FORMAT = "HH:mm:ss:SSS";
    private final static SimpleDateFormat sdfRssi = new SimpleDateFormat(RSSI_TIMESTAMP_FORMAT, Locale.FRANCE);
    private final static SimpleDateFormat sdfFilename = new SimpleDateFormat(FILENAME_TIMESTAMP_FORMAT, Locale.FRANCE);
    private final static String LOG_FILE_PREFIX = RSSI_DIR + "allRssi_";
    private final static String FILE_EXTENSION = ".csv";
    private static File logFile = null;
    private static BufferedWriter buf = null;

    /**
     * Convert a boolean to a string value
     * @param toConvert the boolean to convert
     * @return 1 if toCovert is true, 0 if toCovert is false
     */
    private static String booleanToString(boolean toConvert) {
        if (toConvert) {
            return "1";
        } else {
            return "0";
        }
    }

    public static boolean createBufferedWriter() {
        try {
            buf = new BufferedWriter(new FileWriter(logFile, true));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean closeBufferedWriter() {
        try {
            buf.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create the string to write in the log file and add it
     * @param rssiLeftOriginal the original left trx rssi
     * @param rssiMiddleOriginal the original middle trx rssi
     * @param rssiRightOriginal the original right trx rssi
     * @param rssiBackOriginal the original back trx rssi
     * @param rssiFrontLeftOriginal the original front left trx rssi
     * @param rssiFrontRightOriginal the original front right trx rssi
     * @param rssiRearLeftOriginal the original rear left trx rssi
     * @param rssiRearRightOriginal the original rear right trx rssi
     * @param isSmartphoneInPocket true if the smartphone is in pocket, false otherwise
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
    public static void appendRssiLogs(int rssiLeftOriginal, int rssiMiddleOriginal, int rssiRightOriginal, int rssiTrunkOriginal,
                                      int rssiFrontLeftOriginal, int rssiFrontRightOriginal,
                                      int rssiRearLeftOriginal, int rssiRearRightOriginal, int rssiBackOriginal,
                                      float orientationX, float orientationY, float orientationZ, double acceleration,
                                      boolean isActiveLeft, boolean isActiveMiddle, boolean isActiveRight, boolean isActiveTrunk,
                                      boolean isActiveFrontLeft, boolean isActiveFrontRight,
                                      boolean isActiveRearLeft, boolean isActiveRearRight, boolean isActiveBack,
                                      boolean isSmartphoneInPocket, boolean isLockStatusChangedTimerExpired,
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
        String log = rssiLeftOriginal + comma + rssiMiddleOriginal + comma + rssiRightOriginal + comma + rssiTrunkOriginal + comma +
                rssiFrontLeftOriginal + comma + rssiFrontRightOriginal + comma + rssiRearLeftOriginal + comma + rssiRearRightOriginal + comma + rssiBackOriginal + comma +
                orientationX + comma + orientationY + comma + orientationZ + comma + acceleration + comma +
                booleanToString(isActiveLeft) + comma + booleanToString(isActiveMiddle) + comma + booleanToString(isActiveRight) + comma + booleanToString(isActiveTrunk) + comma +
                booleanToString(isActiveFrontLeft) + comma + booleanToString(isActiveFrontRight) + comma +
                booleanToString(isActiveRearLeft) + comma + booleanToString(isActiveRearRight) + comma + booleanToString(isActiveBack) + comma +
                booleanToString(isSmartphoneInPocket) + comma + booleanToString(isLockStatusChangedTimerExpired) + comma;
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

    /**
     * Create the string to write in the settings log file and add it
     * @param carType the car type
     * @param carBase the car base
     * @param addressConnectable the car connectable trx address
     * @param addressConnectableRemote the remote control address
     * @param addressConnectablePC the computer address
     * @param addressFrontLeft the front left trx address
     * @param addressFrontRight the front right trx address
     * @param addressLeft the left trx address
     * @param addressMiddle the middle trx address
     * @param addressRight the right trx address
     * @param addressTrunk the trunk trx address
     * @param addressRearLeft the rear left trx address
     * @param addressBack the back trx address
     * @param addressRearRight the rear right trx address
     * @param logNumber the next log file number
     * @param preAuthTimeout the preAuth timeout duration
     * @param actionTimeout the action timeout duration
     * @param wantedSpeed the wanted speed for the test procedure
     * @param stepSize the step size for the test procedure
     */
    public static void appendSettingLogs(String carType, String carBase,
                                         String addressConnectable, String addressConnectableRemote, String addressConnectablePC,
                                         String addressFrontLeft, String addressFrontRight,
                                         String addressLeft, String addressMiddle, String addressRight, String addressTrunk,
                                         String addressRearLeft, String addressBack, String addressRearRight,
                                         int logNumber,
                                         float preAuthTimeout, float actionTimeout,
                                         float wantedSpeed, int stepSize) {
        final String comma = ";";
        String log = carType + comma + carBase + comma
                + addressConnectable + comma + addressConnectableRemote + comma
                + addressConnectablePC + comma + addressFrontLeft + comma + addressFrontRight + comma
                + addressLeft + comma + addressMiddle + comma + addressRight + comma + addressTrunk + comma
                + addressRearLeft + comma + addressBack + comma + addressRearRight + comma
                + String.valueOf(logNumber) + comma
                + String.valueOf(preAuthTimeout) + comma
                + String.valueOf(actionTimeout) + comma
                + String.valueOf(wantedSpeed) + comma + String.valueOf(stepSize) + comma;
        appendRssiLog(log);
    }

    /**
     * Function used to debug and write logs into a file.
     */
    private static void appendRssiLog(String text) {
//        PSALogs.d("log", text);
        try {
            String timestamp = sdfRssi.format(new Date());
            //BufferedWriter for performance, true to set append to file flag
            if (logFile != null) {
                buf.append(timestamp).append(";").append(text);
                buf.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create two directories to register the settings and all rssi values
     */
    private static boolean createDirectories(Context mContext) {
        return createLogsDir(mContext) && createConfigDir(mContext);
    }

    /**
     * Create a log file to register the settings and all rssi values
     * @return true if the file exist or is succesfully created, false otherwise
     */
    public static boolean createLogFile(Context mContext) {
        if (createDirectories(mContext)) {
            String timestampLog = sdfFilename.format(new Date());
            SdkPreferencesHelper.getInstance().setLogFileName(LOG_FILE_PREFIX
                    + SdkPreferencesHelper.getInstance().getRssiLogNumber()
                    + "_" + timestampLog + FILE_EXTENSION);
            logFile = new File(mContext.getExternalCacheDir(), SdkPreferencesHelper.getInstance().getLogFileName());
            PSALogs.d("LogFileName", SdkPreferencesHelper.getInstance().getLogFileName());
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
        return false;
    }

    /**
     * Write in the log file a line with the settings column names
     */
    public static void writeFirstColumnSettings() {
        //Write 1st row with column names
        //BufferedWriter for performance, true to set append to file flag
        String ColNames = "TIMESTAMP;carType;carBase;addressConnectable;"
                + "addressConnectableRemote;addressConnectablePC;addressFrontLeft;addressFrontRight;"
                + "addressLeft;addressMiddle;addressRight;addressTrunk;"
                + "addressRearLeft;addressBack;addressRearRight;"
                + "logNumber;"
                + "preAuthTimeout;actionTimeout;wantedSpeed;stepSize;";
        try {
            if (logFile != null) {
                buf.append(ColNames);
                buf.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write in the log file a line with the rssi affiliated parameters column names
     */
    public static void writeFirstColumnLogs() {
        //Write 1st row with column names
        //BufferedWriter for performance, true to set append to file flag
        String ColNames = "TIMESTAMP;"
                + "RSSI LEFT_ORIGIN;RSSI MIDDLE_ORIGIN;RSSI RIGHT_ORIGIN;"
                + "RSSI TRUNK_ORIGIN;RSSI FRONTLEFT_ORIGIN;RSSI FRONTRIGHT_ORIGIN;"
                + "RSSI REARLEFT_ORIGIN;RSSI REARRIGHT_ORIGIN;RSSI BACK_ORIGIN;"
                + "ORIENTATION_X;ORIENTAION_Y;ORIENTATION_Z;ACCELERATION;"
                + "LEFT_IS_ACTIVE;MIDDLE_IS_ACTIVE;RIGHT_IS_ACTIVE;"
                + "TRUNK_IS_ACTIVE;FRONTLEFT_IS_ACTIVE;FRONTRIGHT_IS_ACTIVE;"
                + "REARLEFT_IS_ACTIVE;REARRIGHT_IS_ACTIVE;BACK_IS_ACTIVE;"
                + "IN POCKET;ARE LOCK ACTIONS AVAILABLE;"
                + "IS LOCK;REARM LOCK;REARM UNLOCK;"
                + "REARM WELCOME;WELCOME FLAG;LOCK FLAG;START FLAG;LEFT AREA FLAG;RIGHT AREA FLAG;"
                + "BACK AREA FLAG;WALK AWAY FLAG;APPROACH FLAG;LEFT TURN FLAG;"
                + "RIGHT TURN FLAG;APPROACHSIDE FLAG;APPROACHROAD FLAG;RECORD FLAG;"
                + "PREDICTION;LOCK FROM TRX;LOCK TO SEND;START ALLOWED;IS THATCHAM;"
                + "BLE CHANNEL LEFT;BLE CHANNEL MIDDLE;BLE CHANNEL RIGHT;BLE CHANNEL TRUNK;"
                + "BLE CHANNEL FRONTLEFT;BLE CHANNEL FRONTRIGHT;"
                + "BLE CHANNEL REARLEFT;BLE CHANNEL REARRIGHT;BLE CHANNEL BACK;BEEPINT;";
        try {
            if (logFile != null) {
                buf.append(ColNames);
                buf.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean createDir(File dirPath, String dirName) {
        File dir = new File(dirPath, dirName);
        //if the folder doesn't exist
        if (!dir.exists()) {
            if (dir.mkdir()) {
                PSALogs.d("make", "dir Success");
                return true;
            } else {
                PSALogs.d("make", "dir Failed");
                return false;
            }
        }
        return true;
    }

    private static boolean createLogsDir(Context mContext) {
        return createDir(mContext.getExternalCacheDir(), RSSI_DIR);
    }

    private static boolean createConfigDir(Context mContext) {
        return createDir(mContext.getExternalCacheDir(), CONFIG_DIR);
    }
}
