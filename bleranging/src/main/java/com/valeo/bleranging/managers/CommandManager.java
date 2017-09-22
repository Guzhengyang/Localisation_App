package com.valeo.bleranging.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.bluetooth.protocol.InblueProtocolManager;
import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.BleRangingHelper.connectedCar;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_ACCESS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_BACK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FAR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FRONT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LEFT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LOCK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_NEAR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RIGHT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_ROOF;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_FL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_FR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_RL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_RR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_THATCHAM;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_TRUNK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_WELCOME;
import static com.valeo.bleranging.persistence.Constants.RKE_USE_TIMEOUT;
import static com.valeo.bleranging.utils.SoundUtils.makeNoise;

/**
 * Created by l-avaratha on 13/09/2017
 */

public final class CommandManager {
    /**
     * Delay before lock action are available again
     */
    private final static int LOCK_STATUS_CHANGED_TIMEOUT = 5000;
    /**
     * Single helper instance.
     */
    private static CommandManager sSingleInstance = null;
    private final BleRangingListener bleRangingListener;
    private final Handler mMainHandler = new Handler();
    /**
     * Lock action availability timer handler
     */
    private final Handler mLockStatusChangedHandler = new Handler();
    /**
     * Boolean used to prevent multiple thatcham time out
     */
    private final AtomicBoolean thatchamIsChanging = new AtomicBoolean(false);
    /**
     * False the boolean when thatcham has timed out
     */
    private final Runnable mHasThatchamChanged = new Runnable() {
        @Override
        public void run() {
            thatchamIsChanging.set(false);
        }
    };
    /**
     * Avoid multiple click on rke buttons
     */
    private final AtomicBoolean isRKEAvailable = new AtomicBoolean(true);
    /**
     * Avoid concurrent lock action from rke and strategy loop
     */
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
    /**
     * Previous thatcham value
     */
    private final AtomicBoolean lastThatcham = new AtomicBoolean(false);
    /**
     * Allows/disable welcome action
     */
    private final AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    /**
     * Allows/disable lock action
     */
    private final AtomicBoolean rearmLock = new AtomicBoolean(true);
    /**
     * Allows/disable unlock action
     */
    private final AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    /**
     * Command source of origin, passive entry or rke
     */
    private final AtomicBoolean isRKE = new AtomicBoolean(false);
    /**
     * Handler to manager lock time out
     */
    private final Handler mHandlerLockTimeOut = new Handler();
    /**
     * Handler to manage crypto time out
     */
    private final Handler mHandlerCryptoTimeOut = new Handler();
    /**
     * Handler to manage thatcham time out
     */
    private final Handler mHandlerThatchamTimeOut = new Handler();
    /**
     * Hash map to calculate the prediction accuracy
     */
    private final HashMap<String, Integer> accuracyCounterHMap = new HashMap<>();
    /**
     * Runnable to set back on rke availability
     */
    private final Runnable toggleOnIsRKEAvailable = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("performLock", "before isRKEAvailable =" + isRKEAvailable.get());
            isRKEAvailable.set(true);
            PSALogs.d("performLock", "after isRKEAvailable =" + isRKEAvailable.get());
        }
    };
    private boolean isInWelcomeArea = false;
    private boolean lastCommandFromTrx;
    private boolean lastThatchamChanged = false;
    private String lastPrediction = PREDICTION_UNKNOWN;
    private boolean accuracyMeasureEnabled = false;
    private boolean isAbortRunning = false;
    private final Runnable abortCommandRunner = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("performLock", "abortCommandRunner trx: " + InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx() + " me: " + InblueProtocolManager.getInstance().getPacketOne().isLockedToSend());
            if (InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx() != InblueProtocolManager.getInstance().getPacketOne().isLockedToSend()) { // if command from trx and app are different, make the app send what the trx sent
                InblueProtocolManager.getInstance().getPacketOne().setIsLockedToSend(InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx());
                bleRangingListener.updateCarDoorStatus(InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx());
                rearmLock.set(false);
            }
            isAbortRunning = false;
        }
    };
    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action)) {
                // if car lock status changed
                if (lastCommandFromTrx != InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx()) {
                    PSALogs.d("performLock a_data_a_2", "lastCommandFromTrx =" + lastCommandFromTrx +
                            ", isLockedFromTrx=" + InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx());
                    lastCommandFromTrx = InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx();
                    InblueProtocolManager.getInstance().getPacketOne().setIsLockedToSend(lastCommandFromTrx);
                    //Initialize timeout flag which is cleared in the runnable launched in the next instruction
                    areLockActionsAvailable.set(false);
                    //Launch timeout
                    mLockStatusChangedHandler.postDelayed(mManageIsLockStatusChangedPeriodicTimer, LOCK_STATUS_CHANGED_TIMEOUT);
                    manageRearms(lastCommandFromTrx);
                }
                // if car thatcham status changed
                if (lastThatcham.get() != InblueProtocolManager.getInstance().getPacketOne().isThatcham()) {
                    lastThatcham.set(InblueProtocolManager.getInstance().getPacketOne().isThatcham());
                    if (lastThatcham.get()) { // if in thatcham area, rearm lock
                        rearmLock.set(true);
                        lastThatchamChanged = false;
                    } else { // if not in thatcham area wait for being in lock area to rearm unlock
                        lastThatchamChanged = true; // because when thatcham changed, maybe not in lock area yet
                    }
                }
                if (lastThatchamChanged && lastPrediction.equalsIgnoreCase(PREDICTION_LOCK)) { // when thatcham has changed, and get into lock area
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rearmUnlock.set(true);
                        }
                    }, (long) SdkPreferencesHelper.getInstance().getUnlockTimeout());
                    lastThatchamChanged = false;
                    if (SdkPreferencesHelper.getInstance().getSecurityWALEnabled()) {
                        if (!InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx()) { // if the vehicle is unlocked, lock it
                            new CountDownTimer(600, 90) { // Send safety close command several times in case it got lost
                                public void onTick(long millisUntilFinished) {
                                    performLockWithCryptoTimeout(true, true);
                                }

                                public void onFinish() {
//                                Toast.makeText(mApplicationContext, "All safety close command are sent !", Toast.LENGTH_SHORT).show();
                                }
                            }.start();
                        }
                        // if not in thatcham area and in lock area, rearm unlock
                        makeNoise(context, mMainHandler, ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
                    }
                }
            }
        }
    };

    /**
     * Private constructor.
     */
    private CommandManager(final @NonNull Context context, BleRangingListener bleRangingListener) {
        this.bleRangingListener = bleRangingListener;
        context.registerReceiver(mDataReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE2));
    }

    /**
     * Initialize the helper instance.
     */
    public static void initializeInstance(final @NonNull Context context, BleRangingListener bleRangingListener) {
        if (sSingleInstance == null) {
            sSingleInstance = new CommandManager(context.getApplicationContext(), bleRangingListener);
        }
    }

    /**
     * @return the single helper instance.
     */
    public static CommandManager getInstance() {
        return sSingleInstance;
    }

    /**
     * Unregister the data broadcastReceiver and cancel pending lock status timer
     *
     * @param context the context
     */
    public void closeApp(final Context context) {
        context.unregisterReceiver(mDataReceiver);
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }

    /**
     * Perform a RKE lock or unlock action
     * @param lockCar true to send a lock action, false to send an unlock action
     * @param isFullyConnected true if connected, false otherwise
     */
    public void performRKELockAction(final boolean lockCar, final boolean isFullyConnected) {
        if (isRKEButtonClickable(isFullyConnected)) {
            isRKEAvailable.set(false);
            mHandlerCryptoTimeOut.postDelayed(toggleOnIsRKEAvailable, RKE_USE_TIMEOUT);
            // Send command several times in case it got lost
            new CountDownTimer(200, 50) {
                public void onTick(long millisUntilFinished) {
                    performLockWithCryptoTimeout(true, lockCar);
                }

                public void onFinish() {
                }
            }.start();
        }
    }

    /**
     * Verify if the user can click on rke button by checking if the action can succeed
     *
     * @param isFullyConnected true if connected, false otherwise
     * @return true if the rke button is ready, false otherwise
     */
    public boolean isRKEButtonClickable(boolean isFullyConnected) {
        return isFullyConnected && isRKEAvailable.get()
                && areLockActionsAvailable.get();
    }

    /**
     * Perform a lock action with a crypto timeout
     *
     * @param isRKEAction the action source, true if RKE, false otherwise
     * @param lockCar     true to lock the car, false to unlock
     */
    private void performLockWithCryptoTimeout(final boolean isRKEAction, final boolean lockCar) {
        mHandlerCryptoTimeOut.postDelayed(new Runnable() {
            @Override
            public void run() {
                PSALogs.d("performLock", "isRKEAction=" + isRKEAction + ", lockCar=" + lockCar);
                isRKE.set(isRKEAction);
                performLockVehicleRequest(lockCar);
            }
        }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
    }

    /**
     * Send the lock / unlock request to the vehicle.
     *
     * @param lockVehicle true to lock the vehicle, false to unlock it.
     */
    private void performLockVehicleRequest(final boolean lockVehicle) {
        boolean lastLockCommand = InblueProtocolManager.getInstance().getPacketOne().isLockedToSend();
        PSALogs.d("performLock", "lastCommand=" + lastLockCommand
                + ", lockVehicle=" + lockVehicle);
        if (lastLockCommand != lockVehicle) { // if previous command is different that current one !!!!
            InblueProtocolManager.getInstance().getPacketOne().setIsLockedToSend(lockVehicle);
            PSALogs.d("performLock", "lockToSend=" + InblueProtocolManager.getInstance().getPacketOne().isLockedToSend()
                    + ", isLockedFromTrx=" + InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx());
            // Only if previous and current command are different, or it will always be called
            if (InblueProtocolManager.getInstance().getPacketOne().isLockedFromTrx() != InblueProtocolManager.getInstance().getPacketOne().isLockedToSend()) { // if command sent and command received are different
                if (isAbortRunning) { // stop already running Runnable and start a new one
                    mHandlerLockTimeOut.removeCallbacks(abortCommandRunner);
                    mHandlerLockTimeOut.removeCallbacksAndMessages(null);
                    isAbortRunning = false;
                    PSALogs.d("performLock", "abortCommandRunner removeCallBacks");
                    mHandlerLockTimeOut.postDelayed(abortCommandRunner, 5000); // Relaunch Abort runnable
                    isAbortRunning = true;
                    PSALogs.d("performLock", "abortCommandRunner relaunched");
                } else {
                    mHandlerLockTimeOut.postDelayed(abortCommandRunner, 5000); // Launch Abort runnable only if lockVehicle has changed
                    isAbortRunning = true;
                    PSALogs.d("performLock", "abortCommandRunner launched");
                }
            }
        }
    }

    /**
     * Try all strategy based on machine learning
     */
    void tryMachineLearningStrategies() {
        PSALogs.d("ml_info", "tryMachineLearningStrategies");
        boolean isWelcomeAllowed = false;
        // Cancel previous requested actions
        boolean isInStartArea = false;
        boolean isInUnlockArea = false;
        boolean isInLockArea = false;
        InblueProtocolManager.getInstance().getPacketOne().setIsStartRequested(false);
        InblueProtocolManager.getInstance().getPacketOne().setIsWelcomeRequested(false);
        if (connectedCar != null) {
            // get a zone prediction
            lastPrediction = connectedCar.getMultiPrediction().getPredictionZone(SensorsManager.getInstance().isSmartphoneInPocket());
            // Based on this prediction, create the correct ble packet to send
            switch (lastPrediction) {
                case PREDICTION_INSIDE:
                    isInStartArea = true;
                    if (!InblueProtocolManager.getInstance().getPacketOne().isStartRequested()) {
                        InblueProtocolManager.getInstance().getPacketOne().setIsStartRequested(true);
                    }
                    break;
                case PREDICTION_OUTSIDE:
                    break;
                case PREDICTION_LOCK:
                case PREDICTION_EXTERNAL:
                    isInLockArea = true;
                    if (areLockActionsAvailable.get() && rearmLock.get() && SdkPreferencesHelper.getInstance().getSecurityWALEnabled()) {
                        performLockWithCryptoTimeout(false, true);
                    }
                    break;
                case PREDICTION_INTERNAL:
                case PREDICTION_START:
                case PREDICTION_START_FL:
                case PREDICTION_START_FR:
                case PREDICTION_START_RL:
                case PREDICTION_START_RR:
                case PREDICTION_TRUNK:
                    isInStartArea = true;
                    if (!InblueProtocolManager.getInstance().getPacketOne().isStartRequested()) {
                        InblueProtocolManager.getInstance().getPacketOne().setIsStartRequested(true);
                    }
                    break;
                case PREDICTION_ACCESS:
                case PREDICTION_BACK:
                case PREDICTION_RIGHT:
                case PREDICTION_LEFT:
                case PREDICTION_FRONT:
                    isInUnlockArea = true;
                    if (areLockActionsAvailable.get() && rearmUnlock.get()) {
                        performLockWithCryptoTimeout(false, false);
                    }
                    break;
                case PREDICTION_UNKNOWN:
                case PREDICTION_ROOF:
                case PREDICTION_WELCOME:
                case PREDICTION_THATCHAM:
                default:
                    PSALogs.d("prediction", "No rangingPredictionInt !");
                    break;
            }
            // check if welcome if available
            isInWelcomeArea = rearmWelcome.get() && connectedCar.getMultiPrediction().getPredictionRP().equals(PREDICTION_FAR);
            if (isInWelcomeArea) {
                isWelcomeAllowed = true;
                rearmWelcome.set(false);
                bleRangingListener.doWelcome();
            }
            if (InblueProtocolManager.getInstance().getPacketOne().isWelcomeRequested() != isWelcomeAllowed) {
                InblueProtocolManager.getInstance().getPacketOne().setIsWelcomeRequested(isWelcomeAllowed);
            }
            // check if thatcham is valid
            setIsThatcham(isInLockArea, isInUnlockArea, isInStartArea);
            // check if remote parking is valid
            if (connectedCar.getMultiPrediction().getPredictionRP().equalsIgnoreCase(PREDICTION_NEAR)) {
                InblueProtocolManager.getInstance().getPacketOne().setInRemoteParkingArea(true);
            } else {
                InblueProtocolManager.getInstance().getPacketOne().setInRemoteParkingArea(false);
            }
            // increment prediction occurrence to latter calculate accuracy
            if (accuracyMeasureEnabled) {
                if (accuracyCounterHMap.get(lastPrediction) == null) {
                    PSALogs.d("accuracy", lastPrediction + " was null");
                    accuracyCounterHMap.put(lastPrediction, 0);
                }
                accuracyCounterHMap.put(lastPrediction, accuracyCounterHMap.get(lastPrediction) + 1);
                for (Map.Entry<String, Integer> entry : accuracyCounterHMap.entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();
                    PSALogs.d("accuracy", "key = " + key + ", value = " + value);
                }
            }
        }
    }

    /**
     * Activate thatcham validity time out
     */
    private void launchThatchamValidityTimeOut() {
        InblueProtocolManager.getInstance().getPacketOne().setThatcham(true);
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
     * Check if we are thatcham by looking at the current zone
     * @param isInLockArea true if in lock area, false otherwise
     * @param isInUnlockArea true if in unlock area, false otherwise
     * @param isInStartArea true if in start area, false otherwise
     */
    private void setIsThatcham(boolean isInLockArea, boolean isInUnlockArea, boolean isInStartArea) {
        if (isInLockArea || isInStartArea || !isInUnlockArea) {
            if (!thatchamIsChanging.get()) { // if thatcham is not changing
                InblueProtocolManager.getInstance().getPacketOne().setThatcham(false);
            }
        } else if (isInUnlockArea) {
            launchThatchamValidityTimeOut();
        }
    }

    /**
     * Allow lock/unlock action by setting rearms based on your zone, and current lock status
     * @param newVehicleLockStatus true if lock, false if unlock
     */
    private void manageRearms(final boolean newVehicleLockStatus) {
        if (InblueProtocolManager.getInstance().getPacketOne().isThatcham()) {
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
     * Toggle accuracy measure
     * @param enable true to turn on, false to turn off
     */
    public void enableAccuracyMeasure(boolean enable) {
        accuracyMeasureEnabled = enable;
    }

    /**
     * Clear the accuracy hash map
     */
    public void clearAccuracyCounter() {
        accuracyCounterHMap.clear();
    }

    /**
     * Calculate the accuracy of a selected zone
     * @param selectedAccuracyZone the zone selected
     * @return the accuracy of the zone selected
     */
    public int getSelectedAccuracy(String selectedAccuracyZone) {
        float total = 0.0f;
        // calculate the total accuracy
        for (Integer totalCounter : accuracyCounterHMap.values()) {
            total += totalCounter;
        }
        // if not null, calculate the accuracy of the zone over all zones
        if (accuracyCounterHMap.get(selectedAccuracyZone) != null && total != 0) {
            return (int) ((accuracyCounterHMap.get(selectedAccuracyZone) / (1.0 * total)) * 100);
        } else {
            PSALogs.d("accuracy", "accuracyCounterHMap.get(" + selectedAccuracyZone + ") is null");
            return 0;
        }
    }

    /**
     * Get the availability of lock actions
     *
     * @return true if available, false otherwise
     */
    public boolean getAreLockActionsAvailable() {
        return areLockActionsAvailable.get();
    }

    /**
     * Get the welcome rearm value
     *
     * @return true if welcome can be displayed, false otherwise
     */
    public boolean getRearmWelcome() {
        return rearmWelcome.get();
    }

    /**
     * Set the welcome rearm value
     *
     * @param rearmWelcome true to rearm welcome, false otherwise
     */
    public void setRearmWelcome(boolean rearmWelcome) {
        this.rearmWelcome.set(rearmWelcome);
    }

    /**
     * Get the lock rearm value
     *
     * @return true if lock can be displayed, false otherwise
     */
    public boolean getRearmLock() {
        return rearmLock.get();
    }

    /**
     * Get the unlock rearm value
     *
     * @return true if unlock can be displayed, false otherwise
     */
    public boolean getRearmUnlock() {
        return rearmUnlock.get();
    }

    /**
     * Check if in welcome area
     * @return true if in welcome area, false otherwise
     */
    boolean isInWelcomeArea() {
        return isInWelcomeArea;
    }

    /**
     * Check if the command was rke or not
     * @return true if it was RKE, false otherwise
     */
    public boolean getIsRKE() {
        return isRKE.get();
    }

    /**
     * Set the isRke value
     *
     * @param isRKE true to make it rke, false otherwise
     */
    public void setIsRKE(boolean isRKE) {
        this.isRKE.set(isRKE);
    }

    /**
     * Save the last command/ lock status from car
     *
     * @param lastCommandFromTrx true if lock command, false if unlock command
     */
    public void setLastCommandFromTrx(boolean lastCommandFromTrx) {
        this.lastCommandFromTrx = lastCommandFromTrx;
    }
}