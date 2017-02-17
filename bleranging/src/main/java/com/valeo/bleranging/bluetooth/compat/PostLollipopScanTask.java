package com.valeo.bleranging.bluetooth.compat;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.valeo.bleranging.utils.PSALogs;

import java.util.List;

/**
 * Post Lollipop implementation of the {@link ScanTask}.
 * <p/>
 * {@inheritDoc}
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PostLollipopScanTask implements ScanTask {
    /**
     * PSALogs tag.
     */
    private static final String TAG = PostLollipopScanTask.class.getName();

    /**
     * Start/ stop scan messages handler;
     */
    private final Handler mScanHandler = new Handler(Looper.getMainLooper());

    /**
     * Bluetooth adapter.
     */
    private final BluetoothAdapter mBluetoothAdapter;

    /**
     * Scan Filters.
     */
    private final List<ScanFilter> mListOfScanFilter;

    /**
     * Scan settings.
     */
    private final ScanSettings mScanSettings;
    /**
     * Scan callback.
     */
    private final ScanCallback mScanCallback;
    /**
     * Active scanning period.
     */
    private int mActiveScanningPeriod = 3000;
    /**
     * Inactive scanning period.
     */
    private int mInactiveScanningPeriod = 200;
    /**
     * Current status.
     */
    private boolean mIsScanning;
    /**
     * Boolean to stop the scanning periodic task
     */
    private boolean stopScanTask;

    /** Flag used to suspend the start/stop mechanism */
    private boolean bIsScanningSuspended = false;
    private BluetoothLeScanner bluetoothLeScanner;
    /**
     * Scan execution runnable.
     */
    private final Runnable mScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (!stopScanTask)
                toggleScan();
        }
    };

    /**
     * Create a periodic scan task.
     *
     * @param bluetoothAdapter the Bluetooth adapter to use.
     * @param scanSettings     the scan settings to use.
     * @param scanCallback     the scan results callback.
     */
    PostLollipopScanTask(final BluetoothAdapter bluetoothAdapter, final List<ScanFilter> scanFilterList, final ScanSettings scanSettings, final ScanCallback scanCallback) {
        mIsScanning = false;
        mBluetoothAdapter = bluetoothAdapter;
        mListOfScanFilter = scanFilterList;
        mScanSettings = scanSettings;
        mScanCallback = scanCallback;
    }

    @Override
    public StartLeScanResult start() {
        mIsScanning = false;
        stopScanTask = false;
        return toggleScan();
    }

    @Override
    public void stop() {
        mScanHandler.removeCallbacks(mScanRunnable);
        stopScanTask = true;
        if (mBluetoothAdapter.isEnabled()) {
            BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (bluetoothLeScanner != null) {
                if (mIsScanning) {
                    bluetoothLeScanner.stopScan(mScanCallback);
                }else {
                    PSALogs.w(TAG, "No running scanning to STOP");
                }
                mScanHandler.removeCallbacks(mScanRunnable);
            } else {
                PSALogs.w(TAG, "Bluetooth LE scanner is not available, cannot perform the stopScan call");
            }
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
                if (bluetoothLeScanner == null) {
                    bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                }

                if (bluetoothLeScanner != null) {
                    long nextToggleDelay;

                    if (!mIsScanning) {
                        PSALogs.d("ACH", "Start LE Scan");
                        bluetoothLeScanner.startScan(mListOfScanFilter, mScanSettings, mScanCallback);
                        nextToggleDelay = mActiveScanningPeriod;
                        result = StartLeScanResult.SUCCESS;
                        mIsScanning = true;
                    } else {
                        PSALogs.d("ACH", "Stop LE Scan");
                        bluetoothLeScanner.stopScan(mScanCallback);
                        nextToggleDelay = mInactiveScanningPeriod;
                        result = StartLeScanResult.SUCCESS;
                        mIsScanning = false;
                    }

                    mScanHandler.postDelayed(mScanRunnable, nextToggleDelay);
                } else {
                    PSALogs.w(TAG, "Bluetooth LE scanner is not available, stop scanning");
                    result = StartLeScanResult.ERROR_BLUETOOTH_NOT_AVAILABLE;
                    mIsScanning = false;
                }
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
