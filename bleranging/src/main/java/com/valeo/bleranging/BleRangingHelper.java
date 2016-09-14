package com.valeo.bleranging;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.valeo.bleranging.bluetooth.BluetoothLeService;
import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.bluetooth.BluetoothManagementListener;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.ScanResponse;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Ranging;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 19/07/2016.
 */
public class BleRangingHelper implements SensorEventListener {
    public final static int WELCOME_AREA = 1;
    public final static int LOCK_AREA = 2;
    public final static int UNLOCK_LEFT_AREA = 3;
    public final static int UNLOCK_RIGHT_AREA = 4;
    public final static int UNLOCK_BACK_AREA = 5;
    public final static int START_AREA = 6;
    public final static String BLE_ADDRESS_37 = "D4:F5:13:56:7A:12";
    public final static String BLE_ADDRESS_38 = "D4:F5:13:56:37:32";
    public final static String BLE_ADDRESS_39 = "D4:F5:13:56:39:E7";
    private static final int LOCK_STATUS_CHANGED_TIMEOUT = 3000;
    private static final int PREDICTION_MAX = 16;
    private final Context mContext;
    private final BluetoothManagement mBluetoothManager;
    private final float linAccThreshold = SdkPreferencesHelper.getInstance().getCorrectionLinAcc();
    private final int linAccSize = SdkPreferencesHelper.getInstance().getLinAccSize();
    private final String trxAddressConnectable = SdkPreferencesHelper.getInstance().getTrxAddressConnectable();
    private final String trxAddressLeft = SdkPreferencesHelper.getInstance().getTrxAddressLeft();
    private final String trxAddressMiddle = SdkPreferencesHelper.getInstance().getTrxAddressMiddle();
    private final String trxAddressRight = SdkPreferencesHelper.getInstance().getTrxAddressRight();
    private final String trxAddressBack = SdkPreferencesHelper.getInstance().getTrxAddressBack();
    private final String trxAddressFrontLeft = SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft();
    private final String trxAddressFrontRight = SdkPreferencesHelper.getInstance().getTrxAddressFrontRight();
    private final String trxAddressRearLeft = SdkPreferencesHelper.getInstance().getTrxAddressRearLeft();
    private final String trxAddressRearRight = SdkPreferencesHelper.getInstance().getTrxAddressRearRight();
    public boolean smartphoneIsInPocket = false;
    public boolean smartphoneIsLaidDownLAcc = false;
    private Integer rangingPredictionInt = -1;
    private LinkedList<Integer> predictionHistoric;
    private boolean isLockStrategyValid = false;
    private int isUnlockStrategyValid = 0;
    private boolean isStartStrategyValid = false;
    private boolean isWelcomeStrategyValid = false;
    private boolean isLightCaptorEnabled = SdkPreferencesHelper.getInstance().isLightCaptorEnabled();
    private boolean checkNewPacketOnlyOneLaunch = true;
    private byte[] bytesToSend;
    private byte[] bytesReceived;
    private byte[] lastPacketIdNumber = new byte[2];
    private float R[] = new float[9];
    private float I[] = new float[9];
    private String lastConnectedCarType = "";
    private int totalAverage;
    private boolean newLockStatus;
    private boolean isAbortRunning = false;
    private boolean isFirstConnection = true;
    private boolean isTryingToConnect = false;
    private final Runnable mManageIsTryingToConnectTimer = new Runnable() {
        @Override
        public void run() {
            isTryingToConnect = false;
        }
    };
    private AtomicBoolean isLockStatusChangedTimerExpired = new AtomicBoolean(true);
    /**
     * Create a handler to detect if the vehicle can do a unlock
     */
    private final Runnable mManageIsLockStatusChangedPeriodicTimer = new Runnable() {
        @Override
        public void run() {
            isLockStatusChangedTimerExpired.set(true);
        }
    };
    private AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    private AtomicBoolean rearmLock = new AtomicBoolean(true);
    private AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    private AtomicBoolean isPassiveEntryAction = new AtomicBoolean(false);
    private Antenna.BLEChannel bleChannel = Antenna.BLEChannel.BLE_CHANNEL_37;
    private Handler mMainHandler;
    private Handler mLockStatusChangedHandler;
    private Handler mHandlerTimeOut;
    private Handler mIsLaidTimeOutHandler;
    private ConnectedCar connectedCar;
    private byte welcomeByte = 0;
    private byte lockByte = 0;
    private byte startByte = 0;
    private byte leftAreaByte = 0;
    private byte rightAreaByte = 0;
    private byte backAreaByte = 0;
    private byte walkAwayByte = 0;
    private byte steadyByte = 0;
    private byte approachByte = 0;
    private byte leftTurnByte = 0;
    private byte fullTurnByte = 0;
    private byte rightTurnByte = 0;
    private byte recordByte = 0;
    private InblueProtocolManager mProtocolManager;
    private BleRangingListener bleRangingListener;
    private ArrayList<Double> lAccHistoric = new ArrayList<>(linAccSize);
    private double deltaLinAcc = 0;
    private boolean isLaidRunnableAlreadyLaunched = false;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float orientation[] = new float[3];
    private Runnable checkNewPacketsRunner = new Runnable() {
        @Override
        public void run() {
            Log.d("NIH", "checkNewPacketsRunner " + lastPacketIdNumber[0] + " " + (bytesReceived[0] + " " + lastPacketIdNumber[1] + " " + bytesReceived[1]));
            if((lastPacketIdNumber[0] == bytesReceived[0]) && (lastPacketIdNumber[1] == bytesReceived[1])) {
                mBluetoothManager.disconnect();
            } else {
                lastPacketIdNumber[0] = bytesReceived[0];
                lastPacketIdNumber[1] = bytesReceived[1];
            }
            if (isFullyConnected() && mMainHandler != null) {
                mMainHandler.postDelayed(this, 1000);
            }
        }
    };
    private Runnable checkAntennaRunner = new Runnable() {
        @Override
        public void run() {
            if (isFullyConnected()) {
                Log.w(" rssiHistorics", "************************************** CHECK ANTENNAS ************************************************");
                connectedCar.compareCheckerAndSetAntennaActive();
            }
            if (mMainHandler != null) {
                mMainHandler.postDelayed(this, 2500);
            }
        }
    };
    private Runnable abortCommandRunner = new Runnable() {
        @Override
        public void run() {
            Log.d("NIH rearm", "abortCommandRunner");
            if(mProtocolManager.isLockedFromTrx() != mProtocolManager.isLockedToSend()) {
                mProtocolManager.setIsLockedToSend(mProtocolManager.isLockedFromTrx());
                bleRangingListener.updateCarDoorStatus(mProtocolManager.isLockedFromTrx());
                rearmLock.set(false);
            }
            isAbortRunning = false;
        }
    };
    private Runnable sendPacketRunner = new Runnable() {
        @Override
        public void run() {
            Log.d("NIH", "getPacketOnePayload then sendPackets");
            bytesToSend = mProtocolManager.getPacketOnePayload();
            mBluetoothManager.sendPackets(new byte[][]{bytesToSend});
            if (isFullyConnected() && mMainHandler != null) {
                mMainHandler.postDelayed(this, 200);
            }
        }
    };
    private Runnable isLaidRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsLaidDownLAcc = true; // smartphone is staying still
            mIsLaidTimeOutHandler.removeCallbacks(this);
        }
    };
    private boolean isLoggable = true;
    private Runnable logRunner = new Runnable() {
        @Override
        public void run() {
            if (isLoggable) {
                TrxUtils.appendRssiLogs(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_ID_0),
                        orientation[0], orientation[1], orientation[2],
                        smartphoneIsInPocket, smartphoneIsLaidDownLAcc, isPassiveEntryAction.get(), isLockStatusChangedTimerExpired.get(),
                        rearmLock.get(), rearmUnlock.get(), rearmWelcome.get(), newLockStatus, welcomeByte,
                        lockByte, startByte, leftAreaByte, rightAreaByte, backAreaByte,
                        walkAwayByte, steadyByte, approachByte, leftTurnByte,
                        fullTurnByte, rightTurnByte, recordByte, rangingPredictionInt,
                        mProtocolManager.isLockedFromTrx(), mProtocolManager.isLockedToSend(), mProtocolManager.isStartRequested());
            }
            if (isFullyConnected() && mMainHandler != null) {
                mMainHandler.postDelayed(this, 105);
            }
        }
    };
    private Runnable printRunner = new Runnable() {
        @Override
        public void run() {
            if (isFullyConnected()) {
                Log.w(" rssiHistorics", "************************************** IHM LOOP START *************************************************");
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder = connectedCar.createHeaderDebugData(spannableStringBuilder, bleChannel);
                totalAverage = connectedCar.getAllTrxAverage(Antenna.AVERAGE_DEFAULT);
                tryStrategies(newLockStatus);
                spannableStringBuilder = connectedCar.createFirstFooterDebugData(spannableStringBuilder);
                spannableStringBuilder = connectedCar.createSecondFooterDebugData(spannableStringBuilder,
                        smartphoneIsInPocket, smartphoneIsLaidDownLAcc, totalAverage, rearmLock.get(), rearmUnlock.get());
                spannableStringBuilder = connectedCar.createThirdFooterDebugData(spannableStringBuilder,
                        bytesToSend, bytesReceived, deltaLinAcc, smartphoneIsLaidDownLAcc, mBluetoothManager);
                updateCarLocalization();
                bleRangingListener.printDebugInfo(spannableStringBuilder);
                Log.w(" rssiHistorics", "************************************** IHM LOOP END *************************************************");
            }
            if (mMainHandler != null) {
                mMainHandler.postDelayed(this, 500);
            }
        }
    };

    /**
     * Handles various events fired by the Service.
     * ACTION_GATT_CHARACTERISTIC_SUBSCRIBED: subscribe to GATT characteristic.
     * ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
     * or notification operations.
     */
    private final BroadcastReceiver mTrxUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                bytesReceived = mBluetoothManager.getBytesReceived();
                Log.d("NIH", "TRX ACTION_DATA_AVAILABLE");
                boolean oldLockStatus = newLockStatus;
                newLockStatus = (bytesReceived[5] & 0x01) != 0;
                if (isPassiveEntryAction.get() && oldLockStatus != newLockStatus) {
                    connectedCar.resetWithHysteresis(newLockStatus, isUnlockStrategyValid);
                    bleRangingListener.updateCarDoorStatus(newLockStatus);
                }
                mProtocolManager.setIsLockedFromTrx(newLockStatus);
                if (checkNewPacketOnlyOneLaunch) {
                    checkNewPacketOnlyOneLaunch = false;
                    mMainHandler.postDelayed(checkNewPacketsRunner, 1000);
                }
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_SUBSCRIBED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_CHARACTERISTIC_SUBSCRIBED");
                if (mMainHandler != null) {
                    mMainHandler.post(sendPacketRunner); // send works only after subcribed
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_SERVICES_DISCONNECTED");
                restartConnection(false);
            } else if (BluetoothLeService.ACTION_GATT_CONNECTION_LOSS.equals(action)) {
                Log.w("NIH", "ACTION_GATT_CONNECTION_LOSS");
                restartConnection(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_FAILED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_SERVICES_FAILED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_SERVICES_DISCOVERED");
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_CONNECTED");
            }
        }
    };

    public BleRangingHelper(Context context, BleRangingListener bleRangingListener) {
        this.mContext = context;
        this.mBluetoothManager = new BluetoothManagement(context);
        this.bleRangingListener = bleRangingListener;
        this.predictionHistoric = new LinkedList<>();
        this.mProtocolManager = new InblueProtocolManager();
        this.mLockStatusChangedHandler = new Handler();
        this.mHandlerTimeOut = new Handler();
        this.mIsLaidTimeOutHandler = new Handler();
        mBluetoothManager.addBluetoothManagementListener(new BluetoothManagementListener() {
            @Override
            public void onPassiveEntryTry(BluetoothDevice device, int rssi, ScanResponse scanResponse, byte[] advertisedData) {
                bleChannel = getCurrentChannel(device, bleChannel);
                doPassiveEntry(device, rssi, scanResponse, advertisedData);
            }
        });
        SensorManager senSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor senLinAcceleration = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senLinAcceleration, SensorManager.SENSOR_DELAY_UI);
        senSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        mBluetoothManager.resumeLeScan();
    }

    public void setIsPassiveEntryAction(boolean isPassiveEntryAction) {
        this.isPassiveEntryAction.set(isPassiveEntryAction);
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    public void restartConnection(boolean createConnectedCar) {
        Log.d("NIH", "restartConnection");
        mBluetoothManager.suspendLeScan();
        mBluetoothManager.disconnect();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(checkAntennaRunner);
            mMainHandler.removeCallbacks(printRunner);
            mMainHandler.removeCallbacks(logRunner);
            mMainHandler.removeCallbacks(sendPacketRunner);
            mMainHandler.removeCallbacks(checkNewPacketsRunner);
            mMainHandler.removeCallbacks(null);
            mMainHandler = null;
        }
        mProtocolManager.restartPacketOneCounter();
        rearmWelcome.set(true);
        isFirstConnection = true;
        checkNewPacketOnlyOneLaunch = true;
        makeNoise(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 100);
        if (createConnectedCar) {
            connectedCar = null;
            lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
            connectedCar = ConnectedCarFactory.getConnectedCar(lastConnectedCarType);
        }
        // wait to close every connection before creating them again
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothManager.connect(mTrxUpdateReceiver);
                mBluetoothManager.resumeLeScan();
            }
        }, 250);
    }

    /**
     * Rearm rearm bool if rssi is very low
     * @param rssi the rssi value to compare with threshold
     */
    private void rearmWelcome(int rssi) {
        if (smartphoneIsInPocket && rssi <= -135) {
            rearmWelcome.set(true);
        } else if (!smartphoneIsInPocket && rssi <= -120) {
            rearmWelcome.set(true);
        }
    }

    /**
     * Get the current advertising channel
     * @param device the device that advertise on only one channel
     */
    private Antenna.BLEChannel getCurrentChannel(BluetoothDevice device, Antenna.BLEChannel bleChannel) {
        switch (device.getAddress()) {
            case BLE_ADDRESS_37:
                Log.e("NIH", "****Antenna 37**** " + device.getAddress());
                return Antenna.BLEChannel.BLE_CHANNEL_37;
            case BLE_ADDRESS_38:
                Log.e("NIH", "****Antenna 38**** " + device.getAddress());
                return Antenna.BLEChannel.BLE_CHANNEL_38;
            case BLE_ADDRESS_39:
                Log.e("NIH", "****Antenna 39**** " + device.getAddress());
                return Antenna.BLEChannel.BLE_CHANNEL_39;
        }
        return bleChannel;
    }

    /**
     * Save all trx rssi
     * @param device the trx that send the scanResponse
     * @param rssi the rssi of the scanResponse
     * @param scanResponse the scanResponse received
     */
    private void doPassiveEntry(final BluetoothDevice device, int rssi, ScanResponse scanResponse, byte[] advertisedData) {
        if (device != null && scanResponse != null) {
            rearmWelcome(rssi); // rearm rearmWelcome Boolean
            if (isFirstConnection) {
                if (isFullyConnected()) {
                    isFirstConnection = false;
                    runFirstConnection(scanResponse);
                    mHandlerTimeOut.removeCallbacks(mManageIsTryingToConnectTimer);
                    mHandlerTimeOut.removeCallbacks(null);
                } else if (device.getAddress().equals(trxAddressConnectable) && !isTryingToConnect) {
                    isTryingToConnect = true;
                    mHandlerTimeOut.postDelayed(mManageIsTryingToConnectTimer, 5000);
                    mBluetoothManager.setConnectableDeviceAddress(trxAddressConnectable);
                    mBluetoothManager.connect(mTrxUpdateReceiver);
                }
            } else if (isFullyConnected()) {
                if(device.getAddress().equals(trxAddressLeft)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_LEFT=" + trxAddressLeft + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressMiddle)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_MIDDLE, scanResponse.antennaId, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_MIDDLE=" + trxAddressMiddle + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressRight)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_RIGHT=" + trxAddressRight + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressBack)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_BACK=" + trxAddressBack + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressFrontLeft)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_FRONT_LEFT=" + trxAddressFrontLeft + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressFrontRight)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_FRONT_RIGHT=" + trxAddressFrontRight + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressRearLeft)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_REAR_LEFT=" + trxAddressRearLeft + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressRearRight)) {
                    connectedCar.saveRssi(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_ID_0, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_REAR_RIGHT=" + trxAddressRearRight + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT) + " " + connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else {
                    if (advertisedData != null && advertisedData.length > 0) {
                        Log.d(" rssiHistoric", "BLE_ADDRESS_LOGGER=" + TextUtils.printBleBytes(advertisedData));
                        getAdvertisedBytes(advertisedData);
                    }
                }
                Ranging ranging = connectedCar.prepareRanging(mContext, smartphoneIsInPocket);
                if (predictionHistoric.size() == PREDICTION_MAX) {
                    predictionHistoric.remove(0);
                }
                int prediction = ranging.predict2int();
                predictionHistoric.add(prediction);
            }
        }
    }

    public Integer mostCommon(List<Integer> list) {
        if (list.size() == 0) {
            return null;
        }
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (Integer t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }
        Map.Entry<Integer, Integer> max = null;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }
        return max == null ? -1 : max.getKey();
    }

    /**
     * Create two bytes with all the bits from the switches
     */
    private void getAdvertisedBytes(byte[] advertisedData) {
        if (advertisedData != null) {
            steadyByte = (byte) ((advertisedData[3] & (1 << 7)) >> 7);
            walkAwayByte = (byte) ((advertisedData[3] & (1 << 6)) >> 6);
            backAreaByte = (byte) ((advertisedData[3] & (1 << 5)) >> 5);
            rightAreaByte = (byte) ((advertisedData[3] & (1 << 4)) >> 4);
            leftAreaByte = (byte) ((advertisedData[3] & (1 << 3)) >> 3);
            startByte = (byte) ((advertisedData[3] & (1 << 2)) >> 2);
            lockByte = (byte) ((advertisedData[3] & (1 << 1)) >> 1);
            welcomeByte = (byte) (advertisedData[3] & 1);
            recordByte = (byte) ((advertisedData[4] & (1 << 7)) >> 7);
            rightTurnByte = (byte) ((advertisedData[4] & (1 << 3)) >> 3);
            fullTurnByte = (byte) ((advertisedData[4] & (1 << 2)) >> 2);
            leftTurnByte = (byte) ((advertisedData[4] & (1 << 1)) >> 1);
            approachByte = (byte) (advertisedData[4] & 1);
        }
    }

    /**
     * Make a sound noise from volume at 80%, the sound button level let us decide the remaining 20%
     * @param noiseSelected the noise tonalite
     * @param duration the length of the noise
     */
    private void makeNoise(int noiseSelected, int duration) {
        float streamVolumeOnFifteen = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_SYSTEM);
        float maxVolumeOnFifteen = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        float currentVolumeOnHundred =   streamVolumeOnFifteen / maxVolumeOnFifteen;
        currentVolumeOnHundred *= 20;
        currentVolumeOnHundred = currentVolumeOnHundred + 80;
        final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, (int) currentVolumeOnHundred);
        toneG.startTone(noiseSelected, duration);
        if(mMainHandler != null) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toneG.release();
                }
            }, duration);
        }
    }

    /**
     * Try all strategy based on rssi values
     * @param newLockStatus the lock status of the vehicle
     */
    private void tryStrategies(boolean newLockStatus) {
        if (isFullyConnected()) {
            boolean isStartAllowed = false;
            isLockStrategyValid = connectedCar.lockStrategy(smartphoneIsInPocket);
            isUnlockStrategyValid = connectedCar.unlockStrategy(smartphoneIsInPocket);
            isStartStrategyValid = connectedCar.startStrategy(newLockStatus, smartphoneIsInPocket);
            isWelcomeStrategyValid = connectedCar.welcomeStrategy(totalAverage, newLockStatus, smartphoneIsInPocket);
            rangingPredictionInt = mostCommon(predictionHistoric);
            if (rearmWelcome.get() && isWelcomeStrategyValid) {
                rearmWelcome.set(false);
                //TODO Welcome
            } else if (isLockStatusChangedTimerExpired.get() && rearmLock.get() && isLockStrategyValid && isUnlockStrategyValid == 0) {
                // DO NOT check if !newLockStatus to let the rearm algorithm in performLockVehicle work
                Log.d(" rssiHistorics", "lock");
                isPassiveEntryAction.set(true);
                performLockVehicleRequest(true);
            } else if (isLockStatusChangedTimerExpired.get() && rearmUnlock.get() && isUnlockStrategyValid != 0 && !isLockStrategyValid) {
                // DO NOT check if newLockStatus to let the rearm algorithm in performLockVehicle work
                Log.d(" rssiHistorics", "unlock");
                isPassiveEntryAction.set(true);
                performLockVehicleRequest(false);
            } else if (isStartStrategyValid) {
                isStartAllowed = true;
                //Perform the connection
                if (isLightCaptorEnabled) {
                    makeNoise(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350);
                }
            }
            mProtocolManager.setIsStartRequested(isStartAllowed);
        }
    }

    /**
     * Update the mini map with our location around the car
     */
    private void updateCarLocalization() {
        //START
        if(isStartStrategyValid) {
            bleRangingListener.lightUpArea(START_AREA);
        } else {
            bleRangingListener.darkenArea(START_AREA);
        }
        //UNLOCK
        bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
        bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
        bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
        if(!isLockStrategyValid) {
            switch (isUnlockStrategyValid) {
                case ConnectedCar.NUMBER_TRX_LEFT:
                    bleRangingListener.lightUpArea(UNLOCK_LEFT_AREA);
                    break;
                case ConnectedCar.NUMBER_TRX_RIGHT:
                    bleRangingListener.lightUpArea(UNLOCK_RIGHT_AREA);
                    break;
                case ConnectedCar.NUMBER_TRX_BACK:
                    bleRangingListener.lightUpArea(UNLOCK_BACK_AREA);
                    break;
                default:
                    bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
                    bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
                    bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
                    break;
            }
        }
        // LOCK
        if(isLockStrategyValid && isUnlockStrategyValid == 0) {
            bleRangingListener.lightUpArea(LOCK_AREA);
        } else {
            bleRangingListener.darkenArea(LOCK_AREA);
        }
        // WELCOME
        if (rearmWelcome.get() && isWelcomeStrategyValid) {
            bleRangingListener.lightUpArea(WELCOME_AREA);
        } else {
            bleRangingListener.darkenArea(WELCOME_AREA);
        }
    }

    /**
     * Initialize Trx and antenna then launch IHM looper and antenna active check loop
     * @param scanResponse the scanResponse received
     */
    private void runFirstConnection(final ScanResponse scanResponse) {
        Log.w(" rssiHistorics", "************************************** runFirstConnection ************************************************");
        newLockStatus = (scanResponse.vehicleState & 0x01) != 0;
        bleRangingListener.updateCarDoorStatus(newLockStatus);
        mProtocolManager.setIsLockedToSend(newLockStatus);
        if (connectedCar != null) {
            connectedCar.initializeTrx(newLockStatus);
        }
        mMainHandler = new Handler(Looper.getMainLooper());
        mMainHandler.post(checkAntennaRunner);
        mMainHandler.post(printRunner);
        mMainHandler.post(logRunner);
    }

    public void initializeConnectedCar() {
        if (lastConnectedCarType.equals("")) {
            // on first run, create a new car
            lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
            connectedCar = ConnectedCarFactory.getConnectedCar(lastConnectedCarType);
        } else if (!lastConnectedCarType.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getConnectedCarType())) {
            if (isFullyConnected()) {
                // if car type has changed, stop connection, create a new car, and restart it
                restartConnection(true);
            } else {
                lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
                connectedCar = ConnectedCarFactory.getConnectedCar(lastConnectedCarType);
            }
        }
    }

    /**
     * Calculate acceleration rolling average
     * @param lAccHistoric all acceleration values
     * @return the rolling average of acceleration
     */
    private float getRollingAverageLAcc(ArrayList<Double> lAccHistoric) {
        float average = 0;
        if(lAccHistoric.size() > 0) {
            for (Double element : lAccHistoric) {
                average += element;
            }
            average /= lAccHistoric.size();
        }
        return average;
    }

    /**
     * Calculate the quadratic sum
     * @param x the first axe value
     * @param y the second axe value
     * @param z the third axe value
     * @return the quadratic sum of the three axes
     */
    private double getQuadratiqueSum(float x, float y , float z) {
        return Math.sqrt(x*x + y*y + z*z);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //near
            smartphoneIsInPocket = (event.values[0] == 0);
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
            if (lAccHistoric.size() == linAccSize) {
                lAccHistoric.remove(0);
            }
            double currentLinAcc = getQuadratiqueSum(event.values[0], event.values[1], event.values[2]);
            lAccHistoric.add(currentLinAcc);
            double averageLinAcc = getRollingAverageLAcc(lAccHistoric);
            deltaLinAcc = Math.abs(currentLinAcc - averageLinAcc);
            if (deltaLinAcc < linAccThreshold) {
                if(!isLaidRunnableAlreadyLaunched) {
                    mIsLaidTimeOutHandler.postDelayed(isLaidRunnable, 8000); // wait before apply stillness
                    isLaidRunnableAlreadyLaunched = true;
                }
            } else {
                smartphoneIsLaidDownLAcc = false; // smartphone is moving
                mIsLaidTimeOutHandler.removeCallbacks(isLaidRunnable);
                isLaidRunnableAlreadyLaunched = false;
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            Arrays.fill(R, 0);
            Arrays.fill(I, 0);
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                SensorManager.getOrientation(R, orientation); // orientation contains: azimut, pitch and roll
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Send the lock / unlock request to the vehicle.
     * @param lockVehicle true to lock the vehicle, false to unlock it.
     */
    public void performLockVehicleRequest(final boolean lockVehicle) {
        if (!isFullyConnected()) {
            return;
        }
        boolean lastLockCommand = mProtocolManager.isLockedToSend();
        mProtocolManager.setIsLockedToSend(lockVehicle);
        StringBuilder rearmStringBuilder = new StringBuilder("");
        if (isPassiveEntryAction.get()) { // if PEPS
            rearmStringBuilder.append("if PEPS");
            if (lockVehicle) { // if want to lock, arm unlock
                rearmStringBuilder.append(" and want to LOCK, then arm unlock");
                rearmUnlock.set(true);
                if (isLightCaptorEnabled) {
                    makeNoise(ToneGenerator.TONE_CDMA_REORDER, 200);
                }
            } else { // if want to unlock, arm lock
                rearmStringBuilder.append(" and want to UNLOCK, then arm lock");
                rearmLock.set(true);
                if (isLightCaptorEnabled) {
                    makeNoise(ToneGenerator.TONE_CDMA_MED_L, 200);
                }
            }
        } else { // if RKE
            rearmStringBuilder.append("if RKE");
            if (lockVehicle) { // if want to lock, desarm unlock and arm lock
                rearmStringBuilder.append(" and want to LOCK, then desarm unlock and arm lock");
                rearmLock.set(true);
                rearmUnlock.set(false);
            } else { // if want to unlock, desarm lock and arm unlock
                rearmStringBuilder.append(" and want to UNLOCK, then desarm lock and arm unlock");
                rearmUnlock.set(true);
                rearmLock.set(false);
            }
        }
        if(lastLockCommand != mProtocolManager.isLockedToSend()) {
            //Initialize timeout flag which is cleared in the runnable launched in the next instruction
            isLockStatusChangedTimerExpired.set(false);
            //Launch timeout
            mLockStatusChangedHandler.postDelayed(mManageIsLockStatusChangedPeriodicTimer, LOCK_STATUS_CHANGED_TIMEOUT);
            Log.e("NIH rearm", rearmStringBuilder.toString() + " and isAbortRunning = " + isAbortRunning + " and (LockTrx,LockSend) = (" + mProtocolManager.isLockedFromTrx() + "," + mProtocolManager.isLockedToSend() + ")");
            if (mProtocolManager.isLockedFromTrx() != mProtocolManager.isLockedToSend()) {
                if (isAbortRunning) {
                    Log.e("NIH rearm", "isAbortRunning canceled");
                    mHandlerTimeOut.removeCallbacks(abortCommandRunner);
                    mHandlerTimeOut.removeCallbacksAndMessages(null);
                    isAbortRunning = false;
                } else {
                    Log.e("NIH rearm", "isAbortRunning just launched");
                    mHandlerTimeOut.postDelayed(abortCommandRunner, 5000);
                    isAbortRunning = true;
                }
            }
        }
    }

    public boolean isFullyConnected() {
        if (mBluetoothManager != null) {
            return mBluetoothManager.isFullyConnected();
        } else {
            return false;
        }
    }

    public void closeApp() {
        mBluetoothManager.suspendLeScan();
        mBluetoothManager.disconnect();
        // increase the file number use for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        isLoggable = false;
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }
}
