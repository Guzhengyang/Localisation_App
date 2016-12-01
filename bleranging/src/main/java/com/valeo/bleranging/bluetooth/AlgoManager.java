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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.BleRangingHelper.START_PASSENGER_AREA;
import static com.valeo.bleranging.BleRangingHelper.START_TRUNK_AREA;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_BACK;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_LEFT;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_MIDDLE;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.NUMBER_TRX_RIGHT;
import static com.valeo.bleranging.utils.SoundUtils.makeNoise;

/**
 * Created by l-avaratha on 25/11/2016
 */

public class AlgoManager implements SensorEventListener {
    private final static int PREDICTION_MAX = 7;
    private final static int LOCK_STATUS_CHANGED_TIMEOUT = 5000;
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final PredictionRunnable predictionRunnable = new PredictionRunnable(null);
    private final Context mContext;
    private final Handler mMainHandler;
    private final Handler mHandlerLockTimeOut;
    private final Handler mHandlerThatchamTimeOut;
    private final Handler mHandlerBackTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Handler mLockStatusChangedHandler;
    private final Handler mIsLaidTimeOutHandler;
    private final Handler mIsFrozenTimeOutHandler;
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
    private final AtomicBoolean isRKEAvailable = new AtomicBoolean(true);
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
    private final ArrayList<Double> lAccHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getLinAccSize());
    private final LinkedList<Integer> predictionHistoric;
    private final float R[] = new float[9];
    private final float I[] = new float[9];
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
    private boolean lastSmartphoneIsFrozen = false;
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
    private double deltaLinAcc = 0;
    private boolean smartphoneIsInPocket = false;
    private boolean smartphoneIsFrozen = false;
    private final Runnable isFrozenRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsFrozen = true; // smartphone is staying still
            mIsFrozenTimeOutHandler.removeCallbacks(this);
        }
    };
    private boolean isLaidRunnableAlreadyLaunched = false;
    private boolean isFrozenRunnableAlreadyLaunched = false;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private boolean smartphoneIsMovingSlowly = false;
    private final Runnable isLaidRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsMovingSlowly = true; // smartphone is staying still
            mIsLaidTimeOutHandler.removeCallbacks(this);
        }
    };
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
                if (lastThatchamChanged && isInLockArea) { // when thatcham has changed, and get into lock area
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
        this.mHandlerThatchamTimeOut = new Handler();
        this.mHandlerBackTimeOut = new Handler();
        this.mHandlerCryptoTimeOut = new Handler();
        this.mIsLaidTimeOutHandler = new Handler();
        this.mIsFrozenTimeOutHandler = new Handler();
        this.mLockStatusChangedHandler = new Handler();
        this.predictionHistoric = new LinkedList<>();
        SensorManager senSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor senLinAcceleration = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senLinAcceleration, SensorManager.SENSOR_DELAY_UI);
        senSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        mContext.registerReceiver(callReceiver, new IntentFilter());
        mContext.registerReceiver(bleStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mContext.registerReceiver(mDataReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE2));
    }

    public SpannableStringBuilder createDebugData(SpannableStringBuilder spannableStringBuilder) {
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
        spannableStringBuilder //TODO Remove after test
                .append(String.format(Locale.FRANCE, "%1$.03f", orientation[0])).append("\n")
                .append(String.format(Locale.FRANCE, "%1$.03f", orientation[1])).append("\n")
                .append(String.format(Locale.FRANCE, "%1$.03f", orientation[2])).append("\n");
        return spannableStringBuilder;
    }

    /**
     * Try all strategy based on rssi values
     *
     * @param newLockStatus the lock status of the vehicle
     */
    public void tryStandardStrategies(boolean newLockStatus, boolean isFullyConnected,
                                      boolean isIndoor, ConnectedCar connectedCar, int totalAverage) {
        rearmWelcome(connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE)); // rearm rearmWelcome Boolean
        if (isFullyConnected) {
            boolean isStartAllowed = false;
            boolean isWelcomeAllowed = false;
            String connectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
//            AudioManager audM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//            PSALogs.d("test", "smartphoneComIsActivated " + CallReceiver.smartphoneComIsActivated + " " +
//                    audM.isBluetoothScoOn() + " " + audM.isSpeakerphoneOn() + " " + smartphoneIsInPocket);
            connectedCar.updateThresholdValues(isIndoor, smartphoneIsInPocket, CallReceiver.smartphoneComIsActivated);
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
                rearmForcedBlock(isInLockArea, isInUnlockArea, isInStartArea, smartphoneIsFrozen);
                lastSmartphoneIsFrozen = smartphoneIsFrozen;
            }
            if (isInWelcomeArea) {
                isWelcomeAllowed = true;
                rearmWelcome.set(false);
                SoundUtils.makeNoise(mContext, mMainHandler, ToneGenerator.TONE_SUP_CONFIRM, 300);
                bleRangingListener.doWelcome();
            }
            PSALogs.d("performLock", "isRKEAvailable=" + isRKEAvailable.get());
            if (isRKEAvailable.get() && areLockActionsAvailable.get() && rearmLock.get() && isInLockArea) {
                performLockWithCryptoTimeout(false, true);
            } else if (isInStartArea) { //smartphone in start area and moving
                isStartAllowed = true;
            } else if (isRKEAvailable.get() && areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                performLockWithCryptoTimeout(false, false);
            }
            if (mProtocolManager.isWelcomeRequested() != isWelcomeAllowed) {
                mProtocolManager.setIsWelcomeRequested(isWelcomeAllowed);
            } else if (mProtocolManager.isStartRequested() != isStartAllowed) {
                mProtocolManager.setIsStartRequested(isStartAllowed);
            }
        }
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
    public void tryMachineLearningStrategies(boolean newLockStatus, ConnectedCar connectedCar,
                                             int totalAverage) {
        boolean isWelcomeAllowed = false;
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
                        performLockWithCryptoTimeout(false, false);
                    }
                    break;
                case 2:
                    List<Integer> result2 = new ArrayList<>(1);
                    result2.add(NUMBER_TRX_RIGHT);
                    isUnlockStrategyValid = result2;
                    isInUnlockArea = true;
                    if (areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                        performLockWithCryptoTimeout(false, false);
                    }
                    break;
                case 3:
                    List<Integer> result3 = new ArrayList<>(1);
                    result3.add(NUMBER_TRX_BACK);
                    isUnlockStrategyValid = result3;
                    isInUnlockArea = true;
                    if (areLockActionsAvailable.get() && rearmUnlock.get() && isInUnlockArea) {
                        performLockWithCryptoTimeout(false, false);
                    }
                    break;
                case 4:
                    isInLockArea = true;
                    if (areLockActionsAvailable.get() && rearmLock.get() && isInLockArea) {
                        performLockWithCryptoTimeout(false, true);
                    }
                    break;
                default:
                    PSALogs.d("prediction", "NOOO rangingPredictionInt !");
                    break;
            }
            if (rearmWelcome.get()) {
                boolean isWelcomeStrategyValid = connectedCar.welcomeStrategy(totalAverage, newLockStatus);
                isInWelcomeArea = rearmWelcome.get() && isWelcomeStrategyValid;
                if (isInWelcomeArea) {
                    isWelcomeAllowed = true;
                    rearmWelcome.set(false);
                    SoundUtils.makeNoise(mContext, mMainHandler, ToneGenerator.TONE_SUP_CONFIRM, 300);
                    bleRangingListener.doWelcome();
                }
            }
            if (mProtocolManager.isWelcomeRequested() != isWelcomeAllowed) {
                mProtocolManager.setIsWelcomeRequested(isWelcomeAllowed);
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

    private void setIsThatcham(boolean isInLockArea, boolean isInUnlockArea, boolean isInStartArea) {
        if (isInLockArea || isInStartArea || !isInUnlockArea) {
            if (!thatchamIsChanging.get()) { // if thatcham is not changing
                mProtocolManager.setThatcham(false);
            }
        } else if (isInUnlockArea) {
            launchThatchamValidityTimeOut();
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

    public void createRangingObject(double rssiLeft, double rssiMiddle, double rssiRight, double rssiBack) {
        this.ranging = new Ranging(mContext, rssiLeft, rssiMiddle, rssiRight, rssiBack);
    }

    private boolean prepareRanging(ConnectedCar connectedCar) {
        if (ranging != null) {
            ranging.set(0, connectedCar.getCurrentOriginalRssi(NUMBER_TRX_LEFT));
            ranging.set(1, connectedCar.getCurrentOriginalRssi(NUMBER_TRX_MIDDLE));
            ranging.set(2, connectedCar.getCurrentOriginalRssi(NUMBER_TRX_RIGHT));
            ranging.set(3, connectedCar.getCurrentOriginalRssi(NUMBER_TRX_BACK));
            return true;
        }
        return false;
    }

    private int predict2int() {
        if (ranging != null) {
            return ranging.predict2int();
        }
        return 0;
    }

    /**
     * Rearm rearm bool if rssi is very low
     *
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

    private void rearmForcedBlock(boolean isInLockArea, boolean isInUnlockArea,
                                  boolean isInStartArea, boolean smartphoneIsFrozen) {
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

    public void closeApp() {
        mContext.unregisterReceiver(mDataReceiver);
        mContext.unregisterReceiver(callReceiver);
        mContext.unregisterReceiver(bleStateReceiver);
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }

    /**
     * Calculate acceleration rolling average
     *
     * @param lAccHistoric all acceleration values
     * @return the rolling average of acceleration
     */
    private float getRollingAverageLAcc(ArrayList<Double> lAccHistoric) {
        float average = 0;
        if (lAccHistoric.size() > 0) {
            for (Double element : lAccHistoric) {
                average += element;
            }
            average /= lAccHistoric.size();
        }
        return average;
    }

    /**
     * Calculate the quadratic sum
     *
     * @param x the first axe value
     * @param y the second axe value
     * @param z the third axe value
     * @return the quadratic sum of the three axes
     */
    private double getQuadratiqueSum(float x, float y, float z) {
        return Math.sqrt(x * x + y * y + z * z);
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
                if (!isLaidRunnableAlreadyLaunched) {
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

    public boolean isForcedStart() {
        return forcedStart;
    }

    public boolean isBlockStart() {
        return blockStart;
    }

    public boolean isForcedLock() {
        return forcedLock;
    }

    public boolean isBlockLock() {
        return blockLock;
    }

    public boolean isForcedUnlock() {
        return forcedUnlock;
    }

    public boolean isBlockUnlock() {
        return blockUnlock;
    }

    public Integer getRangingPredictionInt() {
        return rangingPredictionInt;
    }

    public List<Integer> getIsStartStrategyValid() {
        return isStartStrategyValid;
    }

    public List<Integer> getIsUnlockStrategyValid() {
        return isUnlockStrategyValid;
    }

    public boolean isInLockArea() {
        return isInLockArea;
    }

    public boolean isInUnlockArea() {
        return isInUnlockArea;
    }

    public boolean isInStartArea() {
        return isInStartArea;
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

    public boolean isSmartphoneFrozen() {
        return smartphoneIsFrozen;
    }

    public boolean isSmartphoneMovingSlowly() {
        return smartphoneIsMovingSlowly;
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

    public boolean getIsRKE() {
        return isRKE.get();
    }

    public void setIsRKE(boolean enableRKE) {
        isRKE.set(enableRKE);
    }

    public void setLastCommandFromTrx(boolean lastCommandFromTrx) {
        this.lastCommandFromTrx = lastCommandFromTrx;
    }

    public float[] getOrientation() {
        return orientation;
    }

    public double getDeltaLinAcc() {
        return deltaLinAcc;
    }

    public Runnable getFillPredictionArrayRunnable(ConnectedCar connectedCar) {
        predictionRunnable.setConnectedCar(connectedCar);
        return predictionRunnable;
    }

    private final class PredictionRunnable implements Runnable {
        private ConnectedCar connectedCar;

        PredictionRunnable(ConnectedCar connectedCar) {
            this.connectedCar = connectedCar;
        }

        void setConnectedCar(ConnectedCar connectedCar) {
            this.connectedCar = connectedCar;
        }

        @Override
        public void run() {
            if (prepareRanging(connectedCar)) {
                if (predictionHistoric.size() == PREDICTION_MAX) {
                    if (predictionHistoric.get(0) != null) {
                        predictionHistoric.remove(0);
                    }
                }
                int prediction = predict2int();
                predictionHistoric.add(prediction);
                PSALogs.d("prediction", "Add prediction: " + prediction);
                mMainHandler.postDelayed(this, 100);
            } else {
                mMainHandler.removeCallbacks(this);
            }
        }
    }
}
