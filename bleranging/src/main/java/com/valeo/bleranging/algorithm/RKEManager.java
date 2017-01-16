package com.valeo.bleranging.algorithm;

import android.os.Handler;

import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.PSALogs;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by l-avaratha on 16/01/2017
 */

public class RKEManager {
    public final static int LOCK_STATUS_CHANGED_TIMEOUT = 5000;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final Handler mHandlerLockTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final AtomicBoolean rearmLock = new AtomicBoolean(true);
    private final AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    private final AtomicBoolean isRKE = new AtomicBoolean(false);
    /* Avoid multiple click on rke buttons */
    private final AtomicBoolean isRKEAvailable = new AtomicBoolean(true);
    /* Avoid concurrent lock action from rke and strategy loop */
    private final AtomicBoolean areLockActionsAvailable = new AtomicBoolean(true);
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
    }

    public void performLockWithCryptoTimeout(final boolean isRKEAction, final boolean lockCar) {
        if (isRKEAvailable.get() && areLockActionsAvailable.get()) {
            mHandlerCryptoTimeOut.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PSALogs.d("performLock", "isRKEAction=" + isRKEAction + ", lockCar=" + lockCar);
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

    protected void manageRearms(final boolean newVehicleLockStatus) {
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


    public boolean areLockActionsAvailable() {
        return areLockActionsAvailable.get();
    }

    public void setLockActionsAvailable(boolean newValue) {
        areLockActionsAvailable.set(newValue);
    }

    public boolean isRKEAvailable() {
        return isRKEAvailable.get();
    }

    public void setIsRKEAvailable(boolean enableRKE) {
        isRKEAvailable.set(enableRKE);
    }

    public boolean getIsRKE() {
        return isRKE.get();
    }

    public void setIsRKE(boolean enableRKE) {
        isRKE.set(enableRKE);
    }
}
