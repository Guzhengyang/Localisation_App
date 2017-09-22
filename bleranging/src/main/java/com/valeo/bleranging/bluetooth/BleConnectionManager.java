package com.valeo.bleranging.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.bluetooth.protocol.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.scanresponse.BeaconScanResponse;
import com.valeo.bleranging.bluetooth.scanresponse.CentralScanResponse;
import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.managers.CommandManager;
import com.valeo.bleranging.managers.RunnerManager;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.JsonUtils;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.TextUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.valeo.bleranging.BleRangingHelper.connectedCar;

/**
 * Created by l-avaratha on 13/09/2017
 */

public class BleConnectionManager {
    /**
     * Single manager instance.
     */
    private static BleConnectionManager sSingleInstance = null;
    /**
     * Lock to assure that we do not read the bytes received when they are modified by the new ones
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * Last byte packet to compare with the new one and check if we are still connected
     */
    private final byte[] lastPacketIdNumber = new byte[2];
    /**
     * Ble manager
     */
    private final BluetoothManagement mBluetoothManager;
    /**
     * ble listener to update ble status, car door status or remote connection status
     */
    private final BleRangingListener bleRangingListener;
    /**
     * Trying to connect handler
     */
    private final Handler mHandlerTimeOut = new Handler();
    /**
     * Simulates crypto delay handler
     */
    private final Handler mHandlerCryptoTimeOut = new Handler();
    /**
     * Handler to send packet, check them, and stop if not channel 37 ble scan
     */
    private final Handler mMainHandler = new Handler();
    /**
     * Allows only one launch of packet checker runnable
     */
    private boolean checkNewPacketOnlyOneLaunch = true;
    /**
     * Allows a connection restart if they is not one already in progress
     */
    private boolean isRestartAuthorized = true;
    /**
     * Bytes to send to the car
     */
    private byte[] bytesToSend;
    /**
     * Bytes received from the car trx
     */
    private byte[] bytesReceived;
    /**
     * Runnable to create a packet and send it to the car
     */
    public final Runnable sendPacketRunner = new Runnable() {
        @Override
        public void run() {
            // Get the write lock and create packet one payload to send
            lock.writeLock().lock();
            bytesToSend = InblueProtocolManager.getInstance().getPacketOne().getPacketOnePayload(connectedCar);
            lock.writeLock().unlock();
            // Get the read lock and send the packets
            lock.readLock().lock();
            mBluetoothManager.sendPackets(bytesToSend, bytesReceived,
                    InblueProtocolManager.getInstance().getPacketTwo().getPacketTwoPayload(connectedCar.getMultiPrediction().getStandardClasses(),
                            connectedCar.getMultiPrediction().getStandardDistribution()),
                    InblueProtocolManager.getInstance().getPacketThree().getPacketThreePayload(connectedCar.getMultiPrediction().getStandardRssi()),
                    InblueProtocolManager.getInstance().getPacketFour().getPacketFourPayload(connectedCar.getMultiPrediction().getPredictionCoord(), connectedCar.getMultiPrediction().getDist2Car()));
            lock.readLock().unlock();
            // Reset the byte isRKE from packet if it was on
            if (CommandManager.getInstance().getIsRKE()) {
                CommandManager.getInstance().setIsRKE(false);
            }
            // Restart the loop if the connection is made, even if nothing has yet been received
            if (mBluetoothManager.isLinked()) {
                mMainHandler.postDelayed(this, 200);
            }
        }
    };
    /**
     * The lock status sent by the car
     */
    private boolean newLockStatus;
    /**
     * Avoid multiple simultaneous connection try
     */
    private boolean isTryingToConnect = false;
    /**
     * Runnable that reset the simultaneous connection try lock
     */
    private final Runnable mManageIsTryingToConnectTimer = new Runnable() {
        @Override
        public void run() {
            PSALogs.w("NIH", "************************************** isTryingToConnect FALSE ************************************************");
            isTryingToConnect = false;
        }
    };
    /**
     * Flag that represent if bytes have been received
     */
    private boolean dataReceived = false;
    /**
     * Boolean that avoid multiple runFirstConnection call
     */
    private boolean isFirstConnection = true;
    /**
     * Runner that check if the connection is still going on (if packet are different from the last one)
     * and stop it if they are not
     */
    public final Runnable checkNewPacketsRunner = new Runnable() {
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
                    PSALogs.w("NIH", "TWO_CONSECUTIVE_FF_PACKETS_RECEIVED");
                    PSALogs.i("restartConnection", "received FF packet have not changed in a second");
                    restartConnection();
                } else {
                    // Different packet, save it to compare with next one
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
     * ACTION_GATT_DISCONNECTED: Update ble status to not connected, restart scan to initiate new connection
     * ACTION_GATT_CONNECTION_LOSS: Update ble status to not connected, restart scan to initiate new connection
     * ACTION_GATT_SERVICES_FAILED: Update ble status to not connected, restart scan to initiate new connection
     * ACTION_GATT_SERVICES_DISCOVERED: update ble status
     * ACTION_GATT_CONNECTED: start scan after connection success
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
    /**
     * Ignore ble packet if they are not from channel 37
     */
    private boolean alreadyStopped = false;

    /**
     * Private constructor.
     */
    private BleConnectionManager(Context context, BleRangingListener bleRangingListener) {
        this.mBluetoothManager = new BluetoothManagement(context);
        this.bleRangingListener = bleRangingListener;
        final BluetoothManagementListener bleManagementListener = new BluetoothManagementListener() {
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
        };
        this.mBluetoothManager.addBluetoothManagementListener(bleManagementListener);
        this.mBluetoothManager.startLeScan();
    }

    /**
     * Initialize the manager instance.
     */
    public static void initializeInstance(Context context, BleRangingListener bleRangingListener) {
        if (sSingleInstance == null) {
            sSingleInstance = new BleConnectionManager(context.getApplicationContext(), bleRangingListener);
        }
    }

    /**
     * @return the single manager instance.
     */
    public static BleConnectionManager getInstance() {
        return sSingleInstance;
    }

    /**
     * Suspend scan, stop all loops, reinit all variables, then resume scan to be able to reconnect
     */
    public synchronized void restartConnection() {
        if (!mBluetoothManager.isConnecting()) {
            PSALogs.d("NIH", "restartConnection");
            RunnerManager.getInstance().stopRunners();
            mBluetoothManager.stopLeScan();
            mBluetoothManager.disconnect();
            resetParams();
            bleRangingListener.updateBLEStatus();
            mBluetoothManager.startLeScan();
            RunnerManager.getInstance().startRunners();
        }
    }

    /**
     * Reinit all variables to be able to connect like the first time
     */
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
     * Save the address and rssi sent by the car on first connection
     */
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

    /**
     * Stops the ble scan then start it again
     */
    public void relaunchScan() {
        mBluetoothManager.stopLeScan();
        mBluetoothManager.startLeScan();
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

    /**
     * Get trx advertise, then connect to it, or kill current connection and restart connection
     *
     * @param device              the trx that send the centralScanResponse
     * @param centralScanResponse the centralScanResponse received
     */
    private void catchCentralScanResponse(final BluetoothDevice device,
                                          CentralScanResponse centralScanResponse) {
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
    private void catchBeaconScanResponse(final BluetoothDevice device, int rssi,
                                         BeaconScanResponse beaconScanResponse, byte[] advertisedData) {
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
     * Get the bytes to send to the car
     *
     * @return the tab of byte to send
     */
    public byte[] getBytesToSend() {
        return bytesToSend;
    }

    /**
     * Get the bytes received from the car
     *
     * @return the received tab of byte
     */
    public byte[] getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Get the byte tab read/write lock
     *
     * @return the byte tabs lock
     */
    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    /**
     * Get the car lock status
     *
     * @return the car lock status
     */
    public boolean getLockStatus() {
        return newLockStatus;
    }

    /**
     * Toggle bluetooth
     *
     * @param enable turn on bluetooth if true, turn off otherwise
     */
    public void toggleBluetooth(boolean enable) {
        mBluetoothManager.setBluetooth(enable);
    }

    /**
     * Check if bluetooth is enabled
     *
     * @return true if it is enabled, false otherwise
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothManager.isBluetoothEnabled();
    }

    /**
     * Stop the ble scan and disconnect from the car
     */
    public void closeApp() {
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
    }
}
