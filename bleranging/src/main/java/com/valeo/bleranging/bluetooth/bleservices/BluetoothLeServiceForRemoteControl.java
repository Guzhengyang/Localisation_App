package com.valeo.bleranging.bluetooth.bleservices;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.valeo.bleranging.bluetooth.IBluetoothLeServiceListener;
import com.valeo.bleranging.bluetooth.SampleGattAttributes;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class is a service for managing connection and data communication with a GATT server hosted on a given Bluetooth LE device.
 */
public class BluetoothLeServiceForRemoteControl extends Service {
    //Bluetooth SERVICES and CHARACTERISTICS UUIDs
    private final static String VALEO_GENERIC_SERVICE = SampleGattAttributes.VALEO_REMOTE_CONTROL_GENERIC_SERVICE;
    private final static String VALEO_IN_CHARACTERISTIC = SampleGattAttributes.VALEO_REMOTE_CONTROL_IN_CHARACTERISTIC;
    private final static String VALEO_OUT_CHARACTERISTIC = SampleGattAttributes.VALEO_REMOTE_CONTROL_OUT_CHARACTERISTIC;
    private static final short MAX_RETRIES_WRITE_CHARACTERISTIC = 5;
    private final ArrayList<IBluetoothLeServiceListener> mListeners = new ArrayList<>();
    private final IBinder mBinder = new LocalBinder3();
    private boolean mIsServiceDiscovered = false;
    private boolean isFullyConnected = false;
    private boolean isConnecting = false;
    private boolean isBound = false;
    private int mPacketToWriteCount = 0;

    /* Handler to send action to main looper */
    private Handler mainHandler;

    /**
     * Bluetooth Manager.
     */
    private BluetoothManager mBluetoothManager;

    /**
     * Bluetooth Adapter.
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * Bluetooth GATT profile.
     */
    private BluetoothGatt mBluetoothGatt;

    /**
     * Bluetooth device we want to connectToDevice to,
     */
    private BluetoothDevice mDevice;

    /**
     * Implements callback methods for GATT events that the app cares about. For example,
     * connection change and services discovered.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            PSALogs.i("NIH_REMOTE", "onCharacteristicWrite(" + VALEO_IN_CHARACTERISTIC + "): "
                    + TextUtils.printBleBytes(characteristic.getValue()));
            if (mDevice != null && mBluetoothGatt != null) {
                //Disconnect after write
                mPacketToWriteCount--;
                if (mPacketToWriteCount == 0) {
                    for (int i = 0; i < mListeners.size(); i++) {
                        mListeners.get(i).onSendPacketSuccess();
                    }
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                PSALogs.d("NIH_REMOTE", "onMtuChanged mtu request FAILED " + status);
                isFullyConnected = false;
                isConnecting = false;
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                }
            } else {
                PSALogs.d("NIH_REMOTE", "onMtuChanged mtu request SUCCESS " + status);
                PSALogs.i("NIH_REMOTE", "Connected to GATT server.");
                mPacketToWriteCount = 0;
                try {
                    // Attempts to discover services after successful connection.
                    if (!mIsServiceDiscovered) {
                        boolean isStarted = mBluetoothGatt.discoverServices();
                        PSALogs.i("NIH_REMOTE", "Attempting to start service discovery:" + isStarted);
                    } else {
                        isFullyConnected = false;
                        isConnecting = false;
                    }
                } catch (Exception e) {
                    PSALogs.w("NIH_REMOTE", "An exception occurred while trying to start the services discovery");
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            PSALogs.d("NIH_REMOTE", "onConnectionStateChange , Status =" + status + " , NewState = " + newState);
            if (status == 8) {
                PSALogs.i("NIH_REMOTE", "Connection loss, error = 8");
                isFullyConnected = false;
                isConnecting = false;
            } else if (status != BluetoothGatt.GATT_SUCCESS && status != 19) {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    PSALogs.i("NIH_REMOTE", "Error lead to disconnection from GATT server.");
                    isFullyConnected = false;
                    isConnecting = false;
                } else {
                    PSALogs.i("NIH_REMOTE", "Failed to Connected to GATT server, New State = " + newState);
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
                    }
                }
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestMtu(23);
                } else {
                    PSALogs.d("NIH_REMOTE", "onConnectionStateChange no mtu request");
                    isFullyConnected = true;
                    isConnecting = false;
                }
                // Result from the requested action: should be 1 or 15 at the end
                // Otherwise an error occurred during the process
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                PSALogs.i("NIH_REMOTE", "Disconnected from GATT server.");
                isFullyConnected = false;
                isConnecting = false;
                mIsServiceDiscovered = false;
            }
        }

        /**
         * Callback called if a service is discovered during the discovery process.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            PSALogs.i("NIH_REMOTE", "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mIsServiceDiscovered = true;
                subscribeToReadCharacteristic(true);
            } else {
                isFullyConnected = false;
                isConnecting = false;
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                }
            }
        }

        /**
         * Callback called when something is read from a readable characteristic
         * - Read the challenge
         * - Encrypt it
         * - Send it back to the car
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (characteristic == null) {
                PSALogs.e("NIH_REMOTE", "onCharacteristicRead(): null characteristic, status: " + status);
                return;
            }
            if (characteristic.getValue() == null) {
                PSALogs.e("NIH_REMOTE", "onCharacteristicRead(): null value, status: " + status);
                return;
            }
            PSALogs.i("NIH_REMOTE", "onCharacteristicRead(): " + Arrays.toString(characteristic.getValue()) + ", status=" + status);
        }

        /**
         * Callback called when the value of a characteristic with a notify
         * action is changed
         * - Send the response from the car to the challenge exchange
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            PSALogs.i("NIH_REMOTE", "onCharacteristicChanged(): " + Arrays.toString(characteristic.getValue()));
            isFullyConnected = true;
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            PSALogs.i("NIH_REMOTE", "onDescriptorWrite(): " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isFullyConnected = true;
                isConnecting = false;
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            PSALogs.i("NIH_REMOTE", "onReliableWriteCompleted(): " + status);
        }

    };

    public boolean isConnecting3() {
        return isConnecting;
    }

    public boolean isFullyConnected3() {
        return isBound && isFullyConnected;
    }

    public boolean isBound3() {
        return isBound;
    }

    public void onCreate() {
        PSALogs.i("NIH_REMOTE", "BluetoothLeService.onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        PSALogs.i("NIH_REMOTE", "onBind()");
        isBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        PSALogs.i("NIH_REMOTE", "onUnbind()");
        close();
        isBound = false;
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        PSALogs.i("NIH_REMOTE", "initialize()");
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                PSALogs.e("NIH_REMOTE", "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            PSALogs.e("NIH_REMOTE", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        mainHandler = new Handler(getApplicationContext().getMainLooper());
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *                The connection result is reported asynchronously through the
     *                {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *                callback.
     */
    public void connectToDevice(final String address) {
        PSALogs.d("NIH_REMOTE", "connectToDevice.");
        isConnecting = true;
        if (mBluetoothAdapter == null || address == null) {
            PSALogs.w("NIH_REMOTE", "BluetoothAdapter not initialized or unspecified address.");
            return;
        }
        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        //Use a handler to call the bluetooth stack from the main thread.
        //This is due to an issue on some devices such as the Galaxy S4
        //Details here: http://stackoverflow.com/questions/20069507/gatt-callback-fails-to-register
        //How to call something from main thread: http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
        Runnable runFromMainThread = new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt = mDevice.connectGatt(getApplicationContext(), false, mGattCallback);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    PSALogs.d("NIH_REMOTE", "Android version >= 5.0 --> request BALANCED priority (connection interval 7,5ms) 2");
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
                    }
                }
            } // This is your code
        };
        mainHandler.post(runFromMainThread);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        PSALogs.i("NIH_REMOTE", "BluetoothLeService.disconnect()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            PSALogs.w("NIH_REMOTE", "BluetoothAdapter not initialized 1");
            if (mBluetoothAdapter == null) {
                PSALogs.w("NIH_REMOTE", "mBluetoothAdapter is null");
            }
            if (mBluetoothGatt == null) {
                PSALogs.w("NIH_REMOTE", "mBluetoothGatt is null");
            }
            return;
        }
        Runnable runFromMainThread = new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.disconnect();
            } // This is your code
        };
        mainHandler.post(runFromMainThread);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private void close() {
        PSALogs.i("NIH_REMOTE", "BluetoothLeService.close()");
        if (mBluetoothGatt == null) {
            return;
        }
        Runnable runFromMainThread = new Runnable() {
            @Override
            public void run() {
                mIsServiceDiscovered = false;
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            } // This is your code
        };
        mainHandler.post(runFromMainThread);
    }

    private void subscribeToReadCharacteristic(boolean enabled) {
        PSALogs.d("NIH_REMOTE", "subscribeToReadCharacteristic()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            PSALogs.w("NIH_REMOTE", "BluetoothAdapter not initialized 2");
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(VALEO_GENERIC_SERVICE));
        if (service == null) {
            PSALogs.e("NIH_REMOTE", "Service not found");
            return;
        }
        BluetoothGattCharacteristic charac = service.getCharacteristic(UUID.fromString(VALEO_OUT_CHARACTERISTIC));
        if (charac == null) {
            PSALogs.e("NIH_REMOTE", "characteristic not found");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(charac, enabled);
        BluetoothGattDescriptor descriptor =
                charac.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    private boolean writeCharacteristicBatch(byte[][] value) {
        mPacketToWriteCount = value.length;
        short retriesCounter;
        for (byte[] aValue : value) {
            retriesCounter = 0;
            boolean bReturnFunctionCall;
            do {
                retriesCounter++;
                bReturnFunctionCall = writeCharacteristic(aValue);
            } while ((!bReturnFunctionCall) && (retriesCounter < MAX_RETRIES_WRITE_CHARACTERISTIC));
            if (retriesCounter == MAX_RETRIES_WRITE_CHARACTERISTIC) {
                return false;
            }
        }
        return true;
    }

    private boolean writeCharacteristic(byte[] value) {
        boolean bReturn = false;
        if (mBluetoothAdapter == null) {
            PSALogs.w("NIH_REMOTE", "BluetoothAdapter not initialized 2");
        }
        if (mBluetoothGatt == null) {
            PSALogs.w("NIH_REMOTE", "mBluetoothGatt not initialized 2");
            return false;
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(VALEO_GENERIC_SERVICE));
        if (service == null) {
            PSALogs.e("NIH_REMOTE", "Service not found");
            return false;
        }
        BluetoothGattCharacteristic charac = service.getCharacteristic(UUID.fromString(VALEO_IN_CHARACTERISTIC));
        if (charac == null) {
            PSALogs.e("NIH_REMOTE", "characteristic not found");
            return false;
        }
        if (value == null) {
            PSALogs.e("NIH_REMOTE", "writeCharacteristic(): Null value");
            return false;
        }
        charac.setValue(value);
        if (mBluetoothGatt != null) {
            bReturn = mBluetoothGatt.writeCharacteristic(charac);
        }
        return bReturn;
    }

    public BluetoothGattService getBLEGattService() {
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.getService(UUID.fromString(VALEO_GENERIC_SERVICE));
        }
        return null;
    }

    public boolean sendPackets(byte[] value) {
        return writeCharacteristicBatch(new byte[][]{value});
    }

    public class LocalBinder3 extends Binder {
        public BluetoothLeServiceForRemoteControl getService() {
            return BluetoothLeServiceForRemoteControl.this;
        }
    }

}
