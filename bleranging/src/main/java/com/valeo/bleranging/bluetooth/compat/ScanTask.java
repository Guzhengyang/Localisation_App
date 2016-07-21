package com.valeo.bleranging.bluetooth.compat;

/**
 * Android devices have different behaviours while scanning for devices. Some send multiple
 * detection notifications, and keep on scanning wile others only send one result. For this last
 * category, the only way to correctly detect devices is to periodically start and stop scanning
 * for devices.
 */
public interface ScanTask {
    /**
     * Bluetooth LE scan start result.
     */
    enum StartLeScanResult {
        /**
         * No Bluetooth device.
         */
        ERROR_BLUETOOTH_NOT_AVAILABLE,
        /**
         * Bluetooth device is disabled.
         */
        ERROR_BLUETOOTH_DISABLED,
        /**
         * Scan not started.
         */
        ERROR_NOT_STARTED,
        /**
         * Success.
         */
        SUCCESS
    }

    /** Default scan interval for both active and inactive scanning periods. */
    int DEFAULT_SCAN_INTERVAL_MS = 500;

    /**
     * Set the scan periods (active scanning, inactive scanning).
     *
     * @param activeScanPeriod   the active scanning period.
     * @param inactiveScanPeriod the inactive scanning period.
     */
    void setScanPeriods(final int activeScanPeriod, final int inactiveScanPeriod);

    /**
     * Start a periodic scan for Bluetooth LE devices.
     *
     * @return the first start scan result code.
     */
    StartLeScanResult start();

    /**
     * Stop the periodic scan.
     */
    void stop();

    /**
     * This function suspends the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack
     */
    void suspend();

    /**
     * This function resumes the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack. So before the connection,the mechanism is suspended, and after the connection, the
     * mecanism is resumed.
     */
    void resume();
}
