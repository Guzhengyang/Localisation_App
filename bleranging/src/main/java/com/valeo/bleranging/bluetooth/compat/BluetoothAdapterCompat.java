package com.valeo.bleranging.bluetooth.compat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v4.util.ArrayMap;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * "Compat" Bluetooth adapter. Depending on the device, uses the pre or post Lollipop scan methods.
 */
public final class BluetoothAdapterCompat {
    /**
     * PSALogs tag.
     */
    private static final String TAG = BluetoothAdapterCompat.class.getName();
    /** Running detection tasks map. */
    private final Map<ScanCallbackCompat, ScanTask> mRunningDetections = new ArrayMap<>();
    /**
     * Bluetooth adapter.
     */
    private final BluetoothAdapter mBluetoothAdapter;


    /**
     * Create and initialize a "compat" bluetooth adapter.
     *
     * @param context the execution context.
     */
    public BluetoothAdapterCompat(final Context context) {
        BluetoothManager bluetoothManager = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * Pre-Lollipop.
     *
     * @param scanCallbackCompat the "compat" callback.
     * @return the real callback to pass to the start / stop LE scan method, null when no "compat" callback is provided.
     */
    private static BluetoothAdapter.LeScanCallback getPreLollipopScanCallback(final ScanCallbackCompat scanCallbackCompat) {
        BluetoothAdapter.LeScanCallback scanCallback = null;

        if (scanCallbackCompat != null) {
            scanCallback = scanCallbackCompat.getPreLollipopScanCallback();
        }

        return scanCallback;
    }

    /**
     * Post-Lollipop.
     *
     * @param scanCallbackCompat the "compat" callback.
     * @return the real callback to pass to the start / stop LE scan method, null when no "compat" callback is provided.
     */
    private static ScanCallback getPostLollipopScanCallback(final ScanCallbackCompat scanCallbackCompat) {
        ScanCallback scanCallback = null;

        if (scanCallbackCompat != null) {
            scanCallback = scanCallbackCompat.getPostLollipopScanCallback();
        }

        return scanCallback;
    }

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     * Equivalent to: getBluetoothState() == STATE_ON
     * Requires BLUETOOTH
     *
     * @return true if the local adapter is turned on
     */
    public boolean isEnabled() {
        return (mBluetoothAdapter != null) && mBluetoothAdapter.isEnabled();
    }

    public boolean enable(boolean enable) {
        boolean isEnabled = mBluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return mBluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return mBluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    /**
     * Starts a scan for Bluetooth LE devices. Results of the scan are reported using the <code>scanCallbackCompat</code> callback.
     * Requires BLUETOOTH_ADMIN permission.
     *
     * @param scanCallbackCompat the callback LE scan results are delivered.
     * @return the start scan status.
     */
    public ScanTask.StartLeScanResult startLeScan(final ScanCallbackCompat scanCallbackCompat) {
        ScanTask.StartLeScanResult result;
        PSALogs.w("NIH", "startLeScan()");
        if (mBluetoothAdapter == null) {
            PSALogs.w(TAG, "Bluetooth adapter not available");
            result = ScanTask.StartLeScanResult.ERROR_BLUETOOTH_NOT_AVAILABLE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = postLollipopStartLeScan(scanCallbackCompat);
        } else {
            result = preLollipopStartLeScan(scanCallbackCompat);
        }

        return result;
    }

    /**
     * Stops an ongoing Bluetooth LE device scan.
     * Requires BLUETOOTH_ADMIN permission.
     *
     * @param scanCallbackCompat used to identify which scan to stop. Must be the same handle used to start the scan.
     */
    public void stopLeScan(final ScanCallbackCompat scanCallbackCompat) {
        ScanTask scanTask = mRunningDetections.get(scanCallbackCompat);
        if (scanTask != null) {
            scanTask.stop();
            PSALogs.w("NIH", "stopLeScan()");
            mRunningDetections.remove(scanCallbackCompat);
        }
    }

    // region Pre-Lollipop

    /**
     * This function suspends the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack
     */
    public void suspendLeScan(final ScanCallbackCompat scanCallbackCompat) {
        PSALogs.w("NIH", "suspendLeScan()");
        ScanTask scanTask = mRunningDetections.get(scanCallbackCompat);
        scanTask.suspend();
    }

    /**
     * This function resumes the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack. So before the connection,the mechanism is suspended, and after the connection, the
     * mecanism is resumed.
     */
    public void resumeLeScan(final ScanCallbackCompat scanCallbackCompat) {
        PSALogs.w("NIH", "resumeLeScan()");
        ScanTask scanTask = mRunningDetections.get(scanCallbackCompat);
        scanTask.resume();
    }

    // endregion

    // region Post-Lollipop

    /**
     * Starts a scan for Bluetooth LE devices. Results of the scan are reported using the <code>scanCallbackCompat</code> callback.
     * Requires BLUETOOTH_ADMIN permission.
     *
     * @param scanCallbackCompat the callback LE scan results are delivered.
     * @return the start scan status.
     */
    private ScanTask.StartLeScanResult preLollipopStartLeScan(final ScanCallbackCompat scanCallbackCompat) {
        ScanTask.StartLeScanResult result;

        PreLollipopScanTask scanTask = new PreLollipopScanTask(mBluetoothAdapter, getPreLollipopScanCallback(scanCallbackCompat));
        scanTask.setScanPeriods(1000 * 1000, 1000);
//        scanTask.setScanPeriods(3000, 200);
        mRunningDetections.put(scanCallbackCompat, scanTask);

        result = scanTask.start();

        return result;
    }

    /**
     * Post Lollipop code: starts a scan for Bluetooth LE devices. Results of the scan are reported using the <code>scanCallbackCompat</code> callback.
     * Requires BLUETOOTH_ADMIN permission.
     *
     * @param scanCallbackCompat the callback LE scan results are delivered.
     * @return the start scan status.
     */
    private ScanTask.StartLeScanResult postLollipopStartLeScan(final ScanCallbackCompat scanCallbackCompat) {
        ScanTask.StartLeScanResult result = ScanTask.StartLeScanResult.ERROR_BLUETOOTH_NOT_AVAILABLE;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (scanner == null) {
                result = ScanTask.StartLeScanResult.ERROR_BLUETOOTH_DISABLED;
            } else {
                List<ScanFilter> scanFilters = createScanFilters();
                ScanSettings setting = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setReportDelay(0)
                        .build();
                ScanTask runningScanTask = mRunningDetections.get(scanCallbackCompat);
                if (runningScanTask != null) {
                    PSALogs.w(TAG, "Scanning task is not Started, Already Running one found");
                } else {
                    PostLollipopScanTask scanTask = new PostLollipopScanTask(mBluetoothAdapter, scanFilters, setting, getPostLollipopScanCallback(scanCallbackCompat));
//                    scanTask.setScanPeriods(3000, 200);
                    scanTask.setScanPeriods(1000 * 1000, 1000);
                    mRunningDetections.put(scanCallbackCompat, scanTask);
                    scanTask.start();
                }
                result = ScanTask.StartLeScanResult.SUCCESS;
            }
        }
        return result;
    }

    /**
     * Create a filter for each trx
     * @return a list of scanFilter for all trx
     */
    private List<ScanFilter> createScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ScanFilter scanFilterConnectable = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressConnectable().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterConnectable);
            ScanFilter scanFilterLeft = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressLeft().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterLeft);
            ScanFilter scanFilterMiddle = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressMiddle().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterMiddle);
            ScanFilter scanFilterRight = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressRight().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterRight);
            ScanFilter scanFilterTrunk = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressTrunk().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterTrunk);
            ScanFilter scanFilterBack = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressBack().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterBack);
            ScanFilter scanFilterFrontLeft = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterFrontLeft);
            ScanFilter scanFilterFrontRight = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressFrontRight().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterFrontRight);
            ScanFilter scanFilterRearLeft = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressRearLeft().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterRearLeft);
            ScanFilter scanFilterRearRight = new ScanFilter.Builder()
                    .setDeviceAddress(SdkPreferencesHelper.getInstance().getTrxAddressRearRight().toUpperCase(Locale.FRANCE))
                    .build();
            scanFilters.add(scanFilterRearRight);
            ScanFilter scanFilterConnectable2 = new ScanFilter.Builder()
                    .setDeviceAddress("B0:B4:48:BD:56:85")
                    .build();
            scanFilters.add(scanFilterConnectable2);
            ScanFilter scanFilterLogger = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("f000ff15-0451-4000-b000-000000000000"))
                    .build();
            scanFilters.add(scanFilterLogger);
        }
        return scanFilters;
    }

    // endregion
}
