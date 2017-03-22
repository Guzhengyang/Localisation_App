package com.valeo.bleranging.machinelearningalgo;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.listeners.RkeListener;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.CallReceiver;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.SoundUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_ACCESS;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_BACK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FRONT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_INSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_INTERNAL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_NEAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_ROOF;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_FL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_FR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_RL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START_RR;
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
    private final RkeListener rkeListener;
    private final Context mContext;
    private final Handler mMainHandler;
    private final Handler mHandlerLockTimeOut;
    private final Handler mHandlerCryptoTimeOut;
    private final Handler mHandlerThatchamTimeOut;
    private final Handler mLockStatusChangedHandler;
    private final Handler mIsFrozenTimeOutHandler;
    private final ArrayList<Double> lAccHistoric = new ArrayList<>(SdkPreferencesHelper.getInstance().getLinAccSize());
    private final float R[] = new float[9];
    private final float I[] = new float[9];
    private final float orientation[] = new float[3];
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
    private double deltaLinAcc = 0;
    private boolean accuracyMeasureEnabled = false;
    private HashMap<String, Integer> accuracyCounterHMap;
    private boolean smartphoneIsFrozen = false;
    private final Runnable isFrozenRunnable = new Runnable() {
        @Override
        public void run() {
            smartphoneIsFrozen = true; // smartphone is staying still
            mIsFrozenTimeOutHandler.removeCallbacks(this);
        }
    };
    private boolean isFrozenRunnableAlreadyLaunched = false;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private boolean isAbortRunning = false;
    private final Runnable abortCommandRunner = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("performLock", "abortCommandRunner trx: " + mProtocolManager.isLockedFromTrx() + " me: " + mProtocolManager.isLockedToSend());
            if (mProtocolManager.isLockedFromTrx() != mProtocolManager.isLockedToSend()) { // if command from trx and app are different, make the app send what the trx sent
                mProtocolManager.setIsLockedToSend(mProtocolManager.isLockedFromTrx());
                rkeListener.updateCarDoorStatus(mProtocolManager.isLockedFromTrx());
                rearmLock.set(false);
            }
            isAbortRunning = false;
        }
    };
    private boolean isInWelcomeArea = false;
    private boolean smartphoneIsInPocket = false;
    private boolean lastCommandFromTrx;
    private boolean lastThatchamChanged = false;
    private String lastPrediction = PREDICTION_UNKNOWN;
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
                if (lastThatchamChanged && lastPrediction.equalsIgnoreCase(PREDICTION_LOCK)) { // when thatcham has changed, and get into lock area
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rearmUnlock.set(true);
                        }
                    }, (long) SdkPreferencesHelper.getInstance().getUnlockTimeout());
                    lastThatchamChanged = false;
                    if (SdkPreferencesHelper.getInstance().getSecurityWALEnabled()) {
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
                        makeNoise(mContext, mMainHandler, ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 100);
                    }
                }
            }
        }
    };

    public AlgoManager(Context mContext, BleRangingListener bleRangingListener,
                       RkeListener rkeListener,
                       InblueProtocolManager mProtocolManager, Handler mMainHandler) {
        this.mContext = mContext;
        this.bleRangingListener = bleRangingListener;
        this.rkeListener = rkeListener;
        this.mProtocolManager = mProtocolManager;
        this.mMainHandler = mMainHandler;
        this.mHandlerLockTimeOut = new Handler();
        this.mHandlerCryptoTimeOut = new Handler();
        this.mHandlerThatchamTimeOut = new Handler();
        this.mLockStatusChangedHandler = new Handler();
        this.mIsFrozenTimeOutHandler = new Handler();
        this.accuracyCounterHMap = new HashMap<>();
        SensorManager senSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor magnetometer = senSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor senLinAcceleration = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senLinAcceleration, SensorManager.SENSOR_DELAY_UI);
        senSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        mContext.registerReceiver(callReceiver, new IntentFilter());
        mContext.registerReceiver(bleStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mContext.registerReceiver(mDataReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE2));
    }

    public SpannableStringBuilder createDebugData(final ConnectedCar connectedCar) {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (connectedCar != null) {
            spannableStringBuilder.append(connectedCar.printDebug(smartphoneIsInPocket));
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%.3f %.3f %.3f\n", orientation[0], orientation[1], orientation[2]));
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%.3f\n", deltaLinAcc));
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
    public void tryMachineLearningStrategies(ConnectedCar connectedCar) {
        boolean isWelcomeAllowed = false;
        // Cancel previous requested actions
        boolean isInStartArea = false;
        boolean isInUnlockArea = false;
        boolean isInLockArea = false;
        mProtocolManager.setIsStartRequested(false);
        mProtocolManager.setIsWelcomeRequested(false);
        connectedCar.calculatePrediction();
        //TODO Replace SdkPreferencesHelper.getInstance().getComSimulationEnabled() by CallReceiver.smartphoneComIsActivated after demo
        lastPrediction = getPredictionPosition(connectedCar);
        switch (lastPrediction) {
            case PREDICTION_INSIDE:
                isInStartArea = true;
                if (!mProtocolManager.isStartRequested()) {
                    mProtocolManager.setIsStartRequested(true);
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
                if (!mProtocolManager.isStartRequested()) {
                    mProtocolManager.setIsStartRequested(true);
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
                PSALogs.d("prediction", "NOOO rangingPredictionInt !");
                break;
        }
        isInWelcomeArea = rearmWelcome.get() && getPredictionProximity(connectedCar).equals(BleRangingHelper.PREDICTION_FAR);
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
        if (getPredictionProximity(connectedCar).equalsIgnoreCase(PREDICTION_NEAR)) {
            mProtocolManager.setInRemoteParkingArea(true);
        } else {
            mProtocolManager.setInRemoteParkingArea(false);
        }
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

    private void setIsThatcham(boolean isInLockArea, boolean isInUnlockArea, boolean isInStartArea) {
        if (isInLockArea || isInStartArea || !isInUnlockArea) {
            if (!thatchamIsChanging.get()) { // if thatcham is not changing
                mProtocolManager.setThatcham(false);
            }
        } else if (isInUnlockArea) {
            launchThatchamValidityTimeOut();
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

    public void closeApp() {
        mContext.unregisterReceiver(mDataReceiver);
        mContext.unregisterReceiver(callReceiver);
        mContext.unregisterReceiver(bleStateReceiver);
//        faceDetectorUtils.deleteFaceDetector();
        if (mLockStatusChangedHandler != null) {
            mLockStatusChangedHandler.removeCallbacks(mManageIsLockStatusChangedPeriodicTimer);
        }
    }

    public String getPredictionPosition(final ConnectedCar connectedCar) {
        if (connectedCar != null) {
            return connectedCar.getPredictionPosition(smartphoneIsInPocket);
        }
        return PREDICTION_UNKNOWN;
    }

    public PointF getPredictionCoord(final ConnectedCar connectedCar) {
        if (connectedCar != null) {
            return connectedCar.getPredictionCoord();
        }
        return new PointF(0f, 0f);
    }

    public String getPredictionProximity(final ConnectedCar connectedCar) {
        if (connectedCar != null) {
            return connectedCar.getPredictionProximity();
        }
        return PREDICTION_UNKNOWN;
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

    public boolean isInWelcomeArea() {
        return isInWelcomeArea;
    }

    public float[] getOrientation() {
        return orientation;
    }

    public double getAcceleration() {
        return deltaLinAcc;
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

    public boolean getIsRKE() {
        return isRKE.get();
    }

    public void setIsRKE(boolean enableRKE) {
        isRKE.set(enableRKE);
    }

    public void setLastCommandFromTrx(boolean lastCommandFromTrx) {
        this.lastCommandFromTrx = lastCommandFromTrx;
    }

    public void enableAccuracyMeasure(boolean enable) {
        accuracyMeasureEnabled = enable;
    }

    public void clearAccuracyCounter() {
        accuracyCounterHMap.clear();
    }

    public int getSelectedAccuracy(String selectedAccuracyZone) {
        float total = 0.0f;
        for (Integer totalCounter : accuracyCounterHMap.values()) {
            total += totalCounter;
        }
        if (accuracyCounterHMap.get(selectedAccuracyZone) != null && total != 0) {
            return (int) ((accuracyCounterHMap.get(selectedAccuracyZone) / (1.0 * total)) * 100);
        } else {
            PSALogs.d("accuracy", "accuracyCounterHMap.get(" + selectedAccuracyZone + ") is null");
            return 0;
        }
    }
}
