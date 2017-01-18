package com.valeo.bleranging.algorithm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ToneGenerator;
import android.os.Handler;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.RKEManager;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.CallReceiver;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.bleranging.utils.SoundUtils;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_BACK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_FRONT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_TRUNK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_WELCOME;

/**
 * Created by l-avaratha on 25/11/2016
 */
public class AlgoManager {
    private final InblueProtocolManager mProtocolManager;
    private final BleRangingListener bleRangingListener;
    private final AlgoStandard algoStandard;
    private final RKEManager mRKEManager;
    private final Context mContext;
    private final Handler mMainHandler;
    //    private final FaceDetectorUtils faceDetectorUtils;
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
    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE2.equals(action)) {
                algoStandard.onDataAvailable();
            }
        }
    };
    private Ranging ranging;
    private String predictionSd = PREDICTION_UNKNOWN;
    private String predictionMl = PREDICTION_UNKNOWN;

    public AlgoManager(Context context, BleRangingListener bleRangingListener,
                       InblueProtocolManager protocolManager, Handler mainHandler, RKEManager rKEManager) {
        this.mContext = context;
        this.bleRangingListener = bleRangingListener;
        this.mProtocolManager = protocolManager;
        this.mRKEManager = rKEManager;
        this.mMainHandler = mainHandler;
        this.algoStandard = new AlgoStandard(mContext, mProtocolManager, mRKEManager, mMainHandler);
//        this.faceDetectorUtils = new FaceDetectorUtils(mContext);
        mContext.registerReceiver(callReceiver, new IntentFilter());
        mContext.registerReceiver(bleStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mContext.registerReceiver(mDataReceiver, new IntentFilter(BluetoothLeService.ACTION_DATA_AVAILABLE2));
    }

    public SpannableStringBuilder createDebugData(SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.append("Sd:").append(predictionSd).append(" ML:").append(predictionMl).append("\n");
        if (ranging != null) {
            spannableStringBuilder.append(ranging.printDebug());
        }
        spannableStringBuilder = algoStandard.createDebugData(spannableStringBuilder);
        return spannableStringBuilder;
    }

    /**
     * Try a strategy or both strategies at once
     * @param newLockStatus the car lock status, true if locked, false if open
     * @param isFullyConnected true if connected in ble, false otherwise
     * @param isIndoor true if indoor, false otherwise
     * @param connectedCar the connected car
     * @param totalAverage the trxs total average
     * @return the double prediction of both strategy
     */
    public String getPrediction(boolean newLockStatus, boolean isFullyConnected,
                                boolean isIndoor, ConnectedCar connectedCar, int totalAverage) {
        //cancel previous action
        mProtocolManager.setIsWelcomeRequested(false);
        mProtocolManager.setIsStartRequested(false);
        if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(
                ConnectedCarFactory.ALGO_STANDARD)) {
            return algoStandard.tryStandardStrategies(newLockStatus, isFullyConnected,
                    isIndoor, connectedCar, totalAverage);
        } else if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(
                ConnectedCarFactory.MACHINE_LEARNING)) {
            return ranging.tryMachineLearningStrategies(mProtocolManager, connectedCar, newLockStatus);
        } else if (SdkPreferencesHelper.getInstance().getSelectedAlgo().equalsIgnoreCase(
                ConnectedCarFactory.DOUBLE_ALGO)) {
            predictionSd = algoStandard.tryStandardStrategies(newLockStatus, isFullyConnected,
                    isIndoor, connectedCar, totalAverage);
            predictionMl = ranging.tryMachineLearningStrategies(mProtocolManager, connectedCar, newLockStatus);
            if (predictionSd.equalsIgnoreCase(predictionMl)) {
                return predictionSd;
            }
        }
        return PREDICTION_UNKNOWN;
    }

    public void doActionKnowingPrediction(String prediction) {
        mProtocolManager.setThatcham(false);
        switch (prediction) {
            case PREDICTION_LOCK:
                mRKEManager.performLockWithCryptoTimeout(false, true);
                break;
            case PREDICTION_START:
            case PREDICTION_TRUNK:
                if (!mProtocolManager.isStartRequested()) {
                    mProtocolManager.setIsStartRequested(true);
                }
                break;
            case PREDICTION_BACK:
            case PREDICTION_RIGHT:
            case PREDICTION_LEFT:
            case PREDICTION_FRONT:
                mProtocolManager.setThatcham(true);
                mRKEManager.performLockWithCryptoTimeout(false, false);
                break;
            case PREDICTION_WELCOME:
                if (!mProtocolManager.isWelcomeRequested()) {
                    mProtocolManager.setIsWelcomeRequested(true);
                }
                SoundUtils.makeNoise(mContext, mMainHandler, ToneGenerator.TONE_SUP_CONFIRM, 300);
                bleRangingListener.doWelcome();
                break;
            case PREDICTION_UNKNOWN:
            default:
                PSALogs.d("prediction", "NOOO rangingPredictionInt !");
                break;
        }
    }

    public void createRangingObject(double[] rssi) {
        this.ranging = new Ranging(mContext, rssi);
    }

    public void closeApp() {
        mContext.unregisterReceiver(mDataReceiver);
        mContext.unregisterReceiver(callReceiver);
        mContext.unregisterReceiver(bleStateReceiver);
//        faceDetectorUtils.deleteFaceDetector();
    }

    public String getPredictionPosition() {
        if (ranging != null) {
            return ranging.getPredictionPosition();
        }
        return PREDICTION_UNKNOWN;
    }

    public String getPredictionProximity() {
        if (ranging != null) {
            return ranging.getPredictionProximity();
        }
        return PREDICTION_UNKNOWN;
    }

    public void setRearmWelcome(boolean newValue) {
        algoStandard.setRearmWelcome(newValue);
        if (ranging != null) {
            ranging.setRearmWelcome(newValue);
        }
    }

    public Ranging getRanging() {
        return ranging;
    }

    public AlgoStandard getAlgoStandard() {
        return algoStandard;
    }
}
