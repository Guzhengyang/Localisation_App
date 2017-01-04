package com.valeo.bleranging.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.valeo.bleranging.R;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;

/**
 * Constants file for SharedPrefs
 */
public final class SdkPreferencesHelper {
    public final static int OFFSET_EAR_FOR_START = -5;
    public final static int OFFSET_EAR_FOR_LOCK = -5;
    public final static int OFFSET_EAR_FOR_UNLOCK = -5;
    public final static int OFFSET_POCKET_FOR_START = 0;
    public final static int OFFSET_POCKET_FOR_LOCK = 2;
    public final static int OFFSET_POCKET_FOR_UNLOCK = -3;
    public final static int INDOOR_START_THRESHOLD = -73;
    public final static int INDOOR_UNLOCK_IN_THE_RUN_THRESHOLD = -65;
    public final static int INDOOR_WALK_AWAY_LOCKING_THRESHOLD = -70;
    public final static int INDOOR_WELCOME_THRESHOLD = -95;
    public final static int INDOOR_CLOSE_TO_BEACON_THRESHOLD = -50;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD = 8;
    public final static int INDOOR_NEAR_BACKDOOR_RATIO_THRESHOLD_MIN = -5;
    public final static int INDOOR_NEAR_BACKDOOR_RATIO_THRESHOLD_MAX = 5;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_MB = 0;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX = 12;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN = 2;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX = 12;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN = 2;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR = 10;
    public final static int INDOOR_NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR = 10;
    public final static int INDOOR_AVERAGE_DELTA_LOCK_THRESHOLD = -10;
    public final static int INDOOR_AVERAGE_DELTA_UNLOCK_THRESHOLD = 10;
    public final static int OUTSIDE_START_THRESHOLD = -73;
    public final static int OUTSIDE_UNLOCK_IN_THE_RUN_THRESHOLD = -65;
    public final static int OUTSIDE_WALK_AWAY_LOCKING_THRESHOLD = -70;
    public final static int OUTSIDE_WELCOME_THRESHOLD = -95;
    public final static int OUTSIDE_CLOSE_TO_BEACON_THRESHOLD = -50;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD = 8;
    public final static int OUTSIDE_NEAR_BACKDOOR_RATIO_THRESHOLD_MIN = -5;
    public final static int OUTSIDE_NEAR_BACKDOOR_RATIO_THRESHOLD_MAX = 5;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_MB = 0;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX = 12;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN = 2;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX = 12;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN = 2;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR = 10;
    public final static int OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR = 10;
    public final static int OUTSIDE_AVERAGE_DELTA_LOCK_THRESHOLD = -10;
    public final static int OUTSIDE_AVERAGE_DELTA_UNLOCK_THRESHOLD = 10;
    public final static float BACK_TIMEOUT = 3.0f;
    public final static float THATCHAM_TIMEOUT = 3.0f;
    public final static float CRYPTO_PRE_AUTH_TIMEOUT = 1.3f;
    public final static float CRYPTO_ACTION_TIMEOUT = 0.02f;
    public final static int RSSI_LOG_NUMBER = 0;
    public final static int ROLLING_AVERAGE_ELEMENTS = 20;
    public final static int START_NB_ELEMENT = 3;
    public final static int LOCK_NB_ELEMENT = 5;
    public final static int UNLOCK_NB_ELEMENT = 5;
    public final static int WELCOME_NB_ELEMENT = 10;
    public final static int LONG_NB_ELEMENT = 20;
    public final static int SHORT_NB_ELEMENT = 2;
    public final static int UNLOCK_VALID_NB = 1;
    public final static int UNLOCK_MODE = 1;
    public final static int LOCK_MODE = 7;
    public final static int START_MODE = 1;
    public final static int ECRETAGE_REFERENCE_70_100 = 1;
    public final static int ECRETAGE_REFERENCE_50_70 = 1;
    public final static int ECRETAGE_REFERENCE_30_50 = 1;
    public final static int ECRETAGE_REFERENCE_30_30 = 1;
    public final static float ECRETAGE_70_100 = 2.0f;
    public final static float ECRETAGE_50_70 = 2.0f;
    public final static float ECRETAGE_30_50 = 1.0f;
    public final static float ECRETAGE_30_30 = 1.0f;
    public final static int LIN_ACC_SIZE = 10;
    public final static float CORRECTION_LIN_ACC = 0.0f;
    public final static float FROZEN_THRESHOLD = 0.0f;
    public final static float WANTED_SPEED = 5.1f;
    public final static int ONE_STEP_SIZE = 70;
    public final static int EQUALIZER_LEFT = 0;
    public final static int EQUALIZER_MIDDLE = 0;
    public final static int EQUALIZER_RIGHT = 0;
    public final static int EQUALIZER_TRUNK = 2;
    public final static int EQUALIZER_BACK = 1;
    public final static int EQUALIZER_FRONT_LEFT = -5;
    public final static int EQUALIZER_FRONT_RIGHT = -5;
    public final static int EQUALIZER_REAR_LEFT = -5;
    public final static int EQUALIZER_REAR_RIGHT = -5;
    public static final int INDOOR_RATIO_CLOSE_TO_CAR_THR = 0;
    public static final int OUTSIDE_RATIO_CLOSE_TO_CAR_THR = 0;
    public final static String BLE_ADDRESS_CONNECTABLE = "D4:F5:13:56:2A:B8";
    public final static String BLE_ADDRESS_CONNECTABLE_PC = "B0:B4:48:BD:56:85";
    public final static String BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL = "5C:E0:C5:34:4D:32";
    public final static String BLE_ADDRESS_FRONT_LEFT = "24:71:89:1D:4E:4D";
    public final static String BLE_ADDRESS_FRONT_RIGHT = "24:71:89:1D:4E:66";
    public final static String BLE_ADDRESS_LEFT = "24:71:89:1D:4E:79";
    public final static String BLE_ADDRESS_MIDDLE = "24:71:89:1D:4E:1A";
    public final static String BLE_ADDRESS_RIGHT = "24:71:89:1D:4E:3D";
    public final static String BLE_ADDRESS_TRUNK = "24:71:89:1D:4C:FC";
    public final static String BLE_ADDRESS_REAR_LEFT = "24:71:89:1D:4E:6D";
    public final static String BLE_ADDRESS_BACK = "78:A5:04:81:5A:21";
    public final static String BLE_ADDRESS_REAR_RIGHT = "24:71:89:1D:4E:61";
    public static final String SAVED_CC_GENERIC_OPTION = "savedConnectedCarGenericOption";
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

    public int getIndoorRatioCloseToCarThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_ratio_close_to_car_thr_pref_name), INDOOR_RATIO_CLOSE_TO_CAR_THR);
    }

    public int getOutsideRatioCloseToCarThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_ratio_close_to_car_thr_pref_name), OUTSIDE_RATIO_CLOSE_TO_CAR_THR);
    }

    public int getOffsetPocketForStart(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.offset_pocket_for_start_pref_name), OFFSET_POCKET_FOR_START);
    }

    public int getOffsetEarForStart(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.offset_ear_for_start_pref_name), OFFSET_EAR_FOR_START);
    }

    public int getOffsetPocketForLock(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.offset_pocket_for_lock_pref_name), OFFSET_POCKET_FOR_LOCK);
    }

    public int getOffsetEarForLock(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.offset_ear_for_lock_pref_name), OFFSET_EAR_FOR_LOCK);
    }

    public int getOffsetPocketForUnlock(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.offset_pocket_for_unlock_pref_name), OFFSET_POCKET_FOR_UNLOCK);
    }

    public int getOffsetEarForUnlock(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.offset_ear_for_unlock_pref_name), OFFSET_EAR_FOR_UNLOCK);
    }

    public int getIndoorStartThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_start_thr_pref_name), INDOOR_START_THRESHOLD);
    }

    public int getIndoorUnlockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_unlock_thr_pref_name), INDOOR_UNLOCK_IN_THE_RUN_THRESHOLD);
    }

    public int getIndoorLockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_lock_thr_pref_name), INDOOR_WALK_AWAY_LOCKING_THRESHOLD);
    }

    public int getIndoorWelcomeThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_welcome_thr_pref_name), INDOOR_WELCOME_THRESHOLD);
    }

    public int getIndoorCloseToBeaconThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_close_to_beacon_pref_name), INDOOR_CLOSE_TO_BEACON_THRESHOLD);
    }

    public int getIndoorNearDoorRatioThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD);
    }

    public int getIndoorNearBackDoorRatioThresholdMin(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_backdoor_ratio_thr_min_pref_name), INDOOR_NEAR_BACKDOOR_RATIO_THRESHOLD_MIN);
    }

    public int getIndoorNearBackDoorRatioThresholdMax(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_backdoor_ratio_thr_max_pref_name), INDOOR_NEAR_BACKDOOR_RATIO_THRESHOLD_MAX);
    }

    public int getIndoorNearDoorThresholdMLorMRMax(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_ml_mr_max_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX);
    }

    public int getIndoorNearDoorThresholdTLorTRMax(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_tl_tr_max_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX);
    }

    public int getIndoorNearDoorThresholdMB(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_mb_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_MB);
    }

    public int getIndoorNearDoorThresholdMLorMRMin(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_ml_mr_min_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN);
    }

    public int getIndoorNearDoorThresholdTLorTRMin(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_tl_tr_min_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN);
    }

    public int getIndoorNearDoorThresholdMRLorMRR(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_mrl_mrr_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR);
    }

    public int getIndoorNearDoorThresholdTRLorTRR(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_near_door_ratio_thr_trl_trr_pref_name), INDOOR_NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR);
    }

    public int getIndoorAverageDeltaUnlockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_average_delta_unlock_threshold_pref_name), INDOOR_AVERAGE_DELTA_UNLOCK_THRESHOLD);
    }

    public int getIndoorAverageDeltaLockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.indoor_average_delta_lock_threshold_pref_name), INDOOR_AVERAGE_DELTA_LOCK_THRESHOLD);
    }

    public int getOutsideStartThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_start_thr_pref_name), OUTSIDE_START_THRESHOLD);
    }

    public int getOutsideUnlockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_unlock_thr_pref_name), OUTSIDE_UNLOCK_IN_THE_RUN_THRESHOLD);
    }

    public int getOutsideLockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_lock_thr_pref_name), OUTSIDE_WALK_AWAY_LOCKING_THRESHOLD);
    }

    public int getOutsideWelcomeThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_welcome_thr_pref_name), OUTSIDE_WELCOME_THRESHOLD);
    }

    public int getOutsideCloseToBeaconThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_close_to_beacon_pref_name), OUTSIDE_CLOSE_TO_BEACON_THRESHOLD);
    }

    public int getOutsideNearDoorRatioThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD);
    }

    public int getOutsideNearBackDoorRatioThresholdMin(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_backdoor_ratio_thr_min_pref_name), OUTSIDE_NEAR_BACKDOOR_RATIO_THRESHOLD_MIN);
    }

    public int getOutsideNearBackDoorRatioThresholdMax(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_backdoor_ratio_thr_max_pref_name), OUTSIDE_NEAR_BACKDOOR_RATIO_THRESHOLD_MAX);
    }

    public int getOutsideNearDoorThresholdMLorMRMax(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_ml_mr_max_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MAX);
    }

    public int getOutsideNearDoorThresholdTLorTRMax(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_tl_tr_max_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MAX);
    }

    public int getOutsideNearDoorThresholdMB(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_mb_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_MB);
    }

    public int getOutsideNearDoorThresholdMLorMRMin(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_ml_mr_min_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_ML_MR_MIN);
    }

    public int getOutsideNearDoorThresholdTLorTRMin(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_tl_tr_min_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_TL_TR_MIN);
    }

    public int getOutsideNearDoorThresholdMRLorMRR(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_mrl_mrr_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_MRL_MRR);
    }

    public int getOutsideNearDoorThresholdTRLorTRR(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_near_door_ratio_thr_trl_trr_pref_name), OUTSIDE_NEAR_DOOR_RATIO_THRESHOLD_TRL_TRR);
    }

    public int getOutsideAverageDeltaUnlockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_average_delta_unlock_threshold_pref_name), OUTSIDE_AVERAGE_DELTA_UNLOCK_THRESHOLD);
    }

    public int getOutsideAverageDeltaLockThreshold(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.outside_average_delta_lock_threshold_pref_name), OUTSIDE_AVERAGE_DELTA_LOCK_THRESHOLD);
    }

    public int getUnlockValidNb(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.unlock_valid_nb_pref_name), UNLOCK_VALID_NB);
    }

    public int getUnlockMode(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.unlock_mode_pref_name), UNLOCK_MODE);
    }

    public int getLockMode(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.lock_mode_pref_name), LOCK_MODE);
    }

    public int getStartMode(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.start_mode_pref_name), START_MODE);
    }

    public int getEcretageReference_70_100(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.ecretage_reference_70_100_pref_name), ECRETAGE_REFERENCE_70_100);
    }

    public int getEcretageReference_50_70(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.ecretage_reference_50_70_pref_name), ECRETAGE_REFERENCE_50_70);
    }

    public int getEcretageReference_30_50(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.ecretage_reference_30_50_pref_name), ECRETAGE_REFERENCE_30_50);
    }

    public int getEcretageReference_30_30(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.ecretage_reference_30_30_pref_name), ECRETAGE_REFERENCE_30_30);
    }

    public float getEcretage70_100(final String fileName) {
        return readFloat(fileName, mApplicationContext.getString(R.string.ecretage_70_100_pref_name), ECRETAGE_70_100);
    }

    public float getEcretage50_70(final String fileName) {
        return readFloat(fileName, mApplicationContext.getString(R.string.ecretage_50_70_pref_name), ECRETAGE_50_70);
    }

    public float getEcretage30_50(final String fileName) {
        return readFloat(fileName, mApplicationContext.getString(R.string.ecretage_30_50_pref_name), ECRETAGE_30_50);
    }

    public float getEcretage30_30(final String fileName) {
        return readFloat(fileName, mApplicationContext.getString(R.string.ecretage_30_30_pref_name), ECRETAGE_30_30);
    }

    public int getTrxRssiEqualizerLeft(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_left_pref_name), EQUALIZER_LEFT);
    }

    public int getTrxRssiEqualizerFrontLeft(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_front_left_pref_name), EQUALIZER_FRONT_LEFT);
    }

    public int getTrxRssiEqualizerRearLeft(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_rear_left_pref_name), EQUALIZER_REAR_LEFT);
    }

    public int getTrxRssiEqualizerMiddle(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_middle_pref_name), EQUALIZER_MIDDLE);
    }

    public int getTrxRssiEqualizerRight(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_right_pref_name), EQUALIZER_RIGHT);
    }

    public int getTrxRssiEqualizerTrunk(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_trunk_pref_name), EQUALIZER_TRUNK);
    }

    public int getTrxRssiEqualizerFrontRight(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_front_right_pref_name), EQUALIZER_FRONT_RIGHT);
    }

    public int getTrxRssiEqualizerRearRight(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_rear_right_pref_name), EQUALIZER_REAR_RIGHT);
    }

    public int getTrxRssiEqualizerBack(final String fileName) {
        return readInt(fileName, mApplicationContext.getString(R.string.equalizer_back_pref_name), EQUALIZER_BACK);
    }

    public boolean isLightCaptorEnabled() {
        return readBoolean(SAVED_LIGHT_CAPTOR, LIGHT_CAPTOR_PREFERENCES_NAME, false);
    }

    public String getConnectedCarType() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_type_pref_name), ConnectedCarFactory.TYPE_4_A);
    }

    public String getConnectedCarBase() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_base_pref_name), ConnectedCarFactory.BASE_3);
    }

    public String getSelectedAlgo() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.selected_algo_pref_name), ConnectedCarFactory.ALGO_STANDARD);
    }

    public String getMachineLearningModel() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.machine_learning_model_pref_name), ConnectedCarFactory.MODEL_RF);
    }

    public Boolean getIsIndoor() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_indoor_enabled_pref_name), false);
    }

    public Boolean getComSimulationEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.com_simulation_enabled_pref_name), false);
    }

    public Boolean getConnectedCarTrameEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_trame_enabled_pref_name), false);
    }

    public String getConnectedCarTrame() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_trame_pref_name), "");
    }

    public float getThatchamTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.thatcham_timeout_pref_name), THATCHAM_TIMEOUT);
    }

    public float getBackTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.back_timeout_pref_name), BACK_TIMEOUT);
    }

    public float getCryptoPreAuthTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.crypto_pre_auth_timeout_pref_name), CRYPTO_PRE_AUTH_TIMEOUT);
    }

    public float getCryptoActionTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.crypto_action_timeout_pref_name), CRYPTO_ACTION_TIMEOUT);
    }

    public int getRssiLogNumber() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.rssi_log_number_pref_name), RSSI_LOG_NUMBER);
    }

    public void setRssiLogNumber(int rssiLogNumber) {
        saveInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.rssi_log_number_pref_name), rssiLogNumber);
    }

    public int getRollingAvElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.rolling_av_element_pref_name), ROLLING_AVERAGE_ELEMENTS);
    }

    public int getStartNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.start_nb_element_pref_name), START_NB_ELEMENT);
    }

    public int getLockNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.lock_nb_element_pref_name), LOCK_NB_ELEMENT);
    }

    public int getUnlockNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.unlock_nb_element_pref_name), UNLOCK_NB_ELEMENT);
    }

    public int getWelcomeNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.welcome_nb_element_pref_name), WELCOME_NB_ELEMENT);
    }

    public int getLongNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.long_nb_element_pref_name), LONG_NB_ELEMENT);
    }

    public int getShortNbElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.short_nb_element_pref_name), SHORT_NB_ELEMENT);
    }

    public String getTrxAddressConnectable() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_connectable_pref_name), BLE_ADDRESS_CONNECTABLE);
    }

    public String getTrxAddressConnectablePC() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_connectable_pc_pref_name), BLE_ADDRESS_CONNECTABLE_PC);
    }

    public String getTrxAddressConnectableRemoteControl() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_connectable_remote_control_pref_name), BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL);
    }

    public void setTrxAddressConnectableRemoteControl(String address) {
        saveString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_connectable_remote_control_pref_name), address);
    }

    public String getTrxAddressLeft() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_left_pref_name), BLE_ADDRESS_LEFT);
    }

    public String getTrxAddressMiddle() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_middle_pref_name), BLE_ADDRESS_MIDDLE);
    }

    public String getTrxAddressRight() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_right_pref_name), BLE_ADDRESS_RIGHT);
    }

    public String getTrxAddressTrunk() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_trunk_pref_name), BLE_ADDRESS_TRUNK);
    }

    public String getTrxAddressBack() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_back_pref_name), BLE_ADDRESS_BACK);
    }

    public String getTrxAddressFrontLeft() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_front_left_pref_name), BLE_ADDRESS_FRONT_LEFT);
    }

    public String getTrxAddressRearLeft() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_rear_left_pref_name), BLE_ADDRESS_REAR_LEFT);
    }

    public String getTrxAddressFrontRight() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_front_right_pref_name), BLE_ADDRESS_FRONT_RIGHT);
    }

    public String getTrxAddressRearRight() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.address_rear_right_pref_name), BLE_ADDRESS_REAR_RIGHT);
    }

    public int getLinAccSize() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.lin_acc_size_pref_name), LIN_ACC_SIZE);
    }

    public float getCorrectionLinAcc() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.correction_lin_acc_pref_name), CORRECTION_LIN_ACC);
    }

    public float getFrozenThreshold() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.frozen_threshold_pref_name), FROZEN_THRESHOLD);
    }

    public Boolean getUserSpeedEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.user_speed_enabled_pref_name), false);
    }

    public float getWantedSpeed() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.wanted_speed_pref_name), WANTED_SPEED);
    }

    public int getOneStepSize() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.one_step_size_pref_name), ONE_STEP_SIZE);
    }

    public void setUserMail(String userMail) {
        saveString(SAVED_LOGIN_INFO, USER_MAIL_PREFERENCES_NAME, userMail);
    }

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
