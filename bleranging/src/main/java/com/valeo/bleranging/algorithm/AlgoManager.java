package com.valeo.bleranging.algorithm;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.bluetooth.bleservices.BluetoothLeService;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.utils.BleRangingListener;
import com.valeo.bleranging.utils.CallReceiver;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;

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

    public SpannableStringBuilder createDebugData(SpannableStringBuilder spannableStringBuilder,
                                                  String predictionStd, String predictionMl) {
        if (ranging != null) {
            spannableStringBuilder.append(ranging.printDebug());
        }
        spannableStringBuilder = algoStandard.createDebugData(spannableStringBuilder);
        spannableStringBuilder.append("Std:").append(predictionStd).append(" ML:").append(predictionMl).append("\n");
        return spannableStringBuilder;
    }

    /**
     * Try all strategy based on rssi values
     *
     * @param newLockStatus the lock status of the vehicle
     */
    public String tryStandardStrategies(boolean newLockStatus, boolean isFullyConnected,
                                        boolean isIndoor, ConnectedCar connectedCar, int totalAverage) {
        return algoStandard.tryStandardStrategies(newLockStatus, isFullyConnected,
                isIndoor, connectedCar, totalAverage, bleRangingListener);
    }

    /**
     * Try all strategy based on machine learning
     */
    public String tryMachineLearningStrategies(boolean newLockStatus, ConnectedCar connectedCar) {
        return ranging.tryMachineLearningStrategies(mProtocolManager, mRKEManager, connectedCar,
                bleRangingListener, mMainHandler, mContext, newLockStatus);
    }

    public void createRangingObject(double[] rssi) {
        this.ranging = new Ranging(mContext, rssi);
    }

    public void closeApp() {
        mContext.unregisterReceiver(mDataReceiver);
        mContext.unregisterReceiver(callReceiver);
        mContext.unregisterReceiver(bleStateReceiver);
//        faceDetectorUtils.deleteFaceDetector();
        algoStandard.closeApp();
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
