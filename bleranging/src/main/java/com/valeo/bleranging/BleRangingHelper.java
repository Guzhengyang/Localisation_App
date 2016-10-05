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
import android.util.SparseIntArray;

import com.valeo.bleranging.bluetooth.BluetoothLeService;
import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.bluetooth.BluetoothManagementListener;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.ScanResponse;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 19/07/2016
 */
public class BleRangingHelper implements SensorEventListener {
    public final static int WELCOME_AREA = 1;
    public final static int LOCK_AREA = 2;
    public final static int UNLOCK_LEFT_AREA = 3;
    public final static int UNLOCK_RIGHT_AREA = 4;
    public final static int UNLOCK_BACK_AREA = 5;
    public final static int START_AREA = 6;
    public final static int UNLOCK_FRONT_LEFT_AREA = 7;
    public final static int UNLOCK_REAR_LEFT_AREA = 8;
    public final static int UNLOCK_FRONT_RIGHT_AREA = 9;
    public final static int UNLOCK_REAR_RIGHT_AREA = 10;
    public final static int THATCHAM_AREA = 11;
    private final static String BLE_ADDRESS_37 = "D4:F5:13:56:7A:12";
    private final static String BLE_ADDRESS_38 = "D4:F5:13:56:37:32";
    private final static String BLE_ADDRESS_39 = "D4:F5:13:56:39:E7";
    private final static int LOCK_STATUS_CHANGED_TIMEOUT = 3000;
    private final static int PREDICTION_MAX = 16;
    private final Context mContext;
    private final BluetoothManagement mBluetoothManager;
    private final LinkedList<Integer> predictionHistoric;
    private final SparseIntArray mostCommon;
    private final byte[] lastPacketIdNumber = new byte[2];
    private final float R[] = new float[9];
    private final float I[] = new float[9];
    private final AtomicBoolean isLockStatusChangedTimerExpired = new AtomicBoolean(true);
    /**
     * Create a handler to detect if the vehicle can do a unlock
     */
    private final Runnable mManageIsLockStatusChangedPeriodicTimer = new Runnable() {
        @Override
        public void run() {
            isLockStatusChangedTimerExpired.set(true);
        }
    };
    private final AtomicBoolean thatchamIsChanging = new AtomicBoolean(false);
    private final Runnable mHasThatchamChanged = new Runnable() {
        @Override
        public void run() {
            thatchamIsChanging.set(false);
        }
    };
    private final AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    private final AtomicBoolean rearmLock = new AtomicBoolean(true);
    private final AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    private final AtomicBoolean isRKE = new AtomicBoolean(false);
    private final AtomicBoolean lastThatcham = new AtomicBoolean(false);
    private final Handler mMainHandler;
    private final Handler mLockStatusChangedHandler;
    private final Handler mHandlerTimeOut;
    private final Handler mHandlerThatchamTimeOut;
    private final Handler mIsLaidTimeOutHandler;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final ArrayList<Double> lAccHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getLinAccSize());
    private final float orientation[] = new float[3];
    private boolean smartphoneIsInPocket = false;
    private boolean smartphoneIsLaidDownLAcc = false;
    private final Runnable isLaidRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsLaidDownLAcc = true; // smartphone is staying still
            mIsLaidTimeOutHandler.removeCallbacks(this);
        }
    };
    private Integer rangingPredictionInt = -1;
    private boolean isLockStrategyValid = false;
    private List<Integer> isUnlockStrategyValid;
    private boolean isStartStrategyValid = false;
    private boolean isWelcomeStrategyValid = false;
    private boolean checkNewPacketOnlyOneLaunch = true;
    private byte[] bytesToSend;
    private final Runnable sendPacketRunner = new Runnable() {
        @Override
        public void run() {
            Log.d("NIH", "getPacketOnePayload then sendPackets");
            bytesToSend = mProtocolManager.getPacketOnePayload(isRKE.get());
            mBluetoothManager.sendPackets(new byte[][]{bytesToSend});
            if (isRKE.get()) {
                setIsRKE(false);
            }
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, 200);
            }
        }
    };
    private byte[] bytesReceived;
    private final Runnable checkNewPacketsRunner = new Runnable() {
        @Override
        public void run() {
            Log.d("NIH", "checkNewPacketsRunner " + lastPacketIdNumber[0] + " " + (bytesReceived[0] + " " + lastPacketIdNumber[1] + " " + bytesReceived[1]));
            if((lastPacketIdNumber[0] == bytesReceived[0]) && (lastPacketIdNumber[1] == bytesReceived[1])) {
                Log.e("checkNewPacketsRunner", "disconnect()");
                mBluetoothManager.disconnect();
                bleRangingListener.updateBLEStatus();
            } else {
                lastPacketIdNumber[0] = bytesReceived[0];
                lastPacketIdNumber[1] = bytesReceived[1];
            }
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, 10000);
            }
        }
    };
    private String lastConnectedCarType = "";
    private int totalAverage;
    private boolean newLockStatus;
    private boolean lastCommandFromTrx;
    private boolean isAbortRunning = false;
    private final Runnable abortCommandRunner = new Runnable() {
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
    private boolean isFirstConnection = true;
    private boolean isTryingToConnect = false;
    private final Runnable mManageIsTryingToConnectTimer = new Runnable() {
        @Override
        public void run() {
            isTryingToConnect = false;
        }
    };
    private Antenna.BLEChannel bleChannel = Antenna.BLEChannel.BLE_CHANNEL_37;
    private ConnectedCar connectedCar;
    private final Runnable checkAntennaRunner = new Runnable() {
        @Override
        public void run() {
            if (isFullyConnected()) {
                Log.w(" rssiHistorics", "************************************** CHECK ANTENNAS ************************************************");
                connectedCar.compareCheckerAndSetAntennaActive();
            }
            mMainHandler.postDelayed(this, 2500);
        }
    };
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
    private double deltaLinAcc = 0;
    private final Runnable printRunner = new Runnable() {
        @Override
        public void run() {
            Log.w(" rssiHistorics", "************************************** IHM LOOP START *************************************************");
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder = connectedCar.createHeaderDebugData(spannableStringBuilder,
                    bytesToSend, bytesReceived, mBluetoothManager.isFullyConnected());
            totalAverage = connectedCar.getAllTrxAverage(Antenna.AVERAGE_DEFAULT);
            tryStrategies(newLockStatus);
            spannableStringBuilder = connectedCar.createFirstFooterDebugData(spannableStringBuilder);
            spannableStringBuilder = connectedCar.createSecondFooterDebugData(spannableStringBuilder,
                    smartphoneIsInPocket, smartphoneIsLaidDownLAcc, totalAverage, rearmLock.get(), rearmUnlock.get());
            spannableStringBuilder = connectedCar.createThirdFooterDebugData(spannableStringBuilder,
                    bleChannel, deltaLinAcc, smartphoneIsLaidDownLAcc);
            updateCarLocalization();
            bleRangingListener.printDebugInfo(spannableStringBuilder);
            Log.w(" rssiHistorics", "************************************** IHM LOOP END *************************************************");
            mMainHandler.postDelayed(this, 400);
        }
    };
    private boolean isLaidRunnableAlreadyLaunched = false;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private boolean isLoggable = true;
    private final Runnable logRunner = new Runnable() {
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
                        smartphoneIsInPocket, smartphoneIsLaidDownLAcc, isLockStatusChangedTimerExpired.get(),
                        rearmLock.get(), rearmUnlock.get(), rearmWelcome.get(), newLockStatus, welcomeByte,
                        lockByte, startByte, leftAreaByte, rightAreaByte, backAreaByte,
                        walkAwayByte, steadyByte, approachByte, leftTurnByte,
                        fullTurnByte, rightTurnByte, recordByte, rangingPredictionInt,
                        mProtocolManager.isLockedFromTrx(), mProtocolManager.isLockedToSend(), mProtocolManager.isStartRequested());
            }
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, 105);
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
                if (oldLockStatus != newLockStatus) {
//                    connectedCar.resetWithHysteresis(newLockStatus, isUnlockStrategyValid); //TODO concurrentModification
                    bleRangingListener.updateCarDoorStatus(newLockStatus);
                }
                mProtocolManager.setIsLockedFromTrx(newLockStatus);
                if (lastCommandFromTrx != mProtocolManager.isLockedFromTrx()) {
                    lastCommandFromTrx = mProtocolManager.isLockedFromTrx();
                    mProtocolManager.setIsLockedToSend(lastCommandFromTrx);
                    manageRearms(lastCommandFromTrx);
                }
                if (lastThatcham.get() != mProtocolManager.isThatcham()) {
                    lastThatcham.set(mProtocolManager.isThatcham());
                    manageRearms2(lastThatcham.get());
                }
                if (checkNewPacketOnlyOneLaunch) {
                    checkNewPacketOnlyOneLaunch = false;
                    mMainHandler.postDelayed(checkNewPacketsRunner, 1000);
                }
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_SUBSCRIBED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_CHARACTERISTIC_SUBSCRIBED");
                mMainHandler.post(sendPacketRunner); // send works only after subscribed
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.resumeLeScan();
                if (isFirstConnection && isFullyConnected()) {
                    isFirstConnection = false;
                    runFirstConnection(newLockStatus);
                    mHandlerTimeOut.removeCallbacks(mManageIsTryingToConnectTimer);
                    mHandlerTimeOut.removeCallbacks(null);
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_SERVICES_DISCONNECTED");
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartConnection(false);
                    }
                }, 1000);
            } else if (BluetoothLeService.ACTION_GATT_CONNECTION_LOSS.equals(action)) {
                Log.w("NIH", "ACTION_GATT_CONNECTION_LOSS");
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restartConnection(false);
                    }
                }, 1000);
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
        this.mostCommon = new SparseIntArray();
        this.mProtocolManager = new InblueProtocolManager();
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mLockStatusChangedHandler = new Handler();
        this.mHandlerTimeOut = new Handler();
        this.mHandlerThatchamTimeOut = new Handler();
        this.mIsLaidTimeOutHandler = new Handler();
        mBluetoothManager.addBluetoothManagementListener(new BluetoothManagementListener() {
            private final ExecutorService executorService = Executors.newFixedThreadPool(4);

            @Override
            public void onPassiveEntryTry(final BluetoothDevice device, final int rssi, final ScanResponse scanResponse, final byte[] advertisedData) {
                bleChannel = getCurrentChannel(device, bleChannel);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        doPassiveEntry(device, rssi, scanResponse, advertisedData);
                    }
                });
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
        mMainHandler.post(printRunner);
    }

    public void connectToPC() {
        mBluetoothManager.connectToPC("5C:E0:C5:34:4D:32");
//        mBluetoothManager.connectToPC("7C:7A:91:80:86:02");
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    private void restartConnection(boolean createConnectedCar) {
        Log.d("NIH", "restartConnection");
        mBluetoothManager.suspendLeScan();
        mBluetoothManager.disconnect();
        bleRangingListener.updateBLEStatus();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(checkAntennaRunner);
            mMainHandler.removeCallbacks(logRunner);
            mMainHandler.removeCallbacks(sendPacketRunner);
            mMainHandler.removeCallbacks(checkNewPacketsRunner);
        }
        mProtocolManager.restartPacketOneCounter();
        rearmWelcome.set(true);
        isFirstConnection = true;
        checkNewPacketOnlyOneLaunch = true;
        lastPacketIdNumber[0] = 0;
        lastPacketIdNumber[1] = 0;
        makeNoise(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 100);
        if (createConnectedCar) {
            createConnectedCar();
        }
        // wait to close every connection before creating them again
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothManager.connect(mTrxUpdateReceiver);
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
                if (device.getAddress().equals(SdkPreferencesHelper.getInstance().getTrxAddressConnectable())) {
                    Log.w(" rssiHistorics", "CONNECTABLE " + device.getAddress());
                    if (!isTryingToConnect) {
                        Log.w(" rssiHistorics", "************************************** isTryingToConnect ************************************************");
                        isTryingToConnect = true;
                        mBluetoothManager.suspendLeScan();
                        newLockStatus = (scanResponse.vehicleState & 0x01) != 0; // get lock status for initialization later
                        mHandlerTimeOut.postDelayed(mManageIsTryingToConnectTimer, 3000);
                        mBluetoothManager.connect(mTrxUpdateReceiver);
                    } else {
                        Log.w(" rssiHistorics", "already trying to connect");
                    }
                } else {
                    Log.w(" rssiHistorics", "BEACON " + device.getAddress());
                }
            } else if (isFullyConnected()) {
                int trxNumber = connectedCar.getTrxNumber(device.getAddress());
                connectedCar.saveRssi(trxNumber, scanResponse.antennaId, rssi, bleChannel, smartphoneIsLaidDownLAcc);
                Log.d(" rssiHistoric", "BLE_ADDRESS=" + device.getAddress()
                        + " " + connectedCar.getRssiAverage(trxNumber, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)
                        + " " + connectedCar.getRssiAverage(trxNumber, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                if (trxNumber == -1 && advertisedData != null && advertisedData.length > 0) {
                    Log.d(" rssiHistoric", "BLE_ADDRESS_LOGGER=" + TextUtils.printBleBytes(advertisedData));
                    getAdvertisedBytes(advertisedData);
                }
                connectedCar.prepareRanging(smartphoneIsInPocket);
                if (predictionHistoric.size() == PREDICTION_MAX) {
                    int removeEntry = predictionHistoric.get(0);
                    Integer removeEntryValue = mostCommon.get(removeEntry);
                    mostCommon.put(removeEntry, removeEntryValue - 1);
                    predictionHistoric.remove(0);
                }
                int prediction = connectedCar.predict2int();
                predictionHistoric.add(prediction);
                Integer val = mostCommon.get(prediction);
                mostCommon.put(prediction, val + 1);
            }
        }
    }

    private synchronized Integer mostCommon(final SparseIntArray map) {
        Integer max = null;
        int index = 0;
        for (int i = 0; i < map.size(); i++) {
            if (max == null || map.get(i) > max) {
                max = map.get(i);
                index = i;
            }
        }
        return max == null ? -1 : index;
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
        try {
            final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, (int) currentVolumeOnHundred);
            toneG.startTone(noiseSelected, duration);
            if (mMainHandler != null) {
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toneG.release();
                    }
                }, duration);
            }
        } catch (RuntimeException e) {
            // do nothing
        }
    }

    private void setIsThatcham() {
        String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        if (!isLockStrategyValid && isUnlockStrategyValid != null
                && isUnlockStrategyValid.size() >= SdkPreferencesHelper.getInstance().getUnlockValidNb(connectedCarType)) {
            launchThatchamValidityTimeOut();
        } else if (isUnlockStrategyValid == null || isUnlockStrategyValid.size() <
                SdkPreferencesHelper.getInstance().getUnlockValidNb(connectedCarType)) {
            if (!thatchamIsChanging.get()) { // if thatcham is not changing
                mProtocolManager.setThatcham(false);
            }
        }
    }

    /**
     * Try all strategy based on rssi values
     * @param newLockStatus the lock status of the vehicle
     */
    private void tryStrategies(boolean newLockStatus) {
        if (isFullyConnected()) {
            boolean isStartAllowed = false;
            String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
            isStartStrategyValid = connectedCar.startStrategy(smartphoneIsInPocket);
            isLockStrategyValid = connectedCar.lockStrategy(smartphoneIsInPocket);
            isUnlockStrategyValid = connectedCar.unlockStrategy(smartphoneIsInPocket);
            isWelcomeStrategyValid = connectedCar.welcomeStrategy(totalAverage, newLockStatus, smartphoneIsInPocket);
            setIsThatcham();
            rangingPredictionInt = mostCommon(mostCommon);
            if (rearmWelcome.get() && isWelcomeStrategyValid) {
                rearmWelcome.set(false);
                //TODO Welcome
            } else if (isLockStatusChangedTimerExpired.get() && rearmLock.get() && isLockStrategyValid
                    && (isUnlockStrategyValid == null || isUnlockStrategyValid.size() <
                    SdkPreferencesHelper.getInstance().getUnlockValidNb(connectedCarType))) {
                // DO NOT check if !newLockStatus to let the rearm algorithm in performLockVehicle work
                Log.d(" rssiHistorics", "lock");
                performLockVehicleRequest(true);
            } else if (isLockStatusChangedTimerExpired.get() && rearmUnlock.get() && !isLockStrategyValid
                    && isUnlockStrategyValid != null && isUnlockStrategyValid.size() >= SdkPreferencesHelper.getInstance().getUnlockValidNb(connectedCarType)) {
                // DO NOT check if newLockStatus to let the rearm algorithm in performLockVehicle work
                Log.d(" rssiHistorics", "unlock");
                performLockVehicleRequest(false);
            }
            if (isStartStrategyValid) {
                isStartAllowed = true;
                launchThatchamValidityTimeOut();
                //Perform the connection
                if (SdkPreferencesHelper.getInstance().isLightCaptorEnabled()) {
                    makeNoise(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350);
                }
            }
            mProtocolManager.setIsStartRequested(isStartAllowed);
        }
    }

    private void launchThatchamValidityTimeOut() {
        mProtocolManager.setThatcham(true);
        if (thatchamIsChanging.get()) {
            mHandlerThatchamTimeOut.removeCallbacks(mHasThatchamChanged);
            mHandlerThatchamTimeOut.removeCallbacks(null);
        } else {
            thatchamIsChanging.set(true);
        }
        mHandlerThatchamTimeOut.postDelayed(mHasThatchamChanged,
                (long) (SdkPreferencesHelper.getInstance().getThatchamTimeout() * 1000));
    }

    /**
     * Update the mini map with our location around the car
     */
    private void updateCarLocalization() {
        //THATCHAM
        if (mProtocolManager.isThatcham()) {
            bleRangingListener.lightUpArea(THATCHAM_AREA);
        } else {
            bleRangingListener.darkenArea(THATCHAM_AREA);
        }
        //START
        if(isStartStrategyValid) {
            bleRangingListener.lightUpArea(START_AREA);
        } else {
            bleRangingListener.darkenArea(START_AREA);
        }
        // WELCOME
        if (rearmWelcome.get() && isWelcomeStrategyValid) {
            bleRangingListener.lightUpArea(WELCOME_AREA);
        } else {
            bleRangingListener.darkenArea(WELCOME_AREA);
        }
        if (!isLockStrategyValid && isUnlockStrategyValid != null) {
            //UNLOCK
            bleRangingListener.darkenArea(LOCK_AREA);
            bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
            bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
            bleRangingListener.darkenArea(UNLOCK_FRONT_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_REAR_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_FRONT_RIGHT_AREA);
            bleRangingListener.darkenArea(UNLOCK_REAR_RIGHT_AREA);
            for (Integer integer : isUnlockStrategyValid) {
                switch (integer) {
                    case ConnectedCar.NUMBER_TRX_LEFT:
                        bleRangingListener.lightUpArea(UNLOCK_LEFT_AREA);
                        break;
                    case ConnectedCar.NUMBER_TRX_RIGHT:
                        bleRangingListener.lightUpArea(UNLOCK_RIGHT_AREA);
                        break;
                    case ConnectedCar.NUMBER_TRX_BACK:
                        bleRangingListener.lightUpArea(UNLOCK_BACK_AREA);
                        break;
                    case ConnectedCar.NUMBER_TRX_FRONT_LEFT:
                        bleRangingListener.lightUpArea(UNLOCK_FRONT_LEFT_AREA);
                        break;
                    case ConnectedCar.NUMBER_TRX_REAR_LEFT:
                        bleRangingListener.lightUpArea(UNLOCK_REAR_LEFT_AREA);
                        break;
                    case ConnectedCar.NUMBER_TRX_FRONT_RIGHT:
                        bleRangingListener.lightUpArea(UNLOCK_FRONT_RIGHT_AREA);
                        break;
                    case ConnectedCar.NUMBER_TRX_REAR_RIGHT:
                        bleRangingListener.lightUpArea(UNLOCK_REAR_RIGHT_AREA);
                        break;
                    default:
                        bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
                        bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
                        bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
                        bleRangingListener.darkenArea(UNLOCK_FRONT_LEFT_AREA);
                        bleRangingListener.darkenArea(UNLOCK_REAR_LEFT_AREA);
                        bleRangingListener.darkenArea(UNLOCK_FRONT_RIGHT_AREA);
                        bleRangingListener.darkenArea(UNLOCK_REAR_RIGHT_AREA);
                        break;
                }
            }
        } else if (isLockStrategyValid && isUnlockStrategyValid == null) {
            // LOCK
            bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
            bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
            bleRangingListener.darkenArea(UNLOCK_FRONT_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_REAR_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_FRONT_RIGHT_AREA);
            bleRangingListener.darkenArea(UNLOCK_REAR_RIGHT_AREA);
            bleRangingListener.lightUpArea(LOCK_AREA);
        }
    }

    /**
     * Initialize Trx and antenna then launch IHM looper and antenna active check loop
     * @param newLockStatus the lock status
     */
    private void runFirstConnection(final boolean newLockStatus) {
        Log.w(" rssiHistorics", "************************************** runFirstConnection ************************************************");
        bleRangingListener.updateCarDoorStatus(newLockStatus);
        mProtocolManager.setIsLockedToSend(newLockStatus);
        lastCommandFromTrx = newLockStatus;
        if (connectedCar != null) {
            connectedCar.initializeTrx(newLockStatus);
        }
        if (mMainHandler != null) {
            mMainHandler.post(checkAntennaRunner);
            mMainHandler.post(logRunner);
        }
    }

    public void initializeConnectedCar() {
        if (lastConnectedCarType.equals("")) {
            // on first run, create a new car
            createConnectedCar();
        } else if (!lastConnectedCarType.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getConnectedCarType())) {
            // if car type has changed,
            if (isFullyConnected()) {
                // if connected, stop connection, create a new car, and restart it
                restartConnection(true);
            } else {
                // if not connected, create a new car
                createConnectedCar();
            }
        } else {
            // car type did not changed, but settings did
            connectedCar.resetSettings();
            String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
            mProtocolManager.setCarBase(connectedCarBase);
            bleRangingListener.updateCarDrawable();
        }
    }

    private void createConnectedCar() {
        connectedCar = null;
        lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        connectedCar = ConnectedCarFactory.getConnectedCar(mContext, lastConnectedCarType);
        String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
        mProtocolManager.setCarBase(connectedCarBase);
        bleRangingListener.updateCarDrawable();
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
            if (lAccHistoric.size() == SdkPreferencesHelper.getInstance().getLinAccSize()) {
                lAccHistoric.remove(0);
            }
            double currentLinAcc = getQuadratiqueSum(event.values[0], event.values[1], event.values[2]);
            lAccHistoric.add(currentLinAcc);
            double averageLinAcc = getRollingAverageLAcc(lAccHistoric);
            deltaLinAcc = Math.abs(currentLinAcc - averageLinAcc);
            if (deltaLinAcc < SdkPreferencesHelper.getInstance().getCorrectionLinAcc()) {
                if(!isLaidRunnableAlreadyLaunched) {
                    mIsLaidTimeOutHandler.postDelayed(isLaidRunnable, 3000); // wait before apply stillness
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

    private void manageRearms2(final boolean newThatchamStatus) {
        if (newThatchamStatus) {
            rearmLock.set(true);
        } else {
            rearmUnlock.set(true);
        }
    }

    private void manageRearms(final boolean newVehicleLockStatus) {
        if (mProtocolManager.isThatcham()) {
            if (newVehicleLockStatus) { // if has just lock, rearm lock and desarm unlock
                rearmLock.set(true);
                rearmUnlock.set(false);
            } else { // if has just unlock, rearm all
                rearmLock.set(true);
                rearmUnlock.set(true);
            }
        } else {
            if (newVehicleLockStatus) { // if has just lock, rearm all
                rearmLock.set(true);
                rearmUnlock.set(true);
            } else { // if has just unlock, desarm lock and rearm unlock
                rearmLock.set(false);
                rearmUnlock.set(true);
            }
        }
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
        if(lastLockCommand != mProtocolManager.isLockedToSend()) {
            //Initialize timeout flag which is cleared in the runnable launched in the next instruction
            isLockStatusChangedTimerExpired.set(false);
            //Launch timeout
            mLockStatusChangedHandler.postDelayed(mManageIsLockStatusChangedPeriodicTimer, LOCK_STATUS_CHANGED_TIMEOUT);
            if (mProtocolManager.isLockedFromTrx() != mProtocolManager.isLockedToSend()) {
                if (isAbortRunning) {
                    mHandlerTimeOut.removeCallbacks(abortCommandRunner);
                    mHandlerTimeOut.removeCallbacksAndMessages(null);
                    isAbortRunning = false;
                } else {
                    mHandlerTimeOut.postDelayed(abortCommandRunner, 5000);
                    isAbortRunning = true;
                }
            }
        }
    }

    public boolean isFullyConnected() {
        return mBluetoothManager != null && mBluetoothManager.isFullyConnected();
    }

    public void setIsRKE(boolean isRKE) {
        this.isRKE.set(isRKE);
    }

    public void closeApp() {
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(printRunner);
            mMainHandler.removeCallbacks(null);
        }
        mBluetoothManager.suspendLeScan();
        mBluetoothManager.disconnect();
        bleRangingListener.updateBLEStatus();
        // increase the file number use for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        isLoggable = false;
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }
}
