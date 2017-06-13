package com.valeo.bleranging.utils;

import android.content.Context;

import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.machinelearningalgo.AlgoManager;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_BACK;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_FRONT_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_FRONT_RIGHT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_MIDDLE;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_REAR_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_REAR_RIGHT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_RIGHT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.NUMBER_TRX_TRUNK;

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

    private static void writeWithBufferedWriter(String text) {
        if (logFile != null) {
            BufferedWriter buf = null;
            try {
                buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (buf != null) {
                    try {
                        buf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Create the string to write in the log file and add it
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
     */
    public static void appendRssiLogs(final ConnectedCar connectedCar, final AlgoManager mAlgoManager,
                                      boolean lockStatus,
                                      byte welcomeByte, byte lockByte, byte startByte,
                                      byte leftAreaByte, byte rightAreaByte, byte backAreaByte,
                                      byte walkAwayByte, byte approachByte, byte leftTurnByte,
                                      byte rightTurnByte, byte approachSideByte, byte approachRoadByte,
                                      byte recordByte, final InblueProtocolManager mProtocolManager, int beepInt) {
        final String comma = ";";
        String log = connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_TRUNK) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT) + comma +
                connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK) + comma +
                mAlgoManager.getOrientation()[0] + comma + mAlgoManager.getOrientation()[1] + comma + mAlgoManager.getOrientation()[2] + comma +
                mAlgoManager.getGravity()[0] + comma + mAlgoManager.getGravity()[1] + comma + mAlgoManager.getGravity()[2] + comma +
                mAlgoManager.getGeomagnetic()[0] + comma + mAlgoManager.getGeomagnetic()[1] + comma + mAlgoManager.getGeomagnetic()[2] + comma +
                mAlgoManager.getAcceleration() + comma +
                booleanToString(connectedCar.isActive(NUMBER_TRX_LEFT)) + comma + booleanToString(connectedCar.isActive(NUMBER_TRX_MIDDLE)) + comma +
                booleanToString(connectedCar.isActive(NUMBER_TRX_RIGHT)) + comma + booleanToString(connectedCar.isActive(NUMBER_TRX_TRUNK)) + comma +
                booleanToString(connectedCar.isActive(NUMBER_TRX_FRONT_LEFT)) + comma + booleanToString(connectedCar.isActive(NUMBER_TRX_FRONT_RIGHT)) + comma +
                booleanToString(connectedCar.isActive(NUMBER_TRX_REAR_LEFT)) + comma + booleanToString(connectedCar.isActive(NUMBER_TRX_REAR_RIGHT)) + comma +
                booleanToString(connectedCar.isActive(NUMBER_TRX_BACK)) + comma +
                booleanToString(mAlgoManager.isSmartphoneInPocket()) + comma +
                booleanToString(mAlgoManager.areLockActionsAvailable()) + comma;
        if (lockStatus) {
            log += "5" + comma;
        } else {
            log += "4" + comma;
        }
        if (mAlgoManager.getRearmLock()) {
            log += "7" + comma;
        } else {
            log += "6" + comma;
        }
        if (mAlgoManager.getRearmUnlock()) {
            log += "9" + comma;
        } else {
            log += "8" + comma;
        }
        log += booleanToString(mAlgoManager.getRearmWelcome()) + comma + welcomeByte + comma;
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
                + recordByte + comma + mAlgoManager.getPredictionPosition(connectedCar) + comma
                + booleanToString(mProtocolManager.isLockedFromTrx()) + comma
                + booleanToString(mProtocolManager.isLockedToSend()) + comma
                + booleanToString(mProtocolManager.isStartRequested()) + comma
                + booleanToString(mProtocolManager.isThatcham()) + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_LEFT).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_MIDDLE).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_RIGHT).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_TRUNK).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_FRONT_LEFT).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_FRONT_RIGHT).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_REAR_LEFT).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_REAR_RIGHT).toString() + comma
                + connectedCar.getCurrentBLEChannel(NUMBER_TRX_BACK).toString() + comma + beepInt + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_LEFT) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_MIDDLE) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_RIGHT) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_TRUNK) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_FRONT_LEFT) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_FRONT_RIGHT) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_REAR_LEFT) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_REAR_RIGHT) + comma
                + connectedCar.getCurrentAntennaId(NUMBER_TRX_BACK) + comma;
        appendTimestampToLog(log);
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
        appendTimestampToLog(log);
    }

    /**
     * Function used to debug and write logs into a file.
     */
    private static void appendTimestampToLog(String text) {
        String timestamp = sdfRssi.format(new Date());
        writeWithBufferedWriter(timestamp + ";" + text);
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
        String colNames = "TIMESTAMP;carType;carBase;addressConnectable;"
                + "addressConnectableRemote;addressConnectablePC;addressFrontLeft;addressFrontRight;"
                + "addressLeft;addressMiddle;addressRight;addressTrunk;"
                + "addressRearLeft;addressBack;addressRearRight;"
                + "logNumber;"
                + "preAuthTimeout;actionTimeout;wantedSpeed;stepSize;";
        writeWithBufferedWriter(colNames);
    }

    /**
     * Write in the log file a line with the rssi affiliated parameters column names
     */
    public static void writeFirstColumnLogs() {
        //Write 1st row with column names
        //BufferedWriter for performance, true to set append to file flag
        String colNames = "TIMESTAMP;"
                + "RSSI LEFT_ORIGIN;RSSI MIDDLE_ORIGIN;RSSI RIGHT_ORIGIN;"
                + "RSSI TRUNK_ORIGIN;RSSI FRONTLEFT_ORIGIN;RSSI FRONTRIGHT_ORIGIN;"
                + "RSSI REARLEFT_ORIGIN;RSSI REARRIGHT_ORIGIN;RSSI BACK_ORIGIN;"
                + "ORIENTATION_X;ORIENTATION_Y;ORIENTATION_Z;"
                + "GRAVITY_X;GRAVITY_Y;GRAVITY_Z;"
                + "GEOMAGNETIC_X;GEOMAGNETIC_Y;GEOMAGNETIC_Z;"
                + "ACCELERATION;"
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
                + "BLE CHANNEL REARLEFT;BLE CHANNEL REARRIGHT;BLE CHANNEL BACK;BEEPINT;"
                + "ANTENNA LEFT;ANTENNA MIDDLE;ANTENNA RIGHT;ANTENNA TRUNK;"
                + "ANTENNA FRONTLEFT;ANTENNA FRONTRIGHT;"
                + "ANTENNA REARLEFT;ANTENNA REARRIGHT;ANTENNA BACK;";
        writeWithBufferedWriter(colNames);
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
