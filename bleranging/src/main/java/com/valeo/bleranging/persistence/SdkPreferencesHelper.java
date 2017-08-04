package com.valeo.bleranging.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.valeo.bleranging.R;

import static com.valeo.bleranging.persistence.Constants.BASE_1;
import static com.valeo.bleranging.persistence.Constants.THATCHAM_ORIENTED;
import static com.valeo.bleranging.persistence.Constants.TYPE_8_A;

/**
 * Constants file for SharedPrefs
 */
public final class SdkPreferencesHelper {
    public final static int DEFAULT_CALIBRATION_TRUNK_RSSI = -70;
    public final static int OFFSET_SMARTPHONE = 0;
    public final static int THRESHOLD_LOCK = -80;
    public final static int OFFSET_HYSTERESIS_LOCK = 2;
    public final static int OFFSET_HYSTERESIS_UNLOCK = 0;
    public final static float THRESHOLD_PROB_STANDARD = 0.6f;
    public final static float THRESHOLD_DIST_AWAY_STANDARD = 0.10f;
    //    public final static float BACK_TIMEOUT = 3.0f;
    public final static float UNLOCK_TIMEOUT = 0f;
    public final static float THATCHAM_TIMEOUT = 0f;
    public final static float CRYPTO_PRE_AUTH_TIMEOUT = 1.3f;
    public final static float CRYPTO_ACTION_TIMEOUT = 0.02f;
    public final static int RSSI_LOG_NUMBER = 0;
    public final static float WANTED_SPEED = 5.1f;
    public final static int ONE_STEP_SIZE = 70;
    public final static int LIN_ACC_SIZE = 10;
    public final static float CORRECTION_LIN_ACC = 0.0f;
    public final static float FROZEN_THRESHOLD = 3.6f;
    public final static int DEFAULT_FLASH_FREQUENCY = 10;
    public final static int DEFAULT_MEASUREMENT_INTERVAL = 15;
    public final static int DEFAULT_ACTIVE_SCAN_PERIOD = 3000;
    public final static int DEFAULT_INACTIVE_SCAN_PERIOD = 200;
    public final static String BLE_ADDRESS_CONNECTABLE = "D4:F5:13:56:2A:B8";
    public final static String BLE_ADDRESS_CONNECTABLE_PC = "B0:B4:48:BD:56:85";
    public final static String BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL = "5C:E0:C5:34:4D:32";
    public static final String SAVED_CC_GENERIC_OPTION = "savedConnectedCarGenericOption";
    public static final String SAVED_CC_CONNECTION_OPTION = "savedConnectedCarConnectionOption";
    private final static String LOG_FILE_NAME = "/InBlueRssi/allRssi_0_0000.csv";
    private static final String SAVED_LOGIN_INFO = "savedLoginInfo";
    private static final String SAVED_LOGGER_INFO = "savedLoggerInfo";
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

    public String getConnectedCarType() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_type_pref_name), TYPE_8_A);
    }

    public String getConnectedCarBase() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_base_pref_name), BASE_1);
    }

    public String getOpeningStrategy() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.opening_orientation_type_pref_name), THATCHAM_ORIENTED);
    }

    public Boolean getComSimulationEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.com_simulation_enabled_pref_name), false);
    }

    public Boolean getSecurityWALEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.security_wal_enabled_pref_name), false);
    }

    public void setSecurityWALEnabled(boolean value) {
        saveBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.security_wal_enabled_pref_name), value);
    }

    public Boolean getAreBeaconsInside() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.are_beacons_inside_pref_name), false);
    }

    public void setAreBeaconsInside(boolean value) {
        saveBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.are_beacons_inside_pref_name), value);
    }

    public Boolean getIsCarIndoor() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_car_indoor_pref_name), false);
    }

    public void setIsCarIndoor(boolean value) {
        saveBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_car_indoor_pref_name), value);
    }

    public int getAdapterLastPosition() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.adapter_last_position_pref_name), -1);
    }

    public void setAdapterLastPosition(int lastPosition) {
        saveInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.adapter_last_position_pref_name), lastPosition);
    }

    public int getOffsetSmartphone() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.offset_smartphone_pref_name), OFFSET_SMARTPHONE);
    }

    public void setOffsetSmartphone(int value) {
        saveInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.offset_smartphone_pref_name), value);
    }

    public int getThresholdLock() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.threshold_lock_pref_name), THRESHOLD_LOCK);
    }

    public int getOffsetHysteresisLock() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.offset_hysteresis_lock_pref_name), OFFSET_HYSTERESIS_LOCK);
    }

    public int getOffsetHysteresisUnlock() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.offset_hysteresis_unlock_pref_name), OFFSET_HYSTERESIS_UNLOCK);
    }

    public float getThresholdProbStandard() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.threshold_prob_standard_pref_name), THRESHOLD_PROB_STANDARD);
    }

    public float getThresholdDistAwayStandard() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.threshold_dist_away_standard_pref_name), THRESHOLD_DIST_AWAY_STANDARD);
    }

    public float getUnlockTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.unlock_timeout_pref_name), UNLOCK_TIMEOUT);
    }

    public float getThatchamTimeout() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.thatcham_timeout_pref_name), THATCHAM_TIMEOUT);
    }

//    public float getBackTimeout() {
//        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.back_timeout_pref_name), BACK_TIMEOUT);
//    }

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

    public String getTrxAddressConnectable() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_connectable_pref_name), BLE_ADDRESS_CONNECTABLE);
    }

    public String getTrxAddressConnectablePC() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_connectable_pc_pref_name), BLE_ADDRESS_CONNECTABLE_PC);
    }

    public String getTrxAddressConnectableRemoteControl() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_connectable_remote_control_pref_name), BLE_ADDRESS_CONNECTABLE_REMOTE_CONTROL);
    }

    public void setTrxAddressConnectableRemoteControl(String address) {
        saveString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_connectable_remote_control_pref_name), address);
    }

    public void setTrxAddress(int receivedTrxNumber, String address) {
        saveString(SAVED_CC_CONNECTION_OPTION, getTrxAddressKeyName(receivedTrxNumber), address);
    }

    public void setTrxCarRssi(int receivedTrxNumber, int rssi) {
        saveInt(SAVED_CC_CONNECTION_OPTION, getTrxCarRssiKeyName(receivedTrxNumber), rssi);
    }

    private String getTrxAddressKeyName(int receivedTrxNumber) {
        switch (receivedTrxNumber) {
            case 1:
                return mApplicationContext.getString(R.string.address_front_left_pref_name);
            case 2:
                return mApplicationContext.getString(R.string.address_front_right_pref_name);
            case 3:
                return mApplicationContext.getString(R.string.address_left_pref_name);
            case 4:
                return mApplicationContext.getString(R.string.address_middle_pref_name);
            case 5:
                return mApplicationContext.getString(R.string.address_right_pref_name);
            case 6:
                return mApplicationContext.getString(R.string.address_trunk_pref_name);
            case 7:
                return mApplicationContext.getString(R.string.address_rear_left_pref_name);
            case 8:
                return mApplicationContext.getString(R.string.address_back_pref_name);
            case 9:
                return mApplicationContext.getString(R.string.address_rear_right_pref_name);
            default:
                return "";
        }
    }

    private String getTrxCarRssiKeyName(int receivedTrxNumber) {
        switch (receivedTrxNumber) {
            case 1:
                return mApplicationContext.getString(R.string.car_rssi_front_left_pref_name);
            case 2:
                return mApplicationContext.getString(R.string.car_rssi_front_right_pref_name);
            case 3:
                return mApplicationContext.getString(R.string.car_rssi_left_pref_name);
            case 4:
                return mApplicationContext.getString(R.string.car_rssi_middle_pref_name);
            case 5:
                return mApplicationContext.getString(R.string.car_rssi_right_pref_name);
            case 6:
                return mApplicationContext.getString(R.string.car_rssi_trunk_pref_name);
            case 7:
                return mApplicationContext.getString(R.string.car_rssi_rear_left_pref_name);
            case 8:
                return mApplicationContext.getString(R.string.car_rssi_back_pref_name);
            case 9:
                return mApplicationContext.getString(R.string.car_rssi_rear_right_pref_name);
            default:
                return "";
        }
    }

    public String getTrxAddress(int trxNumber) {
        return readString(SAVED_CC_CONNECTION_OPTION, getTrxAddressKeyName(trxNumber), "");
    }

    public int getCarRssi(int trxNumber) {
        return readInt(SAVED_CC_CONNECTION_OPTION, getTrxCarRssiKeyName(trxNumber), -1);
    }

    public Boolean isChannelLimited() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_channel_limited_pref_name), false);
    }

    public Boolean isCalibrated() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_calibrated_pref_name), false);
    }

    public void setIsCalibrated(boolean value) {
        saveBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_calibrated_pref_name), value);
    }

    public Boolean isPrintInsideEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.print_inside_enabled_pref_name), false);
    }

    public Boolean isPrintRooftopEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.print_rooftop_enabled_pref_name), false);
    }

    public Boolean isMiniPredictionUsed() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.is_mini_prediction_used_pref_name), false);
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

    public int getFlashFrequency() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.flash_frequency_pref_name), DEFAULT_FLASH_FREQUENCY);
    }

    public int getMeasurementInterval() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.measurement_interval_pref_name), DEFAULT_MEASUREMENT_INTERVAL);
    }

    public int getActiveScanningPeriod() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.active_scanning_period_pref_name), DEFAULT_ACTIVE_SCAN_PERIOD);
    }

    public int getInactiveScanningPeriod() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.inactive_scanning_period_pref_name), DEFAULT_INACTIVE_SCAN_PERIOD);
    }

    public int getLinAccSize() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.lin_acc_size_pref_name), LIN_ACC_SIZE);
    }

    public float getFrozenThreshold() {
        return readFloat(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.frozen_threshold_pref_name), FROZEN_THRESHOLD);
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
        editor.putBoolean(keyName, value);
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
