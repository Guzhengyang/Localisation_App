package com.valeo.bleranging.bluetooth;

import android.os.CountDownTimer;
import android.os.Handler;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.PSALogs;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 16/01/2017
 */

public class RKEManager {
    public final static int RKE_USE_TIMEOUT = 5000;
    public final static int LOCK_STATUS_CHANGED_TIMEOUT = 5000;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final Handler mHandlerLockTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Handler mLockStatusChangedHandler;
    private final AtomicBoolean rearmLock = new AtomicBoolean(true);
    private final AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    private final AtomicBoolean isRKE = new AtomicBoolean(false);
    /* Avoid multiple click on rke buttons */
    private final AtomicBoolean isRKEAvailable = new AtomicBoolean(true);
    private final Runnable toggleOnIsRKEAvailable = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("performLock", "before isRKEAvailable =" + isRKEAvailable.get());
            isRKEAvailable.set(true);
            PSALogs.d("performLock", "after isRKEAvailable =" + isRKEAvailable.get());
        }
    };
    /* Avoid concurrent lock action from rke and strategy loop */
    private final AtomicBoolean areLockActionsAvailable = new AtomicBoolean(true);
    private final Runnable mManageIsLockStatusChangedPeriodicTimer = new Runnable() {
        @Override
        public void run() {
            areLockActionsAvailable.set(true);
        }
    };
    private boolean isAbortRunning = false;
    private final Runnable abortCommandRunner = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("performLock", "abortCommandRunner trx: " + mProtocolManager.isLockedFromTrx() + " me: " + mProtocolManager.isLockedToSend());
            if (mProtocolManager.isLockedFromTrx() != mProtocolManager.isLockedToSend()) { // if command from trx and app are different, make the app send what the trx sent
                mProtocolManager.setIsLockedToSend(mProtocolManager.isLockedFromTrx());
                bleRangingListener.updateCarDoorStatus(mProtocolManager.isLockedFromTrx());
                rearmLock.set(false);
            }
            isAbortRunning = false;
        }
    };

    public RKEManager(InblueProtocolManager mProtocolManager, BleRangingListener bleRangingListener) {
        this.mProtocolManager = mProtocolManager;
        this.bleRangingListener = bleRangingListener;
        this.mHandlerLockTimeOut = new Handler();
        this.mHandlerCryptoTimeOut = new Handler();
        this.mLockStatusChangedHandler = new Handler();
    }

    public void lockStatusChanged() {
        //Initialize timeout flag which is cleared in the runnable launched in the next instruction
        areLockActionsAvailable.set(false);
        //Launch timeout
        mLockStatusChangedHandler.postDelayed(mManageIsLockStatusChangedPeriodicTimer, LOCK_STATUS_CHANGED_TIMEOUT);
    }

    private void rkeStatusChanged() {
        //Initialize timeout flag which is cleared in the runnable launched in the next instruction
        isRKEAvailable.set(false);
        //Launch timeout
        mHandlerCryptoTimeOut.postDelayed(toggleOnIsRKEAvailable, RKE_USE_TIMEOUT);
    }

    public boolean isRKEButtonAvailable() {
        return (isRKEAvailable.get() && areLockActionsAvailable.get());
    }

    /**
     * Perform RKE action
     *
     * @param isRKEAction true if rke, false otherwise
     * @param lockCar     the wanted car lock status, true to lock, false to open
     */
    public void performMultipleLockWithCryptoTimeout(final boolean isRKEAction, final boolean lockCar) {
        // Send command several times in case it got lost
        new CountDownTimer(200, 50) {
            public void onTick(long millisUntilFinished) {
                performLockWithCryptoTimeout(isRKEAction, lockCar);
            }

            public void onFinish() {
                //Toast.makeText(mContext, "All safety close command are sent !", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    /**
     * Perform lock action with crypto time out
     *
     * @param isRKEAction true if rke action, false otherwise
     * @param lockCar     true to lock the car, false to open it
     */
    public void performLockWithCryptoTimeout(final boolean isRKEAction, final boolean lockCar) {
        if (isRKEAvailable.get() && areLockActionsAvailable.get()) {
            mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRKEAction) {
                        rkeStatusChanged();
                    }
                    isRKE.set(isRKEAction);
                    if (lockCar && rearmLock.get()) {
                        performLockVehicleRequest(true);
                    } else if (!lockCar && rearmUnlock.get()) {
                        performLockVehicleRequest(false);
                    }
                }
            }, (long) (SdkPreferencesHelper.getInstance().getCryptoActionTimeout() * 1000));
        }
    }

    /**
     * Send the lock / unlock request to the vehicle.
     *
     * @param lockVehicle true to lock the vehicle, false to unlock it.
     */
    private void performLockVehicleRequest(final boolean lockVehicle) {
        boolean lastLockCommand = mProtocolManager.isLockedToSend();
        PSALogs.d("performLock", "lastCommand=" + lastLockCommand
                + ", lockVehicle=" + lockVehicle);
        if (lastLockCommand != lockVehicle) { // if previous command is different that current one !!!!
            mProtocolManager.setIsLockedToSend(lockVehicle);
            PSALogs.d("performLock", "lockToSend=" + mProtocolManager.isLockedToSend()
                    + ", isLockedFromTrx=" + mProtocolManager.isLockedFromTrx());
            // Only if previous and current command are different, or it will always be called
            if (mProtocolManager.isLockedFromTrx() != mProtocolManager.isLockedToSend()) { // if command sent and command received are different
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

    public void manageRearms(final boolean newVehicleLockStatus) {
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

    public void closeApp() {
        mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        mHandlerCryptoTimeOut.removeCallbacks(toggleOnIsRKEAvailable);
        mHandlerCryptoTimeOut.removeCallbacks(abortCommandRunner);
    }

    public boolean getRearmLock() {
        return rearmLock.get();
    }

    public void setRearmLock(boolean newValue) {
        rearmLock.set(newValue);
    }

    public boolean getRearmUnlock() {
        return rearmUnlock.get();
    }

    public void setRearmUnlock(boolean newValue) {
        rearmUnlock.set(newValue);
    }

    public boolean getIsRKE() {
        return isRKE.get();
    }

    public boolean areLockActionsAvailable() {
        return areLockActionsAvailable.get();
    }

    public void toggleOffIsRKE() {
        if (isRKE.get()) {
            isRKE.set(false);
        }
    }
}
