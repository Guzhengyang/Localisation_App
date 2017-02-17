package com.valeo.bleranging.bluetooth.compat;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.valeo.bleranging.utils.PSALogs;

/**
 * Pre Lollipop implementation of the {@link ScanTask}.
 * <p/>
 * {@inheritDoc}
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PreLollipopScanTask implements ScanTask {
    /**
     * PSALogs tag.
     */
    private static final String TAG = PreLollipopScanTask.class.getName();

    /** Start/ stop scan messages handler; */
    private final Handler mScanHandler = new Handler(Looper.getMainLooper());

    /** Bluetooth adapter. */
    private final BluetoothAdapter mBluetoothAdapter;
    /**
     * Scan callback.
     */
    private final BluetoothAdapter.LeScanCallback mScanCallback;
    /** Active scanning period. */
    private int mActiveScanningPeriod = DEFAULT_SCAN_INTERVAL_MS;
    /** Inactive scanning period. */
    private int mInactiveScanningPeriod = DEFAULT_SCAN_INTERVAL_MS;
    /** Current status. */
    private boolean mIsScanning;
    /** Flag used to suspend the start/stop mechanism */
    private boolean bIsScanningSuspended = false;

    /** Scan execution runnable. */
    private final Runnable mScanRunnable = new Runnable() {
        @Override
        public void run() {
            toggleScan();
        }
    };

    /**
     * Create a periodic scan task.
     *
     * @param bluetoothAdapter the Bluetooth adapter to use.
     * @param scanCallback     the scan results callback.
     */
    PreLollipopScanTask(final BluetoothAdapter bluetoothAdapter, final BluetoothAdapter.LeScanCallback scanCallback) {
        mIsScanning = false;
        mBluetoothAdapter = bluetoothAdapter;
        mScanCallback = scanCallback;
    }

    @Override
    public StartLeScanResult start() {
        mIsScanning = false;

        return toggleScan();
    }

    @Override
    public void stop() {
        mScanHandler.removeCallbacks(mScanRunnable);

        if (mBluetoothAdapter.isEnabled()) {
            //noinspection deprecation
            mBluetoothAdapter.stopLeScan(mScanCallback);
        } else {
            PSALogs.w(TAG, "Bluetooth adapter is disabled, cannot perform the stopScan call");
        }

        mIsScanning = false;
    }

    /**
     * This function suspends the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack
     */
    public void suspend(){
        bIsScanningSuspended = true;
        //Force status to true, as when we will resume, this will force a stop, leading to a stop
        //followed by a new start after each connection
        mIsScanning = true;
    }

    /**
     * This function resumes the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack. So before the connection,the mechanism is suspended, and after the connection, the
     * mecanism is resumed.
     */
    public void resume(){
        bIsScanningSuspended = false;
    }

    @Override
    public void setScanPeriods(final int activeScanPeriod, final int inactiveScanPeriod) {
        if ((activeScanPeriod >= inactiveScanPeriod)
                && (activeScanPeriod > 650) && (activeScanPeriod < 10240)) {
            mActiveScanningPeriod = activeScanPeriod;
            mInactiveScanningPeriod = inactiveScanPeriod;
        }
    }

    /**
     * Toggle the scan status: when scanning, stop the scan and vice versa.
     *
     * @return the start scan result, when starting the scan, SUCCESS otherwise.
     */
    private StartLeScanResult toggleScan() {
        StartLeScanResult result;

        if(!bIsScanningSuspended) {
            if (mBluetoothAdapter.isEnabled()) {
                long nextToggleDelay;

                if (mIsScanning) {
                    //noinspection deprecation
                    mBluetoothAdapter.stopLeScan(mScanCallback);
                    PSALogs.d("ACH", "stopLeScan");
                    nextToggleDelay = mInactiveScanningPeriod;
                    result = StartLeScanResult.SUCCESS;
                } else {
                    //noinspection deprecation
                    if (mBluetoothAdapter.startLeScan(mScanCallback)) {
                        PSALogs.d("ACH", "startLeScan");
                        result = StartLeScanResult.SUCCESS;
                    } else {
                        result = StartLeScanResult.ERROR_NOT_STARTED;
                    }

                    nextToggleDelay = mActiveScanningPeriod;
                }

                mIsScanning = !mIsScanning;
                mScanHandler.postDelayed(mScanRunnable, nextToggleDelay);
            } else {
                PSALogs.w(TAG, "Bluetooth adapter is disabled, stop scanning");
                result = StartLeScanResult.ERROR_BLUETOOTH_DISABLED;
                mIsScanning = false;
            }
        }
        else{
            //Scanning start/stop mechanism is suspended. Do nothing but call this task again later
            mScanHandler.postDelayed(mScanRunnable, 200);
            result = StartLeScanResult.SUCCESS;
        }

        return result;
    }
}
