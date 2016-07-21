package com.valeo.bleranging.bluetooth.compat;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * "Compat" callback used to receive Bluetooth LE scan results.
 */
public abstract class ScanCallbackCompat {
    /**
     * Pre-Lollipop scan callback.
     */
    private BluetoothAdapter.LeScanCallback mPreLollipopLeScanCallback;

    /**
     * Post-Lollipop scan callback.
     */
    private ScanCallback mPostLollipopLeScanCallback;

    /**
     * Create and initialize a "compat" scan callback.
     */
    public ScanCallbackCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initializePostLollipopCallback();
        } else {
            initializePreLollipopCallback();
        }
    }

    // region Pre-Lollipop

    /**
     * Pre-Lollipop
     *
     * @return the LE scan callback.
     */
    final BluetoothAdapter.LeScanCallback getPreLollipopScanCallback() {
        return mPreLollipopLeScanCallback;
    }

    /**
     * Pre-Lollipop: initializes the scan callback.
     */
    private void initializePreLollipopCallback() {
        mPreLollipopLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                ScanCallbackCompat.this.onScanResult(device, rssi, scanRecord);
            }
        };
    }

    // endregion

    // region Post-Lollipop

    /**
     * Post-Lollipop
     *
     * @return the LE scan callback.
     */
    final ScanCallback getPostLollipopScanCallback() {
        return mPostLollipopLeScanCallback;
    }

    /**
     * Post-Lollipop: initializes the scan callback.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initializePostLollipopCallback() {
        mPostLollipopLeScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(final int callbackType, final ScanResult result) {
                super.onScanResult(callbackType, result);

                handleScanResult(result);
            }

            private void handleScanResult(final ScanResult scanResult) {
                BluetoothDevice device = scanResult.getDevice();
                ScanRecord scanRecord = scanResult.getScanRecord();
                byte[] scanRecordAsBytes = null;

                if (scanRecord != null) {
                    scanRecordAsBytes = scanRecord.getBytes();
                }

                ScanCallbackCompat.this.onScanResult(device, scanResult.getRssi(), scanRecordAsBytes);
            }

            public void onScanFailed(int errorCode) {
                Log.e("NIH", "Scan Failed");
            }
        };
    }

    // endregion

    /**
     * Callback reporting an LE device found during a device scan initiated by the {@link BluetoothAdapterCompat#startLeScan(ScanCallbackCompat)} function.
     *
     * @param device     identifies the remote device.
     * @param rssi       the RSSI value for the remote device as reported by theBluetooth hardware. 0 if no RSSI value is available.
     * @param scanRecord the content of the advertisement record offered by the remote device.
     */
    protected abstract void onScanResult(BluetoothDevice device, int rssi, @Nullable byte[] scanRecord);
}