package com.valeo.bleranging.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.valeo.bleranging.R;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;

import static com.valeo.bleranging.model.connectedcar.ConnectedCar.THATCHAM_ORIENTED;

/**
 * Constants file for SharedPrefs
 */
public final class SdkPreferencesHelper {
    public final static int OFFSET_SMARTPHONE = 0;
    public final static int OFFSET_HYSTERESIS_LOCK = 4;
    public final static int OFFSET_HYSTERESIS_UNLOCK = 0;
    public final static float THRESHOLD_PROB_STANDARD = 0.8f;
    public final static float THRESHOLD_DIST_AWAY_STANDARD = 0.10f;
    //    public final static float BACK_TIMEOUT = 3.0f;
    public final static float UNLOCK_TIMEOUT = 0f;
    public final static float THATCHAM_TIMEOUT = 0f;
    public final static float CRYPTO_PRE_AUTH_TIMEOUT = 1.3f;
    public final static float CRYPTO_ACTION_TIMEOUT = 0.02f;
    public final static int RSSI_LOG_NUMBER = 0;
    public final static int ROLLING_AVERAGE_ELEMENTS = 20;
    public final static float WANTED_SPEED = 5.1f;
    public final static int ONE_STEP_SIZE = 70;
    public final static int DEFAULT_ACTIVE_SCAN_PERIOD = 3000;
    public final static int DEFAULT_INACTIVE_SCAN_PERIOD = 200;
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
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_type_pref_name), ConnectedCarFactory.TYPE_4_B);
    }

    public String getConnectedCarBase() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.connected_car_base_pref_name), ConnectedCarFactory.BASE_3);
    }

    public String getOpeningOrientation() {
        return readString(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.opening_orientation_type_pref_name), THATCHAM_ORIENTED);
    }

    public Boolean getComSimulationEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.com_simulation_enabled_pref_name), false);
    }

    public Boolean getSecurityWALEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.security_wal_enabled_pref_name), false);
    }

    public Boolean getAreBeaconsInside() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.are_beacons_inside_pref_name), false);
    }

    public int getOffsetSmartphone() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.offset_smartphone_pref_name), OFFSET_SMARTPHONE);
    }

    public void setOffsetSmartphone(int value) {
        saveInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.offset_smartphone_pref_name), value);
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

    public int getRollingAvElement() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.rolling_av_element_pref_name), ROLLING_AVERAGE_ELEMENTS);
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

    public String getTrxAddressLeft() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_left_pref_name), BLE_ADDRESS_LEFT);
    }

    public String getTrxAddressMiddle() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_middle_pref_name), BLE_ADDRESS_MIDDLE);
    }

    public String getTrxAddressRight() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_right_pref_name), BLE_ADDRESS_RIGHT);
    }

    public String getTrxAddressTrunk() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_trunk_pref_name), BLE_ADDRESS_TRUNK);
    }

    public String getTrxAddressBack() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_back_pref_name), BLE_ADDRESS_BACK);
    }

    public String getTrxAddressFrontLeft() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_front_left_pref_name), BLE_ADDRESS_FRONT_LEFT);
    }

    public String getTrxAddressRearLeft() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_rear_left_pref_name), BLE_ADDRESS_REAR_LEFT);
    }

    public String getTrxAddressFrontRight() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_front_right_pref_name), BLE_ADDRESS_FRONT_RIGHT);
    }

    public String getTrxAddressRearRight() {
        return readString(SAVED_CC_CONNECTION_OPTION, mApplicationContext.getString(R.string.address_rear_right_pref_name), BLE_ADDRESS_REAR_RIGHT);
    }

    public Boolean isPrintInsideEnabled() {
        return readBoolean(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.print_inside_enabled_pref_name), false);
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

    public int getActiveScanningPeriod() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.active_scanning_period_pref_name), DEFAULT_ACTIVE_SCAN_PERIOD);
    }

    public int getInactiveScanningPeriod() {
        return readInt(SAVED_CC_GENERIC_OPTION, mApplicationContext.getString(R.string.inactive_scanning_period_pref_name), DEFAULT_INACTIVE_SCAN_PERIOD);
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
