package com.valeo.bleranging;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.valeo.bleranging.bluetooth.BleConnectionManager;
import com.valeo.bleranging.bluetooth.protocol.InblueProtocolManager;
import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.listeners.ChessBoardListener;
import com.valeo.bleranging.listeners.DebugListener;
import com.valeo.bleranging.listeners.SpinnerListener;
import com.valeo.bleranging.listeners.TestListener;
import com.valeo.bleranging.machinelearningalgo.MachineLearningManager;
import com.valeo.bleranging.managers.CommandManager;
import com.valeo.bleranging.managers.RunnerManager;
import com.valeo.bleranging.managers.SensorsManager;
import com.valeo.bleranging.managers.UiManager;
import com.valeo.bleranging.model.ConnectedCar;
import com.valeo.bleranging.persistence.LogFileManager;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;

import java.io.IOException;

import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_TRUNK;
import static com.valeo.bleranging.persistence.Constants.PERMISSIONS;
import static com.valeo.bleranging.persistence.Constants.REQUEST_PERMISSION_ALL;
import static com.valeo.bleranging.persistence.Constants.RSSI_DIR;

/**
 * Created by l-avaratha on 19/07/2016
 */
public class BleRangingHelper {
    public static ConnectedCar connectedCar;
    private final Handler mMainHandler;
    private final BleRangingListener bleRangingListener;
    private boolean isCloseAppCalled = false;

    public BleRangingHelper(Context context, ChessBoardListener chessBoardListener,
                            DebugListener debugListener,
                            SpinnerListener accuracyListener, TestListener testListener) {
        askForPermissions(context);
        this.bleRangingListener = (BleRangingListener) context;
        BleConnectionManager.initializeInstance(context, bleRangingListener);
        RunnerManager.initializeInstance(bleRangingListener);
        InblueProtocolManager.initializeInstance(context);
        MachineLearningManager.initializeInstance();
        SensorsManager.initializeInstance(context, bleRangingListener);
        CommandManager.initializeInstance(context, bleRangingListener);
        UiManager.initializeInstance(chessBoardListener, debugListener, accuracyListener, testListener);
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Toggle the ble activation
     *
     * @param enable true to toggle on, false to toggle off
     */
    public void toggleBluetooth(boolean enable) {
        BleConnectionManager.getInstance().toggleBluetooth(enable);
    }

    /**
     * Get the smartphone ble status
     *
     * @return true if the smartphone ble is enable, false otherwise
     */
    public boolean isBluetoothEnabled() {
        return BleConnectionManager.getInstance().isBluetoothEnabled();
    }

    public boolean getLockStatus() {
        return BleConnectionManager.getInstance().getLockStatus();
    }

    /**
     * Stops predictions runner when start is requested to avoid concurrent predictions
     *
     * @param isStartRequested true if start is requested, false otherwise
     */
    public void setIsStartRequested(boolean isStartRequested) {
        if (mMainHandler != null) {
            if (isStartRequested) {
                mMainHandler.removeCallbacks(UiManager.getInstance().updateCarLocalizationRunnable);
            } else {
                mMainHandler.post(UiManager.getInstance().updateCarLocalizationRunnable);
            }
            InblueProtocolManager.getInstance().getPacketOne().setIsStartRequested(isStartRequested);
        }
    }

    /**
     * Get start requested status from ble packet
     *
     * @return true if start is requested, false otherwise
     */
    public boolean isStartRequested() {
        return InblueProtocolManager.getInstance().getPacketOne().isStartRequested();
    }

    /**
     * Get the close app called status
     * @return true if close app has been called, false otherwise
     */
    public boolean isCloseAppCalled() {
        return isCloseAppCalled;
    }

    /**
     * Call this method before closing the app.
     * @param context the context
     * It unregister listeners and removeCallbacks and close cleanly all connections.
     */
    public void closeApp(final Context context) {
        PSALogs.d("NIH", "closeApp()");
        isCloseAppCalled = true;
        try {
            if (LogFileManager.getInstance().createDir(Environment.getExternalStorageDirectory(), RSSI_DIR)) {
                if (context.getExternalCacheDir() != null) {
                    TextUtils.copyFile(context.getExternalCacheDir().getAbsolutePath()
                                    + SdkPreferencesHelper.getInstance().getLogFileName(),
                            Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + SdkPreferencesHelper.getInstance().getLogFileName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // on settings changes, increase the file number used for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        CommandManager.getInstance().closeApp(context);
        SensorsManager.getInstance().closeApp(context);
        RunnerManager.getInstance().stopRunners();
        BleConnectionManager.getInstance().closeApp();
        bleRangingListener.updateBLEStatus();
        RunnerManager.getInstance().setIsLoggable(false);
    }

    /**
     * Get permission by asking user
     */
    private void askForPermissions(final Context context) {
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, REQUEST_PERMISSION_ALL);
        }
    }

    /**
     * Check if every needed permissions are granted
     *
     * @param context     the context
     * @param permissions the needed permissions
     * @return true if all needed permissions are granted, false otherwise
     */
    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculate the accuracy of a prediction
     */
    public void calculateAccuracy() {
        CommandManager.getInstance().enableAccuracyMeasure(true);
    }

    /**
     * Get the previously calculated accuracy of a zone
     * @param zone the zone accuracy to test
     * @return the accuracy of the zone
     */
    public Integer getCalculatedAccuracy(String zone) {
        CommandManager.getInstance().enableAccuracyMeasure(false);
        int result = CommandManager.getInstance().getSelectedAccuracy(zone);
        CommandManager.getInstance().clearAccuracyCounter();
        return result;
    }

    /**
     * Get the zones to display and select for accuracy calculation
     * @return the list of zone available or null
     */
    public String[] getStandardClasses() {
        if (connectedCar != null) {
            return connectedCar.getMultiPrediction().getStandardClasses();
        }
        return null;
    }

    /**
     * Get the phone movement
     * @return true if the smartphone is lay down, false if it is moving
     */
    public boolean isSmartphoneFrozen() {
        return SensorsManager.getInstance().isSmartphoneFrozen();
    }

    /**
     * Set the phone offset
     */
    public void setSmartphoneOffset() {
        if (connectedCar != null) {
            SdkPreferencesHelper.getInstance().setOffsetSmartphone(
                    SdkPreferencesHelper.DEFAULT_CALIBRATION_TRUNK_RSSI
                            - connectedCar.getMultiTrx().getCurrentOriginalRssi(NUMBER_TRX_TRUNK));
            SdkPreferencesHelper.getInstance().setIsCalibrated(true);
        } else {
            PSALogs.e("setSmartphoneOffset", "connectedCar is NULL");
        }
    }

    /**
     * Set a new threshold
     * @param value the new threshold
     */
    public void setNewThreshold(double value) {
        connectedCar.getMultiPrediction().calculatePredictionTest(value);
    }

    /**
     * Set the connected car registration plate
     * @param regPlate the connected car registration plate
     */
    public void setRegPlate(final String regPlate) {
        connectedCar.setRegPlate(regPlate);
    }

    /**
     * Verify if the user can click on rke button by checking if the action can succeed
     *
     * @return true if the rke button is ready, false otherwise
     */
    public boolean isRKEButtonClickable() {
        return CommandManager.getInstance().isRKEButtonClickable(BleConnectionManager.getInstance().isFullyConnected());
    }

    /**
     * Perform a RKE lock or unlock action
     *
     * @param lockCar true to send a lock action, false to send an unlock action
     */
    public void performRKELockAction(boolean lockCar) {
        CommandManager.getInstance().performRKELockAction(lockCar, BleConnectionManager.getInstance().isFullyConnected());
    }

    /**
     * Initialize the connected car.
     * Call this method in onResume.
     * @param context the context
     */
    public void initializeConnectedCar(final Context context) {
        UiManager.getInstance().initializeConnectedCar(context);
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    public void restartConnection() {
        BleConnectionManager.getInstance().restartConnection();
    }

    /**
     * Get the connection status between the smartphone and the car
     *
     * @return true if the smartphone is connected to the car, false otherwise
     */
    public boolean isFullyConnected() {
        return BleConnectionManager.getInstance().isFullyConnected();
    }

    /**
     * Get the connection status between the smartphone and the car
     *
     * @return true if the smartphone is connecting to the car, false otherwise
     */
    public boolean isConnecting() {
        return BleConnectionManager.getInstance().isConnecting();
    }

    /**
     * Stops the ble scan then start it again
     */
    public void relaunchScan() {
        BleConnectionManager.getInstance().relaunchScan();
    }
}
