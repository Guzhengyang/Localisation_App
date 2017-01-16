package com.valeo.bleranging;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.bluetooth.AlgoManager;
import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.bluetooth.BluetoothManagementListener;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.bluetooth.scanresponse.CentralScanResponse;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import static com.valeo.bleranging.utils.SoundUtils.makeNoise;

/**
 * Created by l-avaratha on 19/07/2016
 */
public class BleRangingHelper {
    public static final String PREDICTION_START = "start";
    public static final String PREDICTION_LOCK = "lock";
    public static final String PREDICTION_TRUNK = "trunk";
    public static final String PREDICTION_LEFT = "left";
    public static final String PREDICTION_RIGHT = "right";
    public static final String PREDICTION_BACK = "back";
    public static final String PREDICTION_FRONT = "front";
    public static final String PREDICTION_OUTDOOR = "outdoor";
    public static final String PREDICTION_INDOOR = "indoor";
    public static final String PREDICTION_WELCOME = "welcome";
    public static final String PREDICTION_THATCHAM = "thatcham";
    public static final String PREDICTION_NEAR = "near";
    public static final String PREDICTION_FAR = "far";
    public static final String PREDICTION_UNKNOWN = "unknown";
    public final static int RKE_USE_TIMEOUT = 5000;
    public final static int REQUEST_PERMISSION_ALL = 25110;
    private final static String[] PREDICTIONS = {
            PREDICTION_START,
            PREDICTION_LOCK,
            PREDICTION_TRUNK,
            PREDICTION_LEFT,
            PREDICTION_RIGHT,
            PREDICTION_BACK,
            PREDICTION_FRONT,
            PREDICTION_OUTDOOR,
            PREDICTION_INDOOR,
            PREDICTION_WELCOME,
            PREDICTION_THATCHAM,
            PREDICTION_NEAR,
            PREDICTION_FAR,
            PREDICTION_UNKNOWN
    };
    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.CAMERA
    };
    private final Context mContext;
    private final AlgoManager mAlgoManager;
    private final BluetoothManagement mBluetoothManager;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final byte[] lastPacketIdNumber = new byte[2];
    private final Handler mMainHandler;
    private final Handler mHandlerTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Runnable toggleOnIsRKEAvailable = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("performLock", "before isRKEAvailable =" + mAlgoManager.isRKEAvailable());
            mAlgoManager.setIsRKEAvailable(true);
            PSALogs.d("performLock", "after isRKEAvailable =" + mAlgoManager.isRKEAvailable());
        }
    };
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
            lock.writeLock().lock();
            bytesToSend = mProtocolManager.getPacketOnePayload(mAlgoManager);
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
            lock.readLock().lock();
            mBluetoothManager.sendPackets(bytesToSend, bytesReceived);
            lock.readLock().unlock();
            if (mAlgoManager.getIsRKE()) {
                mAlgoManager.setIsRKE(false);
            }
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, 200);
            }
        }
    };
    private String lastConnectedCarType = "";
    private int totalAverage;
    private boolean isCloseAppCalled = false;
    private boolean newLockStatus;
    private boolean isFirstConnection = true;
    private boolean isTryingToConnect = false;
    private final Runnable mManageIsTryingToConnectTimer = new Runnable() {
        @Override
        public void run() {
            PSALogs.w("NIH", "************************************** isTryingToConnect FALSE ************************************************");
            isTryingToConnect = false;
        }
    };
    private boolean isIndoor = false;
    private ConnectedCar connectedCar;
    private final Runnable setRssiForRangingPrediction = new Runnable() {
        @Override
        public void run() {
            mAlgoManager.getRanging().set_rssi(connectedCar.getRssiForRangingPrediction());
            mMainHandler.postDelayed(this, 105);
        }
    };
    //    private final Runnable checkAntennaRunner = new Runnable() {
//        @Override
//        public void run() {
//            if (isFullyConnected()) {
//                PSALogs.w(" rssiHistorics", "************************************** CHECK ANTENNAS ************************************************");
//                connectedCar.compareCheckerAndSetAntennaActive(mAlgoManager.isSmartphoneMovingSlowly());
//            }
//            mMainHandler.postDelayed(this, 2500);
//        }
//    };
    private final Runnable updateCarLocalizationRunnable = new Runnable() {
        @Override
        public void run() {
            if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(ALGO_STANDARD)) {
                mAlgoManager.tryStandardStrategies(newLockStatus, isFullyConnected(), isIndoor,
                        connectedCar, totalAverage);
            } else if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(MACHINE_LEARNING)) {
                mAlgoManager.tryMachineLearningStrategies(newLockStatus, connectedCar);
            }
            updateCarLocalization(mAlgoManager.getRangingPositionPrediction(), mAlgoManager.getRangingProximityPrediction());
            mMainHandler.postDelayed(this, 400);
        }
    };
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
            spannableStringBuilder = mAlgoManager.createDebugData(spannableStringBuilder);
            spannableStringBuilder = connectedCar.createSecondFooterDebugData(spannableStringBuilder, totalAverage, mAlgoManager);
            spannableStringBuilder = connectedCar.createThirdFooterDebugData(spannableStringBuilder, mAlgoManager);
            bleRangingListener.printDebugInfo(spannableStringBuilder);
            bleRangingListener.updateBLEStatus();
            PSALogs.w(" rssiHistorics", "************************************** IHM LOOP END *************************************************");
            mMainHandler.postDelayed(this, 105);
        }
    };
    private int reconnectionCounter = 0;
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
                lock.writeLock().lock();
                bytesReceived = mBluetoothManager.getBytesReceived();
                lock.writeLock().unlock();
                boolean oldLockStatus = newLockStatus;
                if (bytesReceived != null) {
                    lock.readLock().lock();
                    newLockStatus = (bytesReceived[5] & 0x01) != 0;
                    lock.readLock().unlock();
                }
                if (oldLockStatus != newLockStatus) {
                    bleRangingListener.updateCarDoorStatus(newLockStatus);
                }
                mProtocolManager.setIsLockedFromTrx(newLockStatus);
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
                mAlgoManager.setRearmWelcome(true);
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
    private byte welcomeByte = 0;
    private byte lockByte = 0;
    private byte startByte = 0;
    private byte leftAreaByte = 0;
    private byte rightAreaByte = 0;
    private byte backAreaByte = 0;
    private byte walkAwayByte = 0;
    private byte approachByte = 0;
    private byte leftTurnByte = 0;
    private byte rightTurnByte = 0;
    private byte approachSideByte = 0;
    private byte approachRoadByte = 0;
    private byte recordByte = 0;
    private int beepInt = 0;
    private final Runnable beepRunner = new Runnable() {
        @Override
        public void run() {
            long delayedTime = 500;
            if (SdkPreferencesHelper.getInstance().getUserSpeedEnabled()) {
                beepInt = 1;
                makeNoise(mContext, mMainHandler, ToneGenerator.TONE_CDMA_LOW_SS, 100);
                // interval time between each beep sound in milliseconds
                delayedTime = Math.round(((SdkPreferencesHelper.getInstance().getOneStepSize() / 100.0f) / (SdkPreferencesHelper.getInstance().getWantedSpeed() / 3.6)) * 1000);
            }
            PSALogs.d("beep", "delayedTime " + delayedTime);
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, delayedTime);
            }
        }
    };
    private boolean isLoggable = true;
    private final Runnable logRunner = new Runnable() {
        @Override
        public void run() {
            if (isLoggable) {
                TrxUtils.appendRssiLogs(connectedCar.getCurrentModifiedRssi(NUMBER_TRX_LEFT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_MIDDLE),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_RIGHT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_TRUNK),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_FRONT_LEFT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_FRONT_RIGHT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_REAR_LEFT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_REAR_RIGHT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_BACK),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_TRUNK),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_LEFT),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_MIDDLE),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_RIGHT),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_TRUNK),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_FRONT_LEFT),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_FRONT_RIGHT),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_REAR_LEFT),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_REAR_RIGHT),
                        connectedCar.getCurrentOffset38(NUMBER_TRX_BACK),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_LEFT),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_MIDDLE),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_RIGHT),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_TRUNK),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_FRONT_LEFT),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_FRONT_RIGHT),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_REAR_LEFT),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_REAR_RIGHT),
                        connectedCar.getCurrentOffset39(NUMBER_TRX_BACK),
                        connectedCar.isActive(NUMBER_TRX_LEFT),
                        connectedCar.isActive(NUMBER_TRX_MIDDLE),
                        connectedCar.isActive(NUMBER_TRX_RIGHT),
                        connectedCar.isActive(NUMBER_TRX_TRUNK),
                        connectedCar.isActive(NUMBER_TRX_FRONT_LEFT),
                        connectedCar.isActive(NUMBER_TRX_FRONT_RIGHT),
                        connectedCar.isActive(NUMBER_TRX_REAR_LEFT),
                        connectedCar.isActive(NUMBER_TRX_REAR_RIGHT),
                        connectedCar.isActive(NUMBER_TRX_BACK),
                        mAlgoManager.getOrientation()[0], mAlgoManager.getOrientation()[1], mAlgoManager.getOrientation()[2],
                        mAlgoManager.isSmartphoneInPocket(), mAlgoManager.isSmartphoneMovingSlowly(), mAlgoManager.areLockActionsAvailable(),
                        mAlgoManager.isBlockStart(), mAlgoManager.isForcedStart(),
                        mAlgoManager.isBlockLock(), mAlgoManager.isForcedLock(),
                        mAlgoManager.isBlockUnlock(), mAlgoManager.isForcedUnlock(),
                        mAlgoManager.isSmartphoneFrozen(),
                        mAlgoManager.getRearmLock(), mAlgoManager.getRearmUnlock(),
                        mAlgoManager.getRearmWelcome(), newLockStatus, welcomeByte,
                        lockByte, startByte, leftAreaByte, rightAreaByte, backAreaByte,
                        walkAwayByte, approachByte, leftTurnByte, rightTurnByte,
                        approachSideByte, approachRoadByte, recordByte, mAlgoManager.getRangingPositionPrediction(),
                        mProtocolManager.isLockedFromTrx(), mProtocolManager.isLockedToSend(),
                        mProtocolManager.isStartRequested(), mProtocolManager.isThatcham(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_LEFT).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_MIDDLE).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_RIGHT).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_TRUNK).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_FRONT_LEFT).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_FRONT_RIGHT).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_REAR_LEFT).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_REAR_RIGHT).toString(),
                        connectedCar.getCurrentBLEChannel(NUMBER_TRX_BACK).toString(),
                        beepInt);
                beepInt = 0;
            }
            if (isFullyConnected()) {
                mMainHandler.postDelayed(this, 105);
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
            }
        }
    };

    public BleRangingHelper(Context context, BleRangingListener bleRangingListener) {
        this.mContext = context;
        askForPermissions();
        this.mBluetoothManager = new BluetoothManagement(context);
        this.bleRangingListener = bleRangingListener;
        this.mProtocolManager = new InblueProtocolManager();
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mAlgoManager = new AlgoManager(mContext, bleRangingListener, mProtocolManager, mMainHandler);
        this.mHandlerTimeOut = new Handler();
        this.mHandlerCryptoTimeOut = new Handler();
        mBluetoothManager.addBluetoothManagementListener(new BluetoothManagementListener() {
            private final ExecutorService executorService = Executors.newFixedThreadPool(4);

            @Override
            public void onCentralScanResponseCatch(final BluetoothDevice device,
                                                   final CentralScanResponse centralScanResponse) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        catchCentralScanResponse(device, centralScanResponse);
                    }
                });
            }

            @Override
            public void onBeaconScanResponseCatch(final BluetoothDevice device, final int rssi,
                                                  final BeaconScanResponse beaconScanResponse,
                                                  final byte[] advertisedData) {
                isIndoor = getIsIndoorParameter(beaconScanResponse);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        catchBeaconScanResponse(device, rssi, beaconScanResponse, advertisedData);
                    }
                });
            }
        });
        mBluetoothManager.resumeLeScan();
        mMainHandler.post(printRunner);
    }

    /**
     * Connect the smartphone to a computer
     */
    public void connectToPC() {
        mBluetoothManager.connectToPC(SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC());
    }

    /**
     * Connect the smartphone to a remote control
     */
    public void connectToRemoteControl() {
        mBluetoothManager.connectToRemoteControl(SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl());
    }

    /**
     * Connect the smartphone to the vehicle
     */
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

    /**
     * Relaunch the ble scan
     */
    public void relaunchScan() {
        mBluetoothManager.resumeLeScan();
    }

    /**
     * Toggle the ble activation
     *
     * @param enable true to toggle on, false to toggle off
     */
    public void toggleBluetooth(boolean enable) {
        mBluetoothManager.setBluetooth(enable);
    }

    /**
     * Get the smartphone ble status
     *
     * @return true if the smartphone ble is enable, false otherwise
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothManager.isBluetoothEnabled();
    }

    /**
     * Perform a RKE lock or unlock action
     *
     * @param lockCar true to send a lock action, false to send an unlock action
     */
    public void performRKELockAction(final boolean lockCar) {
        PSALogs.d("performLock", "isRKEAvailable =" + mAlgoManager.isRKEAvailable() +
                ", Fco =" + isFullyConnected() +
                ", lockActionAvailable =" + mAlgoManager.areLockActionsAvailable());
        if (isRKEButtonClickable()) {
            mAlgoManager.setIsRKEAvailable(false);
            mHandlerCryptoTimeOut.postDelayed(toggleOnIsRKEAvailable, RKE_USE_TIMEOUT);
            // Send command several times in case it got lost
            new CountDownTimer(200, 50) {
                public void onTick(long millisUntilFinished) {
                    mAlgoManager.performLockWithCryptoTimeout(true, lockCar);
                }

                public void onFinish() {
                }
            }.start();
        }
    }

    /**
     * Initialize the connected car.
     * Call this method in onResume.
     */
    public void initializeConnectedCar() {
        // on first run, create a new car
        if (lastConnectedCarType.equals("")) {
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
            connectedCar.resetSettings(isIndoor);
            String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
            mProtocolManager.setCarBase(connectedCarBase);
            bleRangingListener.updateCarDrawable();
        }
    }

    /**
     * Get the connection status between the smartphone and the car
     *
     * @return true if the smartphone is connected to the car, false otherwise
     */
    public boolean isFullyConnected() {
        return mBluetoothManager != null && mBluetoothManager.isFullyConnected();
    }

    /**
     * Verify if the user can click on rke button by checking if the action can succeed
     *
     * @return true if the rke button is ready, false otherwise
     */
    public boolean isRKEButtonClickable() {
        return isFullyConnected() && mAlgoManager.isRKEAvailable()
                && mAlgoManager.areLockActionsAvailable();
    }

    /**
     * Get the vehicle car door status
     *
     * @return true if the vehicle is locked, false otherwise
     */
    public boolean isVehicleLocked() {
        return newLockStatus;
    }

    public boolean isCloseAppCalled() {
        return isCloseAppCalled;
    }

    /**
     * Call this method before closing the app.
     * It unregister listeners and removeCallbacks and close cleanly all connections.
     */
    public void closeApp() {
        PSALogs.d("NIH", "closeApp()");
        isCloseAppCalled = true;
        // on settings changes, increase the file number used for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        mAlgoManager.closeApp();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(printRunner);
//            mMainHandler.removeCallbacks(checkAntennaRunner);
            mMainHandler.removeCallbacks(logRunner);
            mMainHandler.removeCallbacks(setRssiForRangingPrediction);
            mMainHandler.removeCallbacks(updateCarLocalizationRunnable);
            mMainHandler.removeCallbacks(beepRunner);
            mMainHandler.removeCallbacks(sendPacketRunner);
            mMainHandler.removeCallbacks(checkNewPacketsRunner);
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

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    private void restartConnection(boolean createConnectedCar) {
        if (!mBluetoothManager.isConnecting()) {
            PSALogs.d("NIH", "restartConnection");
            mBluetoothManager.suspendLeScan();
            mBluetoothManager.disconnect();
            if (mMainHandler != null) {
//                mMainHandler.removeCallbacks(checkAntennaRunner);
                mMainHandler.removeCallbacks(logRunner);
                mMainHandler.removeCallbacks(setRssiForRangingPrediction);
                mMainHandler.removeCallbacks(updateCarLocalizationRunnable);
                mMainHandler.removeCallbacks(beepRunner);
                mMainHandler.removeCallbacks(sendPacketRunner);
                mMainHandler.removeCallbacks(checkNewPacketsRunner);
            }
            mProtocolManager.restartPacketOneCounter();
            mAlgoManager.setRearmWelcome(true);
            isFirstConnection = true;
            checkNewPacketOnlyOneLaunch = true;
            lastPacketIdNumber[0] = 0;
            lastPacketIdNumber[1] = 0;
            resetByteArray(bytesToSend);
            resetByteArray(bytesReceived);
            makeNoise(mContext, mMainHandler, ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 100);
            if (createConnectedCar) {
                createConnectedCar();
            }
            bleRangingListener.updateBLEStatus();
            mBluetoothManager.resumeLeScan();
        }
    }

    /**
     * Reset a byte array
     * @param byteArray the byte array to reinitialize
     */
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
     * Get the current advertising channel from beacon scan response
     *
     * @param scanResponse the beacon scan response
     * @return the received ble channel
     */
    private Antenna.BLEChannel getCurrentChannel(BeaconScanResponse scanResponse) {
        if (scanResponse.advertisingChannel == 0x01) {
            return Antenna.BLEChannel.BLE_CHANNEL_37;
        } else if (scanResponse.advertisingChannel == 0x02) {
            return Antenna.BLEChannel.BLE_CHANNEL_38;
        } else if (scanResponse.advertisingChannel == 0x03) {
            return Antenna.BLEChannel.BLE_CHANNEL_39;
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
    }

    /**
     * Get the current isIndoor parameter from the beacon scan response
     *
     * @param scanResponse the beacon scan response
     * @return the received isIndoor parameter
     */
    private boolean getIsIndoorParameter(BeaconScanResponse scanResponse) {
        return SdkPreferencesHelper.getInstance().getIsIndoor();
//        return ((scanResponse.isIndoor & 0xC0) == 0x40); //TODO change mask and comparaison
    }

    /**
     * Connect to central trx
     *
     * @param device              the trx that send the centralScanResponse
     * @param centralScanResponse the centralScanResponse received
     */
    private void catchCentralScanResponse(final BluetoothDevice device, CentralScanResponse centralScanResponse) {
        if (device != null && centralScanResponse != null) {
            if (isFirstConnection) {
                if (device.getAddress().equals(SdkPreferencesHelper.getInstance().getTrxAddressConnectable())) {
                    PSALogs.w("NIH", "CONNECTABLE " + device.getAddress());
                    if (!isTryingToConnect && !mBluetoothManager.isFullyConnected() && !mBluetoothManager.isConnecting()) {
                        isTryingToConnect = true;
                        mBluetoothManager.suspendLeScan();
                        mHandlerTimeOut.postDelayed(mManageIsTryingToConnectTimer, 3000);
                        PSALogs.w("NIH", "************************************** isTryingToConnect TRUE ************************************************");
                        bleRangingListener.showSnackBar("CONNECTABLE " + device.getAddress());
                        newLockStatus = (centralScanResponse.vehicleState & 0x01) != 0; // get lock status for initialization later
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
                if (trxNumber == -1) {
                    if (SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress())) {
                        PSALogs.i("restartConnection", "connectable is advertising again (central)");
                        if (isRestartAuthorized) { // prevent from restarting connection when a restart is already in progress
                            isRestartAuthorized = false;
                            restartConnection(false);
                        } else {
                            isRestartAuthorized = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Save all trx rssi
     *
     * @param device       the trx that send the beaconScanResponse
     * @param rssi         the rssi of the beaconScanResponse
     * @param beaconScanResponse the beaconScanResponse received
     */
    private void catchBeaconScanResponse(final BluetoothDevice device, int rssi, BeaconScanResponse beaconScanResponse, byte[] advertisedData) {
        if (device != null && beaconScanResponse != null) {
            if (isFullyConnected()) {
                int trxNumber = connectedCar.getTrxNumber(device.getAddress());
                if (trxNumber != -1) {
                    connectedCar.saveBleChannel(trxNumber, getCurrentChannel(beaconScanResponse));
                    PSALogs.d("bleChannel " + trxNumber, "channel " + connectedCar.getCurrentBLEChannel(trxNumber) + " " + beaconScanResponse.advertisingChannel);
                    connectedCar.saveRssi(trxNumber, rssi, mAlgoManager.isSmartphoneMovingSlowly());
//                    PSALogs.d("NIH", "BLE_ADDRESS=" + device.getAddress()
//                            + " " + connectedCar.getRssiAverage(trxNumber, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)
//                            + " " + connectedCar.getRssiAverage(trxNumber, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT));
                } else {
                    if (SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress())) {
                        PSALogs.i("NIH", "restartConnection => connectable is advertising again (beacon)");
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
                    } else {
                        PSALogs.d("NIH", "newConnectable coz advertising is null " + device.getAddress());
                    }
                }
            } else { // not connected after first connection has been established
                PSALogs.i("NIH", "not connected");
                if (isRestartAuthorized) {
                    PSALogs.i("restartConnection", "not connected after first connection");
                    reconnectAfterDisconnection();
                }
            }
        }
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
            approachRoadByte = (byte) ((advertisedData[4] & 0x070) >> 4);
            approachSideByte = (byte) ((advertisedData[4] & (1 << 3)) >> 3);
            rightTurnByte = (byte) ((advertisedData[4] & (1 << 2)) >> 2);
            leftTurnByte = (byte) ((advertisedData[4] & (1 << 1)) >> 1);
            approachByte = (byte) (advertisedData[4] & 1);
        }
    }

    /**
     * Update the mini map with our location around the car
     */
    private void updateCarLocalization(String predictionPosition, String predictionProximity) {
        for (String elementPred : PREDICTIONS) {
            bleRangingListener.darkenArea(elementPred);
        }
        //THATCHAM
        if (mProtocolManager.isThatcham()) {
            bleRangingListener.lightUpArea(PREDICTION_THATCHAM);
        }
        // WELCOME
        if (mAlgoManager.isInWelcomeArea()) {
            bleRangingListener.lightUpArea(PREDICTION_WELCOME);
        }
        if (predictionPosition != null && !predictionPosition.isEmpty()) {
            bleRangingListener.lightUpArea(predictionPosition);
        }
        // REMOTE PARKING
        if (predictionProximity != null && !predictionProximity.isEmpty()) {
            bleRangingListener.lightUpArea(predictionProximity);
        }
        bleRangingListener.applyNewDrawable();
    }

    /**
     * Initialize Trx and antenna then launch IHM looper and antenna active check loop
     *
     * @param newLockStatus the lock status
     */
    private void runFirstConnection(final boolean newLockStatus) {
        PSALogs.w(" rssiHistorics", "************************************** runFirstConnection ************************************************");
        bleRangingListener.updateCarDoorStatus(newLockStatus);
        mProtocolManager.setIsLockedToSend(newLockStatus);
        mAlgoManager.setLastCommandFromTrx(newLockStatus);
        if (connectedCar != null) {
            connectedCar.initializeTrx(newLockStatus);
        }
        if (mMainHandler != null) {
//            mMainHandler.post(checkAntennaRunner);
            mMainHandler.post(logRunner);
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PSALogs.d("prediction", String.format(Locale.FRANCE, "%1d %2d %3d %4d",
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK)));

                    mAlgoManager.createRangingObject(connectedCar.getRssiForRangingPrediction());
                }
            }, 500);
            mMainHandler.postDelayed(setRssiForRangingPrediction, 600);
            mMainHandler.postDelayed(updateCarLocalizationRunnable, 1000);
            mMainHandler.postDelayed(beepRunner, 1000);
        }
    }

    private void createConnectedCar() {
        connectedCar = null;
        lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        connectedCar = ConnectedCarFactory.getConnectedCar(mContext, lastConnectedCarType, isIndoor);
        String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
        mProtocolManager.setCarBase(connectedCarBase);
        bleRangingListener.updateCarDrawable();
    }

    /**
     * Get permission by asking user
     */
    private void askForPermissions() {
        if (!hasPermissions(mContext, PERMISSIONS)) {
            ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST_PERMISSION_ALL);
        }
    }

    /**
     * Check if every needed permissions are granted
     *
     * @param context     the context
     * @param permissions the needed permissions
     * @return true if all needed permissions are granted, false otherwise
     */
    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
