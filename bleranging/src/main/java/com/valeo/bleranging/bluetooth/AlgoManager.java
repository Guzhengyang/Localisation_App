package com.valeo.bleranging.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.model.Ranging;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.CallReceiver;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.SoundUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_BACK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FRONT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_NEAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_THATCHAM;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_TRUNK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_WELCOME;
import static com.valeo.bleranging.utils.SoundUtils.makeNoise;

/**
 * Created by l-avaratha on 25/11/2016
 */
public class AlgoManager implements SensorEventListener {
    private final static int LOCK_STATUS_CHANGED_TIMEOUT = 5000;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final Context mContext;
    //    private final FaceDetectorUtils faceDetectorUtils;
    private final Handler mMainHandler;
    private final Handler mHandlerLockTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Handler mLockStatusChangedHandler;
    /* Avoid multiple click on rke buttons */
    private final AtomicBoolean isRKEAvailable = new AtomicBoolean(true);
    /* Avoid concurrent lock action from rke and strategy loop */
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
    private final AtomicBoolean lastThatcham = new AtomicBoolean(false);
    private final AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    private final AtomicBoolean rearmLock = new AtomicBoolean(true);
    private final AtomicBoolean rearmUnlock = new AtomicBoolean(true);
    private final AtomicBoolean isRKE = new AtomicBoolean(false);
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
    private Ranging ranging;
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
    private Integer rangingPredictionInt = -1;
    private boolean isInWelcomeArea = false;
    private boolean smartphoneIsInPocket = false;
    private boolean lastCommandFromTrx;
    private boolean lastThatchamChanged = false;
    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action)) {
                // if car lock status changed
                if (lastCommandFromTrx != mProtocolManager.isLockedFromTrx()) {
                    PSALogs.d("performLock a_data_a_2", "lastCommandFromTrx =" + lastCommandFromTrx +
                            ", isLockedFromTrx=" + mProtocolManager.isLockedFromTrx());
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
                if (lastThatchamChanged && getRangingPositionPrediction().equalsIgnoreCase(PREDICTION_LOCK)) { // when thatcham has changed, and get into lock area
                    if (!mProtocolManager.isLockedFromTrx()) { // if the vehicle is unlocked, lock it
                        new CountDownTimer(600, 90) { // Send safety close command several times in case it got lost
                            public void onTick(long millisUntilFinished) {
                                performLockWithCryptoTimeout(true, true);
                            }

                            public void onFinish() {
//                                Toast.makeText(mContext, "All safety close command are sent !", Toast.LENGTH_SHORT).show();
                            }
                        }.start();
                    }
                    // if not in thatcham area and in lock area, rearm unlock
                    rearmUnlock.set(true);
                    makeNoise(mContext, mMainHandler, ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
                    lastThatchamChanged = false;
                }
            }
        }
    };

    public AlgoManager(Context mContext, BleRangingListener bleRangingListener,
                       InblueProtocolManager mProtocolManager, Handler mMainHandler) {
        this.mContext = mContext;
        this.bleRangingListener = bleRangingListener;
        this.mProtocolManager = mProtocolManager;
        this.mMainHandler = mMainHandler;
        this.mHandlerLockTimeOut = new Handler();
        this.mHandlerCryptoTimeOut = new Handler();
        this.mLockStatusChangedHandler = new Handler();
//        this.faceDetectorUtils = new FaceDetectorUtils(mContext);
        SensorManager senSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor magnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        mContext.registerReceiver(callReceiver, new IntentFilter());
        mContext.registerReceiver(bleStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mContext.registerReceiver(mDataReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE2));
    }

    public SpannableStringBuilder createDebugData(SpannableStringBuilder spannableStringBuilder) {
        if (ranging != null) {
            spannableStringBuilder.append("Indoor Localisation: ").append(ranging.getPrediction_indoor()).append("\n");
            spannableStringBuilder.append("Near-Far Localisation: ").append(ranging.getPrediction_near_far()).append("\n");
            spannableStringBuilder.append(ranging.printDist());
            spannableStringBuilder.append(ranging.printDebug());
        }
        return spannableStringBuilder;
    }

    public void performLockWithCryptoTimeout(final boolean isRKEAction, final boolean lockCar) {
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

    /**
     * Try all strategy based on machine learning
     */
    public void tryMachineLearningStrategies(boolean newLockStatus, ConnectedCar connectedCar) {
        boolean isWelcomeAllowed = false;
        // Cancel previous requested actions
        boolean isInStartArea = false;
        boolean isInUnlockArea = false;
        boolean isInLockArea = false;
        mProtocolManager.setIsStartRequested(false);
        mProtocolManager.setIsWelcomeRequested(false);
        rangingPredictionInt = ranging.getPrediction(); //TODO Replace SdkPreferencesHelper.getInstance().getComSimulationEnabled() by CallReceiver.smartphoneComIsActivated after demo
        switch (getRangingPositionPrediction()) {
            case PREDICTION_LOCK:
                isInLockArea = true;
                if (areLockActionsAvailable.get() && rearmLock.get()) {
                    performLockWithCryptoTimeout(false, true);
                }
                break;
            case PREDICTION_START:
            case PREDICTION_TRUNK:
                isInStartArea = true;
                if (!mProtocolManager.isStartRequested()) {
                    mProtocolManager.setIsStartRequested(true);
                }
                break;
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
            case PREDICTION_WELCOME:
            case PREDICTION_THATCHAM:
            default:
                PSALogs.d("prediction", "NOOO rangingPredictionInt !");
                break;
        }
        boolean isWelcomeStrategyValid = connectedCar.welcomeStrategy(connectedCar.getAllTrxAverage(), newLockStatus);
        isInWelcomeArea = rearmWelcome.get() && isWelcomeStrategyValid;
        if (isInWelcomeArea) {
            isWelcomeAllowed = true;
            rearmWelcome.set(false);
            SoundUtils.makeNoise(mContext, mMainHandler, ToneGenerator.TONE_SUP_CONFIRM, 300);
            bleRangingListener.doWelcome();
        }
        if (mProtocolManager.isWelcomeRequested() != isWelcomeAllowed) {
            mProtocolManager.setIsWelcomeRequested(isWelcomeAllowed);
        }
        setIsThatcham(isInLockArea, isInUnlockArea, isInStartArea);
        if (ranging.getPrediction_near_far().equalsIgnoreCase(PREDICTION_NEAR)) {
            mProtocolManager.setInRemoteParkingArea(true);
        } else {
            mProtocolManager.setInRemoteParkingArea(false);
        }
    }

    private void setIsThatcham(boolean isInLockArea, boolean isInUnlockArea, boolean isInStartArea) {
        if (isInLockArea || isInStartArea || !isInUnlockArea) {
            mProtocolManager.setThatcham(false);
        } else { // if is in unlock area
            mProtocolManager.setThatcham(true);
        }
    }

    public void createRangingObject(double[] rssi) {
        this.ranging = new Ranging(mContext, rssi);
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

    public void closeApp() {
        mContext.unregisterReceiver(mDataReceiver);
        mContext.unregisterReceiver(callReceiver);
        mContext.unregisterReceiver(bleStateReceiver);
//        faceDetectorUtils.deleteFaceDetector();
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //near
            smartphoneIsInPocket = (event.values[0] == 0);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public boolean isInWelcomeArea() {
        return isInWelcomeArea;
    }

    public boolean getRearmWelcome() {
        return rearmWelcome.get();
    }

    public void setRearmWelcome(boolean enableWelcome) {
        rearmWelcome.set(enableWelcome);
    }

    public boolean getRearmLock() {
        return rearmLock.get();
    }

    public boolean getRearmUnlock() {
        return rearmUnlock.get();
    }

    public boolean isSmartphoneInPocket() {
        return smartphoneIsInPocket;
    }

    public boolean areLockActionsAvailable() {
        return areLockActionsAvailable.get();
    }

    public boolean isRKEAvailable() {
        return isRKEAvailable.get();
    }

    public void setIsRKEAvailable(boolean enableRKE) {
        isRKEAvailable.set(enableRKE);
    }

    public Ranging getRanging() {
        return ranging;
    }

    public String getRangingPositionPrediction() {
        if (rangingPredictionInt == -1) {
            return PREDICTION_UNKNOWN;
        }
        return ranging.classes[rangingPredictionInt];
    }

    public String getRangingProximityPrediction() {
        if (rangingPredictionInt == -1) {
            return PREDICTION_UNKNOWN;
        }
        return ranging.classes_near_far[rangingPredictionInt];
    }

    public boolean getIsRKE() {
        return isRKE.get();
    }

    public void setIsRKE(boolean enableRKE) {
        isRKE.set(enableRKE);
    }

    public void setLastCommandFromTrx(boolean lastCommandFromTrx) {
        this.lastCommandFromTrx = lastCommandFromTrx;
    }
}
