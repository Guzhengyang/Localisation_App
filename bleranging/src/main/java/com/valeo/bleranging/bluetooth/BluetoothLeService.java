package com.valeo.bleranging.bluetooth;

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
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.valeo.bleranging.utils.TextUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.UUID;

/**
 * This class is a service for managing connection and data communication with a GATT server hosted on a given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    //Bluetooth actions
    public final static String ACTION_GATT_CONNECTED = "com.inblue.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.inblue.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.inblue.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.inblue.ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_SERVICES_FAILED = "com.inblue.ACTION_GATT_SERVICES_FAILED";
    public final static String ACTION_GATT_CONNECTION_LOSS = "com.inblue.ACTION_GATT_CONNECTION_LOSS";
    public final static String ACTION_GATT_CHARACTERISTIC_SUBSCRIBED = "com.inblue.ACTION_GATT_CHARACTERISTIC_SUBSCRIBED";
    //Bluetooth SERVICES and CHARACTERISTICS UUIDs
    public final static String VALEO_GENERIC_SERVICE = SampleGattAttributes.VALEO_GENERIC_SERVICE;
    public final static String VALEO_IN_CHARACTERISTIC = SampleGattAttributes.VALEO_IN_CHARACTERISTIC;
    public final static String VALEO_OUT_CHARACTERISTIC = SampleGattAttributes.VALEO_OUT_CHARACTERISTIC;
    private static final short MAX_RETRIES_WRITE_CHARACTERISTIC = 5;
    private final ArrayList<IBluetoothLeServiceListener> mListeners = new ArrayList<>();
    private final IBinder mBinder = new LocalBinder();
    private boolean mIsServiceDiscovered=false;
    private boolean isFullyConnected = false;
    private int mPacketToWriteCount = 0;
    private Deque<byte[]> mReceiveQueue;

    /** Bluetooth Manager. */
    private BluetoothManager mBluetoothManager;

    /** Bluetooth Adapter. */
    private BluetoothAdapter mBluetoothAdapter;

    /** Bluetooth GATT profile. */
    private BluetoothGatt mBluetoothGatt;

    /** Bluetooth device we want to connectToDevice to, */
    private BluetoothDevice mDevice;

    /** Handler to return to the controller results from the action requested */
    private Handler mBSHandler;
    /**
     * Implements callback methods for GATT events that the app cares about. For example,
     * connection change and services discovered.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("NIH", "onCharacteristicWrite(): status=" + status + " " +
                    TextUtils.getHexString(characteristic.getValue()));
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
            String intentAction;
            if (status != BluetoothGatt.GATT_SUCCESS) {
                // makeNoise when connexion failed
                final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, 70);
                toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 50);
                Log.d("NIH", "onMtuChanged mtu request FAILED " + status);
                intentAction = ACTION_GATT_SERVICES_FAILED;
                broadcastUpdate(intentAction);
            } else {
                Log.d("NIH", "onMtuChanged mtu request SUCCESS " + status);
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i("NIH", "Connected to GATT server.");
                mPacketToWriteCount = 0;
                try {
                    // Attempts to discover services after successful connection.
                    if (!mIsServiceDiscovered) {
                        boolean isStarted = mBluetoothGatt.discoverServices();
                        Log.i("NIH", "Attempting to start service discovery:" + isStarted);
                    }
                } catch (Exception e) {
                    Log.w("NIH", "An exception occurred while trying to start the services discovery", e);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("NIH", "onConnectionStateChange , Status =" + status + " , NewState = " + newState);
            String intentAction;
            if (status == 8) {
                intentAction = ACTION_GATT_CONNECTION_LOSS;
                broadcastUpdate(intentAction);
                isFullyConnected = false;
            } else if (status != BluetoothGatt.GATT_SUCCESS && status != 19) {
                // makeNoise when connexion failed
                final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, 70);
                toneG.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 50);
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    Log.i("NIH", "Error lead to disconnection from GATT server.");
                    if (mBSHandler != null) {
                        // Send a message to the right handler
                        // Return the result from the requested action
                        Message msg = mBSHandler.obtainMessage();
                        mBSHandler.sendMessage(msg);
                    }
                    broadcastUpdate(intentAction);
                    isFullyConnected = false;
                } else {
                    Log.i("NIH", "Failed to Connected to GATT server, New State = " + newState);
                    intentAction = ACTION_GATT_SERVICES_FAILED;
                    broadcastUpdate(intentAction);
                }
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestMtu(42);
                } else {
                    Log.d("NIH", "onConnectionStateChange no mtu request");
                }
                // Result from the requested action: should be 1 or 15 at the end
                // Otherwise an error occurred during the process
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i("NIH", "Disconnected from GATT server.");
                if (mBSHandler != null) {
                    // Send a message to the right handler
                    // Return the result from the requested action
                    Message msg = mBSHandler.obtainMessage();
                    mBSHandler.sendMessage(msg);
                }
                broadcastUpdate(intentAction);
                isFullyConnected = false;
            }
        }

        /**
         * Callback called if a service is discovered during the discovery process.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("NIH", "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mIsServiceDiscovered = true;
                subscribeToReadCharacteristic(true);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                broadcastUpdate(ACTION_GATT_SERVICES_FAILED);
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
                Log.e("NIH", "onCharacteristicRead(): null characteristic, status: " + status);
                return;
            }
            if (characteristic.getValue() == null) {
                Log.e("NIH", "onCharacteristicRead(): null value, status: " + status);
                return;
            }
            Log.i("NIH", "onCharacteristicRead(): " + Arrays.toString(characteristic.getValue()) + ", status=" + status);
        }

        /**
         * Callback called when the value of a characteristic with a notify
         * action is changed
         * - Send the response from the car to the challenge exchange
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i("NIH", "onCharacteristicChanged(): " + Arrays.toString(characteristic.getValue()));
            mReceiveQueue.add(characteristic.getValue());
            broadcastUpdate(ACTION_DATA_AVAILABLE);
            isFullyConnected = true;
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.i("NIH", "onDescriptorWrite(): " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_CHARACTERISTIC_SUBSCRIBED);
                isFullyConnected = true;
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.i("NIH", "onReliableWriteCompleted(): " + status);
        }

    };

    public Deque<byte[]> getReceiveQueue(){
        return mReceiveQueue;
    }

    /**
     * Send an action to the broadcast channel
     * @param action the action to execute
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public boolean isFullyConnected() {
        return isFullyConnected;
    }

    public void init() {
        mBSHandler = new Handler();
        mReceiveQueue = new ArrayDeque<>();
    }

    public void onCreate() {
        Log.i("NIH", "BluetoothLeService.onCreate()");
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("NIH", "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        Log.i("NIH", "onUnbind()");
        close();
        isFullyConnected = false;
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        Log.i("NIH", "initialize()");
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e("NIH", "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("NIH", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connectToDevice(final String address) {
        Log.d("NIH", "connectToDevice.");
        if (mBluetoothAdapter == null || address == null) {
            Log.w("NIH", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        //Use a handler to call the bluetooth stack from the main thread.
        //This is due to an issue on some devices such as the Galaxy S4
        //Details here: http://stackoverflow.com/questions/20069507/gatt-callback-fails-to-register
        //How to call something from main thread: http://stackoverflow.com/questions/11123621/running-code-in-main-thread-from-another-thread
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        Runnable runFromMainThread = new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt = mDevice.connectGatt(getApplicationContext(), true, mGattCallback);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.d("NIH", "Android version >= 5.0 --> request HIGH priority (connection interval 7,5ms) 2");
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                    }
                }
            } // This is your code
        };
        mainHandler.post(runFromMainThread);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        Log.i("NIH", "BluetoothLeService.disconnect()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("NIH", "BluetoothAdapter not initialized 1");
            if (mBluetoothAdapter == null) {
                Log.w("NIH", "mBluetoothAdapter is null");
            }
            if (mBluetoothGatt == null) {
                Log.w("NIH", "mBluetoothGatt is null");
            }
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        Log.i("NIH", "BluetoothLeService.close()");
        if (mBluetoothGatt == null) {
            return;
        }
        mIsServiceDiscovered = false;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void subscribeToReadCharacteristic(boolean enabled){
        Log.d("NIH", "subscribeToReadCharacteristic()");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w("NIH", "BluetoothAdapter not initialized 2");
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(VALEO_GENERIC_SERVICE));
        if(service == null){
            Log.e("NIH", "Service not found");
            return;
        }
        BluetoothGattCharacteristic charac = service.getCharacteristic(UUID.fromString(VALEO_OUT_CHARACTERISTIC));
        if (charac == null) {
            Log.e("NIH", "characteristic not found");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(charac, enabled);
        BluetoothGattDescriptor descriptor =
                charac.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    private void writeCharacteristicBatch(byte[][] value){
        mPacketToWriteCount = value.length;
        for (byte[] aValue : value) {
            short retriesCounter = 0;
            boolean bReturnFunctionCall;
            do {
                retriesCounter++;
                bReturnFunctionCall = writeCharacteristic(aValue);
            } while ((!bReturnFunctionCall) && (retriesCounter < MAX_RETRIES_WRITE_CHARACTERISTIC));
        }
    }

    private boolean writeCharacteristic(byte[] value) {
        boolean bReturn = false;
        if (mBluetoothAdapter == null) {
            Log.w("NIH", "BluetoothAdapter not initialized 2");
        }
        if (mBluetoothGatt == null) {
            Log.w("NIH", "mBluetoothGatt not initialized 2");
            return false;
        }
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(VALEO_GENERIC_SERVICE));
        if(service == null){
            Log.e("NIH", "Service not found");
            return false;
        }
        BluetoothGattCharacteristic charac = service.getCharacteristic(UUID.fromString(VALEO_IN_CHARACTERISTIC));
        if (charac == null) {
            Log.e("NIH", "characteristic not found");
            return false;
        }
        if(value == null){
            Log.e("NIH", "writeCharacteristic(): Null value");
            return false;
        }
        Log.d("NIH", "writeCharacteristic(" + VALEO_IN_CHARACTERISTIC + "): " + TextUtils.getHexString(value));
        charac.setValue(value);
        if(mBluetoothGatt != null) {
            bReturn = mBluetoothGatt.writeCharacteristic(charac);
        }
        return bReturn;
    }

    public void sendPackets(byte[][] value) {
        writeCharacteristicBatch(value);
    }

    public void registerListener(IBluetoothLeServiceListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(IBluetoothLeServiceListener listener) {
        mListeners.remove(listener);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

}
