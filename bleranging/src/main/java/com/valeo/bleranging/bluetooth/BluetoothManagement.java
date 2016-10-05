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
import android.util.Log;

import com.valeo.bleranging.bluetooth.compat.BluetoothAdapterCompat;
import com.valeo.bleranging.bluetooth.compat.ScanCallbackCompat;
import com.valeo.bleranging.bluetooth.compat.ScanTask;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

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
    /**
     * Code to manage Service lifecycle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i("NIH bind", "onServiceConnected()");
            mBluetoothLeService = (((BluetoothLeService.LocalBinder) service).getService());
            mBluetoothLeService.registerListener(mBLEServiceListener);
            if (mBluetoothLeService.initialize()) {
                mBluetoothLeService.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("NIH bind", "onServiceDisconnected()");
            mBluetoothLeService.unregisterListener(mBLEServiceListener);
            mBluetoothLeService = null;
        }
    };
    private BluetoothLeService2 mBluetoothLeService2;
    private final ServiceConnection mServiceConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i("NIH_PC", "onServiceConnected()");
            mBluetoothLeService2 = (((BluetoothLeService2.LocalBinder2) service).getService());
            if (mBluetoothLeService2.initialize()) {
                mBluetoothLeService2.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectable2());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("NIH_PC", "onServiceDisconnected()");
            mBluetoothLeService2 = null;
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
        mBluetoothManagementListeners = new ArrayList<>();
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
        try {
            if (isFullyConnected()) {
                mBluetoothLeService.disconnect();
            }
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

    /**
     * Open the connection between the phone and the current device
     */
    public void connect(BroadcastReceiver mTrxUpdateReceiver) {
        if (!isFullyConnected()) {
            if (!isReceiverRegistered) {
                this.mTrxUpdateReceiver = mTrxUpdateReceiver;
                mContext.registerReceiver(this.mTrxUpdateReceiver, makeTrxUpdateIntentFilter());
                isReceiverRegistered = true;
            }
            if (!isBound()) {
                Log.i("NIH bind", "BluetoothManagement bindService()");
                // the connection will happen onServiceConnected after binding
                Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
                mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            } else if (mBluetoothLeService != null) {
                Log.i("NIH bind", "BluetoothManagement connectToDevice: " + SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
                mBluetoothLeService.connectToDevice(SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
            }
        }
    }

    /**
     * Open the connection between the phone and the pc
     */
    public void connectToPC(String address) {
        if (mBluetoothLeService2 == null) {
            Log.i("NIH_PC", "BluetoothManagement bindService: " + address);
            Intent gattServiceIntent = new Intent(mContext, BluetoothLeService2.class);
            mContext.bindService(gattServiceIntent, mServiceConnection2, Context.BIND_AUTO_CREATE);
        } else {
            Log.i("NIH_PC", "BluetoothManagement connectToPC: " + address);
            mBluetoothLeService2.connectToDevice(address);
        }
    }


    /**
     * Start or stop the scan by calling the corresponding callback
     *
     * @param enable: indicates if we want to enable or disable the BLE adapter
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.i(TAG, "Start scanning for LE device");
            ScanTask.StartLeScanResult result = mBluetoothAdapterCompat.startLeScan(mLeScanCallbackMain);
            switch (result) {
                case ERROR_BLUETOOTH_NOT_AVAILABLE:
                    // TODO Bluetooth is not available, display a nice message
                    Log.d(TAG, "Bluetooth is not available");
                    break;
                case ERROR_BLUETOOTH_DISABLED:
                    // TODO Bluetooth is disabled, display a nice message
                    Log.d(TAG, "Bluetooth is disabled");
                    break;
                case ERROR_NOT_STARTED:
                    Log.i(TAG, "LE Scan start failed");
                    break;
                case SUCCESS:
                    Log.i(TAG, "LE Scan start succeeded");
                    break;
            }
        } else {
            Log.i(TAG, "Stop scanning for LE device");
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
        //Get profile version
        byte profileVersion = checkProfileVersion(scanRecord);
        //Check BTLE profile version
        //Implement 1 case for each version as the content might be different from one version to another
        switch(profileVersion) {
            case 1:
                //Parse content of advertising data
                ScanResponse scanResponse = parseResponseProfileV1(scanRecord);
                //Process the content
                processResponseProfileV1(scanResponse, device, rssi, advertisedData);
                break;
            default:
                //The protocol is unknown. do nothing
                //Parse content of advertising data
                ScanResponse scanResponse2 = parseResponseProfileV1(scanRecord);
                //Process the content
                processResponseProfileV1(scanResponse2, device, rssi, advertisedData);
                break;
        }
    }

    private int indexOf(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    private byte checkProfileVersion(final byte[] advertisedData) {
        byte version = 0;
        byte[] pattern = {(byte) 0xFF, (byte) 0xEE, 0x01};
        int patternIndex = indexOf(advertisedData,pattern);
        if(patternIndex <0)
            return version;
        int versionIndex = patternIndex + pattern.length;
        if(versionIndex < advertisedData.length){
            version = advertisedData[versionIndex];
        }
        //In the InBlue profile, we decided that advertising content byte 7 contains code for
        //"manufacturer spefic", byte 8 contains the 1st byte of Valeo manufacturer ID, byte 9
        //contains the 2nd byte of Valeo manufacturer ID and finally the byte 10 contains the
        //profile version. Every byte after that depends on the profile version used
//        if(  (advertisedData[7]==(byte)0xFF)
//          && (advertisedData[8]==(byte)0xEE)
//          && (advertisedData[9]==(byte)0x01) ) {
//            //Get version
//            version = advertisedData[10];
//        }
        return version;
    }

    /**
     * Enable parsing UUIDs from a device
     * @param advertisedData the advertised data
     * @return List of UUIDs on the device
     */
    private ScanResponse parseResponseProfileV1(final byte[] advertisedData) {
        byte [] random = null;
        byte [] mac = null;
        byte vehicleState = (byte)0xFF;
        byte protocolVersion = (byte)0;
        byte antennaId = (byte)0xFF;
        byte mode = (byte)0xFF; //mode: RKE, PE, PS, etc
        int reSynchro = 0; //Timestamp InSync updated

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;
            int type = advertisedData[offset++];
            switch (type) {
                case (byte)0x02: // Partial list of 16-bit UUIDs
                case (byte)0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        len -= 2;
                    }
                    break;
                case (byte)0x06:// Partial list of 128-bit UUIDs
                case (byte)0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        // Move the offset to read the next uuid.
                        offset += 15;
                        len -= 16;
                    }
                    break;
                case (byte)0xFF:// GAP_ADTYPE_MANUFACTURER_SPECIFIC
                    // Type of the response : Random identifier data
                    if(advertisedData[offset] == (byte)0xEE && advertisedData[offset+1] == (byte)0x01) {
                        //Length 8 is the content of the advertised data
                        if(len == 8) {
                            protocolVersion = advertisedData[offset+2];
                            antennaId = (byte)((advertisedData[offset+3]>>4)&0x0F);
                            mode = (byte)(advertisedData[offset+3] & 0x0F);
                            reSynchro = ((((int)advertisedData[offset+4]) <<8) | advertisedData[offset+5]);
                            vehicleState = advertisedData[offset+6];
                        }
                        //Length 19 is the content of the Scan Response
                        else if(len == 19){
                            random = new byte[9];
                            random[0] = advertisedData[offset+2];
                            random[1] = advertisedData[offset+3];
                            random[2] = advertisedData[offset+4];
                            random[3] = advertisedData[offset+5];
                            random[4] = advertisedData[offset+6];
                            random[5] = advertisedData[offset+7];
                            random[6] = advertisedData[offset+8];
                            random[7] = advertisedData[offset+9];
                            random[8] = advertisedData[offset+10];
                            mac = new byte[7];
                            mac[0] = advertisedData[offset+11];
                            mac[1] = advertisedData[offset+12];
                            mac[2] = advertisedData[offset+13];
                            mac[3] = advertisedData[offset+14];
                            mac[4] = advertisedData[offset+15];
                            mac[5] = advertisedData[offset+16];
                            mac[6] = advertisedData[offset+17];
                            //ACH DEBUG. Force uuids of extra services
                        }
                    }
                    offset += (len - 1);
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return new ScanResponse(random, mac, protocolVersion, antennaId, mode, reSynchro, vehicleState);
    }

    /**
     * This function checks the content the of advertising and scan response. It is specific to the
     * version 1 of the InBlue profile
     * @param scanResponse Structured content from the advertising data and scan response
     * @param device bluetooth device detected
     * @param rssi the signal strength of the scan response
     * @param advertisedData the data advertised
     */
    private void processResponseProfileV1(ScanResponse scanResponse, BluetoothDevice device, int rssi, byte[] advertisedData) {
        firePassiveEntryTry(device, rssi, scanResponse, advertisedData);
    }

    public void sendPackets(byte[][] value) {
        mBluetoothLeService.sendPackets(value);
        if (mBluetoothLeService2 != null && mBluetoothLeService2.isFullyConnected()) {
            mBluetoothLeService2.sendPackets(value);
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

    private void firePassiveEntryTry(BluetoothDevice device, int rssi, ScanResponse scanResponse, byte[] advertisedData) {
        for(BluetoothManagementListener listener : mBluetoothManagementListeners) {
            listener.onPassiveEntryTry(device, rssi, scanResponse, advertisedData);
        }
    }

    public boolean isFullyConnected() {
        return mBluetoothLeService != null && mBluetoothLeService.isFullyConnected();
    }

    private boolean isBound() {
        return mBluetoothLeService != null && mBluetoothLeService.isBound();
    }
}
