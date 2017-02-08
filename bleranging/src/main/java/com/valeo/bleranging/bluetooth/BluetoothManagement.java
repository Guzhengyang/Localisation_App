package com.valeo.bleranging.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeServiceForPC;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeServiceForRemoteControl;
import com.valeo.bleranging.bluetooth.compat.BluetoothAdapterCompat;
import com.valeo.bleranging.bluetooth.compat.ScanCallbackCompat;
import com.valeo.bleranging.bluetooth.compat.ScanTask;
import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.bluetooth.scanresponse.CentralScanResponse;
import com.valeo.bleranging.bluetooth.scanresponse.ScanResponseParser;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main Bluetooth class
 * Manage all the Bluetooth operations
 * - BStart/Stop scan
 * - Handle BT devices list
 * - Handle notification action
 */
public class BluetoothManagement {
    private static final int MESSAGE_COMMAND_SENT_SUCCESS = 27;
    private static final String TAG = BluetoothManagement.class.getSimpleName();
    private static final int BEACON_SCAN_RESPONSE_LENGTH = 62;
    private static BluetoothAdapterCompat mBluetoothAdapterCompat;
    private final Context mContext;
    private final ArrayList<BluetoothManagementListener> mBluetoothManagementListeners;
    private final IBluetoothLeServiceListener mBLEServiceListener = new IBluetoothLeServiceListener() {
        private final Handler mHandler = new Handler();
        @Override
        public void onSendPacketSuccess() {
            Message msg = mHandler.obtainMessage();
            msg.what = MESSAGE_COMMAND_SENT_SUCCESS;
            mHandler.sendMessage(msg);
        }
    };
    /**
     * - Increase counters
     * - Update current devices list
     * - Detect PEPS
     * - Handle connection to last saved device
     */
    private final ScanCallbackCompat mLeScanCallbackMain =
            new ScanCallbackCompat() {
                @Override
                public void onScanResult(final BluetoothDevice device, final int rssi, final byte[] scanRecord, final byte[] advertisedData) {
                    if (scanRecord != null) {
                        // Check if the passive entry service is available
                        onScanRecordsGet(device, rssi, scanRecord, advertisedData);
                    }
                }
            };
    private boolean isReceiverRegistered = false;
    private BluetoothLeService mBluetoothLeService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            PSALogs.i("NIH bind", "onServiceConnected()");
            mBluetoothLeService = (((BluetoothLeService.LocalBinder) service).getService());
            mBluetoothLeService.registerListener(mBLEServiceListener);
            if (mBluetoothLeService.initialize()) {
                mBluetoothLeService.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            PSALogs.i("NIH bind", "onServiceDisconnected()");
            mBluetoothLeService.unregisterListener(mBLEServiceListener);
            mBluetoothLeService = null;
        }
    };
    private BluetoothLeServiceForPC mBluetoothLeServiceForPC;
    private final ServiceConnection mServiceConnectionForPC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            PSALogs.i("NIH_PC", "onServiceConnected()");
            mBluetoothLeServiceForPC = (((BluetoothLeServiceForPC.LocalBinder2) service).getService());
            if (mBluetoothLeServiceForPC.initialize()) {
                mBluetoothLeServiceForPC.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            PSALogs.i("NIH_PC", "onServiceDisconnected()");
            mBluetoothLeServiceForPC = null;
        }
    };
    private BluetoothLeServiceForRemoteControl mBluetoothLeServiceForRemoteControl;
    private final ServiceConnection mServiceConnectionForRemoteControl = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            PSALogs.i("NIH_REMOTE", "onServiceConnected()");
            mBluetoothLeServiceForRemoteControl = (((BluetoothLeServiceForRemoteControl.LocalBinder3) service).getService());
            if (mBluetoothLeServiceForRemoteControl.initialize()) {
                mBluetoothLeServiceForRemoteControl.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            PSALogs.i("NIH_REMOTE", "onServiceDisconnected()");
            mBluetoothLeServiceForRemoteControl = null;
        }
    };
    private BroadcastReceiver mTrxUpdateReceiver;

    /**
     * Class constructor
     *
     * @param context the context
     */
    public BluetoothManagement(Context context) {
        this.mContext = context;
        mBluetoothAdapterCompat = new BluetoothAdapterCompat(context);
        this.mBluetoothManagementListeners = new ArrayList<>();
    }

    private IntentFilter makeTrxUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_SUBSCRIBED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTION_LOSS);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_FAILED);
        return intentFilter;
    }

    /**
     * Close the connection between the phone and the current device
     */
    public void disconnect() {
        if (isFullyConnected()) {
            try {
                mBluetoothLeService.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (isBound()) {
                    mContext.unbindService(mServiceConnection);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (isReceiverRegistered) {
                    mContext.unregisterReceiver(mTrxUpdateReceiver);
                    isReceiverRegistered = false;
                }
            }
        }
    }

    /**
     * Close the connection between the phone and the pc
     */
    public void disconnectPc() {
        if (isFullyConnected2()) {
            try {
                mBluetoothLeServiceForPC.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (isBound2()) {
                    mContext.unbindService(mServiceConnectionForPC);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the connection between the phone and the RemoteControl
     */
    public void disconnectRemoteControl() {
        if (isFullyConnected3()) {
            try {
                mBluetoothLeServiceForRemoteControl.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (isBound3()) {
                    mContext.unbindService(mServiceConnectionForRemoteControl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Open the connection between the phone and the current device
     */
    public void connect(BroadcastReceiver mTrxUpdateReceiver) {
        if (!isFullyConnected() && !isConnecting()) {
            if (!isReceiverRegistered) {
                this.mTrxUpdateReceiver = mTrxUpdateReceiver;
                mContext.registerReceiver(this.mTrxUpdateReceiver, makeTrxUpdateIntentFilter());
                isReceiverRegistered = true;
            }
            if (!isBound()) {
                PSALogs.i("NIH bind", "BluetoothManagement bindService()");
                // the connection will happen onServiceConnected after binding
                Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
                mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            } else if (mBluetoothLeService != null) {
                PSALogs.i("NIH bind", "BluetoothManagement connectToDevice: " + SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
                mBluetoothLeService.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
            }
        }
    }

    /**
     * Open the connection between the phone and the pc
     */
    public void connectToPC(String address) {
        if (!isFullyConnected2() && !isConnecting2()) {
            if (!isBound2()) {
                PSALogs.i("NIH_PC", "BluetoothManagement bindService: " + address);
                Intent gattServiceIntent = new Intent(mContext, BluetoothLeServiceForPC.class);
                mContext.bindService(gattServiceIntent, mServiceConnectionForPC, Context.BIND_AUTO_CREATE);
            } else if (mBluetoothLeServiceForPC != null) {
                PSALogs.i("NIH_PC", "BluetoothManagement connectToPC: " + address);
                mBluetoothLeServiceForPC.connectToDevice(address);
            }
        }
    }

    /**
     * Open the connection between the phone and the RemoteControl
     */
    public void connectToRemoteControl(String address) {
        if (!isFullyConnected3() && !isConnecting3()) {
            if (!isBound3()) {
                PSALogs.i("NIH_REMOTE", "BluetoothManagement bindService: " + address);
                Intent gattServiceIntent = new Intent(mContext, BluetoothLeServiceForRemoteControl.class);
                mContext.bindService(gattServiceIntent, mServiceConnectionForRemoteControl, Context.BIND_AUTO_CREATE);
            } else if (mBluetoothLeServiceForRemoteControl != null) {
                PSALogs.i("NIH_REMOTE", "BluetoothManagement connectToRemoteControl: " + address);
                mBluetoothLeServiceForRemoteControl.connectToDevice(address);
            }
        }
    }


    /**
     * Start or stop the scan by calling the corresponding callback
     *
     * @param enable: indicates if we want to enable or disable the BLE adapter
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            PSALogs.i(TAG, "Start scanning for LE device");
            ScanTask.StartLeScanResult result = mBluetoothAdapterCompat.startLeScan(mLeScanCallbackMain);
            switch (result) {
                case ERROR_BLUETOOTH_NOT_AVAILABLE:
                    // TODO Bluetooth is not available, display a nice message
                    PSALogs.d(TAG, "Bluetooth is not available");
                    break;
                case ERROR_BLUETOOTH_DISABLED:
                    // TODO Bluetooth is disabled, display a nice message
                    PSALogs.d(TAG, "Bluetooth is disabled");
                    break;
                case ERROR_NOT_STARTED:
                    PSALogs.i(TAG, "LE Scan start failed");
                    break;
                case SUCCESS:
                    PSALogs.i(TAG, "LE Scan start succeeded");
                    break;
            }
        } else {
            PSALogs.i(TAG, "Stop scanning for LE device");
            mBluetoothAdapterCompat.stopLeScan(mLeScanCallbackMain);
        }
    }

    /**
     * This function suspends the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack
     */
    public void suspendLeScan() {
        scanLeDevice(false);
    }

    /**
     * This function resumes the start/stop scanning mechanism. Primary use is to avoid stopping
     * the scanning in the middle of the connection as this interferes with the Android bluetooth
     * stack. So before the connection,the mechanism is suspended, and after the connection, the
     * mecanism is resumed.
     */
    public void resumeLeScan() {
        scanLeDevice(true);
    }

    /**
     * Callback called when a scan record is available. Try to parse the byte[] response and turn
     * it into a list of UUIDs.
     *
     * @param device:     Bluetooth device
     * @param rssi:       RSSI adapter
     * @param scanRecord: bytes array. Raw data from adapter
     */
    private void onScanRecordsGet(BluetoothDevice device, int rssi, byte[] scanRecord, byte[] advertisedData) {
        if (scanRecord != null) {
            PSALogs.d("catch_address", device.getAddress() + " " + SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
            if (device.getAddress().equalsIgnoreCase(SdkPreferencesHelper.getInstance().getTrxAddressConnectable())) {
                fireCentralScanResponseCatch(device, ScanResponseParser.parseCentralScanResponse(scanRecord));
            } else {
                BeaconScanResponse beaconScanResponse = ScanResponseParser.parseBeaconScanResponse(scanRecord);
                fireBeaconScanResponseCatch(device, rssi, beaconScanResponse, advertisedData);
            }
        }
    }

    public void sendPackets(final byte[] byteToSend, final byte[] byteReceived) {
        mBluetoothLeService.sendPackets(byteToSend);
        final byte[] concatBytes = concatByte(byteToSend, byteReceived);
        sendToPC(concatBytes);
        sendToRemoteControl(concatBytes);
    }

    private byte[] concatByte(final byte[] byteToSend, final byte[] byteReceived) {
        if (byteToSend != null && byteReceived != null) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(byteToSend);
                outputStream.write(byteReceived);
                byte[] concatBytes = outputStream.toByteArray();
                PSALogs.d("NIH", "send: " + TextUtils.printBleBytes(concatBytes));
                return concatBytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return byteToSend;
    }

    private void sendToPC(final byte[] concatBytes) {
        if (isFullyConnected2() && mBluetoothLeServiceForPC.getBLEGattService() != null) {
            if (!mBluetoothLeServiceForPC.sendPackets(concatBytes)) {
                disconnectPc();
            }
        }
    }

    private void sendToRemoteControl(final byte[] concatBytes) {
        if (isFullyConnected3() && mBluetoothLeServiceForRemoteControl.getBLEGattService() != null) {
            if (!mBluetoothLeServiceForRemoteControl.sendPackets(concatBytes)) {
                disconnectRemoteControl();
            }
        }
    }

    //GETTERS

    public byte[] getBytesReceived() {
        return mBluetoothLeService.getReceiveQueue().poll();
    }

    public void addBluetoothManagementListener(BluetoothManagementListener listener) {
        if (!mBluetoothManagementListeners.contains(listener)) {
            mBluetoothManagementListeners.add(listener);
        }
    }

    private void fireCentralScanResponseCatch(BluetoothDevice device, CentralScanResponse centralScanResponse) {
        for(BluetoothManagementListener listener : mBluetoothManagementListeners) {
            listener.onCentralScanResponseCatch(device, centralScanResponse);
        }
    }

    private void fireBeaconScanResponseCatch(BluetoothDevice device, int rssi, BeaconScanResponse beaconScanResponse, byte[] advertisedData) {
        for (BluetoothManagementListener listener : mBluetoothManagementListeners) {
            listener.onBeaconScanResponseCatch(device, rssi, beaconScanResponse, advertisedData);
        }
    }

    public boolean isConnecting() {
        return mBluetoothLeService != null && mBluetoothLeService.isConnecting();
    }

    public boolean isConnecting2() {
        return mBluetoothLeServiceForPC != null && mBluetoothLeServiceForPC.isConnecting2();
    }

    public boolean isConnecting3() {
        return mBluetoothLeServiceForRemoteControl != null && mBluetoothLeServiceForRemoteControl.isConnecting3();
    }

    public boolean isFullyConnected() {
        return mBluetoothLeService != null && mBluetoothLeService.isFullyConnected();
    }

    public boolean isFullyConnected2() {
        return mBluetoothLeServiceForPC != null && mBluetoothLeServiceForPC.isFullyConnected2();
    }

    public boolean isFullyConnected3() {
        return mBluetoothLeServiceForRemoteControl != null && mBluetoothLeServiceForRemoteControl.isFullyConnected3();
    }

    private boolean isBound() {
        return mBluetoothLeService != null && mBluetoothLeService.isBound();
    }

    private boolean isBound2() {
        return mBluetoothLeServiceForPC != null && mBluetoothLeServiceForPC.isBound2();
    }

    private boolean isBound3() {
        return mBluetoothLeServiceForRemoteControl != null && mBluetoothLeServiceForRemoteControl.isBound3();
    }

    public boolean setBluetooth(boolean enable) {
        return mBluetoothAdapterCompat.enable(enable);
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapterCompat.isEnabled();
    }
}
