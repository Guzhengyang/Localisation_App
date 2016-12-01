package com.valeo.bleranging;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.widget.Toast;

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
    private final Context mContext;
    private final AlgoManager mAlgoManager;
    private final BluetoothManagement mBluetoothManager;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final byte[] lastPacketIdNumber = new byte[2];
    private final Handler mMainHandler;
    private final Handler mHandlerTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Runnable beepRunner = new Runnable() {
        @Override
        public void run() {
            long delayedTime = 500;
            if (SdkPreferencesHelper.getInstance().getUserSpeedEnabled()) {
                makeNoise(mContext, mMainHandler, ToneGenerator.TONE_CDMA_LOW_SS, 100);
                // interval time between each beep sound in milliseconds
                delayedTime = Math.round(((SdkPreferencesHelper.getInstance().getOneStepSize() / 100) / (SdkPreferencesHelper.getInstance().getWantedSpeed() / 3.6)) * 1000);
            }
            PSALogs.d("beep", "delayedTime " + delayedTime);
            mMainHandler.postDelayed(this, delayedTime);
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
//            PSALogs.d("Runner", "START sendPacket Runner");
//            PSALogs.d("Runner", "   Construct Packet: start");
            lock.writeLock().lock();
            bytesToSend = mProtocolManager.getPacketOnePayload(mAlgoManager);
            //isUnlockStrategyValid, isInUnlockArea, isStartStrategyValid, isInStartArea, isInLockArea);
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
            if (mAlgoManager.getIsRKE()) {
                mAlgoManager.setIsRKE(false);
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
    private final Runnable checkAntennaRunner = new Runnable() {
        @Override
        public void run() {
            if (isFullyConnected()) {
                PSALogs.w(" rssiHistorics", "************************************** CHECK ANTENNAS ************************************************");
                connectedCar.compareCheckerAndSetAntennaActive(mAlgoManager.isSmartphoneMovingSlowly());
            }
            mMainHandler.postDelayed(this, 2500);
        }
    };
    private final Runnable updateCarLocalizationRunnable = new Runnable() {
        @Override
        public void run() {
            if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(ALGO_STANDARD)) {
                mAlgoManager.tryStandardStrategies(newLockStatus, isFullyConnected(), isIndoor,
                        connectedCar, totalAverage);
            } else if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(MACHINE_LEARNING)) {
                mAlgoManager.tryMachineLearningStrategies(newLockStatus, connectedCar, totalAverage);
            }
            updateCarLocalization();
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
    private boolean isLoggable = true;
    private final Runnable logRunner = new Runnable() {
        @Override
        public void run() {
            if (isLoggable) {
                TrxUtils.appendRssiLogs(connectedCar.getCurrentModifiedRssi(NUMBER_TRX_LEFT),
                        connectedCar.getCurrentModifiedRssi(NUMBER_TRX_MIDDLE),
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
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_TRUNK),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT),
                        connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK),
                        mAlgoManager.getOrientation()[0], mAlgoManager.getOrientation()[1], mAlgoManager.getOrientation()[2],
                        mAlgoManager.isSmartphoneInPocket(), mAlgoManager.isSmartphoneMovingSlowly(), mAlgoManager.areLockActionsAvailable(),
                        mAlgoManager.isBlockStart(), mAlgoManager.isForcedStart(),
                        mAlgoManager.isBlockLock(), mAlgoManager.isForcedLock(),
                        mAlgoManager.isBlockUnlock(), mAlgoManager.isForcedUnlock(),
                        mAlgoManager.isSmartphoneFrozen(),
                        mAlgoManager.getRearmLock(), mAlgoManager.getRearmUnlock(), mAlgoManager.getRearmWelcome(), newLockStatus, welcomeByte,
                        lockByte, startByte, leftAreaByte, rightAreaByte, backAreaByte,
                        walkAwayByte, approachByte, leftTurnByte,
                        fullTurnByte, rightTurnByte, recordByte, mAlgoManager.getRangingPredictionInt(),
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
//                PSALogs.d("autoRelock", "newLockStatus =" + newLockStatus +
//                        ", isLockedFromTrx=" + mProtocolManager.isLockedFromTrx());
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

    public BleRangingHelper(Context context, BleRangingListener bleRangingListener) {
        this.mContext = context;
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
            if (mMainHandler != null) {
                mMainHandler.removeCallbacks(checkAntennaRunner);
                mMainHandler.removeCallbacks(logRunner);
                mMainHandler.removeCallbacks(sendPacketRunner);
                mMainHandler.removeCallbacks(checkNewPacketsRunner);
                mMainHandler.removeCallbacks(mAlgoManager.getFillPredictionArrayRunnable(connectedCar));
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

    public void performLockWithCryptoTimeout(final boolean isRke, final boolean lockCar) {
        new CountDownTimer(200, 50) { // Send command several times in case it got lost
            public void onTick(long millisUntilFinished) {
                PSALogs.d("performLock", "loop isRKE=" + isRke + ", lockCar=" + lockCar);
                mAlgoManager.performLockWithCryptoTimeout(isRke, lockCar);
            }

            public void onFinish() {
            }
        }.start();
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
                        restartConnection(false);
                        return;
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
                        PSALogs.i("restartConnection", "connectable is advertising again (beacon)");
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
        if (mAlgoManager.isInWelcomeArea()) {
            // WELCOME
            bleRangingListener.lightUpArea(WELCOME_AREA);
        }
        if (mAlgoManager.isInLockArea()) {
            // LOCK
            bleRangingListener.lightUpArea(LOCK_AREA);
        } else if (mAlgoManager.getIsStartStrategyValid() != null && mAlgoManager.isInStartArea()) {
            //START
            for (Integer integer : mAlgoManager.getIsStartStrategyValid()) {
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
        } else if (mAlgoManager.getIsUnlockStrategyValid() != null && mAlgoManager.isInUnlockArea()) { // if unlock forced, unlock Strategy may be null
            //UNLOCK
            for (Integer integer : mAlgoManager.getIsUnlockStrategyValid()) {
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
            mMainHandler.post(checkAntennaRunner);
            mMainHandler.post(logRunner);
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PSALogs.d("prediction", String.format(Locale.FRANCE, "%1d %2d %3d %4d",
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK)));
                    mAlgoManager.createRangingObject(
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT),
                            connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK));
                }
            }, 500);
            mMainHandler.postDelayed(mAlgoManager.getFillPredictionArrayRunnable(connectedCar), 600);
            mMainHandler.postDelayed(updateCarLocalizationRunnable, 1000);
            mMainHandler.postDelayed(beepRunner, 1000);
        }
    }

    public void initializeConnectedCar() {
        // Warning if ML and connected car NOT in type 4A
        if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(ConnectedCarFactory.MACHINE_LEARNING)
                && (!SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_A))) {
            Toast.makeText(mContext, "You should change car type to 4 beacons !", Toast.LENGTH_LONG).show();
        }
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

    private void createConnectedCar() {
        connectedCar = null;
        lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        connectedCar = ConnectedCarFactory.getConnectedCar(mContext, lastConnectedCarType, isIndoor);
        String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
        mProtocolManager.setCarBase(connectedCarBase);
        bleRangingListener.updateCarDrawable();
    }

    public boolean isFullyConnected() {
        return mBluetoothManager != null && mBluetoothManager.isFullyConnected();
    }

    public boolean areLockActionsAvailable() {
        return mAlgoManager.areLockActionsAvailable();
    }

    public boolean isRKEAvailable() {
        return mAlgoManager.isRKEAvailable();
    }

    public void setIsRKEAvailable(boolean enableRKE) {
        mAlgoManager.setIsRKEAvailable(enableRKE);
    }

    public void closeApp() {
        PSALogs.d("NIH", "closeApp()");
        // on settings changes, increase the file number used for logs files name
        SdkPreferencesHelper.getInstance().setRssiLogNumber(SdkPreferencesHelper.getInstance().getRssiLogNumber() + 1);
        mAlgoManager.closeApp();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(printRunner);
            mMainHandler.removeCallbacks(updateCarLocalizationRunnable);
            mMainHandler.removeCallbacks(beepRunner);
            mMainHandler.removeCallbacks(mAlgoManager.getFillPredictionArrayRunnable(connectedCar));
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
}
