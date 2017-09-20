package com.valeo.bleranging;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.SpannedString;
import android.widget.Toast;

import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.bluetooth.BluetoothManagementListener;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.bluetooth.protocol.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.bluetooth.scanresponse.CentralScanResponse;
import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.listeners.ChessBoardListener;
import com.valeo.bleranging.listeners.DebugListener;
import com.valeo.bleranging.listeners.SpinnerListener;
import com.valeo.bleranging.listeners.TestListener;
import com.valeo.bleranging.managers.CommandManager;
import com.valeo.bleranging.managers.LogFileManager;
import com.valeo.bleranging.managers.SensorsManager;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.JsonUtils;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.valeo.bleranging.persistence.Constants.BASE_2;
import static com.valeo.bleranging.persistence.Constants.BASE_3;
import static com.valeo.bleranging.persistence.Constants.NUMBER_TRX_TRUNK;
import static com.valeo.bleranging.persistence.Constants.PERMISSIONS;
import static com.valeo.bleranging.persistence.Constants.PREDICTIONS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_THATCHAM;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_WELCOME;
import static com.valeo.bleranging.persistence.Constants.REQUEST_PERMISSION_ALL;
import static com.valeo.bleranging.persistence.Constants.RSSI_DIR;
import static com.valeo.bleranging.utils.SoundUtils.makeNoise;
import static com.valeo.bleranging.utils.TextUtils.createFirstFooterDebugData;
import static com.valeo.bleranging.utils.TextUtils.createHeaderDebugData;

/**
 * Created by l-avaratha on 19/07/2016
 */
public class BleRangingHelper {
    private final Context mContext;
    private final BluetoothManagement mBluetoothManager;
    private final BleRangingListener bleRangingListener;
    private final ChessBoardListener chessBoardListener;
    private final DebugListener debugListener;
    private final SpinnerListener spinnerListener;
    private final TestListener testListener;
    private final byte[] lastPacketIdNumber = new byte[2];
    private final Handler mMainHandler;
    private final Handler mHandlerTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean checkNewPacketOnlyOneLaunch = true;
    private boolean isRestartAuthorized = true;
    private byte[] bytesToSend;
    private byte[] bytesReceived;
    private String lastConnectedCarType = "";
    private String lastOpeningOrientation = "";
    private Boolean lastPrintRooftop;
    private Boolean lastMiniPredictionUsed;
    private boolean isCloseAppCalled = false;
    private boolean newLockStatus;
    private boolean canStartRunner = true;
    private boolean isFirstConnection = true;
    private boolean isTryingToConnect = false;
    private final Runnable mManageIsTryingToConnectTimer = new Runnable() {
        @Override
        public void run() {
            PSALogs.w("NIH", "************************************** isTryingToConnect FALSE ************************************************");
            isTryingToConnect = false;
        }
    };
    private ConnectedCar connectedCar;
    private final Runnable sendPacketRunner = new Runnable() {
        @Override
        public void run() {
            lock.writeLock().lock();
            bytesToSend = InblueProtocolManager.getInstance().getPacketOne().getPacketOnePayload(connectedCar);
            lock.writeLock().unlock();
            lock.readLock().lock();
            mBluetoothManager.sendPackets(bytesToSend, bytesReceived,
                    InblueProtocolManager.getInstance().getPacketTwo().getPacketTwoPayload(connectedCar.getMultiPrediction().getStandardClasses(),
                            connectedCar.getMultiPrediction().getStandardDistribution()),
                    InblueProtocolManager.getInstance().getPacketThree().getPacketThreePayload(connectedCar.getMultiPrediction().getStandardRssi()),
                    InblueProtocolManager.getInstance().getPacketFour().getPacketFourPayload(connectedCar.getMultiPrediction().getPredictionCoord(), connectedCar.getMultiPrediction().getDist2Car()));
            lock.readLock().unlock();
            if (CommandManager.getInstance().getIsRKE()) {
                CommandManager.getInstance().setIsRKE(false);
            }
            if (mBluetoothManager.isLinked()) {
                mMainHandler.postDelayed(this, 200);
            }
        }
    };
    private final Runnable setRssiForRangingPrediction = new Runnable() {
        @Override
        public void run() {
            if (connectedCar != null) {
                double[] rssi = connectedCar.getMultiTrx().getRssiForRangingPrediction();
                if (rssi != null) {
                    connectedCar.getMultiPrediction().setRssi(rssi);
                } else {
                    PSALogs.d("init2", "setRssiForRangingPrediction is NULL\n");
                }
            }
            mMainHandler.postDelayed(this, 100);
        }
    };
    private final Runnable calculateCoordPrediction = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("mlinfo", "calculateCoordPrediction");
            if (connectedCar != null) {
                connectedCar.getMultiPrediction().calculatePredictionCoord();
            }
            mMainHandler.postDelayed(this, 100);
        }
    };
    private final Runnable calculateZonePrediction = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("mlinfo", "calculateZonePrediction");
            if (connectedCar != null) {
                connectedCar.getMultiPrediction().calculatePredictionZone();
            }
            mMainHandler.postDelayed(this, 400);
        }
    };
    private final Runnable updateCarLocalizationRunnable = new Runnable() {
        @Override
        public void run() {
            if (connectedCar != null) {
                // update ble trame
                CommandManager.getInstance().tryMachineLearningStrategies(mContext, connectedCar);
                // update car localization img
                updateCarLocalization(connectedCar.getMultiPrediction().getPredictionZone(SensorsManager.getInstance().isSmartphoneInPocket()),
                        connectedCar.getMultiPrediction().getPredictionRP(),
                        connectedCar.getMultiPrediction().getPredictionCoord(),
                        connectedCar.getMultiPrediction().getDist2Car());
            }
            mMainHandler.postDelayed(this, 400);
        }
    };
    //    private int reconnectionCounter = 0;
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
            mMainHandler.postDelayed(this, delayedTime);
        }
    };
    private boolean alreadyStopped = false;
    private boolean isLoggable = true;
    private final Runnable logRunner = new Runnable() {
        @Override
        public void run() {
            if (isLoggable) {
                LogFileManager.getInstance().appendRssiLogs(connectedCar, newLockStatus,
                        bleRangingListener.getMeasureCounterByte(), beepInt);
                beepInt = 0;
            }
            mMainHandler.postDelayed(this, 100);
        }
    };
    private boolean dataReceived = false;
    private final Runnable printRunner = new Runnable() {
        @Override
        public void run() {
            lock.readLock().lock();
            final SpannedString spannedString =
                    (SpannedString) android.text.TextUtils.concat(
                            createHeaderDebugData(bytesToSend, bytesReceived,
                                    isFullyConnected()),
                            createFirstFooterDebugData(connectedCar),
                            SensorsManager.getInstance().createSensorsDebugData());
            lock.readLock().unlock();
            debugListener.printDebugInfo(spannedString);
            bleRangingListener.updateBLEStatus();
            mMainHandler.postDelayed(this, 100);
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
                    PSALogs.i("restartConnection", "received counter packet have not changed in a second");
                    restartConnection();
                } else if (Byte.valueOf(bytesReceived[bytesReceived.length - 1]).equals((byte) 0xFF)) {
                    lock.readLock().unlock();
                    PSALogs.w("NIH", "TWO_CONSECUTIVES_FF_PACKETS_RECEIVED");
                    PSALogs.i("restartConnection", "received FF packet have not changed in a second");
                    restartConnection();
                } else {
                    lastPacketIdNumber[0] = bytesReceived[0];
                    lastPacketIdNumber[1] = bytesReceived[1];
                    lock.readLock().unlock();
                    if (isFullyConnected()) {
                        mMainHandler.postDelayed(this, 6000);
                    }
                }
            } else {
                PSALogs.w("NIH", "PACKETS_RECEIVED_ARE_NULL");
                PSALogs.i("restartConnection", "received packet is null");
                restartConnection();
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
                lock.writeLock().lock();
                bytesReceived = mBluetoothManager.getBytesReceived();
                lock.writeLock().unlock();
                boolean oldLockStatus = newLockStatus;
                if (bytesReceived != null) {
                    dataReceived = true;
                    lock.readLock().lock();
                    newLockStatus = (bytesReceived[5] & 0x01) != 0;
                    saveCarData();
                    lock.readLock().unlock();
                }
                if (oldLockStatus != newLockStatus) {
                    bleRangingListener.updateCarDoorStatus(newLockStatus);
                }
                InblueProtocolManager.getInstance().getPacketOne().setIsLockedFromTrx(newLockStatus);
                runFirstConnection(newLockStatus);
                bleRangingListener.updateBLEStatus();
                if (checkNewPacketOnlyOneLaunch) {
                    checkNewPacketOnlyOneLaunch = false;
                    mMainHandler.postDelayed(checkNewPacketsRunner, 1000);
                }
            } else if (BluetoothLeService.ACTION_GATT_CHARACTERISTIC_SUBSCRIBED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_CHARACTERISTIC_SUBSCRIBED");
                mMainHandler.post(sendPacketRunner); // send works only after subscribed
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_SERVICES_DISCONNECTED");
                bleRangingListener.updateBLEStatus();
                PSALogs.i("restartConnection", "after being disconnected");
                resetParams();
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.startLeScan();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTION_LOSS.equals(action)) {
                PSALogs.w("NIH", "ACTION_GATT_CONNECTION_LOSS");
                bleRangingListener.updateBLEStatus();
                PSALogs.i("restartConnection", "after connection loss");
                resetParams();
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.startLeScan();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_FAILED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_SERVICES_FAILED");
                isRestartAuthorized = true;
                resetParams();
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.startLeScan();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_SERVICES_DISCOVERED");
                bleRangingListener.updateBLEStatus();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                PSALogs.d("NIH", "TRX ACTION_GATT_CONNECTED");
                bleRangingListener.updateBLEStatus();
                mBluetoothManager.startLeScan();
            }
        }
    };

    public BleRangingHelper(Context context, ChessBoardListener chessBoardListener,
                            DebugListener debugListener,
                            SpinnerListener accuracyListener, TestListener testListener) {
        this.mContext = context;
        askForPermissions();
        this.mBluetoothManager = new BluetoothManagement(context);
        this.bleRangingListener = (BleRangingListener) context;
        this.chessBoardListener = chessBoardListener;
        this.debugListener = debugListener;
        this.spinnerListener = accuracyListener;
        this.testListener = testListener;
        InblueProtocolManager.initializeInstance(context);
        this.mMainHandler = new Handler(Looper.getMainLooper());
        SensorsManager.initializeInstance(mContext, bleRangingListener);
        CommandManager.initializeInstance(mContext, bleRangingListener);
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
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        catchBeaconScanResponse(device, rssi, beaconScanResponse, advertisedData);
                    }
                });
            }
        });
        mBluetoothManager.startLeScan();
    }

    private void saveCarData() {
        if (bytesReceived.length >= 7) {
            final int infoType = bytesReceived[6] & 0xF0;
            final int receivedTrxNumber = bytesReceived[6] & 0x0F;
            if (infoType == 0x00) {
                final byte[] address = Arrays.copyOfRange(bytesReceived, 7, 13);
                connectedCar.getMultiTrx().saveCarAddress(receivedTrxNumber,
                        TextUtils.printAddressBytes(address));
                SdkPreferencesHelper.getInstance().setTrxAddress(receivedTrxNumber, TextUtils.printAddressBytes(address));
            } else if (infoType == 0x10) {
                connectedCar.getMultiTrx().saveCarRssi(receivedTrxNumber, bytesReceived[12]);
                SdkPreferencesHelper.getInstance().setTrxCarRssi(receivedTrxNumber, bytesReceived[12]);
            }
        }
    }

    /**
     * Connect the smartphone to a computer
     */
    private void connectToPC() {
        mBluetoothManager.connectToPC(SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC());
    }

    /**
     * Connect the smartphone to a remote control
     */
    private void connectToRemoteControl() {
        mBluetoothManager.connectToRemoteControl(SdkPreferencesHelper.getInstance().getTrxAddressConnectableRemoteControl());
    }

    /**
     * Connect the smartphone to the vehicle
     */
    private void connect() {
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
        mBluetoothManager.stopLeScan();
        mBluetoothManager.startLeScan();
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
     * Initialize the connected car.
     * Call this method in onResume.
     */
    public void initializeConnectedCar() {
        if (!lastConnectedCarType.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getConnectedCarType())
                || !lastOpeningOrientation.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getOpeningStrategy())
                || !lastPrintRooftop.equals(SdkPreferencesHelper.getInstance().isPrintRooftopEnabled())
                || !lastMiniPredictionUsed.equals(SdkPreferencesHelper.getInstance().isMiniPredictionUsed())) {
            // if car type has changed,
            if (isFullyConnected()) {
                PSALogs.w("NIH", "INITIALIZED_NEW_CAR");
                // if connected, stop connection, and restart it
                PSALogs.i("restartConnection", "disconnect after changing car type");
                restartConnection();
            }
        }
        createConnectedCar(); // then create a new car
        // car type did not changed, but settings did
        enableSecurityWal();
        debugListener.updateCarDrawable(newLockStatus);
        chessBoardListener.applyNewDrawable();
        if (mMainHandler != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    PSALogs.d("init2", "readPredictionsRawFiles\n");
                    if (connectedCar != null) {
                        connectedCar.getMultiPrediction().readPredictionsRawFiles(mContext);
                    } else {
                        mMainHandler.postDelayed(this, 500);
                    }
                }
            });
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (connectedCar == null ||
                            connectedCar.getMultiTrx().getRssiForRangingPrediction() == null) {
                        mMainHandler.postDelayed(this, 500);
                    } else if (connectedCar != null && connectedCar.isInitialized()) {
                        PSALogs.d("init2", "initPredictions\n");
                        connectedCar.initPredictions();
                        startRunners();
                        spinnerListener.updateAccuracySpinner();
                    } else {
                        mMainHandler.postDelayed(this, 500);
                    }
                }
            });
        }
    }

    public boolean getLockStatus() {
        return newLockStatus;
    }

    /**
     * Stops predictions runner when start is requested to avoid concurrent predictions
     *
     * @param isStartRequested true if start is requested, false otherwise
     */
    public void setIsStartRequested(boolean isStartRequested) {
        if (mMainHandler != null) {
            if (isStartRequested) {
                mMainHandler.removeCallbacks(updateCarLocalizationRunnable);
            } else {
                mMainHandler.post(updateCarLocalizationRunnable);
            }
            InblueProtocolManager.getInstance().getPacketOne().setIsStartRequested(isStartRequested);
        }
    }

    public boolean isStartRequested() {
        return InblueProtocolManager.getInstance().getPacketOne().isStartRequested();
    }

    /**
     * Get the connection status between the smartphone and the car
     *
     * @return true if the smartphone is connected to the car, false otherwise
     */
    public boolean isFullyConnected() {
        return mBluetoothManager != null && mBluetoothManager.isLinked() && dataReceived;
    }

    /**
     * Get the connection status between the smartphone and the car
     *
     * @return true if the smartphone is connecting to the car, false otherwise
     */
    public boolean isConnecting() {
        return isTryingToConnect || mBluetoothManager.isConnecting();
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
        try {
            if (LogFileManager.getInstance().createDir(Environment.getExternalStorageDirectory(), RSSI_DIR)) {
                if (mContext.getExternalCacheDir() != null) {
                    TextUtils.copyFile(mContext.getExternalCacheDir().getAbsolutePath()
                                    + SdkPreferencesHelper.getInstance().getLogFileName(),
                            Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + SdkPreferencesHelper.getInstance().getLogFileName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // on settings changes, increase the file number used for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        CommandManager.getInstance().closeApp(mContext);
        stopRunners();
        mBluetoothManager.stopLeScan();
        if (mBluetoothManager.isLinked() && !mBluetoothManager.isConnecting()) {
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

    private synchronized void stopRunners() {
        if (mMainHandler != null) {
            PSALogs.d("NIH", "stopRunners");
            mMainHandler.removeCallbacks(printRunner);
            mMainHandler.removeCallbacks(logRunner);
            mMainHandler.removeCallbacks(setRssiForRangingPrediction);
            mMainHandler.removeCallbacks(calculateZonePrediction);
            mMainHandler.removeCallbacks(calculateCoordPrediction);
            mMainHandler.removeCallbacks(updateCarLocalizationRunnable);
            mMainHandler.removeCallbacks(beepRunner);
            mMainHandler.removeCallbacks(sendPacketRunner);
            mMainHandler.removeCallbacks(checkNewPacketsRunner);
            mMainHandler.removeCallbacks(null);
            canStartRunner = true;
        }
    }

    private synchronized void startRunners() {
        if (mMainHandler != null && canStartRunner) {
            PSALogs.d("NIH", "startRunners");
            mMainHandler.post(printRunner);
            mMainHandler.post(logRunner);
            mMainHandler.post(setRssiForRangingPrediction);
            mMainHandler.post(calculateZonePrediction);
            mMainHandler.post(calculateCoordPrediction);
            mMainHandler.post(updateCarLocalizationRunnable);
            mMainHandler.post(beepRunner);
            canStartRunner = false;
        }
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    public synchronized void restartConnection() {
        if (!mBluetoothManager.isConnecting()) {
            PSALogs.d("NIH", "restartConnection");
            stopRunners();
            mBluetoothManager.stopLeScan();
            mBluetoothManager.disconnect();
            resetParams();
            bleRangingListener.updateBLEStatus();
            mBluetoothManager.startLeScan();
            startRunners();
        }
    }

    private void resetParams() {
        InblueProtocolManager.getInstance().getPacketOne().restartPacketOneCounter();
        CommandManager.getInstance().setRearmWelcome(true);
        dataReceived = false;
        isFirstConnection = true;
        checkNewPacketOnlyOneLaunch = true;
        lastPacketIdNumber[0] = 0;
        lastPacketIdNumber[1] = 0;
        resetByteArray(bytesToSend);
        resetByteArray(bytesReceived);
        makeNoise(mContext, mMainHandler, ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 100);
    }

    /**
     * Reset a byte array
     *
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
                    if (!isTryingToConnect && !isFullyConnected() && !mBluetoothManager.isConnecting()) {
                        isTryingToConnect = true;
                        mBluetoothManager.stopLeScan();
                        mHandlerTimeOut.postDelayed(mManageIsTryingToConnectTimer, 3000);
                        PSALogs.w("NIH", "************************************** isTryingToConnect TRUE ************************************************");
                        newLockStatus = (centralScanResponse.vehicleState & 0x01) != 0; // get lock status for initialization later
                        connect();
                    } else {
                        PSALogs.w("NIH", "already trying to connect \nisTryingToConnect:" + isTryingToConnect + " isFullyConnected:" + isFullyConnected() + " isConnecting:" + mBluetoothManager.isConnecting());
                    }
                } else {
                    PSALogs.w("NIH", "BEACON " + device.getAddress());
                }
            } else if (isFullyConnected() && connectedCar != null) {
                int trxNumber = JsonUtils.getTrxNumber(connectedCar.getRegPlate(), device.getAddress());
                if (trxNumber == -1) {
                    if (SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress())) {
                        PSALogs.d("NIH", "connectable is connected but adv again (central)");
                        if (isRestartAuthorized) { // prevent from restarting connection when a restart is already in progress
                            isRestartAuthorized = false;
                            restartConnection();
                        } else {
                            isRestartAuthorized = true;
                        }
                    }
                }
            } else if (mBluetoothManager != null && mBluetoothManager.isLinked()) {
                PSALogs.d("NIH", "connectable isLinked but adv again (central)");
                resetParams();
            }
        }
    }

    /**
     * Save all trx rssi
     *
     * @param device             the trx that send the beaconScanResponse
     * @param rssi               the rssi of the beaconScanResponse
     * @param beaconScanResponse the beaconScanResponse received
     */
    private void catchBeaconScanResponse(final BluetoothDevice device, int rssi, BeaconScanResponse beaconScanResponse, byte[] advertisedData) {
        if (device != null && beaconScanResponse != null && connectedCar != null) {
            int trxNumber = JsonUtils.getTrxNumber(connectedCar.getRegPlate(), device.getAddress());
            if (trxNumber != -1) {
                final Antenna.BLEChannel receivedBleChannel = InblueProtocolManager.getInstance().getCurrentChannel(beaconScanResponse);
                if (SdkPreferencesHelper.getInstance().isChannelLimited()) {
                    if (alreadyStopped) {
                        PSALogs.d("bleChannel2 ", "not 37, do not parse scanResponse");
                        return;
                    }
                    if (!receivedBleChannel.equals(Antenna.BLEChannel.BLE_CHANNEL_37)) { // if ble channel equals to 38, 39, unknown channel
                        alreadyStopped = true;
                        mBluetoothManager.stopLeScan();
                        PSALogs.d("bleChannel2 ", "not 37, stop scan");
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                alreadyStopped = false;
                                mBluetoothManager.startLeScan();
                                PSALogs.d("bleChannel2 ", "not 37, restart scan after being stopped");
                            }
                        }, 200);
                        return;
                    }
                }
                // save rssi even when channel is limited
                connectedCar.getMultiTrx().saveRssi(trxNumber, rssi, (byte) 0, receivedBleChannel); //TODO antennaId
                PSALogs.d("bleChannel " + trxNumber, "channel " + connectedCar.getMultiTrx().getCurrentBLEChannel(trxNumber) + " " + beaconScanResponse.getAdvertisingChannel());
            } else {
                if (SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress())) {
                    PSALogs.i("NIH", "restartConnection => connectable is advertising again (beacon)");
                    return;
                } else if ((SdkPreferencesHelper.getInstance().getTrxAddressConnectablePC().equals(device.getAddress()))
                        && (!mBluetoothManager.isFullyConnected2() && !mBluetoothManager.isConnecting2())) { // connect to pc
                    bleRangingListener.showSnackBar("connect to PC " + device.getAddress());
                    PSALogs.i("NIH_PC", "connect to address PC : " + device.getAddress());
                    connectToPC();
                    return;
                } else if (isFullyConnected() && !SdkPreferencesHelper.getInstance().getTrxAddressConnectable().equalsIgnoreCase(device.getAddress()) &&
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
                    InblueProtocolManager.getInstance().getPacketLog().getAdvertisedBytes(advertisedData);
                } else {
                    PSALogs.d("NIH", "newConnectable coz advertising is null " + device.getAddress());
                }
            }
        }
    }

    /**
     * Update the mini map with our location around the car
     */
    private void updateCarLocalization(String predictionPosition,
                                       String predictionProximity, List<PointF> coords, List<Double> dists) {
        for (String elementPred : PREDICTIONS) {
            debugListener.darkenArea(elementPred);
        }
        //THATCHAM
        if (InblueProtocolManager.getInstance().getPacketOne().isThatcham()) {
            debugListener.lightUpArea(PREDICTION_THATCHAM);
        }
        // WELCOME
        if (CommandManager.getInstance().isInWelcomeArea()) {
            debugListener.lightUpArea(PREDICTION_WELCOME);
        }
        if (predictionPosition != null && !predictionPosition.isEmpty()) {
            debugListener.lightUpArea(predictionPosition);
        }
        // REMOTE PARKING
        if (predictionProximity != null && !predictionProximity.isEmpty()) {
            debugListener.lightUpArea(predictionProximity);
        }
        debugListener.applyNewDrawable();
        chessBoardListener.updateChessboard(coords, dists);
        testListener.changeColor(connectedCar.getMultiPrediction().getPredictionPositionTest());
    }

    /**
     * Initialize Trx and antenna then launch IHM looper and antenna active check loop
     *
     * @param newLockStatus the lock status
     */
    private void runFirstConnection(final boolean newLockStatus) {
        if (isFirstConnection && mBluetoothManager.isLinked()) {
            isFirstConnection = false;
            PSALogs.w(" rssiHistorics", "*********************************** runFirstConnection ************************************************");
            bleRangingListener.updateCarDoorStatus(newLockStatus);
            InblueProtocolManager.getInstance().getPacketOne().setIsLockedToSend(newLockStatus);
            CommandManager.getInstance().setLastCommandFromTrx(newLockStatus);
        }
    }

    private void createConnectedCar() {
        connectedCar = null;
        lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        lastOpeningOrientation = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        lastPrintRooftop = SdkPreferencesHelper.getInstance().isPrintRooftopEnabled();
        lastMiniPredictionUsed = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        connectedCar = ConnectedCarFactory.getConnectedCar(mContext, lastConnectedCarType);
        if (connectedCar == null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "ConnectedCar is NULL", Toast.LENGTH_SHORT).show();
                    PSALogs.d("init2", "ConnectedCar is NULL\n");
                }
            });
        }
        PSALogs.d("init2", "createConnectedCar\n");
    }

    private void enableSecurityWal() {
        String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
        InblueProtocolManager.getInstance().getPacketOne().setCarBase(connectedCarBase);
        if (connectedCarBase.equalsIgnoreCase(BASE_2)
                || connectedCarBase.equalsIgnoreCase(BASE_3)) {
            SdkPreferencesHelper.getInstance().setSecurityWALEnabled(true);
        }
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

    public void calculateAccuracy() {
        CommandManager.getInstance().enableAccuracyMeasure(true);
    }

    public Integer getCalculatedAccuracy(String zone) {
        CommandManager.getInstance().enableAccuracyMeasure(false);
        int result = CommandManager.getInstance().getSelectedAccuracy(zone);
        CommandManager.getInstance().clearAccuracyCounter();
        return result;
    }

    public String[] getStandardClasses() {
        if (connectedCar != null) {
            return connectedCar.getMultiPrediction().getStandardClasses();
        }
        return null;
    }

    public boolean isSmartphoneFrozen() {
        return SensorsManager.getInstance().isSmartphoneFrozen();
    }

    public void setSmartphoneOffset() {
        if (connectedCar != null) {
            SdkPreferencesHelper.getInstance().setOffsetSmartphone(
                    SdkPreferencesHelper.DEFAULT_CALIBRATION_TRUNK_RSSI
                            - connectedCar.getMultiTrx().getCurrentOriginalRssi(NUMBER_TRX_TRUNK));
            SdkPreferencesHelper.getInstance().setIsCalibrated(true);
        } else {
            PSALogs.e("setSmartphoneOffset", "connectedCar is NULL");
        }
    }

    public void setNewThreshold(double value) {
        connectedCar.getMultiPrediction().calculatePredictionTest(value);
    }

    public void setRegPlate(final String regPlate) {
        connectedCar.setRegPlate(regPlate);
    }

    public boolean isRKEButtonClickable() {
        return CommandManager.getInstance().isRKEButtonClickable(isFullyConnected());
    }

    public void performRKELockAction(boolean b) {
        CommandManager.getInstance().performRKELockAction(b, isFullyConnected());
    }
}
