package com.valeo.bleranging.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;

/**
 * Constants file for SharedPrefs
 */
public final class SdkPreferencesHelper {
    public final static int OFFSET_EAR_FOR_START = -5;
    public final static int OFFSET_EAR_FOR_LOCK = -5;
    public final static int OFFSET_EAR_FOR_UNLOCK = -5;
    public final static int OFFSET_POCKET_FOR_START = -5;
    public final static int OFFSET_POCKET_FOR_LOCK = -5;
    public final static int OFFSET_POCKET_FOR_UNLOCK = -5;
    public final static int START_THRESHOLD = -73;
    public final static int UNLOCK_IN_THE_RUN_THRESHOLD = -65;
    public final static int WALK_AWAY_LOCKING_THRESHOLD = -70;
    public final static int WELCOME_THRESHOLD = -95;
    public final static int CLOSE_TO_BEACON_THRESHOLD = -50;
    public final static int NEAR_DOOR_RATIO_THRESHOLD = 8;
    public final static int NEAR_BACKDOOR_RATIO_THRESHOLD_MIN = -5;
    public final static int NEAR_BACKDOOR_RATIO_THRESHOLD_MAX = 5;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_MB = 0;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX = 12;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN = 2;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX = 12;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN = 2;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR = 10;
    public final static int NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR = 10;
    public final static int AVERAGE_DELTA_UNLOCK_THRESHOLD = 10;
    public final static int AVERAGE_DELTA_LOCK_THRESHOLD = -10;
    public final static float THATCHAM_TIMEOUT = 3.5f;
    public final static float CRYPTO_PRE_AUTH_TIMEOUT = 0f;
    public final static float CRYPTO_ACTION_TIMEOUT = 0f;
    public final static int RSSI_LOG_NUMBER = 0;
    public final static int ROLLING_AVERAGE_ELEMENTS = 50;
    public final static int START_NB_ELEMENT = 10;
    public final static int LOCK_NB_ELEMENT = 5;
    public final static int UNLOCK_NB_ELEMENT = 3;
    public final static int WELCOME_NB_ELEMENT = 10;
    public final static int LONG_NB_ELEMENT = 20;
    public final static int SHORT_NB_ELEMENT = 2;
    public final static int UNLOCK_VALID_NB = 1;
    public final static int UNLOCK_MODE = 7;
    public final static int LOCK_MODE = 3;
    public final static int START_MODE = 12;
    public final static float ECRETAGE_70_100 = 20.0f;
    public final static float ECRETAGE_50_70 = 20.0f;
    public final static float ECRETAGE_30_50 = 25.0f;
    public final static float ECRETAGE_30_30 = 30.0f;
    public final static int LIN_ACC_SIZE = 50;
    public final static float CORRECTION_LIN_ACC = 4.0f;
    public final static float FROZEN_THRESHOLD = 4.0f;
    public final static int EQUALIZER_LEFT = 5;
    public final static int EQUALIZER_MIDDLE = 5;
    public final static int EQUALIZER_RIGHT = 5;
    public final static int EQUALIZER_TRUNK = 5;
    public final static int EQUALIZER_BACK = 5;
    public final static int EQUALIZER_FRONT_LEFT = 5;
    public final static int EQUALIZER_FRONT_RIGHT = 5;
    public final static int EQUALIZER_REAR_LEFT = 5;
    public final static int EQUALIZER_REAR_RIGHT = 5;
    //    public static final int NEAR_DOOR_THR = 10;
//    public static final int EQUALLY_NEAR_DOOR_THR = 10;
//    public static final int NEARER_DOOR_THR = 10;
    public static final int RATIO_MAX_MIN_THR = 20;
    public static final int RATIO_CLOSE_TO_CAR_THR = 30;
    public final static String BLE_ADDRESS_CONNECTABLE = "D4:F5:13:56:73:88";
    public final static String BLE_ADDRESS_CONNECTABLE_PC = "B0:B4:48:BD:56:85";
    public final static String BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL = "5C:E0:C5:34:4D:32";
    public final static String BLE_ADDRESS_FRONT_LEFT = "D4:F5:13:56:5C:F0";
    public final static String BLE_ADDRESS_FRONT_RIGHT = "D4:F5:13:56:7C:C2";
    public final static String BLE_ADDRESS_LEFT = "D4:F5:13:56:39:A5";
    public final static String BLE_ADDRESS_MIDDLE = "D4:F5:13:56:6B:65";
    public final static String BLE_ADDRESS_RIGHT = "D4:F5:13:56:65:16";
    public final static String BLE_ADDRESS_TRUNK = "D4:F5:13:56:5C:B4";
    public final static String BLE_ADDRESS_REAR_LEFT = "D4:F5:13:56:7A:55";
    public final static String BLE_ADDRESS_BACK = "78:A5:04:81:5A:21";
    public final static String BLE_ADDRESS_REAR_RIGHT = "D4:F5:13:56:71:4B";
    public final static String BLE_ADDRESS_37 = "D4:F5:13:56:7A:12";
    public final static String BLE_ADDRESS_38 = "D4:F5:13:56:37:32";
    public final static String BLE_ADDRESS_39 = "D4:F5:13:56:39:E7";
    public static final String SAVED_CC_GENERIC_OPTION = "savedConnectedCarGenericOption";
    /** Key formatter. */
    public static final String ADDRESS_CONNECTABLE_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_CONNECTABLE";
    public static final String ADDRESS_CONNECTABLE_PC_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_CONNECTABLE_PC";
    public static final String ADDRESS_CONNECTABLE_REMOTE_CONTROL_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_CONNECTABLE_REMOTE_CONTROL";
    public static final String ADDRESS_LEFT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_LEFT";
    public static final String ADDRESS_MIDDLE_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_MIDDLE";
    public static final String ADDRESS_RIGHT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_RIGHT";
    public static final String ADDRESS_TRUNK_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_TRUNK";
    public static final String ADDRESS_BACK_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_BACK";
    public static final String ADDRESS_FRONT_LEFT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_FRONT_LEFT";
    public static final String ADDRESS_REAR_LEFT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_REAR_LEFT";
    public static final String ADDRESS_FRONT_RIGHT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_FRONT_RIGHT";
    public static final String ADDRESS_REAR_RIGHT_PREFERENCE_NAME = "com.inblue.PREFERENCE_ADDRESS_REAR_RIGHT";
    public static final String CONNECTED_CAR_TYPE_PREFERENCES_NAME = "com.inblue.PREFERENCE_CONNECTED_CAR_TYPE";
    public static final String CONNECTED_CAR_BASE_PREFERENCES_NAME = "com.inblue.PREFERENCE_CONNECTED_CAR_BASE";
    public static final String CONNECTED_CAR_TRAME_ENABLED_PREFERENCES_NAME = "com.inblue.PREFERENCE_CONNECTED_CAR_TRAME_ENABLED";
    public static final String CONNECTED_CAR_TRAME_PREFERENCES_NAME = "com.inblue.PREFERENCE_CONNECTED_CAR_TRAME";
    public static final String OFFSET_EAR_FOR_START_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_EAR_FOR_START";
    public static final String OFFSET_EAR_FOR_LOCK_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_EAR_FOR_LOCK";
    public static final String OFFSET_EAR_FOR_UNLOCK_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_EAR_FOR_UNLOCK";
    public static final String OFFSET_POCKET_FOR_START_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_POCKET_FOR_START";
    public static final String OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_POCKET_FOR_LOCK";
    public static final String OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME = "com.inblue.PREFERENCE_OFFSET_POCKET_FOR_UNLOCK";
    public static final String START_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_START_THR";
    public static final String UNLOCK_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_UNLOCK_THR";
    public static final String LOCK_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_LOCK_THR";
    public static final String WELCOME_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_WELCOME_THR";
    public static final String CLOSE_TO_BEACON_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_CLOSE_TO_BEACON_THR";
    public static final String NEAR_DOOR_RATIO_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR";
    public static final String NEAR_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_BACKDOOR_RATIO_THR_MIN";
    public static final String NEAR_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_BACKDOOR_RATIO_THR_MAX";
    public static final String NEAR_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_ML_MR_MAX";
    public static final String NEAR_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_ML_MR_MIN";
    public static final String NEAR_DOOR_RATIO_THR_TL_TR_MAX_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_TL_TR_MAX";
    public static final String NEAR_DOOR_RATIO_THR_TL_TR_MIN_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_TL_TR_MIN";
    public static final String NEAR_DOOR_RATIO_THR_MRL_MRR_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_MRL_MRR";
    public static final String NEAR_DOOR_RATIO_THR_TRL_TRR_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_TRL_TRR";
    public static final String NEAR_DOOR_RATIO_THR_MB_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_RATIO_THR_MB";
    public static final String AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME = "com.inblue.PREFERENCE_AVERAGE_DELTA_LOCK_THRESHOLD";
    public static final String AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME = "com.inblue.PREFERENCE_AVERAGE_DELTA_UNLOCK_THRESHOLD";
    public static final String UNLOCK_VALID_NB_PREFERENCES_NAME = "com.inblue.PREFERENCE_UNLOCK_VALID_NB_ELEMENT";
    public static final String THATCHAM_TIMEOUT_PREFERENCES_NAME = "com.inblue.PREFERENCE_THATCHAM_TIMEOUT";
    public static final String CRYPTO_PRE_AUTH_TIMEOUT_PREFERENCES_NAME = "com.inblue.PREFERENCE_CRYPTO_PRE_AUTH_TIMEOUT";
    public static final String CRYPTO_ACTION_TIMEOUT_PREFERENCES_NAME = "com.inblue.PREFERENCE_CRYPTO_ACTION_TIMEOUT";
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
    public static final String LIN_ACC_SIZE_PREFERENCES_NAME = "com.inblue.PREFERENCE_LIN_ACC_SIZE";
    public static final String CORRECTION_LIN_ACC_PREFERENCES_NAME = "com.inblue.PREFERENCE_CORRECTION_LIN_ACC";
    public static final String FROZEN_THRESHOLD_PREFERENCES_NAME = "com.inblue.PREFERENCE_FROZEN_THRESHOLD";
    public static final String EQUALIZER_LEFT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_LEFT";
    public static final String EQUALIZER_MIDDLE_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_MIDDLE";
    public static final String EQUALIZER_RIGHT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_RIGHT";
    public static final String EQUALIZER_TRUNK_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_TRUNK";
    public static final String EQUALIZER_BACK_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_BACK";
    public static final String EQUALIZER_FRONT_LEFT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_FRONT_LEFT";
    public static final String EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_FRONT_RIGHT";
    public static final String EQUALIZER_REAR_LEFT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_REAR_LEFT";
    public static final String EQUALIZER_REAR_RIGHT_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALIZER_REAR_RIGHT";
    //    public static final String NEAR_DOOR_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEAR_DOOR_THR";
//    public static final String EQUALLY_NEAR_DOOR_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_EQUALLY_NEAR_DOOR_THR";
//    public static final String NEARER_DOOR_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_NEARER_DOOR_THR";
    public static final String RATIO_MAX_MIN_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_RATIO_MAX_MIN_THR";
    public static final String RATIO_CLOSE_TO_CAR_THR_PREFERENCES_NAME = "com.inblue.PREFERENCE_CLOSE_TO_CAR_THR";
    private final static String LOG_FILE_NAME = "sdcard/InBlueRssi/allRssi_0_0000.csv";
    /**
     * Preferences file name.
     */
    private static final String SAVED_LIGHT_CAPTOR = "savedLightCaptor";
    private static final String SAVED_LOGIN_INFO = "savedLoginInfo";
    private static final String SAVED_LOGGER_INFO = "savedLoggerInfo";
    private static final String LIGHT_CAPTOR_PREFERENCES_NAME = "com.inblue.PREFERENCE_LIGHT_CAPTOR";
    //    private static final String SELECTED_LOCATION_PREFERENCES_NAME = "com.inblue.PREFERENCE_SELECTED_LOCATION";
    private static final String USER_MAIL_PREFERENCES_NAME = "com.inblue.PREFERENCE_USER_MAIL";
    private static final String PASSWORD_PREFERENCES_NAME = "com.inblue.PREFERENCE_PASSWORD";
    private static final String LOG_FILE_NAME_PREFERENCES_NAME = "com.inblue.PREFERENCE_LOG_FILE_NAME";

    /** Single helper instance. */
    private static SdkPreferencesHelper sSingleInstance = null;

    /** Application context. */
    private final Context mApplicationContext;

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

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearDoorThreshold(final String fileName, int nearDoorThreshold) {
//        saveInt(fileName, NEAR_DOOR_THR_PREFERENCES_NAME, nearDoorThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEquallyNearThreshold(final String fileName, int equallyNearDoorThreshold) {
//        saveInt(fileName, EQUALLY_NEAR_DOOR_THR_PREFERENCES_NAME, equallyNearDoorThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearerThreshold(final String fileName, int nearerDoorThreshold) {
//        saveInt(fileName, NEARER_DOOR_THR_PREFERENCES_NAME, nearerDoorThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setRatioMaxMinThreshold(final String fileName, int ratioMaxMinThreshold) {
//        saveInt(fileName, RATIO_MAX_MIN_THR_PREFERENCES_NAME, ratioMaxMinThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    // --Commented out by Inspection START (30/09/2016 11:26):
//    public void setRatioCloseToCarThreshold(final String fileName, int ratioCloseToCarThreshold) {
//        saveInt(fileName, RATIO_CLOSE_TO_CAR_THR_PREFERENCES_NAME, ratioCloseToCarThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
// --Commented out by Inspection START (30/09/2016 11:26):
//    public int getNearDoorThreshold(final String fileName) {
//        return readInt(fileName, NEAR_DOOR_THR_PREFERENCES_NAME, NEAR_DOOR_THR);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
// --Commented out by Inspection START (30/09/2016 11:26):
//    public int getEquallyNearThreshold(final String fileName) {
//        return readInt(fileName, EQUALLY_NEAR_DOOR_THR_PREFERENCES_NAME, EQUALLY_NEAR_DOOR_THR);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
// --Commented out by Inspection START (30/09/2016 11:26):
//    public int getNearerThreshold(final String fileName) {
//        return readInt(fileName, NEARER_DOOR_THR_PREFERENCES_NAME, NEARER_DOOR_THR);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)
    public int getRatioMaxMinThreshold(final String fileName) {
        return readInt(fileName, RATIO_MAX_MIN_THR_PREFERENCES_NAME, RATIO_MAX_MIN_THR);
    }

    public int getRatioCloseToCarThreshold(final String fileName) {
        return readInt(fileName, RATIO_CLOSE_TO_CAR_THR_PREFERENCES_NAME, RATIO_CLOSE_TO_CAR_THR);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearDoorThresholdLMorMRMax(final String fileName, int nearDoorRatioThreshold) {
//        saveInt(fileName, NEAR_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME, nearDoorRatioThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearDoorThresholdMB(final String fileName, int nearDoorRatioThreshold) {
//        saveInt(fileName, NEAR_DOOR_RATIO_THR_MB_PREFERENCES_NAME, nearDoorRatioThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearDoorThresholdLMorMRMin(final String fileName, int nearDoorRatioThreshold) {
//        saveInt(fileName, NEAR_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME, nearDoorRatioThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerLeft(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_LEFT_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerMiddle(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_MIDDLE_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerRight(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_RIGHT_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerBack(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_BACK_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerFrontLeft(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_FRONT_LEFT_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerFrontRight(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerRearLeft(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_REAR_LEFT_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEqualizerRearRight(final String fileName, int equalizer) {
//        saveInt(fileName, EQUALIZER_REAR_RIGHT_PREFERENCES_NAME, equalizer);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setSelectedLocation(final String fileName, final String selectedLocation) {
//        saveString(fileName, SELECTED_LOCATION_PREFERENCES_NAME, selectedLocation);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public String getSelectedLocation(final String fileName, final String defaultValue) {
//        return readString(fileName, SELECTED_LOCATION_PREFERENCES_NAME, defaultValue);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getOffsetPocketForStart(final String fileName) {
        return readInt(fileName, OFFSET_POCKET_FOR_START_PREFERENCES_NAME, OFFSET_POCKET_FOR_START);
    }

    public int getOffsetEarForStart(final String fileName) {
        return readInt(fileName, OFFSET_EAR_FOR_START_PREFERENCES_NAME, OFFSET_EAR_FOR_START);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setOffsetPocketForStart(final String fileName, int offsetPocket) {
//        saveInt(fileName, OFFSET_POCKET_FOR_START_PREFERENCES_NAME, offsetPocket);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getOffsetPocketForLock(final String fileName) {
        return readInt(fileName, OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME, OFFSET_POCKET_FOR_LOCK);
    }

    public int getOffsetEarForLock(final String fileName) {
        return readInt(fileName, OFFSET_EAR_FOR_LOCK_PREFERENCES_NAME, OFFSET_EAR_FOR_LOCK);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setOffsetPocketForLock(final String fileName, int offsetPocket) {
//        saveInt(fileName, OFFSET_POCKET_FOR_LOCK_PREFERENCES_NAME, offsetPocket);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getOffsetPocketForUnlock(final String fileName) {
        return readInt(fileName, OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME, OFFSET_POCKET_FOR_UNLOCK);
    }

    public int getOffsetEarForUnlock(final String fileName) {
        return readInt(fileName, OFFSET_EAR_FOR_UNLOCK_PREFERENCES_NAME, OFFSET_EAR_FOR_UNLOCK);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setOffsetPocketForUnlock(final String fileName, int offsetPocket) {
//        saveInt(fileName, OFFSET_POCKET_FOR_UNLOCK_PREFERENCES_NAME, offsetPocket);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getStartThreshold(final String fileName) {
        return readInt(fileName, START_THR_PREFERENCES_NAME, START_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setStartThreshold(final String fileName, int startThreshold) {
//        saveInt(fileName, START_THR_PREFERENCES_NAME, startThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getUnlockThreshold(final String fileName) {
        return readInt(fileName, UNLOCK_THR_PREFERENCES_NAME, UNLOCK_IN_THE_RUN_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setUnlockThreshold(final String fileName, int unlockThreshold) {
//        saveInt(fileName, UNLOCK_THR_PREFERENCES_NAME, unlockThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getLockThreshold(final String fileName) {
        return readInt(fileName, LOCK_THR_PREFERENCES_NAME, WALK_AWAY_LOCKING_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setLockThreshold(final String fileName, int lockThreshold) {
//        saveInt(fileName, LOCK_THR_PREFERENCES_NAME, lockThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getWelcomeThreshold(final String fileName) {
        return readInt(fileName, WELCOME_THR_PREFERENCES_NAME, WELCOME_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setWelcomeThreshold(final String fileName, int welcomeThreshold) {
//        saveInt(fileName, WELCOME_THR_PREFERENCES_NAME, welcomeThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getCloseToBeaconThreshold(final String fileName) {
        return readInt(fileName, CLOSE_TO_BEACON_THR_PREFERENCES_NAME, CLOSE_TO_BEACON_THRESHOLD);
    }

    public int getNearDoorRatioThreshold(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearDoorRatioThreshold(final String fileName, int nearDoorRatioThreshold) {
//        saveInt(fileName, NEAR_DOOR_RATIO_THR_PREFERENCES_NAME, nearDoorRatioThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getNearBackDoorRatioThresholdMin(final String fileName) {
        return readInt(fileName, NEAR_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME, NEAR_BACKDOOR_RATIO_THRESHOLD_MIN);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearBackDoorRatioThresholdMin(final String fileName, int nearBackDoorRatioThresholdMin) {
//        saveInt(fileName, NEAR_BACKDOOR_RATIO_THR_MIN_PREFERENCES_NAME, nearBackDoorRatioThresholdMin);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getNearBackDoorRatioThresholdMax(final String fileName) {
        return readInt(fileName, NEAR_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME, NEAR_BACKDOOR_RATIO_THRESHOLD_MAX);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setNearBackDoorRatioThresholdMax(final String fileName, int nearBackDoorRatioThresholdMax) {
//        saveInt(fileName, NEAR_BACKDOOR_RATIO_THR_MAX_PREFERENCES_NAME, nearBackDoorRatioThresholdMax);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getNearDoorThresholdMLorMRMax(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_ML_MR_MAX_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX);
    }

    public int getNearDoorThresholdTLorTRMax(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_TL_TR_MAX_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX);
    }

    public int getNearDoorThresholdMB(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_MB_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_MB);
    }

    public int getNearDoorThresholdMLorMRMin(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_ML_MR_MIN_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN);
    }

    public int getNearDoorThresholdTLorTRMin(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_TL_TR_MIN_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN);
    }

    public int getNearDoorThresholdMRLorMRR(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_MRL_MRR_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR);
    }

    public int getNearDoorThresholdTRLorTRR(final String fileName) {
        return readInt(fileName, NEAR_DOOR_RATIO_THR_TRL_TRR_PREFERENCES_NAME, NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR);
    }

    public int getAverageDeltaUnlockThreshold(final String fileName) {
        return readInt(fileName, AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME, AVERAGE_DELTA_UNLOCK_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setAverageDeltaUnlockThreshold(final String fileName, int averageDeltaThreshold) {
//        saveInt(fileName, AVERAGE_DELTA_UNLOCK_THRESHOLD_PREFERENCES_NAME, averageDeltaThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getAverageDeltaLockThreshold(final String fileName) {
        return readInt(fileName, AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME, AVERAGE_DELTA_LOCK_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setAverageDeltaLockThreshold(final String fileName, int averageDeltaThreshold) {
//        saveInt(fileName, AVERAGE_DELTA_LOCK_THRESHOLD_PREFERENCES_NAME, averageDeltaThreshold);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getUnlockValidNb(final String fileName) {
        return readInt(fileName, UNLOCK_VALID_NB_PREFERENCES_NAME, UNLOCK_VALID_NB);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setUnlockValidNb(final String fileName, int unlockValidNb) {
//        saveInt(fileName, UNLOCK_VALID_NB_PREFERENCES_NAME, unlockValidNb);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getUnlockMode(final String fileName) {
        return readInt(fileName, UNLOCK_MODE_PREFERENCES_NAME, UNLOCK_MODE);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setUnlockMode(final String fileName, int unlockMode) {
//        saveInt(fileName, UNLOCK_MODE_PREFERENCES_NAME, unlockMode);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getLockMode(final String fileName) {
        return readInt(fileName, LOCK_MODE_PREFERENCES_NAME, LOCK_MODE);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setLockMode(final String fileName, int lockMode) {
//        saveInt(fileName, LOCK_MODE_PREFERENCES_NAME, lockMode);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getStartMode(final String fileName) {
        return readInt(fileName, START_MODE_PREFERENCES_NAME, START_MODE);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setStartMode(final String fileName, int startMode) {
//        saveInt(fileName, START_MODE_PREFERENCES_NAME, startMode);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public float getEcretage70_100(final String fileName) {
        return readFloat(fileName, ECRETAGE_70_100_PREFERENCES_NAME, ECRETAGE_70_100);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEcretage70_100(final String fileName, float ecretage) {
//        saveFloat(fileName, ECRETAGE_70_100_PREFERENCES_NAME, ecretage);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public float getEcretage50_70(final String fileName) {
        return readFloat(fileName, ECRETAGE_50_70_PREFERENCES_NAME, ECRETAGE_50_70);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEcretage50_70(final String fileName, float ecretage) {
//        saveFloat(fileName, ECRETAGE_50_70_PREFERENCES_NAME, ecretage);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public float getEcretage30_50(final String fileName) {
        return readFloat(fileName, ECRETAGE_30_50_PREFERENCES_NAME, ECRETAGE_30_50);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEcretage30_50(final String fileName, float ecretage) {
//        saveFloat(fileName, ECRETAGE_30_50_PREFERENCES_NAME, ecretage);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public float getEcretage30_30(final String fileName) {
        return readFloat(fileName, ECRETAGE_30_30_PREFERENCES_NAME, ECRETAGE_30_30);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setEcretage30_30(final String fileName, float ecretage) {
//        saveFloat(fileName, ECRETAGE_30_30_PREFERENCES_NAME, ecretage);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getTrxRssiEqualizerLeft(final String fileName) {
        return readInt(fileName, EQUALIZER_LEFT_PREFERENCES_NAME, EQUALIZER_LEFT);
    }

    public int getTrxRssiEqualizerFrontLeft(final String fileName) {
        return readInt(fileName, EQUALIZER_FRONT_LEFT_PREFERENCES_NAME, EQUALIZER_FRONT_LEFT);
    }

    public int getTrxRssiEqualizerRearLeft(final String fileName) {
        return readInt(fileName, EQUALIZER_REAR_LEFT_PREFERENCES_NAME, EQUALIZER_REAR_LEFT);
    }

    public int getTrxRssiEqualizerMiddle(final String fileName) {
        return readInt(fileName, EQUALIZER_MIDDLE_PREFERENCES_NAME, EQUALIZER_MIDDLE);
    }

    public int getTrxRssiEqualizerRight(final String fileName) {
        return readInt(fileName, EQUALIZER_RIGHT_PREFERENCES_NAME, EQUALIZER_RIGHT);
    }

    public int getTrxRssiEqualizerTrunk(final String fileName) {
        return readInt(fileName, EQUALIZER_TRUNK_PREFERENCES_NAME, EQUALIZER_TRUNK);
    }

    public int getTrxRssiEqualizerFrontRight(final String fileName) {
        return readInt(fileName, EQUALIZER_FRONT_RIGHT_PREFERENCES_NAME, EQUALIZER_FRONT_RIGHT);
    }

    public int getTrxRssiEqualizerRearRight(final String fileName) {
        return readInt(fileName, EQUALIZER_REAR_RIGHT_PREFERENCES_NAME, EQUALIZER_REAR_RIGHT);
    }

    public int getTrxRssiEqualizerBack(final String fileName) {
        return readInt(fileName, EQUALIZER_BACK_PREFERENCES_NAME, EQUALIZER_BACK);
    }

    public boolean isLightCaptorEnabled() {
        return readBoolean(SAVED_LIGHT_CAPTOR, LIGHT_CAPTOR_PREFERENCES_NAME, false);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void enableLightCaptorEnabled(final boolean enable) {
//        saveBoolean(SAVED_LIGHT_CAPTOR, LIGHT_CAPTOR_PREFERENCES_NAME, enable);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getConnectedCarType() {
        return readString(SAVED_CC_GENERIC_OPTION, CONNECTED_CAR_TYPE_PREFERENCES_NAME, ConnectedCarFactory.TYPE_4_A);
    }

    public String getConnectedCarBase() {
        return readString(SAVED_CC_GENERIC_OPTION, CONNECTED_CAR_BASE_PREFERENCES_NAME, ConnectedCarFactory.BASE_3);
    }

    public Boolean getConnectedCarTrameEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, CONNECTED_CAR_TRAME_ENABLED_PREFERENCES_NAME, false);
    }

    public String getConnectedCarTrame() {
        return readString(SAVED_CC_GENERIC_OPTION, CONNECTED_CAR_TRAME_PREFERENCES_NAME, "");
    }

    public float getThatchamTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, THATCHAM_TIMEOUT_PREFERENCES_NAME, THATCHAM_TIMEOUT);
    }

    public float getCryptoPreAuthTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, CRYPTO_PRE_AUTH_TIMEOUT_PREFERENCES_NAME, CRYPTO_PRE_AUTH_TIMEOUT);
    }

    public float getCryptoActionTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, CRYPTO_ACTION_TIMEOUT_PREFERENCES_NAME, CRYPTO_ACTION_TIMEOUT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setThatchamTimeout(float thatchamTimeout) {
//        saveFloat(SAVED_CC_GENERIC_OPTION, THATCHAM_TIMEOUT_PREFERENCES_NAME, thatchamTimeout);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getRssiLogNumber() {
        return readInt(SAVED_CC_GENERIC_OPTION, RSSI_LOG_NUMBER_PREFERENCES_NAME, RSSI_LOG_NUMBER);
    }

    public void setRssiLogNumber(int rssiLogNumber) {
        saveInt(SAVED_CC_GENERIC_OPTION, RSSI_LOG_NUMBER_PREFERENCES_NAME, rssiLogNumber);
    }

    public int getRollingAvElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, ROLLING_AV_ELEMENT_PREFERENCES_NAME, ROLLING_AVERAGE_ELEMENTS);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setRollingAvElement(int rollingAvElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, ROLLING_AV_ELEMENT_PREFERENCES_NAME, rollingAvElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getStartNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, START_NB_ELEMENT_PREFERENCES_NAME, START_NB_ELEMENT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setStartNbElement(int startNbElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, START_NB_ELEMENT_PREFERENCES_NAME, startNbElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getLockNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, LOCK_NB_ELEMENT_PREFERENCES_NAME, LOCK_NB_ELEMENT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setLockNbElement(int lockNbElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, LOCK_NB_ELEMENT_PREFERENCES_NAME, lockNbElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getUnlockNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, UNLOCK_NB_ELEMENT_PREFERENCES_NAME, UNLOCK_NB_ELEMENT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setUnlockNbElement(int unlockNbElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, UNLOCK_NB_ELEMENT_PREFERENCES_NAME, unlockNbElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getWelcomeNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, WELCOME_NB_ELEMENT_PREFERENCES_NAME, WELCOME_NB_ELEMENT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setWelcomeNbElement(int welcomeNbElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, WELCOME_NB_ELEMENT_PREFERENCES_NAME, welcomeNbElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getLongNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, LONG_NB_ELEMENT_PREFERENCES_NAME, LONG_NB_ELEMENT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setLongNbElement(int longNbElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, LONG_NB_ELEMENT_PREFERENCES_NAME, longNbElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public int getShortNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, SHORT_NB_ELEMENT_PREFERENCES_NAME, SHORT_NB_ELEMENT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setShortNbElement(int shortNbElement) {
//        saveInt(SAVED_CC_GENERIC_OPTION, SHORT_NB_ELEMENT_PREFERENCES_NAME, shortNbElement);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getTrxAddressConnectable() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_CONNECTABLE_PREFERENCE_NAME, BLE_ADDRESS_CONNECTABLE);
    }

    public String getTrxAddressConnectablePC() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_CONNECTABLE_PC_PREFERENCE_NAME, BLE_ADDRESS_CONNECTABLE_PC);
    }

    public void setTrxAddressConnectablePC(String address) {
        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_CONNECTABLE_PC_PREFERENCE_NAME, address);
    }

    public String getTrxAddressConnectableRemoteControl() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_CONNECTABLE_REMOTE_CONTROL_PREFERENCE_NAME, BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL);
    }

    public void setTrxAddressConnectableRemoteControl(String address) {
        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_CONNECTABLE_REMOTE_CONTROL_PREFERENCE_NAME, address);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setTrxAddressConnectable(String address) {
//        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_CONNECTABLE_PREFERENCE_NAME, address);
//    }
//
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getTrxAddressLeft() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_LEFT_PREFERENCE_NAME, BLE_ADDRESS_LEFT);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setTrxAddressLeft(String address) {
//        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_LEFT_PREFERENCE_NAME, address);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getTrxAddressMiddle() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_MIDDLE_PREFERENCE_NAME, BLE_ADDRESS_MIDDLE);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setTrxAddressMiddle(String address) {
//        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_MIDDLE_PREFERENCE_NAME, address);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getTrxAddressRight() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_RIGHT_PREFERENCE_NAME, BLE_ADDRESS_RIGHT);
    }

    public String getTrxAddressTrunk() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_TRUNK_PREFERENCE_NAME, BLE_ADDRESS_TRUNK);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setTrxAddressRight(String address) {
//        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_RIGHT_PREFERENCE_NAME, address);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getTrxAddressBack() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_BACK_PREFERENCE_NAME, BLE_ADDRESS_BACK);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setTrxAddressBack(String address) {
//        saveString(SAVED_CC_GENERIC_OPTION, ADDRESS_BACK_PREFERENCE_NAME, address);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public String getTrxAddressFrontLeft() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_FRONT_LEFT_PREFERENCE_NAME, BLE_ADDRESS_FRONT_LEFT);
    }

    public String getTrxAddressRearLeft() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_REAR_LEFT_PREFERENCE_NAME, BLE_ADDRESS_REAR_LEFT);
    }

    public String getTrxAddressFrontRight() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_FRONT_RIGHT_PREFERENCE_NAME, BLE_ADDRESS_FRONT_RIGHT);
    }

    public String getTrxAddressRearRight() {
        return readString(SAVED_CC_GENERIC_OPTION, ADDRESS_REAR_RIGHT_PREFERENCE_NAME, BLE_ADDRESS_REAR_RIGHT);
    }

    public int getLinAccSize() {
        return readInt(SAVED_CC_GENERIC_OPTION, LIN_ACC_SIZE_PREFERENCES_NAME, LIN_ACC_SIZE);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setLinAccSize(int linAccSize) {
//        saveInt(SAVED_CC_GENERIC_OPTION, LIN_ACC_SIZE_PREFERENCES_NAME, linAccSize);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public float getCorrectionLinAcc() {
        return readFloat(SAVED_CC_GENERIC_OPTION, CORRECTION_LIN_ACC_PREFERENCES_NAME, CORRECTION_LIN_ACC);
    }

    public float getFrozenThreshold() {
        return readFloat(SAVED_CC_GENERIC_OPTION, FROZEN_THRESHOLD_PREFERENCES_NAME, FROZEN_THRESHOLD);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public void setCorrectionLinAcc(float correctionLinAcc) {
//        saveFloat(SAVED_CC_GENERIC_OPTION, CORRECTION_LIN_ACC_PREFERENCES_NAME, correctionLinAcc);
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

// --Commented out by Inspection START (30/09/2016 11:26):
//    public String getUserMail() {
//        return readString(SAVED_LOGIN_INFO, USER_MAIL_PREFERENCES_NAME, "");
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public void setUserMail(String userMail) {
        saveString(SAVED_LOGIN_INFO, USER_MAIL_PREFERENCES_NAME, userMail);
    }

// --Commented out by Inspection START (30/09/2016 11:26):
//    public String getPassword() {
//        return readString(SAVED_LOGIN_INFO, PASSWORD_PREFERENCES_NAME, "");
//    }
// --Commented out by Inspection STOP (30/09/2016 11:26)

    public void setPassword(String password) {
        saveString(SAVED_LOGIN_INFO, PASSWORD_PREFERENCES_NAME, password);
    }

    public String getLogFileName() {
        return readString(SAVED_LOGGER_INFO, LOG_FILE_NAME_PREFERENCES_NAME, LOG_FILE_NAME);
    }

    public void setLogFileName(String logFileName) {
        saveString(SAVED_LOGGER_INFO, LOG_FILE_NAME_PREFERENCES_NAME, logFileName);
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
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPref.getString(keyName, defaultValue);
    }

    /**
     * Save a string value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveString(final String fileName, final String keyName, final String value) {
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, value);
        editor.apply();
        editor.commit();
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
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(keyName, defaultValue);
    }

    /**
     * Save a boolean value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveBoolean(final String fileName, final String keyName, final boolean value) {
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, String.valueOf(value));
        editor.apply();
        editor.commit();
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
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return Float.parseFloat(sharedPref.getString(keyName, String.valueOf(defaultValue)));
    }

    /**
     * Save a float value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveFloat(final String fileName, final String keyName, final float value) {
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, String.valueOf(value));
        editor.apply();
        editor.commit();
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
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return Integer.parseInt(sharedPref.getString(keyName, String.valueOf(defaultValue)));
    }

    /**
     * Save an integer value in the shared preferences.
     *
     * @param fileName the preferences file name.
     * @param keyName  the value key name.
     * @param value    the value to save.
     */
    private void saveInt(final String fileName, final String keyName, final int value) {
        SharedPreferences sharedPref = mApplicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyName, String.valueOf(value));
        editor.apply();
        editor.commit();
    }

    // endregion
}
