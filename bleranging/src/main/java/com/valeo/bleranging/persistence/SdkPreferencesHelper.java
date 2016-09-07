package com.valeo.bleranging.persistence;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;

/**
 * Constants file for SharedPrefs
 */
public final class SdkPreferencesHelper {
    public final static int OFFSET_POCKET_FOR_START = -5;
    public final static int OFFSET_POCKET_FOR_LOCK = -5;
    public final static int OFFSET_POCKET_FOR_UNLOCK = -5;
    public final static int START_THRESHOLD = -73;
    public final static int UNLOCK_IN_THE_RUN_THRESHOLD = -65;
    public final static int WALK_AWAY_LOCKING_THRESHOLD = -70;
    public final static int WELCOME_THRESHOLD = -95;
    public final static int NEXT_TO_DOOR_RATIO_THRESHOLD = 8;
    public final static int NEXT_TO_BACKDOOR_RATIO_THRESHOLD_MIN = -5;
    public final static int NEXT_TO_BACKDOOR_RATIO_THRESHOLD_MAX = 5;
    public final static int NEXT_TO_DOOR_RATIO_THRESHOLD_ML_MR_MAX = 12;
    public final static int NEXT_TO_DOOR_RATIO_THRESHOLD_ML_MR_MIN = 2;
    public final static int AVERAGE_DELTA_UNLOCK_THRESHOLD = 10;
    public final static int AVERAGE_DELTA_LOCK_THRESHOLD = -10;
    public final static int RSSI_LOG_NUMBER = 0;
    public final static int ROLLING_AVERAGE_ELEMENTS = 50;
    public final static int START_NB_ELEMENT = 10;
    public final static int LOCK_NB_ELEMENT = 5;
    public final static int UNLOCK_NB_ELEMENT = 3;
    public final static int WELCOME_NB_ELEMENT = 10;
    public final static int LONG_NB_ELEMENT = 20;
    public final static int SHORT_NB_ELEMENT = 2;
    public final static int UNLOCK_MODE = 7;
    public final static int LOCK_MODE = 3;
    public final static int START_MODE = 12;
    public final static float ECRETAGE_70_100 = 20.0f;
    public final static float ECRETAGE_50_70 = 20.0f;
    public final static float ECRETAGE_30_50 = 25.0f;
    public final static float ECRETAGE_30_30 = 30.0f;
    public final static int LIN_ACC_SIZE = 50;
    public final static float CORRECTION_LIN_ACC = 4.0f;
    public final static int EQUALIZER_LEFT = 5;
    public final static int EQUALIZER_MIDDLE = 5;
    public final static int EQUALIZER_RIGHT = 5;
    public final static int EQUALIZER_BACK = 5;
    public final static int EQUALIZER_FRONT_LEFT = 5;
    public final static int EQUALIZER_FRONT_RIGHT = 5;
    public final static int EQUALIZER_REAR_LEFT = 5;
    public final static int EQUALIZER_REAR_RIGHT = 5;
    public final static String BLE_ADDRESS_CONNECTABLE = "D4:F5:13:56:73:88";
    public final static String BLE_ADDRESS_LEFT = "D4:F5:13:56:39:A5";
    public final static String BLE_ADDRESS_MIDDLE = "D4:F5:13:56:6B:65";
    public final static String BLE_ADDRESS_RIGHT = "D4:F5:13:56:65:16";
    public final static String BLE_ADDRESS_BACK = "78:A5:04:81:5A:21"; //"D4:F5:13:56:5C:B4";
    public final static String BLE_ADDRESS_FRONT_LEFT = "D4:F5:13:56:39:A6";
    public final static String BLE_ADDRESS_REAR_LEFT = "D4:F5:13:56:39:A7";
    public final static String BLE_ADDRESS_FRONT_RIGHT = "D4:F5:13:56:39:A8";
    public final static String BLE_ADDRESS_REAR_RIGHT = "D4:F5:13:56:39:A9";
    public final static String BLE_ADDRESS_37 = "D4:F5:13:56:7A:12";
    public final static String BLE_ADDRESS_38 = "D4:F5:13:56:37:32";
    public final static String BLE_ADDRESS_39 = "D4:F5:13:56:39:E7";
    /** Calibration preferences file name. */
    public static final String SAVED_SELECTED_LOCATION = "savedSelectedLocation";
    public static final String SAVED_LIGHT_CAPTOR = "savedLightCaptor";
    /** Calibration measurement key formatter. */
    public static final String ADDRESS_CONNECTABLE_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_CONNECTABLE";
    public static final String ADDRESS_LEFT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_LEFT";
    public static final String ADDRESS_MIDDLE_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_MIDDLE";
    public static final String ADDRESS_RIGHT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_RIGHT";
    public static final String ADDRESS_BACK_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_BACK";
    public static final String ADDRESS_FRONT_LEFT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_FRONT_LEFT";
    public static final String ADDRESS_REAR_LEFT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_REAR_LEFT";
    public static final String ADDRESS_FRONT_RIGHT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_FRONT_RIGHT";
    public static final String ADDRESS_REAR_RIGHT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_REAR_RIGHT";
    public static final String CONNECTED_CAR_TYPE_PREFERENCES_NAME = "com.inblue.PREFERENCE_CONNECTED_CAR_TYPE";
    public static final String OFFSET_POCKET_FOR_START_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_POCKET_FOR_START";
    public static final String OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_POCKET_FOR_LOCK";
    public static final String OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_POCKET_FOR_UNLOCK";
    public static final String START_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_START_THR";
    public static final String UNLOCK_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_UNLOCK_THR";
    public static final String LOCK_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_LOCK_THR";
    public static final String WELCOME_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_WELCOME_THR";
    public static final String NEXT_TO_DOOR_RATIO_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEXT_TO_DOOR_RATIO_THR";
    public static final String NEXT_TO_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEXT_TO_BACKDOOR_RATIO_THR_MIN";
    public static final String NEXT_TO_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEXT_TO_BACKDOOR_RATIO_THR_MAX";
    public static final String NEXT_TO_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEXT_TO_DOOR_RATIO_THR_ML_MR_MAX";
    public static final String NEXT_TO_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEXT_TO_DOOR_RATIO_THR_ML_MR_MIN";
    public static final String AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME = "com.inblue.PREFERENCE_AVERAGE_DELTA_LOCK_THRESHOLD";
    public static final String AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME = "com.inblue.PREFERENCE_AVERAGE_DELTA_UNLOCK_THRESHOLD";
    public static final String RSSI_LOG_NUMBER_PREFERENCES_NAME = "com.inblue.PREFERENCE_RSSI_LOG_NUMBER";
    public static final String ROLLING_AV_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_ROLLING_AV_ELEMENT";
    public static final String START_NB_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_START_NB_ELEMENT";
    public static final String LOCK_NB_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_LOCK_NB_ELEMENT";
    public static final String UNLOCK_NB_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_UNLOCK_NB_ELEMENT";
    public static final String WELCOME_NB_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_WELCOME_NB_ELEMENT";
    public static final String LONG_NB_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_LONG_NB_ELEMENT";
    public static final String SHORT_NB_ELEMENT_PREFERENCES_NAME = "com.inblue.PREFERENCE_SHORT_NB_ELEMENT";
    public static final String UNLOCK_MODE_PREFERENCES_NAME = "com.inblue.PREFERENCE_UNLOCK_MODE";
    public static final String LOCK_MODE_PREFERENCES_NAME = "com.inblue.PREFERENCE_LOCK_MODE";
    public static final String START_MODE_PREFERENCES_NAME = "com.inblue.PREFERENCE_START_MODE";
    public static final String ECRETAGE_70_100_PREFERENCES_NAME = "com.inblue.PREFERENCE_ECRETAGE_70_100";
    public static final String ECRETAGE_50_70_PREFERENCES_NAME = "com.inblue.PREFERENCE_ECRETAGE_50_70";
    public static final String ECRETAGE_30_50_PREFERENCES_NAME = "com.inblue.PREFERENCE_ECRETAGE_30_50";
    public static final String ECRETAGE_30_30_PREFERENCES_NAME = "com.inblue.PREFERENCE_ECRETAGE_30_30";
    public static final String LIGHT_CAPTOR_PREFERENCES_NAME = "com.inblue.PREFERENCE_LIGHT_CAPTOR";
    public static final String SELECTED_LOCATION_PREFERENCES_NAME = "com.inblue.PREFERENCE_SELECTED_LOCATION";
    public static final String LIN_ACC_SIZE_PREFERENCES_NAME = "com.inblue.PREFERENCE_LIN_ACC_SIZE";
    public static final String CORRECTION_LIN_ACC_PREFERENCES_NAME = "com.inblue.PREFERENCE_CORRECTION_LIN_ACC";
    public static final String EQUALIZER_LEFT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_LEFT";
    public static final String EQUALIZER_MIDDLE_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_MIDDLE";
    public static final String EQUALIZER_RIGHT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_RIGHT";
    public static final String EQUALIZER_BACK_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_BACK";
    public static final String EQUALIZER_FRONT_LEFT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_FRONT_LEFT";
    public static final String EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_FRONT_RIGHT";
    public static final String EQUALIZER_REAR_LEFT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_REAR_LEFT";
    public static final String EQUALIZER_REAR_RIGHT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_REAR_RIGHT";
    private static final String USER_MAIL_PREFERENCES_NAME = "com.inblue.PREFERENCE_USER_MAIL";
    private static final String PASSWORD_PREFERENCES_NAME = "com.inblue.PREFERENCE_PASSWORD";

    /** Single helper instance. */
    private static SdkPreferencesHelper sSingleInstance = null;

    /** Application context. */
    private Context mApplicationContext;

    /**
     * Private constructor.
     *
     * @param context the application context.
     */
    private SdkPreferencesHelper(final @NonNull Context context) {
        mApplicationContext = context;
    }

    /**
     * Initialize the helper instance.
     *
     * @param context the application context.
     */
    public static void initializeInstance(final @NonNull Context context) {
        if(sSingleInstance == null) {
            sSingleInstance = new SdkPreferencesHelper(context.getApplicationContext());
        }
    }

    /**
     * @return the single helper instance.
     */
    public static SdkPreferencesHelper getInstance() {
        return sSingleInstance;
    }

    public void setNextToDoorThresholdLMorMRMax(int nextToDoorRatioThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, NEXT_TO_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME, nextToDoorRatioThreshold);
    }

    public void setNextToDoorThresholdLMorMRMin(int nextToDoorRatioThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, NEXT_TO_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME, nextToDoorRatioThreshold);
    }

    public void setEqualizerLeft(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_LEFT_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerMiddle(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_MIDDLE_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerRight(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_RIGHT_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerBack(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_BACK_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerFrontLeft(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_FRONT_LEFT_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerFrontRight(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerRearLeft(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_REAR_LEFT_PREFERENCES_NAME, equalizer);
    }

    public void setEqualizerRearRight(int equalizer) {
        saveInt(SAVED_SELECTED_LOCATION, EQUALIZER_REAR_RIGHT_PREFERENCES_NAME, equalizer);
    }

    public void setSelectedLocation(final String selectedLocation) {
        saveString(SAVED_SELECTED_LOCATION, SELECTED_LOCATION_PREFERENCES_NAME, selectedLocation);
    }

    public String getSelectedLocation(final String defaultValue) {
        return readString(SAVED_SELECTED_LOCATION, SELECTED_LOCATION_PREFERENCES_NAME, defaultValue);
    }

    public boolean isLightCaptorEnabled() {
        return readBoolean(SAVED_LIGHT_CAPTOR, LIGHT_CAPTOR_PREFERENCES_NAME, false);
    }

    public void enableLightCaptorEnabled(final boolean enable) {
        saveBoolean(SAVED_LIGHT_CAPTOR, LIGHT_CAPTOR_PREFERENCES_NAME, enable);
    }

    public String getConnectedCarType() {
        return readString(SAVED_SELECTED_LOCATION, CONNECTED_CAR_TYPE_PREFERENCES_NAME, ConnectedCarFactory.TYPE_4_A);
    }

    public int getOffsetPocketForStart() {
        return readInt(SAVED_SELECTED_LOCATION, OFFSET_POCKET_FOR_START_PREFERENCES_NAME, OFFSET_POCKET_FOR_START);
    }

    public void setOffsetPocketForStart(int offsetPocket) {
        saveInt(SAVED_SELECTED_LOCATION, OFFSET_POCKET_FOR_START_PREFERENCES_NAME, offsetPocket);
    }

    public int getOffsetPocketForLock() {
        return readInt(SAVED_SELECTED_LOCATION, OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME, OFFSET_POCKET_FOR_LOCK);
    }

    public void setOffsetPocketForLock(int offsetPocket) {
        saveInt(SAVED_SELECTED_LOCATION, OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME, offsetPocket);
    }

    public int getOffsetPocketForUnlock() {
        return readInt(SAVED_SELECTED_LOCATION, OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME, OFFSET_POCKET_FOR_UNLOCK);
    }

    public void setOffsetPocketForUnlock(int offsetPocket) {
        saveInt(SAVED_SELECTED_LOCATION, OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME, offsetPocket);
    }

    public int getStartThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, START_THR_PREFERENCES_NAME, START_THRESHOLD);
    }

    public void setStartThreshold(int startThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, START_THR_PREFERENCES_NAME, startThreshold);
    }

    public int getUnlockThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, UNLOCK_THR_PREFERENCES_NAME, UNLOCK_IN_THE_RUN_THRESHOLD);
    }

    public void setUnlockThreshold(int unlockThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, UNLOCK_THR_PREFERENCES_NAME, unlockThreshold);
    }

    public int getLockThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, LOCK_THR_PREFERENCES_NAME, WALK_AWAY_LOCKING_THRESHOLD);
    }

    public void setLockThreshold(int lockThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, LOCK_THR_PREFERENCES_NAME, lockThreshold);
    }

    public int getWelcomeThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, WELCOME_THR_PREFERENCES_NAME, WELCOME_THRESHOLD);
    }

    public void setWelcomeThreshold(int welcomeThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, WELCOME_THR_PREFERENCES_NAME, welcomeThreshold);
    }

    public int getNextToDoorRatioThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, NEXT_TO_DOOR_RATIO_THR_PREFERENCES_NAME, NEXT_TO_DOOR_RATIO_THRESHOLD);
    }

    public void setNextToDoorRatioThreshold(int nextToDoorRatioThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, NEXT_TO_DOOR_RATIO_THR_PREFERENCES_NAME, nextToDoorRatioThreshold);
    }

    public int getNextToBackDoorRatioThresholdMin() {
        return readInt(SAVED_SELECTED_LOCATION, NEXT_TO_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME, NEXT_TO_BACKDOOR_RATIO_THRESHOLD_MIN);
    }

    public void setNextToBackDoorRatioThresholdMin(int nextToBackDoorRatioThresholdMin) {
        saveInt(SAVED_SELECTED_LOCATION, NEXT_TO_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME, nextToBackDoorRatioThresholdMin);
    }

    public int getNextToBackDoorRatioThresholdMax() {
        return readInt(SAVED_SELECTED_LOCATION, NEXT_TO_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME, NEXT_TO_BACKDOOR_RATIO_THRESHOLD_MAX);
    }

    public void setNextToBackDoorRatioThresholdMax(int nextToBackDoorRatioThresholdMax) {
        saveInt(SAVED_SELECTED_LOCATION, NEXT_TO_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME, nextToBackDoorRatioThresholdMax);
    }

    public int getNextToDoorThresholdMLorMRMax() {
        return readInt(SAVED_SELECTED_LOCATION, NEXT_TO_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME, NEXT_TO_DOOR_RATIO_THRESHOLD_ML_MR_MAX);
    }

    public int getNextToDoorThresholdMLorMRMin() {
        return readInt(SAVED_SELECTED_LOCATION, NEXT_TO_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME, NEXT_TO_DOOR_RATIO_THRESHOLD_ML_MR_MIN);
    }

    public int getRssiLogNumber() {
        return readInt(SAVED_SELECTED_LOCATION, RSSI_LOG_NUMBER_PREFERENCES_NAME, RSSI_LOG_NUMBER);
    }

    public void setRssiLogNumber(int rssiLogNumber) {
        saveInt(SAVED_SELECTED_LOCATION, RSSI_LOG_NUMBER_PREFERENCES_NAME, rssiLogNumber);
    }

    public int getAverageDeltaUnlockThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME, AVERAGE_DELTA_UNLOCK_THRESHOLD);
    }

    public void setAverageDeltaUnlockThreshold(int averageDeltaThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME, averageDeltaThreshold);
    }

    public int getAverageDeltaLockThreshold() {
        return readInt(SAVED_SELECTED_LOCATION, AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME, AVERAGE_DELTA_LOCK_THRESHOLD);
    }

    public void setAverageDeltaLockThreshold(int averageDeltaThreshold) {
        saveInt(SAVED_SELECTED_LOCATION, AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME, averageDeltaThreshold);
    }

    public int getRollingAvElement() {
        return readInt(SAVED_SELECTED_LOCATION, ROLLING_AV_ELEMENT_PREFERENCES_NAME, ROLLING_AVERAGE_ELEMENTS);
    }

    public void setRollingAvElement(int rollingAvElement) {
        saveInt(SAVED_SELECTED_LOCATION, ROLLING_AV_ELEMENT_PREFERENCES_NAME, rollingAvElement);
    }

    public int getStartNbElement() {
        return readInt(SAVED_SELECTED_LOCATION, START_NB_ELEMENT_PREFERENCES_NAME, START_NB_ELEMENT);
    }

    public void setStartNbElement(int startNbElement) {
        saveInt(SAVED_SELECTED_LOCATION, START_NB_ELEMENT_PREFERENCES_NAME, startNbElement);
    }

    public int getLockNbElement() {
        return readInt(SAVED_SELECTED_LOCATION, LOCK_NB_ELEMENT_PREFERENCES_NAME, LOCK_NB_ELEMENT);
    }

    public void setLockNbElement(int lockNbElement) {
        saveInt(SAVED_SELECTED_LOCATION, LOCK_NB_ELEMENT_PREFERENCES_NAME, lockNbElement);
    }

    public int getUnlockNbElement() {
        return readInt(SAVED_SELECTED_LOCATION, UNLOCK_NB_ELEMENT_PREFERENCES_NAME, UNLOCK_NB_ELEMENT);
    }

    public void setUnlockNbElement(int unlockNbElement) {
        saveInt(SAVED_SELECTED_LOCATION, UNLOCK_NB_ELEMENT_PREFERENCES_NAME, unlockNbElement);
    }

    public int getWelcomeNbElement() {
        return readInt(SAVED_SELECTED_LOCATION, WELCOME_NB_ELEMENT_PREFERENCES_NAME, WELCOME_NB_ELEMENT);
    }

    public void setWelcomeNbElement(int welcomeNbElement) {
        saveInt(SAVED_SELECTED_LOCATION, WELCOME_NB_ELEMENT_PREFERENCES_NAME, welcomeNbElement);
    }

    public int getLongNbElement() {
        return readInt(SAVED_SELECTED_LOCATION, LONG_NB_ELEMENT_PREFERENCES_NAME, LONG_NB_ELEMENT);
    }

    public void setLongNbElement(int longNbElement) {
        saveInt(SAVED_SELECTED_LOCATION, LONG_NB_ELEMENT_PREFERENCES_NAME, longNbElement);
    }

    public int getShortNbElement() {
        return readInt(SAVED_SELECTED_LOCATION, SHORT_NB_ELEMENT_PREFERENCES_NAME, SHORT_NB_ELEMENT);
    }

    public void setShortNbElement(int shortNbElement) {
        saveInt(SAVED_SELECTED_LOCATION, SHORT_NB_ELEMENT_PREFERENCES_NAME, shortNbElement);
    }

    public int getUnlockMode() {
        return readInt(SAVED_SELECTED_LOCATION, UNLOCK_MODE_PREFERENCES_NAME, UNLOCK_MODE);
    }

    public void setUnlockMode(int unlockMode) {
        saveInt(SAVED_SELECTED_LOCATION, UNLOCK_MODE_PREFERENCES_NAME, unlockMode);
    }

    public int getLockMode() {
        return readInt(SAVED_SELECTED_LOCATION, LOCK_MODE_PREFERENCES_NAME, LOCK_MODE);
    }

    public void setLockMode(int lockMode) {
        saveInt(SAVED_SELECTED_LOCATION, LOCK_MODE_PREFERENCES_NAME, lockMode);
    }

    public int getStartMode() {
        return readInt(SAVED_SELECTED_LOCATION, START_MODE_PREFERENCES_NAME, START_MODE);
    }

    public void setStartMode(int startMode) {
        saveInt(SAVED_SELECTED_LOCATION, START_MODE_PREFERENCES_NAME, startMode);
    }

    public float getEcretage70_100() {
        return readFloat(SAVED_SELECTED_LOCATION, ECRETAGE_70_100_PREFERENCES_NAME, ECRETAGE_70_100);
    }

    public void setEcretage70_100(float ecretage) {
        saveFloat(SAVED_SELECTED_LOCATION, ECRETAGE_70_100_PREFERENCES_NAME, ecretage);
    }

    public float getEcretage50_70() {
        return readFloat(SAVED_SELECTED_LOCATION, ECRETAGE_50_70_PREFERENCES_NAME, ECRETAGE_50_70);
    }

    public void setEcretage50_70(float ecretage) {
        saveFloat(SAVED_SELECTED_LOCATION, ECRETAGE_50_70_PREFERENCES_NAME, ecretage);
    }

    public float getEcretage30_50() {
        return readFloat(SAVED_SELECTED_LOCATION, ECRETAGE_30_50_PREFERENCES_NAME, ECRETAGE_30_50);
    }

    public void setEcretage30_50(float ecretage) {
        saveFloat(SAVED_SELECTED_LOCATION, ECRETAGE_30_50_PREFERENCES_NAME, ecretage);
    }

    public float getEcretage30_30() {
        return readFloat(SAVED_SELECTED_LOCATION, ECRETAGE_30_30_PREFERENCES_NAME, ECRETAGE_30_30);
    }

    public void setEcretage30_30(float ecretage) {
        saveFloat(SAVED_SELECTED_LOCATION, ECRETAGE_30_30_PREFERENCES_NAME, ecretage);
    }

    public String getTrxAddressConnectable() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_CONNECTABLE_PREFERENCE_NAME, BLE_ADDRESS_CONNECTABLE);
    }

    public void setTrxAddressConnectable(String address) {
        saveString(SAVED_SELECTED_LOCATION, ADDRESS_CONNECTABLE_PREFERENCE_NAME, address);
    }

    public String getTrxAddressLeft() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_LEFT_PREFERENCE_NAME, BLE_ADDRESS_LEFT);
    }

    public void setTrxAddressLeft(String address) {
        saveString(SAVED_SELECTED_LOCATION, ADDRESS_LEFT_PREFERENCE_NAME, address);
    }

    public String getTrxAddressMiddle() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_MIDDLE_PREFERENCE_NAME, BLE_ADDRESS_MIDDLE);
    }

    public void setTrxAddressMiddle(String address) {
        saveString(SAVED_SELECTED_LOCATION, ADDRESS_MIDDLE_PREFERENCE_NAME, address);
    }

    public String getTrxAddressRight() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_RIGHT_PREFERENCE_NAME, BLE_ADDRESS_RIGHT);
    }

    public void setTrxAddressRight(String address) {
        saveString(SAVED_SELECTED_LOCATION, ADDRESS_RIGHT_PREFERENCE_NAME, address);
    }

    public String getTrxAddressBack() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_BACK_PREFERENCE_NAME, BLE_ADDRESS_BACK);
    }

    public void setTrxAddressBack(String address) {
        saveString(SAVED_SELECTED_LOCATION, ADDRESS_BACK_PREFERENCE_NAME, address);
    }

    public String getTrxAddressFrontLeft() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_FRONT_LEFT_PREFERENCE_NAME, BLE_ADDRESS_FRONT_LEFT);
    }

    public String getTrxAddressRearLeft() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_REAR_LEFT_PREFERENCE_NAME, BLE_ADDRESS_REAR_LEFT);
    }

    public String getTrxAddressFrontRight() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_FRONT_RIGHT_PREFERENCE_NAME, BLE_ADDRESS_FRONT_RIGHT);
    }

    public String getTrxAddressRearRight() {
        return readString(SAVED_SELECTED_LOCATION, ADDRESS_REAR_RIGHT_PREFERENCE_NAME, BLE_ADDRESS_REAR_RIGHT);
    }

    public int getLinAccSize() {
        return readInt(SAVED_SELECTED_LOCATION, LIN_ACC_SIZE_PREFERENCES_NAME, LIN_ACC_SIZE);
    }

    public void setLinAccSize(int linAccSize) {
        saveInt(SAVED_SELECTED_LOCATION, LIN_ACC_SIZE_PREFERENCES_NAME, linAccSize);
    }

    public float getCorrectionLinAcc() {
        return readFloat(SAVED_SELECTED_LOCATION, CORRECTION_LIN_ACC_PREFERENCES_NAME, CORRECTION_LIN_ACC);
    }

    public void setCorrectionLinAcc(float correctionLinAcc) {
        saveFloat(SAVED_SELECTED_LOCATION, CORRECTION_LIN_ACC_PREFERENCES_NAME, correctionLinAcc);
    }

    public int getTrxRssiEqualizerLeft() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_LEFT_PREFERENCES_NAME, EQUALIZER_LEFT);
    }

    public int getTrxRssiEqualizerFrontLeft() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_FRONT_LEFT_PREFERENCES_NAME, EQUALIZER_FRONT_LEFT);
    }

    public int getTrxRssiEqualizerRearLeft() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_REAR_LEFT_PREFERENCES_NAME, EQUALIZER_REAR_LEFT);
    }

    public int getTrxRssiEqualizerMiddle() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_MIDDLE_PREFERENCES_NAME, EQUALIZER_MIDDLE);
    }

    public int getTrxRssiEqualizerRight() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_RIGHT_PREFERENCES_NAME, EQUALIZER_RIGHT);
    }

    public int getTrxRssiEqualizerFrontRight() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME, EQUALIZER_FRONT_RIGHT);
    }

    public int getTrxRssiEqualizerRearRight() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_REAR_RIGHT_PREFERENCES_NAME, EQUALIZER_REAR_RIGHT);
    }

    public int getTrxRssiEqualizerBack() {
        return readInt(SAVED_SELECTED_LOCATION, EQUALIZER_BACK_PREFERENCES_NAME, EQUALIZER_BACK);
    }

    public String getUserMail() {
        return readString(SAVED_SELECTED_LOCATION, USER_MAIL_PREFERENCES_NAME, "");
    }

    public void setUserMail(String userMail) {
        saveString(SAVED_SELECTED_LOCATION, USER_MAIL_PREFERENCES_NAME, userMail);
    }

    public String getPassword() {
        return readString(SAVED_SELECTED_LOCATION, PASSWORD_PREFERENCES_NAME, "");
    }

    public void setPassword(String password) {
        saveString(SAVED_SELECTED_LOCATION, PASSWORD_PREFERENCES_NAME, password);
    }

    // region Internal methods

    /**
     * Read a string value in the shared preferences.
     *
     * @param fileName     the preferences file name.
     * @param keyName      the value key name.
     * @param defaultValue the default value to return.
     * @return the read value, or the default one.
     */
    private String readString(final String fileName, final String keyName, final String defaultValue) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        return sharedPref.getString(keyName, defaultValue);
        return PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .getString(keyName, defaultValue);
    }

    /**
     * Save a string value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveString(final String fileName, final String keyName, final String value) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(keyName, value);
//        editor.commit();
        PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .edit().putString(keyName, value).commit();
    }

    /**
     * Read a boolean value in the shared preferences.
     *
     * @param fileName     the preferences file name.
     * @param keyName      the value key name.
     * @param defaultValue the default value to return.
     * @return the read value, or the default one.
     */
    private boolean readBoolean(final String fileName, final String keyName, final boolean defaultValue) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        return sharedPref.getBoolean(keyName, defaultValue);
        return Boolean.valueOf(PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .getString(keyName, String.valueOf(defaultValue)));
    }

    /**
     * Save a boolean value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveBoolean(final String fileName, final String keyName, final boolean value) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean(keyName, value);
//        editor.commit();
        PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .edit().putString(keyName, String.valueOf(value)).commit();
    }

    /**
     * Read a float value in the shared preferences.
     *
     * @param fileName     the preferences file name.
     * @param keyName      the value key name.
     * @param defaultValue the default value to return.
     * @return the read value, or the default one.
     */
    private float readFloat(final String fileName, final String keyName, final float defaultValue) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        return sharedPref.getFloat(keyName, defaultValue);
        return Float.valueOf(PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .getString(keyName, String.valueOf(defaultValue)));
    }

    /**
     * Save a float value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveFloat(final String fileName, final String keyName, final float value) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putFloat(keyName, value);
//        editor.commit();
        PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .edit().putString(keyName, String.valueOf(value)).commit();
    }

    /**
     * Read an integer value in the shared preferences.
     *
     * @param fileName     the preferences file name.
     * @param keyName      the value key name.
     * @param defaultValue the default value to return.
     * @return the read value, or the default one.
     */
    private int readInt(final String fileName, final String keyName, final int defaultValue) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        return sharedPref.getInt(keyName, defaultValue);
        return Integer.valueOf(PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .getString(keyName, String.valueOf(defaultValue)));
    }

    /**
     * Save an integer value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveInt(final String fileName, final String keyName, final int value) {
//        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putInt(keyName, value);
//        editor.commit();
        PreferenceManager
                .getDefaultSharedPreferences(mApplicationContext)
                .edit().putString(keyName, String.valueOf(value)).commit();
    }

    // endregion
}
