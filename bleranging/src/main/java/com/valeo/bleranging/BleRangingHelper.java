package com.valeo.bleranging;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.bluetooth.BluetoothManagementListener;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.ScanResponse;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.CallReceiver;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.valeo.bleranging.bluetooth.InblueProtocolManager.MAX_BLE_TRAME_BYTE;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_BACK;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_FRONT_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_FRONT_RIGHT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_MIDDLE;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_REAR_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_REAR_RIGHT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_RIGHT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_TRUNK;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.ALGO_STANDARD;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.MACHINE_LEARNING;

/**
 * Created by l-avaratha on 19/07/2016
 */
public class BleRangingHelper implements SensorEventListener {
    public final static int WELCOME_AREA = 1;
    public final static int LOCK_AREA = 2;
    public final static int UNLOCK_LEFT_AREA = 3;
    public final static int UNLOCK_RIGHT_AREA = 4;
    public final static int UNLOCK_BACK_AREA = 5;
    public final static int START_PASSENGER_AREA = 6;
    public final static int UNLOCK_FRONT_LEFT_AREA = 7;
    public final static int UNLOCK_REAR_LEFT_AREA = 8;
    public final static int UNLOCK_FRONT_RIGHT_AREA = 9;
    public final static int UNLOCK_REAR_RIGHT_AREA = 10;
    public final static int START_TRUNK_AREA = 11;
    public final static int THATCHAM_AREA = 12;
    private final static int LOCK_STATUS_CHANGED_TIMEOUT = 5000;
    private final static int PREDICTION_MAX = 11;
    private final Context mContext;
    private final BluetoothManagement mBluetoothManager;
    private final LinkedList<Integer> predictionHistoric;
    private final byte[] lastPacketIdNumber = new byte[2];
    private final float R[] = new float[9];
    private final float I[] = new float[9];
    private final AtomicBoolean areLockActionsAvailable = new AtomicBoolean(true);
    /**
     * Create a handler to detect if the vehicle can do a unlock
     */
    private final Runnable mManageIsLockStatusChangedPeriodicTimer = new Runnable() {
        @Override
        public void run() {
            areLockActionsAvailable.set(true);
        }
    };
    private final AtomicBoolean thatchamIsChanging = new AtomicBoolean(false);
    private final Runnable mHasThatchamChanged = new Runnable() {
        @Override
        public void run() {
            thatchamIsChanging.set(false);
        }
    };
    private final AtomicBoolean backIsChanging = new AtomicBoolean(false);
    private final Runnable mHasBackChanged = new Runnable() {
        @Override
        public void run() {
            backIsChanging.set(false);
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
    private final Handler mHandlerBackTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Handler mIsLaidTimeOutHandler;
    private final Handler mIsFrozenTimeOutHandler;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final ArrayList<Double> lAccHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getLinAccSize());
    private final float orientation[] = new float[3];
    private final CallReceiver callReceiver = new CallReceiver();
    private final BroadcastReceiver bleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bleRangingListener.askBleOn();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };
    private final Runnable beepRunner = new Runnable() {
        @Override
        public void run() {
            long delayedTime = 500;
            if (SdkPreferencesHelper.getInstance().getUserSpeedEnabled()) {
                makeNoise(ToneGenerator.TONE_CDMA_LOW_SS, 100);
                delayedTime = Math.round(((SdkPreferencesHelper.getInstance().getOneStepSize() / 100) / (SdkPreferencesHelper.getInstance().getWantedSpeed() / 3.6)) * 1000);
            }
            PSALogs.d("beep", "delayedTime " + delayedTime);
            mMainHandler.postDelayed(this, delayedTime);
        }
    };
    private boolean lastThatchamChanged = false;
    private boolean smartphoneIsInPocket = false;
    private boolean smartphoneIsMovingSlowly = false;
    private final Runnable isLaidRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsMovingSlowly = true; // smartphone is staying still
            mIsLaidTimeOutHandler.removeCallbacks(this);
        }
    };
    private boolean lastSmartphoneIsFrozen = false;
    private boolean smartphoneIsFrozen = false;
    private final Runnable isFrozenRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsFrozen = true; // smartphone is staying still
            mIsFrozenTimeOutHandler.removeCallbacks(this);
        }
    };
    private boolean forcedStart = false;
    private boolean blockStart = false;
    private boolean forcedLock = false;
    private boolean blockLock = false;
    private boolean forcedUnlock = false;
    private boolean blockUnlock = false;
    private Integer rangingPredictionInt = -1;
    private List<Integer> isStartStrategyValid;
    private List<Integer> isUnlockStrategyValid;
    private boolean isInLockArea = false;
    private boolean isInUnlockArea = false;
    private boolean isInStartArea = false;
    private boolean isInWelcomeArea = false;
    private boolean checkNewPacketOnlyOneLaunch = true;
    private boolean isRestartAuthorized = true;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private byte[] bytesToSend;
    private byte[] bytesReceived;
    private final Runnable sendPacketRunner = new Runnable() {
        private List<String> getParts(String string, int partitionSize) {
            List<String> parts = new ArrayList<>();
            int len = string.length();
            for (int i = 0; i < len; i += partitionSize) {
                parts.add(string.substring(i, Math.min(len, i + partitionSize)));
            }
            return parts;
        }

        @Override
        public void run() {
//            PSALogs.d("Runner", "START sendPacket Runner");
//            PSALogs.d("Runner", "   Construct Packet: start");
            lock.writeLock().lock();
            bytesToSend = mProtocolManager.getPacketOnePayload(isRKE.get(), isUnlockStrategyValid,
                    isInUnlockArea, isStartStrategyValid, isInStartArea, isInLockArea);
            if (SdkPreferencesHelper.getInstance().getConnectedCarTrameEnabled()
                    && !SdkPreferencesHelper.getInstance().getConnectedCarTrame().isEmpty()) { // Replace by forced trame if enabled
                int index = 3;
                for (String item : getParts(SdkPreferencesHelper.getInstance().getConnectedCarTrame().replaceAll("\\s", ""), 2)) {
                    if (index < MAX_BLE_TRAME_BYTE) {
                        try {
                            bytesToSend[index++] = (byte) Integer.parseInt(item, 16);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            lock.writeLock().unlock();
//            PSALogs.d("Runner", "   Construct Packet: stop");
            lock.readLock().lock();
            mBluetoothManager.sendPackets(bytesToSend, bytesReceived);
            lock.readLock().unlock();
            if (isRKE.get()) {
                setIsRKE(false);
            }
//            PSALogs.d("Runner", "STOP sendPacket Runner");
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, 200);
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
            PSALogs.d("NIH rearm", "abortCommand Runnable");
            PSALogs.d("NIH rearm", "trx: " + mProtocolManager.isLockedFromTrx() + " me: " + mProtocolManager.isLockedToSend());
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
            PSALogs.w("NIH", "************************************** isTryingToConnect FALSE ************************************************");
            isTryingToConnect = false;
        }
    };
    private Antenna.BLEChannel bleChannel = Antenna.BLEChannel.UNKNOWN;
    private ConnectedCar connectedCar;
    private final Runnable checkAntennaRunner = new Runnable() {
        @Override
        public void run() {
            if (isFullyConnected()) {
                PSALogs.w(" rssiHistorics", "************************************** CHECK ANTENNAS ************************************************");
                connectedCar.compareCheckerAndSetAntennaActive(bleChannel, smartphoneIsMovingSlowly);
            }
            mMainHandler.postDelayed(this, 2500);
        }
    };
    private final Runnable updateCarLocalizationRunnable = new Runnable() {
        @Override
        public void run() {
            if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(ALGO_STANDARD)) {
                tryStandardStrategies(newLockStatus);
            } else if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(MACHINE_LEARNING)) {
                tryMachineLearningStrategies();
            }
            updateCarLocalization();
            mMainHandler.postDelayed(this, 400);
        }
    };
    private final Runnable fillPredictionArray = new Runnable() {
        @Override
        public void run() {
            if (connectedCar.prepareRanging()) {
                if (predictionHistoric.size() == PREDICTION_MAX) {
                    if (predictionHistoric.get(0) != null) {
                        predictionHistoric.remove(0);
                    }
                }
                int prediction = connectedCar.predict2int();
                predictionHistoric.add(prediction);
                PSALogs.d("prediction", "Add prediction: " + prediction);
                mMainHandler.postDelayed(this, 100);
            } else {
                mMainHandler.removeCallbacks(this);
            }
        }
    };
    private final Runnable checkNewPacketsRunner = new Runnable() {
        @Override
        public void run() {
            if (bytesReceived != null) {
                lock.readLock().lock();
                PSALogs.d("NIH", "checkNewPacketsRunnable " + lastPacketIdNumber[0] + " " + (bytesReceived[0] + " " + lastPacketIdNumber[1] + " " + bytesReceived[1]));
                if ((lastPacketIdNumber[0] == bytesReceived[0]) && (lastPacketIdNumber[1] == bytesReceived[1])) {
                    lock.readLock().unlock();
                    PSALogs.w("NIH", "LAST_EQUALS_NEW_PACKETS_RECEIVED");
                    PSALogs.i("restartConnection", "received packet have not changed in a second");
                    restartConnection(false);
                    bleRangingListener.updateBLEStatus();
                } else {
                    lastPacketIdNumber[0] = bytesReceived[0];
                    lastPacketIdNumber[1] = bytesReceived[1];
                    lock.readLock().unlock();
                    if (isFullyConnected()) {
                        mMainHandler.postDelayed(this, 1000);
                    }
                }
            } else {
                PSALogs.w("NIH", "PACKETS_RECEIVED_ARE_NULL");
                PSALogs.i("restartConnection", "received packet is null");
                restartConnection(false);
                bleRangingListener.updateBLEStatus();
            }
        }
    };
    private int reconnectionCounter = 0;
    private byte welcomeByte = 0;
    private byte lockByte = 0;
    private byte startByte = 0;
    private byte leftAreaByte = 0;
    private byte rightAreaByte = 0;
    private byte backAreaByte = 0;
    private byte walkAwayByte = 0;
    private byte approachByte = 0;
    private byte leftTurnByte = 0;
    private byte fullTurnByte = 0;
    private byte rightTurnByte = 0;
    private byte recordByte = 0;
    private double deltaLinAcc = 0;
    private final Runnable printRunner = new Runnable() {
        @Override
        public void run() {
            PSALogs.w(" rssiHistorics", "************************************** IHM LOOP START *************************************************");
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            lock.readLock().lock();
            spannableStringBuilder = connectedCar.createHeaderDebugData(spannableStringBuilder,
                    bytesToSend, bytesReceived, mBluetoothManager.isFullyConnected());
            lock.readLock().unlock();
            totalAverage = connectedCar.getAllTrxAverage(Antenna.AVERAGE_DEFAULT);
            spannableStringBuilder = connectedCar.createFirstFooterDebugData(spannableStringBuilder);
            if (rangingPredictionInt != null) {
                spannableStringBuilder.append("rangingPrediction: ").append(String.valueOf(rangingPredictionInt)).append("\n");
            }
            spannableStringBuilder
                    .append("blockStart: ").append(String.valueOf(blockStart)).append(" ")
                    .append("forcedStart: ").append(String.valueOf(forcedStart)).append("\n")
                    .append("blockLock: ").append(String.valueOf(blockLock)).append(" ")
                    .append("forcedLock: ").append(String.valueOf(forcedLock)).append("\n")
                    .append("blockUnlock: ").append(String.valueOf(blockUnlock)).append(" ")
                    .append("forcedUnlock: ").append(String.valueOf(forcedUnlock)).append("\n")
                    .append("frozen: ").append(String.valueOf(smartphoneIsFrozen)).append(" ");
            spannableStringBuilder = connectedCar.createSecondFooterDebugData(spannableStringBuilder,
                    smartphoneIsInPocket, smartphoneIsMovingSlowly, totalAverage, rearmLock.get(), rearmUnlock.get());
            spannableStringBuilder = connectedCar.createThirdFooterDebugData(spannableStringBuilder,
                    bleChannel, deltaLinAcc, smartphoneIsMovingSlowly);
            spannableStringBuilder //TODO Remove after test
                    .append(String.format(Locale.FRANCE, "%1$.03f", orientation[0])).append("\n")
                    .append(String.format(Locale.FRANCE, "%1$.03f", orientation[1])).append("\n")
                    .append(String.format(Locale.FRANCE, "%1$.03f", orientation[2])).append("\n");
            bleRangingListener.printDebugInfo(spannableStringBuilder);
            PSALogs.w(" rssiHistorics", "************************************** IHM LOOP END *************************************************");
            mMainHandler.postDelayed(this, 105);
        }
    };
    private boolean isLaidRunnableAlreadyLaunched = false;
    private boolean isFrozenRunnableAlreadyLaunched = false;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private boolean isLoggable = true;
    private final Runnable logRunner = new Runnable() {
        @Override
        public void run() {
            if (isLoggable) {
                TrxUtils.appendRssiLogs(connectedCar.getCurrentModifiedRssi(NUMBER_TRX_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_TRUNK, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_BACK, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_TRUNK, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_ID_0),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK, Trx.ANTENNA_ID_0),
                        orientation[0], orientation[1], orientation[2],
                        smartphoneIsInPocket, smartphoneIsMovingSlowly, areLockActionsAvailable.get(),
                        blockStart, forcedStart, blockLock, forcedLock, blockUnlock, forcedUnlock,
                        smartphoneIsFrozen,
                        rearmLock.get(), rearmUnlock.get(), rearmWelcome.get(), newLockStatus, welcomeByte,
                        lockByte, startByte, leftAreaByte, rightAreaByte, backAreaByte,
                        walkAwayByte, approachByte, leftTurnByte,
                        fullTurnByte, rightTurnByte, recordByte, rangingPredictionInt,
                        mProtocolManager.isLockedFromTrx(), mProtocolManager.isLockedToSend(),
                        mProtocolManager.isStartRequested(), mProtocolManager.isThatcham());
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
//                PSALogs.d("NIH", "Received (before): " + TextUtils.printBleBytes(bytesReceived));
                lock.writeLock().lock();
                bytesReceived = mBluetoothManager.getBytesReceived();
                lock.writeLock().unlock();
//                PSALogs.d("NIH", "Received (after): " + TextUtils.printBleBytes(bytesReceived));
                boolean oldLockStatus = newLockStatus;
                if (bytesReceived != null) {
                    lock.readLock().lock();
                    newLockStatus = (bytesReceived[5] & 0x01) != 0;
                    lock.readLock().unlock();
                }
                if (oldLockStatus != newLockStatus) {
//                    connectedCar.resetWithHysteresis(newLockStatus, isUnlockStrategyValid); //TODO concurrentModification
                    bleRangingListener.updateCarDoorStatus(newLockStatus);
                }
                mProtocolManager.setIsLockedFromTrx(newLockStatus);
                // if car lock status changed
                if (lastCommandFromTrx != mProtocolManager.isLockedFromTrx()) {
                    lastCommandFromTrx = mProtocolManager.isLockedFromTrx();
                    mProtocolManager.setIsLockedToSend(lastCommandFromTrx);
                    //Initialize timeout flag which is cleared in the runnable launched in the next instruction
                    areLockActionsAvailable.set(false);
                    //Launch timeout
                    mLockStatusChangedHandler.postDelayed(mManageIsLockStatusChangedPeriodicTimer, LOCK_STATUS_CHANGED_TIMEOUT);
                    manageRearms(lastCommandFromTrx);
                }
                // if car thatcham status changed
                if (lastThatcham.get() != mProtocolManager.isThatcham()) {
                    lastThatcham.set(mProtocolManager.isThatcham());
                    if (lastThatcham.get()) { // if in thatcham area, rearm lock
                        rearmLock.set(true);
                        lastThatchamChanged = false;
                    } else { // if not in thatcham area wait for being in lock area to rearm unlock
                        lastThatchamChanged = true; // because when thatcham changed, maybe not in lock area yet
                    }
                }
                if (lastThatchamChanged && isInLockArea) { // when thatcham has changed, and get into lock area
                    if (!mProtocolManager.isLockedFromTrx()) { // if the vehicle is unlocked, lock it
                        new CountDownTimer(600, 90) { // Send safety close command several times in case it got lost
                            public void onTick(long millisUntilFinished) {
                                mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        isRKE.set(true);
                                        performLockVehicleRequest(true);
                                    }
                                }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
                            }

                            public void onFinish() {
//                                Toast.makeText(mContext, "All safety close command are sent !", Toast.LENGTH_SHORT).show();
                            }
                        }.start();
                    }
                    // if not in thatcham area and in lock area, rearm unlock
                    rearmUnlock.set(true);
                    makeNoise(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
                    lastThatchamChanged = false;
                }
                if (checkNewPacketOnlyOneLaunch) {
                    checkNewPacketOnlyOneLaunch = false;
                    mMainHandler.postDelayed(checkNewPacketsRunner, 1000);
                }
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_SUBSCRIBED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_CHARACTERISTIC_SUBSCRIBED");
                mMainHandler.post(sendPacketRunner); // send works only after subscribed
                bleRangingListener.updateBLEStatus();
                if (isFirstConnection && isFullyConnected()) {
                    isFirstConnection = false;
                    runFirstConnection(newLockStatus);
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_SERVICES_DISCONNECTED");
                bleRangingListener.updateBLEStatus();
                PSALogs.i("restartConnection", "after being disconnected");
                reconnectAfterDisconnection();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTION_LOSS.equals(action)) {
                PSALogs.w("NIH", "ACTION_GATT_CONNECTION_LOSS");
                bleRangingListener.updateBLEStatus();
                PSALogs.i("restartConnection", "after connection loss");
                reconnectAfterDisconnection();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_FAILED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_SERVICES_FAILED");
                isRestartAuthorized = true;
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.resumeLeScan();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_SERVICES_DISCOVERED");
                bleRangingListener.updateBLEStatus();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_CONNECTED");
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.resumeLeScan();
            }
        }
    };

    public BleRangingHelper(Context context, BleRangingListener bleRangingListener) {
        this.mContext = context;
        this.mBluetoothManager = new BluetoothManagement(context);
        this.bleRangingListener = bleRangingListener;
        this.predictionHistoric = new LinkedList<>();
        this.mProtocolManager = new InblueProtocolManager();
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mLockStatusChangedHandler = new Handler();
        this.mHandlerTimeOut = new Handler();
        this.mHandlerThatchamTimeOut = new Handler();
        this.mHandlerBackTimeOut = new Handler();
        this.mHandlerCryptoTimeOut = new Handler();
        this.mIsLaidTimeOutHandler = new Handler();
        this.mIsFrozenTimeOutHandler = new Handler();
        mBluetoothManager.addBluetoothManagementListener(new BluetoothManagementListener() {
            private final ExecutorService executorService = Executors.newFixedThreadPool(4);

            @Override
            public void onPassiveEntryTry(final BluetoothDevice device, final int rssi, final ScanResponse scanResponse, final byte[] advertisedData) {
                bleChannel = getCurrentChannel(scanResponse);
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
        mContext.registerReceiver(callReceiver, new IntentFilter());
        mContext.registerReceiver(bleStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mBluetoothManager.resumeLeScan();
        mMainHandler.post(printRunner);
    }

    private void reconnectAfterDisconnection() {
        long restartConnectionDelay;
        isRestartAuthorized = false;
        if (reconnectionCounter < 3) {
            restartConnectionDelay = 1000;
        } else if (reconnectionCounter < 6) {
            restartConnectionDelay = 3000;
        } else {
            isRestartAuthorized = true;
            reconnectionCounter = 0;
            return;
        }
        if (!mBluetoothManager.isFullyConnected()
                && !mBluetoothManager.isConnecting()) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    restartConnection(false);
                }
            }, restartConnectionDelay);
        }
        reconnectionCounter++;
    }

    public void connectToPC() {
        mBluetoothManager.connectToPC(SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC());
    }

    public void connectToRemoteControl() {
        mBluetoothManager.connectToRemoteControl(SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl());
    }

    public void connect() {
        if (mMainHandler != null) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isRestartAuthorized = false;
                            mBluetoothManager.connect(mTrxUpdateReceiver);
                        }
                    }, (long) (SdkPreferencesHelper.getInstance().getCryptoPreAuthTimeout() * 1000));
                }
            }, 250);
        }
    }

    public void relaunchScan() {
        mBluetoothManager.resumeLeScan();
    }

    public void toggleBluetooth(boolean enable) {
        mBluetoothManager.setBluetooth(enable);
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothManager.isBluetoothEnabled();
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    private void restartConnection(boolean createConnectedCar) {
        if (!mBluetoothManager.isConnecting()) {
            PSALogs.d("NIH", "restartConnection");
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
            resetByteArray(bytesToSend);
            resetByteArray(bytesReceived);
            makeNoise(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 100);
            if (createConnectedCar) {
                createConnectedCar();
            }
            mBluetoothManager.resumeLeScan();
        }
    }

    private void resetByteArray(byte[] byteArray) {
        if (byteArray != null) {
            lock.writeLock().lock();
            for (int index = 0; index < byteArray.length; index++) {
                byteArray[index] = (byte) 0xFF;
            }
            lock.writeLock().unlock();
        }
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

    private void rearmForcedBlock(boolean isInLockArea, boolean isInUnlockArea, boolean isInStartArea) {
        if (smartphoneIsFrozen) {
            if (isInLockArea) { // smartphone in lock area and frozen, then force lock
                forcedLock = true;
                blockLock = false;
                forcedUnlock = false;
                blockUnlock = true;
                forcedStart = false;
                blockStart = true;
            } else { // smartphone not in lock area and frozen, then block lock
                forcedLock = false;
                blockLock = true;
                if (isInStartArea) { // smartphone in start area and frozen, then force start
                    forcedStart = true;
                    blockStart = false;
                    forcedUnlock = false;
                    blockUnlock = true;
                } else { // smartphone not in start area and frozen, then block start
                    forcedStart = false;
                    blockStart = true;
                    if (isInUnlockArea) { // smartphone in unlock area and frozen, then force unlock
                        forcedUnlock = true;
                        blockUnlock = false;
                    } else { // smartphone not in unlock area and frozen, then block unlock
                        forcedUnlock = false;
                        blockUnlock = true;
                    }
                    if (blockUnlock && blockStart && blockLock) {
                        PSALogs.d("block", "all");
                    }
                }
            }
        } else { // smartphone not frozen, then deactivate force start and block start
            forcedStart = false;
            blockStart = false;
            forcedLock = false;
            blockLock = false;
            forcedUnlock = false;
            blockUnlock = false;
        }
    }

    /**
     * Get the current advertising channel
     *
     * @param scanResponse the peripheral scan response
     * @return the received ble channel
     */
    private Antenna.BLEChannel getCurrentChannel(ScanResponse scanResponse) {
        if ((scanResponse.vehicleState & 0x40) == 0x40) {
            return Antenna.BLEChannel.BLE_CHANNEL_37;
        } else if ((scanResponse.vehicleState & 0x80) == 0x80) {
            return Antenna.BLEChannel.BLE_CHANNEL_38;
        } else if ((scanResponse.vehicleState & 0xC0) == 0xC0) {
            return Antenna.BLEChannel.BLE_CHANNEL_39;
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
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
                    PSALogs.w("NIH", "CONNECTABLE " + device.getAddress());
                    if (!isTryingToConnect && !mBluetoothManager.isFullyConnected() && !mBluetoothManager.isConnecting()) {
                        isTryingToConnect = true;
                        mBluetoothManager.suspendLeScan();
                        mHandlerTimeOut.postDelayed(mManageIsTryingToConnectTimer, 3000);
                        PSALogs.w("NIH", "************************************** isTryingToConnect TRUE ************************************************");
                        bleRangingListener.showSnackBar("CONNECTABLE " + device.getAddress());
                        newLockStatus = (scanResponse.vehicleState & 0x01) != 0; // get lock status for initialization later
                        connect();
                    } else {
                        PSALogs.w("NIH", "already trying to connect " + isTryingToConnect + " " + mBluetoothManager.isFullyConnected() + " " + mBluetoothManager.isConnecting());
                        bleRangingListener.showSnackBar("Already trying to connect to " + device.getAddress());
                    }
                } else {
                    PSALogs.w("NIH", "BEACON " + device.getAddress());
                }
            } else if (isFullyConnected()) {
                int trxNumber = connectedCar.getTrxNumber(device.getAddress());
                if (trxNumber != -1) {
                    connectedCar.saveRssi(trxNumber, scanResponse.antennaId, rssi, bleChannel, smartphoneIsMovingSlowly);
//                    PSALogs.d("NIH", "BLE_ADDRESS=" + device.getAddress()
//                            + " " + connectedCar.getRssiAverage(trxNumber, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)
//                            + " " + connectedCar.getRssiAverage(trxNumber, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else {
                    if (SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress())) {
                        PSALogs.i("restartConnection", "connectable is advertising again");
                        restartConnection(false);
                        return;
                    } else if ((SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC().equals(device.getAddress()))
                            && (!mBluetoothManager.isFullyConnected2() && !mBluetoothManager.isConnecting2())) { // connect to pc
                        bleRangingListener.showSnackBar("connect to PC " + device.getAddress());
                        PSALogs.i("NIH_PC", "connect to address PC : " + device.getAddress());
//                        SdkPreferencesHelper.getInstance().setTrxAddressConnectablePC(device.getAddress());
                        connectToPC();
                        return;
                    } else if (!SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress()) &&
                            (!SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl().equalsIgnoreCase(device.getAddress())
                                    || (!mBluetoothManager.isFullyConnected3() && !mBluetoothManager.isConnecting3()))) { // connect to remote control
                        bleRangingListener.showSnackBar("connect to REMOTE " + device.getAddress());
                        PSALogs.i("NIH_REMOTE_CONTROL", "connectable address REMOTE CONTROL changed from : "
                                + SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl() + " to : " + device.getAddress());
                        PSALogs.i("NIH_REMOTE_CONTROL", "compare " + device.getAddress() + " and " + SdkPreferencesHelper.getInstance().getTrxAddressConnectable());
                        if (!SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress())) {
                            SdkPreferencesHelper.getInstance().setTrxAddressConnectableRemoteControl(device.getAddress());
                            connectToRemoteControl();
                            return;
                        }
                        return;
                    }
                    if (advertisedData != null && advertisedData.length > 0) {
                        PSALogs.d("NIH", "BLE_ADDRESS_LOGGER= " + TextUtils.printBleBytes(advertisedData));
                        getAdvertisedBytes(advertisedData);
                    }
                }
            } else { // not connected after first connection has been established
                PSALogs.i("NIH", "overload nothing works");
                if (isRestartAuthorized) {
                    PSALogs.i("restartConnection", "not connected after first connection");
                    reconnectAfterDisconnection();
                }
            }
        }
    }

    private synchronized Integer mostCommon(final List<Integer> list) {
        if (list.size() == 0) {
            return -1;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }
        Map.Entry<Integer, Integer> max = null;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() >= max.getValue()) {
                max = e;
            }
        }
        return max == null ? -1 : max.getKey();
    }

    /**
     * Create two bytes with all the bits from the switches
     */
    private void getAdvertisedBytes(byte[] advertisedData) {
        if (advertisedData != null) {
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

    private void setIsThatcham(boolean isInLockArea, boolean isInUnlockArea, boolean isInStartArea) {
        if (isInLockArea || isInStartArea || !isInUnlockArea) {
            if (!thatchamIsChanging.get()) { // if thatcham is not changing
                mProtocolManager.setThatcham(false);
            }
        } else if (isInUnlockArea) {
            launchThatchamValidityTimeOut();
        }
    }

    private void setIsBackValid() {
        if (isUnlockStrategyValid != null) {
            if (isUnlockStrategyValid.contains(NUMBER_TRX_BACK)) {
                launchBackValidityTimeOut();
            } else if (backIsChanging.get()) {
                isUnlockStrategyValid.add(NUMBER_TRX_BACK);
            }
        } else {
            if (backIsChanging.get()) {
                isUnlockStrategyValid = new ArrayList<>();
                isUnlockStrategyValid.add(NUMBER_TRX_BACK);
            }
        }
    }

    /**
     * Try all strategy based on rssi values
     * @param newLockStatus the lock status of the vehicle
     */
    private void tryStandardStrategies(boolean newLockStatus) {
        if (isFullyConnected()) {
            boolean isStartAllowed = false;
            boolean isWelcomeAllowed = false;
            String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
//            AudioManager audM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//            PSALogs.d("test", "smartphoneComIsActivated " + CallReceiver.smartphoneComIsActivated + " " +
//                    audM.isBluetoothScoOn() + " " + audM.isSpeakerphoneOn() + " " + smartphoneIsInPocket);
            connectedCar.updateThresholdValues(smartphoneIsInPocket, CallReceiver.smartphoneComIsActivated);
            isStartStrategyValid = connectedCar.startStrategy();
            isUnlockStrategyValid = connectedCar.unlockStrategy();
            setIsBackValid(); // activate a timer when back is detected
            boolean isLockStrategyValid = connectedCar.lockStrategy();
            boolean isWelcomeStrategyValid = connectedCar.welcomeStrategy(totalAverage, newLockStatus);
            isInLockArea = forcedLock || (!blockLock && isLockStrategyValid && (isUnlockStrategyValid == null || isUnlockStrategyValid.size() < SdkPreferencesHelper.getInstance().getUnlockValidNb(connectedCarType)));
            isInUnlockArea = forcedUnlock || (!blockUnlock && !isLockStrategyValid && isUnlockStrategyValid != null && isUnlockStrategyValid.size() >= SdkPreferencesHelper.getInstance().getUnlockValidNb(connectedCarType));
            isInStartArea = forcedStart || (!blockStart && isStartStrategyValid != null);
            isInWelcomeArea = rearmWelcome.get() && isWelcomeStrategyValid;
            setIsThatcham(isInLockArea, isInUnlockArea, isInStartArea);
            if (lastSmartphoneIsFrozen != smartphoneIsFrozen) {
                rearmForcedBlock(isInLockArea, isInUnlockArea, isInStartArea);
                lastSmartphoneIsFrozen = smartphoneIsFrozen;
            }
            if (isInWelcomeArea) {
                isWelcomeAllowed = true;
                rearmWelcome.set(false);
                makeNoise(ToneGenerator.TONE_SUP_CONFIRM, 300);
                bleRangingListener.doWelcome();
            }
            if (areLockActionsAvailable.get() && rearmLock.get() && isInLockArea) {
                mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRKE.set(false);
                        performLockVehicleRequest(true);
                    }
                }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
            } else if (isInStartArea) { //smartphone in start area and moving
                isStartAllowed = true;
            } else if (areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRKE.set(false);
                        performLockVehicleRequest(false);
                    }
                }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
            }
            if (mProtocolManager.isWelcomeRequested() != isWelcomeAllowed) {
                mProtocolManager.setIsWelcomeRequested(isWelcomeAllowed);
            } else if (mProtocolManager.isStartRequested() != isStartAllowed) {
                mProtocolManager.setIsStartRequested(isStartAllowed);
            }
        }
    }

    /**
     * Try all strategy based on machine learning
     */
    private void tryMachineLearningStrategies() {
        isStartStrategyValid = null;
        isUnlockStrategyValid = null;
        isInStartArea = false;
        isInUnlockArea = false;
        isInLockArea = false;
        rangingPredictionInt = mostCommon(predictionHistoric);
        if (rangingPredictionInt != -1) {
            PSALogs.d("prediction", "rangingPredictionInt = " + rangingPredictionInt);
            switch (rangingPredictionInt) {
                case 0:
                    List<Integer> result0 = new ArrayList<>(2);
                    result0.add(START_PASSENGER_AREA);
                    result0.add(START_TRUNK_AREA);
                    isStartStrategyValid = result0;
                    isInStartArea = true;
                    if (mProtocolManager.isStartRequested() != isInStartArea) {
                        mProtocolManager.setIsStartRequested(isInStartArea);
                    }
                    break;
                case 1:
                    List<Integer> result1 = new ArrayList<>(1);
                    result1.add(NUMBER_TRX_LEFT);
                    isUnlockStrategyValid = result1;
                    isInUnlockArea = true;
                    if (areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                        mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRKE.set(false);
                                performLockVehicleRequest(false);
                            }
                        }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
                    }
                    break;
                case 2:
                    List<Integer> result2 = new ArrayList<>(1);
                    result2.add(NUMBER_TRX_RIGHT);
                    isUnlockStrategyValid = result2;
                    isInUnlockArea = true;
                    if (areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                        mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRKE.set(false);
                                performLockVehicleRequest(false);
                            }
                        }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
                    }
                    break;
                case 3:
                    List<Integer> result3 = new ArrayList<>(1);
                    result3.add(NUMBER_TRX_BACK);
                    isUnlockStrategyValid = result3;
                    isInUnlockArea = true;
                    if (areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                        mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRKE.set(false);
                                performLockVehicleRequest(false);
                            }
                        }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
                    }
                    break;
                case 4:
                    isInLockArea = true;
                    if (areLockActionsAvailable.get() && rearmLock.get() && isInLockArea) {
                        mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRKE.set(false);
                                performLockVehicleRequest(true);
                            }
                        }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
                    }
                    break;
                default:
                    PSALogs.d("prediction", "NOOO rangingPredictionInt !");
                    break;
            }
            setIsThatcham(isInLockArea, isInUnlockArea, isInStartArea);
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

    private void launchBackValidityTimeOut() {
        if (backIsChanging.get()) {
            mHandlerBackTimeOut.removeCallbacks(mHasBackChanged);
            mHandlerBackTimeOut.removeCallbacks(null);
        } else {
            backIsChanging.set(true);
        }
        mHandlerBackTimeOut.postDelayed(mHasBackChanged,
                (long) (SdkPreferencesHelper.getInstance().getBackTimeout() * 1000));
    }

    /**
     * Update the mini map with our location around the car
     */
    private void updateCarLocalization() {
        bleRangingListener.darkenArea(THATCHAM_AREA);
        bleRangingListener.darkenArea(UNLOCK_LEFT_AREA);
        bleRangingListener.darkenArea(UNLOCK_RIGHT_AREA);
        bleRangingListener.darkenArea(UNLOCK_BACK_AREA);
        bleRangingListener.darkenArea(START_TRUNK_AREA);
        bleRangingListener.darkenArea(UNLOCK_FRONT_LEFT_AREA);
        bleRangingListener.darkenArea(UNLOCK_REAR_LEFT_AREA);
        bleRangingListener.darkenArea(UNLOCK_FRONT_RIGHT_AREA);
        bleRangingListener.darkenArea(UNLOCK_REAR_RIGHT_AREA);
        bleRangingListener.darkenArea(START_PASSENGER_AREA);
        bleRangingListener.darkenArea(LOCK_AREA);
        bleRangingListener.darkenArea(WELCOME_AREA);
        //THATCHAM
        if (mProtocolManager.isThatcham()) {
            bleRangingListener.lightUpArea(THATCHAM_AREA);
        }
        if (isInWelcomeArea) {
            // WELCOME
            bleRangingListener.lightUpArea(WELCOME_AREA);
        }
        if (isInLockArea) {
            // LOCK
            bleRangingListener.lightUpArea(LOCK_AREA);
        } else if (isStartStrategyValid != null && isInStartArea) {
            //START
            for (Integer integer : isStartStrategyValid) {
                switch (integer) {
                    case START_PASSENGER_AREA:
                        bleRangingListener.lightUpArea(START_PASSENGER_AREA);
                        break;
                    case START_TRUNK_AREA:
                        bleRangingListener.lightUpArea(START_TRUNK_AREA);
                        break;
                    default:
                        bleRangingListener.lightUpArea(START_PASSENGER_AREA);
                        bleRangingListener.lightUpArea(START_TRUNK_AREA);
                        break;
                }
            }
        } else if (isUnlockStrategyValid != null && isInUnlockArea) { // if unlock forced, unlock Strategy may be null
            //UNLOCK
            for (Integer integer : isUnlockStrategyValid) {
                switch (integer) {
                    case NUMBER_TRX_LEFT:
                        bleRangingListener.lightUpArea(UNLOCK_LEFT_AREA);
                        break;
                    case NUMBER_TRX_RIGHT:
                        bleRangingListener.lightUpArea(UNLOCK_RIGHT_AREA);
                        break;
                    case NUMBER_TRX_BACK:
                        bleRangingListener.lightUpArea(UNLOCK_BACK_AREA);
                        break;
                    case NUMBER_TRX_FRONT_LEFT:
                        bleRangingListener.lightUpArea(UNLOCK_FRONT_LEFT_AREA);
                        break;
                    case NUMBER_TRX_REAR_LEFT:
                        bleRangingListener.lightUpArea(UNLOCK_REAR_LEFT_AREA);
                        break;
                    case NUMBER_TRX_FRONT_RIGHT:
                        bleRangingListener.lightUpArea(UNLOCK_FRONT_RIGHT_AREA);
                        break;
                    case NUMBER_TRX_REAR_RIGHT:
                        bleRangingListener.lightUpArea(UNLOCK_REAR_RIGHT_AREA);
                        break;
                    default:
                        bleRangingListener.lightUpArea(UNLOCK_LEFT_AREA);
                        bleRangingListener.lightUpArea(UNLOCK_RIGHT_AREA);
                        bleRangingListener.lightUpArea(UNLOCK_BACK_AREA);
                        bleRangingListener.lightUpArea(UNLOCK_FRONT_LEFT_AREA);
                        bleRangingListener.lightUpArea(UNLOCK_REAR_LEFT_AREA);
                        bleRangingListener.lightUpArea(UNLOCK_FRONT_RIGHT_AREA);
                        bleRangingListener.lightUpArea(UNLOCK_REAR_RIGHT_AREA);
                        break;
                }
            }
        }
        bleRangingListener.applyNewDrawable();
    }

    /**
     * Initialize Trx and antenna then launch IHM looper and antenna active check loop
     * @param newLockStatus the lock status
     */
    private void runFirstConnection(final boolean newLockStatus) {
        PSALogs.w(" rssiHistorics", "************************************** runFirstConnection ************************************************");
        bleRangingListener.updateCarDoorStatus(newLockStatus);
        mProtocolManager.setIsLockedToSend(newLockStatus);
        lastCommandFromTrx = newLockStatus;
        if (connectedCar != null) {
            connectedCar.initializeTrx(newLockStatus);
        }
        if (mMainHandler != null) {
            mMainHandler.post(checkAntennaRunner);
            mMainHandler.post(logRunner);
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PSALogs.d("prediction", String.format(Locale.FRANCE, "%1d %2d %3d %4d",
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK, Trx.ANTENNA_ID_1)));
                    connectedCar.createRangingObject(
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK, Trx.ANTENNA_ID_1));
                }
            }, 500);
            mMainHandler.postDelayed(fillPredictionArray, 600);
            mMainHandler.postDelayed(updateCarLocalizationRunnable, 1000);
            mMainHandler.postDelayed(beepRunner, 1000);
        }
    }

    public void initializeConnectedCar() {
        if (lastConnectedCarType.equals("")) {
            // on first run, create a new car
            createConnectedCar();
        } else if (!lastConnectedCarType.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getConnectedCarType())) {
            // if car type has changed,
            if (isFullyConnected()) {
                PSALogs.w("NIH", "INITIALIZED_NEW_CAR");
                // if connected, stop connection, create a new car, and restart it
                PSALogs.i("restartConnection", "disconnect after changing car type");
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
            if (deltaLinAcc < SdkPreferencesHelper.getInstance().getFrozenThreshold()) {
                if (!isFrozenRunnableAlreadyLaunched) {
                    mIsFrozenTimeOutHandler.postDelayed(isFrozenRunnable, 3000); // wait before apply stillness
                    isFrozenRunnableAlreadyLaunched = true;
                }
            } else {
                smartphoneIsFrozen = false; // smartphone is moving
                mIsFrozenTimeOutHandler.removeCallbacks(isFrozenRunnable);
                isFrozenRunnableAlreadyLaunched = false;
            }
            if (deltaLinAcc < SdkPreferencesHelper.getInstance().getCorrectionLinAcc()) {
                if(!isLaidRunnableAlreadyLaunched) {
                    mIsLaidTimeOutHandler.postDelayed(isLaidRunnable, 3000); // wait before apply stillness
                    isLaidRunnableAlreadyLaunched = true;
                }
            } else {
                smartphoneIsMovingSlowly = false; // smartphone is moving
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
        if(lastLockCommand != mProtocolManager.isLockedToSend()) {
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

    public boolean areLockActionsAvailable() {
        return areLockActionsAvailable.get();
    }

    public void setIsRKE(boolean isRKE) {
        this.isRKE.set(isRKE);
    }

    public void closeApp() {
        PSALogs.d("NIH", "closeApp()");
        // on settings changes, increase the file number used for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        mContext.unregisterReceiver(callReceiver);
        mContext.unregisterReceiver(bleStateReceiver);
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(printRunner);
            mMainHandler.removeCallbacks(updateCarLocalizationRunnable);
            mMainHandler.removeCallbacks(beepRunner);
            mMainHandler.removeCallbacks(fillPredictionArray);
            mMainHandler.removeCallbacks(null);
        }
        mBluetoothManager.suspendLeScan();
        if (mBluetoothManager.isFullyConnected() && !mBluetoothManager.isConnecting()) {
            PSALogs.d("NIH", "closeApp() disconnect");
            mBluetoothManager.disconnect();
        }
        if (mBluetoothManager.isFullyConnected2() && !mBluetoothManager.isConnecting2()) {
            PSALogs.d("NIH", "closeApp() disconnectPc");
            mBluetoothManager.disconnectPc();
        }
        if (mBluetoothManager.isFullyConnected3() && !mBluetoothManager.isConnecting3()) {
            PSALogs.d("NIH", "closeApp() disconnectRemoteControl");
            mBluetoothManager.disconnectRemoteControl();
        }
        bleRangingListener.updateBLEStatus();
        isLoggable = false;
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }
}
