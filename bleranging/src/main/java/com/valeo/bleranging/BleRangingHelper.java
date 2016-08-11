package com.valeo.bleranging;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.valeo.bleranging.bluetooth.BluetoothLeService;
import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.bluetooth.BluetoothManagementListener;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.ScanResponse;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 19/07/2016.
 */
public class BleRangingHelper implements SensorEventListener {
    public final static int RSSI_LOCK_DEFAULT_VALUE = -120;
    public final static int RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE = -50;
    public final static int RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE = -30;
    public final static int RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE = -70;
    public final static int RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE = -80;
    public final static int WELCOME_AREA = 1;
    public final static int LOCK_AREA = 2;
    public final static int UNLOCK_LEFT_AREA = 3;
    public final static int UNLOCK_RIGHT_AREA = 4;
    public final static int UNLOCK_BACK_AREA = 5;
    public final static int START_AREA = 6;
    public final static String BLE_ADDRESS_37 = "D4:F5:13:56:7A:12";
    public final static String BLE_ADDRESS_38 = "D4:F5:13:56:37:32";
    public final static String BLE_ADDRESS_39 = "D4:F5:13:56:39:E7";
    private static final int START_POSITION_TIMEOUT = 400;
    private static final int WELCOME_POSITION_TIMEOUT = 400;
    private static final int LOCK_STATUS_CHANGED_TIMEOUT = 3000;
    private final Context mContext;
    private final BluetoothManagement mBluetoothManager;
    private final int welcomeThreshold = SdkPreferencesHelper.getInstance().getWelcomeThreshold();
    private final int lockThreshold = SdkPreferencesHelper.getInstance().getLockThreshold();
    private final int unlockThreshold = SdkPreferencesHelper.getInstance().getUnlockThreshold();
    private final int startThreshold = SdkPreferencesHelper.getInstance().getStartThreshold();
    private final int averageDeltaLockThreshold = SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold();
    private final int averageDeltaUnlockThreshold = SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold();
    private final int lockMode = SdkPreferencesHelper.getInstance().getLockMode();
    private final int unlockMode = SdkPreferencesHelper.getInstance().getUnlockMode();
    private final int startMode = SdkPreferencesHelper.getInstance().getStartMode();
    private final float linAccThreshold = SdkPreferencesHelper.getInstance().getCorrectionLinAcc();
    private final int nextToDoorRatioThreshold = SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold();
    private final int nextToBackDoorRatioThresholdMin = SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin();
    private final int nextToBackDoorRatioThresholdMax = SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax();
    private final int nextToDoorThresholdMLorMRMin = SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin();
    private final int nextToDoorThresholdMLorMRMax = SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax();
    private final int rollingAvElement = SdkPreferencesHelper.getInstance().getRollingAvElement();
    private final int startNbElement = SdkPreferencesHelper.getInstance().getStartNbElement();
    private final int lockNbElement = SdkPreferencesHelper.getInstance().getLockNbElement();
    private final int unlockNbElement = SdkPreferencesHelper.getInstance().getUnlockNbElement();
    private final int welcomeNbElement = SdkPreferencesHelper.getInstance().getWelcomeNbElement();
    private final int longNbElement = SdkPreferencesHelper.getInstance().getLongNbElement();
    private final int shortNbElement = SdkPreferencesHelper.getInstance().getShortNbElement();
    private final int linAccSize = SdkPreferencesHelper.getInstance().getLinAccSize();
    private final String trxAddressConnectable = SdkPreferencesHelper.getInstance().getTrxAddressConnectable();
    private final String trxAddressLeft = SdkPreferencesHelper.getInstance().getTrxAddressLeft();
    private final String trxAddressMiddle = SdkPreferencesHelper.getInstance().getTrxAddressMiddle();
    private final String trxAddressRight = SdkPreferencesHelper.getInstance().getTrxAddressRight();
    private final String trxAddressBack = SdkPreferencesHelper.getInstance().getTrxAddressBack();
    public boolean smartphoneIsInPocket = false;
    public boolean smartphoneIsLaidDownLAcc = false;
    private boolean isLockStrategyValid = false;
    private int isUnlockStrategyValid = 0;
    private boolean isStartStrategyValid = false;
    private boolean isWelcomeStrategyValid = false;
    private boolean isLightCaptorEnabled = SdkPreferencesHelper.getInstance().isLightCaptorEnabled();
    private boolean checkNewPacketOnlyOneLaunch = true;
    private byte[] bytesToSend;
    private byte[] bytesReceived;
    private byte[] lastPacketIdNumber = new byte[2];
    private int totalAverage;
    private boolean newLockStatus;
    private boolean isAbortRunning = false;
    private boolean isFirstConnection = true;
    private AtomicBoolean isOnStartPostionTimerExpired = new AtomicBoolean(true);
    /**
     * Create a handler to detect if the vehicle can do a start
     */
    private final Runnable mManageStartPositionPeriodicTimer = new Runnable() {
        @Override
        public void run() {
            isOnStartPostionTimerExpired.set(true);
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
    private AtomicBoolean isOnWelcomePostionTimerExpired = new AtomicBoolean(true);
    /**
     * Create a handler to detect if the vehicle can do a welcome
     */
    private final Runnable mManageWelcomePositionPeriodicTimer = new Runnable() {
        @Override
        public void run() {
            isOnWelcomePostionTimerExpired.set(true);
        }
    };
    private AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    private AtomicBoolean rearmLock = new AtomicBoolean(true);
    private AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    private AtomicBoolean isPassiveEntryAction = new AtomicBoolean(false);
    private Antenna.BLEChannel bleChannel = Antenna.BLEChannel.BLE_CHANNEL_37;
    private Handler mMainHandler;
    private Handler mWelcomeHandler;
    private Handler mLockStatusChangedHandler;
    private Handler mStartPositionHandler;
    private Handler mHandlerTimeOut;
    private Handler mIsLaidTimeOutHandler;
    private Trx trxLeft;
    private Trx trxMiddle;
    private Trx trxRight;
    private Trx trxBack;
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
            if(mBluetoothManager.isFullyConnected() && mMainHandler != null) {
                mMainHandler.postDelayed(this, 1000);
            }
        }
    };
    private Runnable checkAntennaRunner = new Runnable() {
        @Override
        public void run() {
            Log.w(" rssiHistorics", "************************************** CHECK ANTENNAS ************************************************");
            checkAllAntennas();
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
                rearmLock.set(false);
            }
            isAbortRunning = false;
        }
    };
    private Runnable printRunner = new Runnable() {
        @Override
        public void run() {
            Log.w(" rssiHistorics", "************************************** IHM LOOP START *************************************************");
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            createHeaderDebugData(spannableStringBuilder);
            calculateTotalAverage();
            tryStrategies(newLockStatus);
            createFooterDebugData(spannableStringBuilder);
            updateCarLocalization();
            bleRangingListener.printDebugInfo(spannableStringBuilder);
            Log.w(" rssiHistorics", "************************************** IHM LOOP END *************************************************");
            if (mMainHandler != null) {
                mMainHandler.postDelayed(this, 400);
            }
        }
    };
    private Runnable sendPacketRunner = new Runnable() {
        @Override
        public void run() {
            Log.d("NIH", "getPacketOnePayload then sendPackets");
            bytesToSend = mProtocolManager.getPacketOnePayload();
            mBluetoothManager.sendPackets(new byte[][]{bytesToSend});
            if (mBluetoothManager.isFullyConnected() && mMainHandler != null) {
                mMainHandler.postDelayed(this, 200);
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
                    if (!newLockStatus) { // just perform an unlock
                        switch (isUnlockStrategyValid) {
                            case Trx.NUMBER_TRX_LEFT:
                                trxLeft.resetWithHysteresis(RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE);
                                trxRight.resetWithHysteresis(RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                                trxBack.resetWithHysteresis(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                                break;
                            case Trx.NUMBER_TRX_RIGHT:
                                trxLeft.resetWithHysteresis(RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                                trxRight.resetWithHysteresis(RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE);
                                trxBack.resetWithHysteresis(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                                break;
                            case Trx.NUMBER_TRX_BACK:
                                trxLeft.resetWithHysteresis(RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                                trxRight.resetWithHysteresis(RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                                trxBack.resetWithHysteresis(RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE);
                                break;
                            default:
                                trxLeft.resetWithHysteresis(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                                trxRight.resetWithHysteresis(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                                trxBack.resetWithHysteresis(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                                break;
                        }
                        trxMiddle.resetWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
                    } else { // just perform a lock
                        trxLeft.resetWithHysteresis(trxLeft.getTrxRssiAverage(Antenna.AVERAGE_LOCK));
                        trxMiddle.resetWithHysteresis(trxMiddle.getTrxRssiAverage(Antenna.AVERAGE_LOCK));
                        trxRight.resetWithHysteresis(trxRight.getTrxRssiAverage(Antenna.AVERAGE_LOCK));
                        trxBack.resetWithHysteresis(trxBack.getTrxRssiAverage(Antenna.AVERAGE_LOCK));
                    }
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
                restartConnection();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTION_LOSS.equals(action)) {
                Log.w("NIH", "ACTION_GATT_CONNECTION_LOSS");
                if (isFullyConnected()) {
                    Log.d("NIH", "ACTION_GATT_CONNECTION_LOSS disconnect()");
                    mBluetoothManager.disconnect();
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_FAILED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_SERVICES_FAILED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_SERVICES_DISCOVERED");
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("NIH", "TRX ACTION_GATT_CONNECTED");
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

    public BleRangingHelper(Context context, BleRangingListener bleRangingListener) {
        this.mContext = context;
        this.mBluetoothManager = new BluetoothManagement(context);
        this.bleRangingListener = bleRangingListener;
        mProtocolManager = new InblueProtocolManager();
        mStartPositionHandler = new Handler();
        mLockStatusChangedHandler = new Handler();
        mWelcomeHandler = new Handler();
        mHandlerTimeOut = new Handler();
        mIsLaidTimeOutHandler = new Handler();
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
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senLinAcceleration, SensorManager.SENSOR_DELAY_UI);
        mBluetoothManager.resumeLeScan();
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     * @param historicDefaultValuePeriph the peripheral trx default value
     * @param historicDefaultValueCentral the central trx default value
     */
    private void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxLeft = new Trx(Trx.NUMBER_TRX_LEFT, historicDefaultValuePeriph);
        trxMiddle = new Trx(Trx.NUMBER_TRX_MIDDLE, historicDefaultValueCentral);
        trxRight = new Trx(Trx.NUMBER_TRX_RIGHT, historicDefaultValuePeriph);
        trxBack = new Trx(Trx.NUMBER_TRX_BACK, historicDefaultValuePeriph);
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    private void restartConnection() {
        Log.d("NIH", "restartConnection");
        mBluetoothManager.suspendLeScan();
        mBluetoothManager.disconnect();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(checkAntennaRunner);
            mMainHandler.removeCallbacks(printRunner);
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
        mBluetoothManager.connect(mTrxUpdateReceiver);
        mBluetoothManager.resumeLeScan();
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
            Log.d(" rssiHistoric", "BLE_ADDRESS_LOGGER2=" + TextUtils.printBleBytes(advertisedData));
            if (isFirstConnection && !mBluetoothManager.isFullyConnected() && device.getAddress().equals(trxAddressConnectable)) {
                runFirstConnection(scanResponse);
                mBluetoothManager.setConnectableDeviceAddress(trxAddressConnectable);
                mBluetoothManager.connect(mTrxUpdateReceiver);
            } else if (!isFirstConnection && mBluetoothManager.isFullyConnected()) {
                if(device.getAddress().equals(trxAddressLeft)) {
                    trxLeft.getAntenna1().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    trxLeft.getAntenna2().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_LEFT=" + trxAddressLeft + " " + trxLeft.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT) + " " + trxLeft.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressMiddle)) {
                    if (scanResponse.antennaId == Trx.ANTENNA_ID_1) {
                        trxMiddle.getAntenna1().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                        if(!trxMiddle.getAntenna2().isAntennaActive()) {
                            trxMiddle.getAntenna2().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                        }
                    } else if (scanResponse.antennaId == Trx.ANTENNA_ID_2) {
                        trxMiddle.getAntenna2().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                        if(!trxMiddle.getAntenna1().isAntennaActive()) {
                            trxMiddle.getAntenna1().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                        }
                    } else if(scanResponse.antennaId == Trx.ANTENNA_ID_0) {
                        trxMiddle.getAntenna1().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                        trxMiddle.getAntenna2().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    }
                    Log.d(" rssiHistoric", "BLE_ADDRESS_MIDDLE=" + trxAddressMiddle + " " + trxMiddle.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT) + " " + trxMiddle.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressRight)) {
                    trxRight.getAntenna1().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    trxRight.getAntenna2().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_RIGHT=" + trxAddressRight + " " + trxRight.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT) + " " + trxRight.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT));
                } else if (device.getAddress().equals(trxAddressBack)) {
                    trxBack.getAntenna1().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    trxBack.getAntenna2().saveRssi(rssi, bleChannel, smartphoneIsLaidDownLAcc);
                    Log.d(" rssiHistoric", "BLE_ADDRESS_BACK=" + trxAddressBack + " " + trxBack.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT) + " " + trxBack.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT));
                } else {
                    Log.d(" rssiHistoric", "BLE_ADDRESS_LOGGER=" + TextUtils.printBleBytes(advertisedData));
                    getAdvertisedBytes(advertisedData);
                }
            }
        }
    }

    /**
     * Create two bytes with all the bits from the switches
     */
    private void getAdvertisedBytes(byte[] advertisedData) {
        steadyByte = (byte) (advertisedData[3] & (1 << 7));
        walkAwayByte = (byte) (advertisedData[3] & (1 << 6));
        backAreaByte = (byte) (advertisedData[3] & (1 << 5));
        rightAreaByte = (byte) (advertisedData[3] & (1 << 4));
        leftAreaByte = (byte) (advertisedData[3] & (1 << 3));
        startByte = (byte) (advertisedData[3] & (1 << 2));
        lockByte = (byte) (advertisedData[3] & (1 << 1));
        welcomeByte = (byte) (advertisedData[3] & 1);
        recordByte = (byte) (advertisedData[4] & (1 << 7));
        rightTurnByte = (byte) (advertisedData[4] & (1 << 3));
        fullTurnByte = (byte) (advertisedData[4] & (1 << 2));
        leftTurnByte = (byte) (advertisedData[4] & (1 << 1));
        approachByte = (byte) (advertisedData[4] & 1);
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
     * Update all antenna active boolean
     */
    private void checkAllAntennas() {
        trxLeft.compareCheckerAndSetAntennaActive();
        trxMiddle.compareCheckerAndSetAntennaActive();
        trxRight.compareCheckerAndSetAntennaActive();
        trxBack.compareCheckerAndSetAntennaActive();
    }

    /**
     * Create a string of header debug
     * @param spannableStringBuilder the spannable string builder to fill
     */
    private void createHeaderDebugData(SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.append("Scanning on channel: ").append(bleChannel.toString()).append("\n");
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append(TextUtils.colorText(trxLeft.isActive(),
                "      LEFT       ", Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(TextUtils.colorText(trxMiddle.isActive(),
                "   MIDDLE        ", Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(TextUtils.colorText(trxRight.isActive(),
                "   RIGHT     ", Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(TextUtils.colorText(trxBack.isActive(),
                "   BACK\n", Color.WHITE, Color.DKGRAY));
    }

    /**
     * Calculate totalAverage to show it in debug mode
     * @return 0 if all trx offline, total average otherwise
     */
    private int calculateTotalAverage() {
        int numberOfAntenna = 0;
        totalAverage = 0;
        if (trxLeft.isActive()) {
            totalAverage += (trxLeft.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT));
            numberOfAntenna++;
        }
        if (trxMiddle.isActive()) {
            totalAverage += (trxMiddle.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT));
            numberOfAntenna++;
        }
        if (trxRight.isActive()) {
            totalAverage += (trxRight.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT));
            numberOfAntenna++;
        }
        if (trxBack.isActive()) {
            totalAverage += (trxBack.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT));
            numberOfAntenna++;
        }
        if (numberOfAntenna == 0) { // If all trx are down restart the app
//                                Intent i = mContext.getPackageManager()
//                                        .getLaunchIntentForPackage(mContext.getPackageName() );
//                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                int mPendingIntentId = 123456;
//                                PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, i, PendingIntent.FLAG_CANCEL_CURRENT);
//                                AlarmManager mgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//                                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//                                System.exit(0);
            return 0;
        }
        totalAverage /= numberOfAntenna;
        return totalAverage;
    }

    /**
     * Try all strategy based on rssi values
     * @param newLockStatus the lock status of the vehicle
     */
    private void tryStrategies(boolean newLockStatus) {
        if(mBluetoothManager.isFullyConnected()) {
            boolean isStartAllowed = false;
            isLockStrategyValid = TrxUtils.lockStrategy(trxLeft, trxMiddle, trxRight, trxBack, smartphoneIsInPocket);
            isUnlockStrategyValid = TrxUtils.unlockStrategy(trxLeft, trxMiddle, trxRight, trxBack, smartphoneIsInPocket);
            isStartStrategyValid = TrxUtils.startStrategy(newLockStatus, trxLeft, trxMiddle, trxRight, trxBack, smartphoneIsInPocket);
            isWelcomeStrategyValid = TrxUtils.welcomeStrategy(totalAverage, smartphoneIsInPocket);
            if (isOnWelcomePostionTimerExpired.get() && rearmWelcome.get() && newLockStatus && isWelcomeStrategyValid) {
                Log.d(" rssiHistorics", "welcome");
                //Initialize timeout flag which is cleared in the runnable launched in the next instruction
                isOnWelcomePostionTimerExpired.set(false);
                //Launch timeout
                mWelcomeHandler.postDelayed(mManageWelcomePositionPeriodicTimer, WELCOME_POSITION_TIMEOUT);
                if (isLightCaptorEnabled) {
                    makeNoise(ToneGenerator.TONE_CDMA_CONFIRM, 200);
                }
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
            } else if (isOnStartPostionTimerExpired.get() && isStartStrategyValid) {
                Log.d(" rssiHistorics", "start");
                //Initialize timeout flag which is cleared in the runnable launched in the next instruction
                isOnStartPostionTimerExpired.set(false);
                //Launch timeout
                mStartPositionHandler.postDelayed(mManageStartPositionPeriodicTimer, START_POSITION_TIMEOUT);
                isStartAllowed = true;
                //Perform the connection
                if (isLightCaptorEnabled) {
                    makeNoise(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350);
                }
            }
            mProtocolManager.setIsStartRequested(isStartAllowed);
        }
        TrxUtils.appendRssiLogs(trxLeft.getAntenna1().getCurrentOriginalRssi(),
                trxMiddle.getAntenna1().getCurrentOriginalRssi(), trxMiddle.getAntenna2().getCurrentOriginalRssi(),
                trxRight.getAntenna1().getCurrentOriginalRssi(), trxBack.getAntenna1().getCurrentOriginalRssi(),
                smartphoneIsInPocket, smartphoneIsLaidDownLAcc, isPassiveEntryAction.get(),
                rearmLock.get(), rearmUnlock.get(), rearmWelcome.get(), welcomeByte,
                lockByte, startByte, leftAreaByte, rightAreaByte, backAreaByte,
                walkAwayByte, steadyByte, approachByte, leftTurnByte,
                fullTurnByte, rightTurnByte, recordByte,
                mProtocolManager.isLockedFromTrx(), mProtocolManager.isLockedToSend(), mProtocolManager.isStartRequested());
    }

    /**
     * Create a string of footer debug
     * @param spannableStringBuilder the spannable string builder to fill
     */
    private void createFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.append("\n"); // return to line after tryStrategies print if success
        StringBuilder dataStringBuilder = new StringBuilder()
                .append(trxLeft.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("     ").append(trxLeft.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("      ")
                .append(trxMiddle.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("     ").append(trxMiddle.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("      ")
                .append(trxRight.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("     ").append(trxRight.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("      ")
                .append(trxBack.getAntenna1().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("     ").append(trxBack.getAntenna2().getAntennaRssiAverage(Antenna.AVERAGE_DEFAULT)).append("\n");
        dataStringBuilder
                .append(trxLeft.getAntenna1().getCurrentOriginalRssi()).append("     ").append(trxLeft.getAntenna2().getCurrentOriginalRssi()).append("      ")
                .append(trxMiddle.getAntenna1().getCurrentOriginalRssi()).append("     ").append(trxMiddle.getAntenna2().getCurrentOriginalRssi()).append("      ")
                .append(trxRight.getAntenna1().getCurrentOriginalRssi()).append("     ").append(trxRight.getAntenna2().getCurrentOriginalRssi()).append("      ")
                .append(trxBack.getAntenna1().getCurrentOriginalRssi()).append("     ").append(trxBack.getAntenna2().getCurrentOriginalRssi()).append("\n");
        dataStringBuilder
                .append("      ").append(trxLeft.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT)).append("           ")
                .append("      ").append(trxMiddle.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT)).append("           ")
                .append("      ").append(trxRight.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT)).append("           ")
                .append("      ").append(trxBack.getTrxRssiAverage(Antenna.AVERAGE_DEFAULT)).append("\n");
        dataStringBuilder.append("                               ")
                .append("Total :").append(" ").append(totalAverage).append("\n");
        spannableStringBuilder.append(dataStringBuilder.toString());
        // WELCOME
        spannableStringBuilder.append("welcome ");
        StringBuilder welcomeStringBuilder = new StringBuilder().append("rssi > (")
                .append(TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket))
                .append("): ").append(totalAverage).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                totalAverage > TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket),
                welcomeStringBuilder.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_WELCOME, Color.WHITE,
                TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket), ">"));
        // LOCK
        spannableStringBuilder.append("lock").append("  mode : ").append(String.valueOf(lockMode)).append(" ");
        StringBuilder averageLSDeltaLockStringBuilder = new StringBuilder().append(String.valueOf(TrxUtils.getAverageLSDelta(Antenna.AVERAGE_LONG, Antenna.AVERAGE_SHORT, trxLeft, trxMiddle, trxRight, trxBack))).append(" ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getAverageLSDeltaGreaterThanThreshold(trxLeft, trxMiddle, trxRight, trxBack, TrxUtils.getCurrentLockThreshold(averageDeltaLockThreshold, smartphoneIsInPocket)),
                averageLSDeltaLockStringBuilder.toString(), Color.RED, Color.DKGRAY));
        StringBuilder lockStringBuilder = new StringBuilder().append("rssi < (").append(TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.isInLockArea(trxLeft, trxMiddle, trxRight, trxBack, TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket)),
                lockStringBuilder.toString(), Color.RED, Color.DKGRAY));
        StringBuilder rearmLockStringBuilder = new StringBuilder().append("rearm Lock: ").append(rearmLock.get()).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                rearmLock.get(),
                rearmLockStringBuilder.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_LOCK, Color.RED,
                TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket), "<"));
        // UNLOCK
        spannableStringBuilder.append("unlock").append("  mode : ").append(String.valueOf(unlockMode)).append(" ");
        StringBuilder averageLSDeltaUnlockStringBuilder = new StringBuilder().append(String.valueOf(TrxUtils.getAverageLSDelta(Antenna.AVERAGE_LONG, Antenna.AVERAGE_SHORT, trxLeft, trxMiddle, trxRight, trxBack))).append(" ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getAverageLSDeltaLowerThanThreshold(trxLeft, trxMiddle, trxRight, trxBack, TrxUtils.getCurrentUnlockThreshold(averageDeltaUnlockThreshold, smartphoneIsInPocket)),
                averageLSDeltaUnlockStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder unlockStringBuilder = new StringBuilder().append("rssi > (").append(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.isInUnlockArea(trxLeft, trxMiddle, trxRight, trxBack, TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket)),
                unlockStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder rearmUnlockStringBuilder = new StringBuilder().append("rearm Unlock: ").append(rearmUnlock.get()).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                rearmUnlock.get(),
                rearmUnlockStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_UNLOCK, Color.GREEN,
                TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket), ">"));
        StringBuilder ratioLRStringBuilder = new StringBuilder().append("       ratio L/R > (+/-")
                .append(nextToDoorRatioThreshold)
                .append("): ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_UNLOCK, trxLeft, trxRight)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxRight, nextToDoorRatioThreshold)
                        || TrxUtils.getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxRight, -nextToDoorRatioThreshold),
                ratioLRStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder ratioLBStringBuilder = new StringBuilder().append("       ratio LouR - B (")
                .append("< ").append(nextToBackDoorRatioThresholdMin)
                .append("): ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_UNLOCK, trxLeft, trxBack))
                .append("|").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_UNLOCK, trxRight, trxBack)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                trxBack.isActive() && (
                        TrxUtils.getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxBack, nextToBackDoorRatioThresholdMin)
                                || TrxUtils.getRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, trxRight, trxBack, nextToBackDoorRatioThresholdMin)),
                ratioLBStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder ratioRBStringBuilder = new StringBuilder().append("       ratio LouR - B (")
                .append(" > ").append(nextToBackDoorRatioThresholdMax)
                .append("): ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_UNLOCK, trxLeft, trxBack))
                .append("|").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_UNLOCK, trxRight, trxBack)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                trxBack.isActive() && (
                        TrxUtils.getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, trxLeft, trxBack, nextToBackDoorRatioThresholdMax)
                                || TrxUtils.getRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, trxRight, trxBack, nextToBackDoorRatioThresholdMax)),
                ratioRBStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        // START
        spannableStringBuilder.append("start").append("  mode : ").append(String.valueOf(startMode)).append(" ");
        StringBuilder startStringBuilder = new StringBuilder().append("rssi > (").append(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)).append(")\n");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.isInStartArea(trxLeft, trxMiddle, trxRight, trxBack, TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)),
                startStringBuilder.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_START, Color.CYAN,
                TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket), ">"));
        StringBuilder ratioMLMRMaxStringBuilder = new StringBuilder().append("       ratio M/L OR M/R Max > (")
                .append(nextToDoorThresholdMLorMRMax)
                .append("): ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxLeft))
                .append(" | ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxRight)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxLeft) > nextToDoorThresholdMLorMRMax
                        || TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxRight) > nextToDoorThresholdMLorMRMax,
                ratioMLMRMaxStringBuilder.toString(), Color.CYAN, Color.DKGRAY));
        StringBuilder ratioMLMRMinStringBuilder = new StringBuilder().append("       ratio M/L AND M/R Min > (")
                .append(nextToDoorThresholdMLorMRMin)
                .append("): ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxLeft))
                .append(" & ").append(TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxRight)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxLeft) > nextToDoorThresholdMLorMRMin
                        && TrxUtils.getRatioNextToDoor(Antenna.AVERAGE_START, trxMiddle, trxRight) > nextToDoorThresholdMLorMRMin,
                ratioMLMRMinStringBuilder.toString(), Color.CYAN, Color.DKGRAY));
        if(mBluetoothManager.isFullyConnected()) {
            spannableStringBuilder.append("Connected").append("\n")
                    .append("       Send:       ").append(TextUtils.printBleBytes((bytesToSend))).append("\n")
                    .append("       Receive: ").append(TextUtils.printBleBytes(bytesReceived)).append("\n");
        } else {
            SpannableString disconnectedSpanString = new SpannableString("Disconnected\n");
            disconnectedSpanString.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, "Disconnected\n".length(), 0);
            spannableStringBuilder.append(disconnectedSpanString);
        }
        StringBuilder lAccStringBuilder = new StringBuilder().append("Linear Acceleration < (")
                .append(linAccThreshold)
                .append("): ").append(String.format(Locale.FRANCE, "%1$.4f", deltaLinAcc)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                smartphoneIsLaidDownLAcc,
                lAccStringBuilder.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append("offset channel 38 :\n");
        StringBuilder offset38StringBuilder = new StringBuilder()
                .append(trxLeft.getAntenna1().getOffsetBleChannel38()).append("     ").append(trxLeft.getAntenna2().getOffsetBleChannel38()).append("      ")
                .append(trxMiddle.getAntenna1().getOffsetBleChannel38()).append("     ").append(trxMiddle.getAntenna2().getOffsetBleChannel38()).append("      ");
        offset38StringBuilder
                .append(trxRight.getAntenna1().getOffsetBleChannel38()).append("     ").append(trxRight.getAntenna2().getOffsetBleChannel38()).append("      ")
                .append(trxBack.getAntenna1().getOffsetBleChannel38()).append("     ").append(trxBack.getAntenna2().getOffsetBleChannel38()).append("\n");
        spannableStringBuilder.append(offset38StringBuilder.toString());
        spannableStringBuilder.append("-------------------------------------------------------------------------");
    }

    /**
     * Color each antenna average with color if comparaisonSign (> or <) threshold, DK_GRAY otherwise
     * @param mode the average mode to calculate
     * @param color the color to use if the conditions is checked
     * @param threshold the threshold to compare with
     * @param comparaisonSign the comparaison sign
     * @return a colored spannablestringbuilder with all the trx's average
     */
    private SpannableStringBuilder printModedAverage(int mode, int color, int threshold, String comparaisonSign) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(String.valueOf(getNbElement(mode)) + "     ");
        ssb.append(TextUtils.colorAntennaAverage(trxLeft.getAntenna1().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxLeft.getAntenna2().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxMiddle.getAntenna1().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxMiddle.getAntenna2().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxRight.getAntenna1().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxRight.getAntenna2().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxBack.getAntenna1().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append(TextUtils.colorAntennaAverage(trxBack.getAntenna2().getAntennaRssiAverage(mode), color, threshold, comparaisonSign));
        ssb.append("\n");
        return ssb;
    }

    /**
     * Calculate the number of element to use to calculate the rolling average
     * @param mode the average mode
     * @return the number of element to calculate the average
     */
    private int getNbElement(int mode) {
        if(smartphoneIsLaidDownLAcc) {
            return rollingAvElement;
        }
        switch (mode) {
            case Antenna.AVERAGE_DEFAULT:
                return rollingAvElement;
            case Antenna.AVERAGE_START:
                return startNbElement;
            case Antenna.AVERAGE_LOCK:
                return lockNbElement;
            case Antenna.AVERAGE_UNLOCK:
                return unlockNbElement;
            case Antenna.AVERAGE_WELCOME:
                return welcomeNbElement;
            case Antenna.AVERAGE_LONG:
                return longNbElement;
            case Antenna.AVERAGE_SHORT:
                return shortNbElement;
            default:
                return rollingAvElement;
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
        if(!isLockStrategyValid) {
            bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
            bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
            bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
            switch (isUnlockStrategyValid) {
                case Trx.NUMBER_TRX_LEFT:
                    bleRangingListener.lightUpArea(UNLOCK_LEFT_AREA);
                    break;
                case Trx.NUMBER_TRX_RIGHT:
                    bleRangingListener.lightUpArea(UNLOCK_RIGHT_AREA);
                    break;
                case Trx.NUMBER_TRX_BACK:
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
        if(isWelcomeStrategyValid) {
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
        if(isFirstConnection) {
            Log.w(" rssiHistorics", "************************************** runFirstConnection ************************************************");
            newLockStatus = (scanResponse.vehicleState & 0x01)!=0;
            mProtocolManager.setIsLockedToSend(newLockStatus);
            if(newLockStatus) {
                initializeTrx(RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE);
            } else {
                initializeTrx(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
            }
            mMainHandler = new Handler(Looper.getMainLooper());
            mMainHandler.post(checkAntennaRunner);
            mMainHandler.post(printRunner);
            isFirstConnection = false;
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Send the lock / unlock request to the vehicle.
     * @param lockVehicle true to lock the vehicle, false to unlock it.
     */
    public void performLockVehicleRequest(final boolean lockVehicle) {
        if(!mBluetoothManager.isFullyConnected()) {
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
        return mBluetoothManager.isFullyConnected();
    }

    public void closeApp() {
        // increase the file number use for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        if(mStartPositionHandler != null)
            mStartPositionHandler.removeCallbacks(mManageStartPositionPeriodicTimer);
        if(mLockStatusChangedHandler != null)
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        if(mWelcomeHandler != null)
            mWelcomeHandler.removeCallbacks(mManageWelcomePositionPeriodicTimer);
        mBluetoothManager.disconnect();
    }
}
